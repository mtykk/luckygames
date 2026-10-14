package io.github.mtykk.luckygames;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.jeff_media.customblockdata.CustomBlockData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class LuckyGames extends JavaPlugin implements Listener{
    private static LuckyGames instance;
    private LuckyBlock luckyBlock;
    private OverridableResource overridableResource;
    private PluginTranslator pluginTranslator;
    private WorldLobby worldLobby;
    private GameSettings gameSettings;
    private GameState gameState;
    private BlockProtector blockProtector;
    private GameStructures gameStructures;
    private WorldGame worldGame;
    private PluginScoreboards pluginScoreboards;
    private boolean worldGamePreparing;
    private long gameSessionId;
    private Location finalArenaSpawnLocation;
    private boolean finalMatchStarted;
    private boolean finishReportPending;
    private final Map<UUID, Set<PotionEffectType>> matchPotionEffects = new HashMap<>();
    // Participants must respawn in the arena, not at a checkpoint from the race.
    private final Map<UUID, Location> finalArenaPlayerSpawnLocations = new HashMap<>();
    public static LuckyGames getInstance(){
        return instance;
    }
    public OverridableResource getOverridableResource() {
        return overridableResource;
    }

    @Override
    public void onEnable(){
        instance = this;
        saveResource("config.yml", false);

        overridableResource = new OverridableResource(this);

        pluginTranslator = new PluginTranslator();
        Bukkit.getPluginManager().registerEvents(this, this);
        CustomBlockData.registerListener(this);

        PluginStructureLoader.loadStructures(this, overridableResource);

        blockProtector = new BlockProtector(this);
        luckyBlock = new LuckyBlock();
        worldLobby = new WorldLobby(this,overridableResource,luckyBlock.getLuckyEvents().getAllWeight());
        gameSettings = new GameSettings();
        gameSettings.readFromConfig(this);
        gameState = new GameState(this,this::onEpilogue,this::onFinish);
        gameStructures = new GameStructures(blockProtector);
        worldGame = new WorldGame(this,blockProtector,luckyBlock,gameStructures);
        pluginScoreboards = new PluginScoreboards(this,gameState,luckyBlock.getLuckyEvents().getPlayerOngoingChallenges());
        registerCommands();
        showHealthInTab();

        Bukkit.getScheduler().runTaskTimer(this, new Consumer<BukkitTask>() {
            int gameFinishTimer = 0;
            @Override
            public void accept(BukkitTask bukkitTask) {
                if(gameState.getPhase() == GamePhase.FINISHED){
                    gameFinishTimer++;
                    if(gameFinishTimer > 10) {
                        wrapUpGame();
                        return;
                    }
                    World gameWorld = Bukkit.getWorld(worldGame.getWorldKey());
                    if (gameWorld != null && gameWorld.getPlayers().isEmpty() && !finishReportPending) wrapUpGame();
                }else{
                    gameFinishTimer = 0;
                }
                // Do not end the game while the post-race wait/countdown is still in progress.
                // A finisher who disconnects during RUNNING remains eligible to rejoin until the
                // epilogue's delayed offline-player cleanup has run.
                if (gameState.getPhase() == GamePhase.EPILOGUE && finalMatchStarted) {
                    World gameWorld = Bukkit.getWorld(worldGame.getWorldKey());
                    if (gameWorld != null) {
                        long playerCounter = gameWorld.getPlayers().stream()
                                .filter(player -> GamePlayer.getPlayerTeam(player) >= 0)
                                .count();
                        if (playerCounter <= 1) onFinish();
                    }
                }
            }
        }, 100, 40);
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) clearMatchEffects(player);
        if (luckyBlock != null) luckyBlock.getLuckyEvents().getPlayerOngoingChallenges().clear();
        if (pluginTranslator != null) pluginTranslator.unregister();
        if (pluginScoreboards != null) pluginScoreboards.cleanup();
        if (gameSettings != null) {
            gameSettings.saveToConfig(this);
            try {
                this.getConfig().save(new java.io.File(this.getDataFolder(), "config.yml"));
                this.getLogger().info("Configuration saved");
            } catch (IOException exception) {
                this.getLogger().warning("Failed to save configuration to file: " + exception.getMessage());
            }
        }
        instance = null;
    }

    public LuckyBlock getLuckyBlockInstance(){
        return luckyBlock;
    }

    public GameState getGameState() {
        return gameState;
    }

    /**
     * Run lucky games
     * @return If the game was started successfully
     */
    public boolean runGame() {
        if (gameState.getPhase() != GamePhase.WAITING || worldGamePreparing) return false;

        List<UUID> participantIds = Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).toList();
        if (participantIds.isEmpty() || participantIds.size() > GamePlayer.MAX_TEAM_COUNT) return false;

        GameSettings activeSettings = new GameSettings(gameSettings);
        gameState.setPhase(GamePhase.PREPARING);
        gameState.setThisGameSettings(activeSettings);
        gameState.clearPlayerTeamAttribution();
        gameState.setTeamNumbers(participantIds.size());
        finalArenaSpawnLocation = null;
        finalMatchStarted = false;
        finishReportPending = false;
        finalArenaPlayerSpawnLocations.clear();

        Bukkit.getServer().sendMessage(Component.empty());
        Bukkit.getServer().sendMessage(Component.translatable("luckygames.message.get_prepared"));
        Bukkit.getServer().sendMessage(Component.empty());
        int team = 0;
        for (UUID participantId : participantIds) {
            Player player = Bukkit.getPlayer(participantId);
            if (player == null) continue;
            gameState.setPlayerTeamAttribution(player, team);
            Bukkit.getServer().sendMessage(Component.text("[", NamedTextColor.AQUA)
                    .append(Component.text(team, NamedTextColor.YELLOW))
                    .append(Component.text("]", NamedTextColor.AQUA))
                    .append(Component.space())
                    .append(Component.text(player.getName(), NamedTextColor.GOLD)));
            team++;
        }
        Bukkit.getServer().sendMessage(Component.empty());

        // Use one immutable-for-this-match snapshot for both indexing and world generation.
        gameState.setTeamNumbers(team);
        gameState.initPlayerGameStates();
        worldGamePreparing = true;
        long sessionId = ++gameSessionId;
        try {
            worldGame.reset(activeSettings, gameState.getTeamNumbers(), gameState.getIndexedGameLocation(), world -> {
                worldGamePreparing = false;
                if (sessionId != gameSessionId || gameState.getPhase() != GamePhase.PREPARING) return;

                gameState.setPhase(GamePhase.RUNNING);
                for (UUID participantId : participantIds) {
                    Player player = Bukkit.getPlayer(participantId);
                    if (player == null || GamePlayer.getPlayerTeam(participantId) < 0) continue;
                    try {
                        if (!gameState.trySetPlayerRespawn(participantId, 0, true)) {
                            handleFailedGameTeleport(player, "starting the race");
                            continue;
                        }
                        player.setGameMode(GameMode.SURVIVAL);
                        player.getInventory().clear();
                        player.getEnderChest().clear();
                        clearMatchEffects(player);
                        for (PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) {
                            player.removePotionEffect(effect.getType());
                        }
                        applyMatchEffects(player);
                        Bukkit.getScheduler().runTaskLater(this, task -> {
                            if (sessionId != gameSessionId || !player.isOnline()
                                    || gameState.getPhase() != GamePhase.RUNNING) return;
                            player.showTitle(Title.title(Component.text("GO", NamedTextColor.GOLD, TextDecoration.BOLD), Component.empty()));
                            player.sendMessage(Component.translatable("luckygames.message.hint"));
                            player.sendMessage(Component.empty());
                        }, 10);
                    } catch (IllegalArgumentException exception) {
                        getLogger().log(java.util.logging.Level.WARNING,
                                "Could not place participant " + player.getName() + " at their starting location", exception);
                        gameState.handlePlayerQuit(player);
                        worldGame.sendHereAsSpectator(player, true);
                    }
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (GamePlayer.getPlayerTeam(player) < 0) worldGame.sendHereAsSpectator(player, true);
                }

                if (gameState.getAllPlayerTeamAttribution().isEmpty()) {
                    onFinish();
                } else if (gameState.areAllLanesFinished()) {
                    onEpilogue();
                }
                Bukkit.getScheduler().runTaskLater(this, task -> {
                    if (sessionId == gameSessionId && gameState.getPhase() != GamePhase.WAITING) worldLobby.reset();
                }, 100);
            }, exception -> {
                worldGamePreparing = false;
                if (sessionId != gameSessionId || gameState.getPhase() != GamePhase.PREPARING) return;
                gameSessionId++;
                gameState.setPhase(GamePhase.WAITING);
                gameState.clearPlayerTeamAttribution();
                getLogger().log(java.util.logging.Level.SEVERE, "Could not generate the game world", exception);
            });
        } catch (RuntimeException exception) {
            worldGamePreparing = false;
            gameSessionId++;
            gameState.setPhase(GamePhase.WAITING);
            gameState.clearPlayerTeamAttribution();
            getLogger().log(java.util.logging.Level.SEVERE, "Could not prepare the game world", exception);
            return false;
        }
        return true;
    }

    private LiteralArgumentBuilder<CommandSourceStack> gameRunCommand(){
        return Commands.literal("run")
                .requires(sender->sender.getSender().hasPermission("luckygames.control_game"))
                .executes(ctx->{
                    if(runGame()){
                        ctx.getSource().getSender().sendMessage(Component.translatable("luckygames.message.game_starting"));
                        return Command.SINGLE_SUCCESS;
                    }else{
                        ctx.getSource().getSender().sendMessage(Component.translatable("luckygames.warning.could_not_start_game"));
                        return Command.SINGLE_SUCCESS;
                    }
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> lobbyCommand(){
        return Commands.literal("lobby")
                .executes(ctx->{
                    if(gameState.getPhase() != GamePhase.FINISHED){
                        ctx.getSource().getSender().sendMessage(Component.translatable("luckygames.message.can_not_use_this_right_now"));
                        return Command.SINGLE_SUCCESS;
                    }
                    Entity executor = ctx.getSource().getExecutor();
                    if(executor instanceof Player player){
                        worldLobby.sendHereThenSetRespawnWhenAvailable(player);
                    }else{
                        ctx.getSource().getSender().sendMessage(Component.text("Only players can be sent to lobby"));
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("force")
                        .requires(sender->sender.getSender().hasPermission("luckygames.control_game"))
                        .executes(ctx->{
                            wrapUpGame();
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> challengeCommand(){
        return Commands.literal("list_challenges")
                .executes(ctx->{
                    CommandSender sender = ctx.getSource().getSender();
                    Entity executor = ctx.getSource().getExecutor();
                    if(executor == null){
                        sender.sendMessage(Component.text("Can't process"));
                        return Command.SINGLE_SUCCESS;
                    }
                    UUID executorUUID = executor.getUniqueId();
                    if(luckyBlock.getLuckyEvents().getPlayerOngoingChallenges().getOngoingChallenges().isEmpty()){
                        sender.sendMessage(Component.translatable("luckygames.message.nothing_to_show"));
                    }else {
                        for (Map.Entry<UUID, PlayerChallengeDesc> entry : luckyBlock.getLuckyEvents().getPlayerOngoingChallenges().getOngoingChallenges().entrySet()) {
                            sender.sendMessage(Component.text("[", NamedTextColor.YELLOW)
                                    .append(Component.text(Objects.requireNonNullElse(Bukkit.getOfflinePlayer(entry.getKey()).getName(),"E"), NamedTextColor.AQUA))
                                    .append(Component.text("] ", NamedTextColor.YELLOW))
                                    .append(entry.getValue().getChallenge(entry.getKey().equals(executorUUID))));
                        }
                    }
                    return Command.SINGLE_SUCCESS;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> matchCommand(){
        return Commands.literal("match")
                .requires(sender -> sender.getSender().hasPermission("luckygames.control_game"))
                .executes(ctx->{
                    CommandSender sender = ctx.getSource().getSender();
                    if(gameState.getPhase() != GamePhase.RUNNING){
                        sender.sendMessage(Component.translatable("luckygames.warning.could_not_run_the_match"));
                    }else{
                        onEpilogue();
                        sender.sendMessage(Component.translatable("luckygames.message.success"));
                    }
                    return Command.SINGLE_SUCCESS;
                });
    }

    private void wrapUpGame() {
        gameSessionId++;
        finalArenaSpawnLocation = null;
        finalMatchStarted = false;
        finishReportPending = false;
        finalArenaPlayerSpawnLocations.clear();
        gameState.setPhase(GamePhase.WAITING);
        gameState.clearPlayerTeamAttribution();
        for (Player player : List.copyOf(Bukkit.getOnlinePlayers())) {
            clearMatchEffects(player);
            worldLobby.sendHereThenSetRespawnWhenAvailable(player);
            player.setGameMode(GameMode.SURVIVAL);
        }
    }

    private void registerCommands(){
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,commands->{
            //Register luckygame commands
            commands.registrar().register(Commands.literal("luckygames")
                    .then(gameRunCommand())
                    .then(gameSettings.settingsCommand())
                    .then(lobbyCommand())
                    .then(challengeCommand())
                    .then(matchCommand())
                    .build());
        });
    }

    public BlockProtector getBlockProtector() {
        return blockProtector;
    }

    public GameSettings getGameSettings() {
        return gameSettings;
    }

    private void showHealthInTab(){
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard mainBoard = manager.getMainScoreboard();
        String objective = "health";
        if(mainBoard.getObjective(objective) != null) return;
        Objective healthObjective = mainBoard.registerNewObjective(objective,
                Criteria.HEALTH,
                Component.empty(),
                RenderType.HEARTS);
        healthObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
    }

    private void onEpilogue() {
        if (gameState.getPhase() != GamePhase.RUNNING) return;
        gameState.setPhase(GamePhase.EPILOGUE);
        finalMatchStarted = false;
        final long sessionId = gameSessionId;
        World gameWorld = Bukkit.getWorld(worldGame.getWorldKey());
        if (gameWorld == null) {
            getLogger().severe("The game world disappeared before the final match could start");
            onFinish();
            return;
        }
        gameWorld.setGameRule(GameRules.PVP, false);
        Bukkit.getServer().sendMessage(Component.translatable("luckygames.message.everyone_has_finished"));
        Bukkit.getServer().sendMessage(Component.translatable("luckygames.message.get_ready_for_the_match"));
        if (gameState.getAllPlayerTeamAttribution().isEmpty()) {
            onFinish();
            return;
        }

        Bukkit.getScheduler().runTaskLater(this, task -> {
            if (sessionId != gameSessionId || gameState.getPhase() != GamePhase.EPILOGUE) return;
            World currentGameWorld = Bukkit.getWorld(worldGame.getWorldKey());
            if (currentGameWorld == null) {
                getLogger().severe("The game world disappeared while preparing the final match");
                onFinish();
                return;
            }

            Location placeLocation = gameState.getIndexedGameLocation()
                    .getLocation(0, gameState.getThisGameSettings().getLastIndex())
                    .clone().add(new Vector(60, -20, 0));
            finalArenaSpawnLocation = placeLocation.clone();
            try {
                gameStructures.getArena().placeFast(placeLocation, true, block -> block.setType(Material.AIR));
            } catch (RuntimeException exception) {
                getLogger().log(java.util.logging.Level.SEVERE, "Could not place the final arena", exception);
                onFinish();
                return;
            }

            Iterator<Map.Entry<UUID, Integer>> iterator = gameState.getAllPlayerTeamAttribution().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, Integer> entry = iterator.next();
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player == null || !player.isConnected()) {
                    String name = Objects.requireNonNullElse(Bukkit.getOfflinePlayer(entry.getKey()).getName(), "Unknown");
                    gameState.addPlayerRankEntry(new PlayerRankEntry(name, entry.getValue()));
                    iterator.remove();
                    continue;
                }
                boolean teleported = teleportPlayerToFinalArena(player);
                if (!teleported) {
                    getLogger().warning("Could not move " + player.getName() + " into the final arena; removing them from the match");
                    player.sendMessage(Component.translatable("luckygames.message.game_teleport_failed"));
                    player.setGameMode(GameMode.SPECTATOR);
                    gameState.addPlayerRankEntry(new PlayerRankEntry(player.getName(), entry.getValue()));
                    iterator.remove();
                    continue;
                }
                player.setGameMode(GameMode.ADVENTURE);
            }

            if (gameState.getAllPlayerTeamAttribution().size() <= 1) {
                onFinish();
                return;
            }

            List<Player> attendingPlayers = currentGameWorld.getPlayers().stream()
                    .filter(player -> GamePlayer.getPlayerTeam(player) >= 0)
                    .toList();
            Audience.audience(attendingPlayers).sendMessage(
                    Component.translatable("luckygames.message.match_beginning_in_5_seconds"));
            Bukkit.getScheduler().runTaskTimer(this, new Consumer<BukkitTask>() {
                int countdown = 5;

                @Override
                public void accept(BukkitTask bukkitTask) {
                    if (sessionId != gameSessionId || gameState.getPhase() != GamePhase.EPILOGUE) {
                        bukkitTask.cancel();
                        return;
                    }
                    List<Player> activePlayers = currentGameWorld.getPlayers().stream()
                            .filter(player -> GamePlayer.getPlayerTeam(player) >= 0)
                            .toList();
                    if (activePlayers.size() <= 1) {
                        onFinish();
                        bukkitTask.cancel();
                        return;
                    }
                    Audience activeAudience = Audience.audience(activePlayers);
                    if (countdown == 0) {
                        activeAudience.showTitle(Title.title(
                                Component.translatable("luckygames.message.match_begin"),
                                Component.translatable("luckygames.message.be_the_last_one_standing")));
                        Bukkit.getServer().sendMessage(Component.translatable("luckygames.message.match_begin"));
                        Bukkit.getServer().sendMessage(Component.translatable("luckygames.message.be_the_last_one_standing"));
                        finalMatchStarted = true;
                        currentGameWorld.setGameRule(GameRules.PVP, true);
                        activePlayers.forEach(player -> player.setGameMode(GameMode.SURVIVAL));
                        bukkitTask.cancel();
                        return;
                    }
                    activeAudience.showTitle(Title.title(
                            Component.text(countdown, countdown > 3 ? NamedTextColor.GREEN
                                    : countdown > 1 ? NamedTextColor.YELLOW : NamedTextColor.RED, TextDecoration.BOLD),
                            Component.empty()));
                    countdown--;
                }
            }, 0, 20);
        }, 80);
    }

    private void onFinish() {
        if (gameState.getPhase() == GamePhase.FINISHED || gameState.getPhase() == GamePhase.WAITING) return;
        gameState.setPhase(GamePhase.FINISHED);
        finishReportPending = true;
        for (Player player : Bukkit.getOnlinePlayers()) clearMatchEffects(player);
        final long sessionId = gameSessionId;
        Bukkit.getScheduler().runTaskLater(this, task -> {
            if (sessionId != gameSessionId || gameState.getPhase() != GamePhase.FINISHED) return;
            try {
                for (Map.Entry<UUID, Integer> entry : gameState.getAllPlayerTeamAttribution().entrySet()) {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player != null && player.isConnected()) {
                        player.showTitle(Title.title(Component.translatable("luckygames.message.victory"),
                                Component.text("LUCKY!", NamedTextColor.GREEN)));
                    }
                    String name = Objects.requireNonNullElse(Bukkit.getOfflinePlayer(entry.getKey()).getName(), "Unknown");
                    gameState.addPlayerRankEntry(new PlayerRankEntry(name, entry.getValue()));
                }
                TextComponent.Builder playerRank = Component.text();
                int rank = 1;
                for (PlayerRankEntry entry : gameState.getPlayerRankEntries().reversed()) {
                    TextColor textColor = switch (rank) {
                        case 1 -> NamedTextColor.YELLOW;
                        case 2 -> NamedTextColor.BLUE;
                        case 3 -> NamedTextColor.GREEN;
                        default -> NamedTextColor.GRAY;
                    };
                    playerRank.append(Component.text("#", textColor));
                    playerRank.append(Component.text(rank, textColor));
                    playerRank.append(Component.space());
                    playerRank.append(Component.text("["));
                    playerRank.append(Component.text(entry.getTeam(), textColor));
                    playerRank.append(Component.text("] "));
                    playerRank.append(Component.text(entry.getName(), NamedTextColor.AQUA));
                    playerRank.append(Component.newline());
                    rank++;
                }
                Bukkit.getServer().sendMessage(Component.translatable("luckygames.message.game_ended", playerRank.build()));
            } finally {
                finishReportPending = false;
            }
        }, 2);
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent e){
        if(e.getType() == ServerLoadEvent.LoadType.STARTUP){
            worldLobby.reset();
            gameState.setPhase(GamePhase.WAITING);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        clearMatchEffects(e.getPlayer());
        gameState.handlePlayerQuit(e.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        clearMatchEffects(player);
        GamePhase phase = gameState.getPhase();
        int playerTeam = GamePlayer.getPlayerTeam(player);
        boolean alreadyInGameWorld = player.getWorld().getKey().equals(worldGame.getWorldKey());

        if (phase == GamePhase.WAITING || phase == GamePhase.PREPARING) {
            worldLobby.sendHereThenSetRespawnWhenAvailable(player);
            player.setGameMode(GameMode.SURVIVAL);
            return;
        }

        if (phase == GamePhase.RUNNING) {
            if (playerTeam >= 0) {
                if (!alreadyInGameWorld && !gameState.trySetPlayerRespawn(player.getUniqueId(),
                        gameState.getPlayerRespawnLocationIndex(player.getUniqueId()), true)) {
                    handleFailedGameTeleport(player, "rejoining the race");
                    return;
                }
                player.setGameMode(GameMode.SURVIVAL);
                applyMatchEffects(player);
            } else if (alreadyInGameWorld) {
                player.setGameMode(GameMode.SPECTATOR);
            } else {
                worldGame.sendHereAsSpectator(player, true);
            }
            return;
        }

        if (phase == GamePhase.EPILOGUE) {
            if (playerTeam >= 0) {
                boolean teleported = true;
                if (finalArenaSpawnLocation != null) {
                    teleported = teleportPlayerToFinalArena(player);
                } else if (!alreadyInGameWorld) {
                    int respawnIndex = gameState.getPlayerRespawnLocationIndex(player.getUniqueId());
                    teleported = gameState.trySetPlayerRespawn(player.getUniqueId(), respawnIndex, true);
                }
                if (!teleported) {
                    handleFailedGameTeleport(player, "rejoining the final match");
                    return;
                }
                player.setGameMode(finalMatchStarted ? GameMode.SURVIVAL : GameMode.ADVENTURE);
                applyMatchEffects(player);
            } else if (alreadyInGameWorld) {
                player.setGameMode(GameMode.SPECTATOR);
            } else {
                worldGame.sendHereAsSpectator(player, true);
            }
            return;
        }

        if (!alreadyInGameWorld) worldGame.sendHereAsSpectator(player, true);
        else player.setGameMode(GameMode.SPECTATOR);
    }

    private Location createFinalArenaPlayerSpawnLocation() {
        if (finalArenaSpawnLocation == null) {
            throw new IllegalStateException("The final arena has not been placed");
        }
        return finalArenaSpawnLocation.clone().add(new Vector(
                PluginRandom.randomGenerator.nextFloat(), 0, PluginRandom.randomGenerator.nextFloat()));
    }

    private boolean teleportPlayerToFinalArena(Player player) {
        if (finalArenaSpawnLocation == null) return false;
        Location spawnLocation = finalArenaPlayerSpawnLocations.computeIfAbsent(
                player.getUniqueId(), ignored -> createFinalArenaPlayerSpawnLocation());
        if (!player.teleport(spawnLocation.clone())) return false;
        player.setRespawnLocation(spawnLocation.clone(), true);
        return true;
    }

    private boolean isInsideFinalArena(Location location) {
        if (finalArenaSpawnLocation == null || location == null || location.getWorld() == null
                || !location.getWorld().equals(finalArenaSpawnLocation.getWorld())) {
            return false;
        }
        Vector offset = gameStructures.getArena().getOffset();
        int minX = finalArenaSpawnLocation.getBlockX() + offset.getBlockX();
        int minY = finalArenaSpawnLocation.getBlockY() + offset.getBlockY();
        int minZ = finalArenaSpawnLocation.getBlockZ() + offset.getBlockZ();
        return location.getBlockX() >= minX
                && location.getBlockX() < minX + gameStructures.getArena().getSize().getBlockX()
                && location.getBlockY() >= minY
                && location.getBlockY() < minY + gameStructures.getArena().getSize().getBlockY()
                && location.getBlockZ() >= minZ
                && location.getBlockZ() < minZ + gameStructures.getArena().getSize().getBlockZ();
    }

    private void handleFailedGameTeleport(Player player, String context) {
        getLogger().warning("Could not teleport " + player.getName() + " while " + context + "; removing them from active play");
        player.sendMessage(Component.translatable("luckygames.message.game_teleport_failed"));
        gameState.handlePlayerQuit(player);
        if (Bukkit.getWorld(worldGame.getWorldKey()) != null) {
            worldGame.sendHereAsSpectator(player, true);
        } else {
            player.setGameMode(GameMode.SPECTATOR);
        }
    }

    private void applyMatchEffects(Player player) {
        GameSettings activeSettings = gameState.getThisGameSettings();
        if (activeSettings == null) return;
        Set<PotionEffectType> appliedEffects = matchPotionEffects.computeIfAbsent(
                player.getUniqueId(), ignored -> new HashSet<>());
        if (activeSettings.isSaturation()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, -1, 0, false, false, true));
            appliedEffects.add(PotionEffectType.SATURATION);
        }
        if (activeSettings.isNightVision()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, false, false, true));
            appliedEffects.add(PotionEffectType.NIGHT_VISION);
        }
        if (appliedEffects.isEmpty()) matchPotionEffects.remove(player.getUniqueId());
    }

    private void clearMatchEffects(Player player) {
        Set<PotionEffectType> appliedEffects = matchPotionEffects.remove(player.getUniqueId());
        if (appliedEffects == null) return;
        for (PotionEffectType effectType : appliedEffects) {
            player.removePotionEffect(effectType);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerPostRespawnEvent e){
        Player player = e.getPlayer();
        clearMatchEffects(player);
        GamePhase phase = gameState.getPhase();
        if (phase == GamePhase.WAITING || phase == GamePhase.PREPARING) {
            player.setGameMode(GameMode.SURVIVAL);
            if (!e.getRespawnLocation().getWorld().getKey().equals(worldLobby.getWorldKey())) {
                worldLobby.sendHereThenSetRespawnWhenAvailable(player);
            }
        } else if (phase == GamePhase.RUNNING || phase == GamePhase.EPILOGUE) {
            boolean respawnedInGameWorld = e.getRespawnLocation().getWorld().getKey().equals(worldGame.getWorldKey());
            if (GamePlayer.getPlayerTeam(player) < 0) {
                if (!respawnedInGameWorld) worldGame.sendHereAsSpectator(player, true);
                else player.setGameMode(GameMode.SPECTATOR);
            } else {
                if (phase == GamePhase.EPILOGUE && finalArenaSpawnLocation != null) {
                    Location arenaSpawn = finalArenaPlayerSpawnLocations.computeIfAbsent(
                            player.getUniqueId(), ignored -> createFinalArenaPlayerSpawnLocation());
                    if (!isInsideFinalArena(e.getRespawnLocation()) && !player.teleport(arenaSpawn.clone())) {
                        handleFailedGameTeleport(player, "respawning into the final match");
                        return;
                    }
                    player.setRespawnLocation(arenaSpawn.clone(), true);
                } else if (!respawnedInGameWorld) {
                    int respawnIndex = gameState.getPlayerRespawnLocationIndex(player.getUniqueId());
                    if (!gameState.trySetPlayerRespawn(player.getUniqueId(), respawnIndex, true)) {
                        handleFailedGameTeleport(player, "respawning into the game");
                        return;
                    }
                }
                applyMatchEffects(player);
                player.setGameMode(phase == GamePhase.EPILOGUE && !finalMatchStarted
                        ? GameMode.ADVENTURE : GameMode.SURVIVAL);
            }
        } else {
            worldGame.sendHereAsSpectator(player,true);
        }
    }
}
