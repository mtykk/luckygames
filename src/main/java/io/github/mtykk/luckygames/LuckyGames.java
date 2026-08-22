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
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class LuckyGames extends JavaPlugin implements Listener{
    private static final Logger log = LoggerFactory.getLogger(LuckyGames.class);
    private static LuckyGames instance;
    private LuckyBlock luckyBlock;
    private OverridableResource overridableResource;
    private WorldLobby worldLobby;
    private GameSettings gameSettings;
    private GameState gameState;
    private BlockProtector blockProtector;
    private GameStructures gameStructures;
    private WorldGame worldGame;
    private PluginScoreboards pluginScoreboards;
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

        new PluginTranslator();
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
        worldGame = new WorldGame(this,blockProtector,luckyBlock,gameStructures,gameSettings.getLuckyBlockDistribution());
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
                    if(gameWorld != null && gameWorld.getPlayers().isEmpty()) wrapUpGame();
                }else{
                    gameFinishTimer = 0;
                }
                if(gameState.getPhase() == GamePhase.EPILOGUE){
                    World gameWorld = Bukkit.getWorld(worldGame.getWorldKey());
                    if(gameWorld != null){
                        long playerCounter = gameWorld.getPlayers().stream().filter(player -> GamePlayer.getPlayerTeam(player)>=0).count();
                        if(playerCounter <= 1){
                            onFinish();
                        }
                    }
                }
            }
        }, 100, 40);
    }

    @Override
    public void onDisable(){
        gameSettings.saveToConfig(this);
        try {
            this.getConfig().save(this.getDataFolder().getCanonicalPath()+"/config.yml");
            this.getLogger().info("Configuration saved");
        }catch (IOException exception){
            this.getLogger().warning("Failed to save configuration to file");
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
    public boolean runGame(){
        GamePhase phase = gameState.getPhase();
        if(phase != GamePhase.WAITING) return false;
        gameState.setPhase(GamePhase.PREPARING);
        gameState.setThisGameSettings(gameSettings);
        Collection<? extends Player> playerList = Bukkit.getOnlinePlayers();
        if(playerList.isEmpty()) return false;
        gameState.clearPlayerTeamAttribution();
        gameState.setTeamNumbers(playerList.size());
        Bukkit.getServer().sendMessage(Component.empty());
        Bukkit.getServer().sendMessage(Component.translatable("luckygames.message.get_prepared"));
        Bukkit.getServer().sendMessage(Component.empty());
        {
            int counter = 0;
            for(Player player: playerList){
                gameState.setPlayerTeamAttribution(player, counter);
                Bukkit.getServer().sendMessage(Component.text("[", NamedTextColor.AQUA).append(Component.text(counter,NamedTextColor.YELLOW)).append(Component.text("]",NamedTextColor.AQUA)).append(Component.space()).append(Component.text(player.getName(),NamedTextColor.GOLD)));
                counter++;
            }
        }
        Bukkit.getServer().sendMessage(Component.empty());
        gameState.initPlayerGameStates();
        blockProtector.clear();
        worldGame.reset(gameSettings,gameState.getTeamNumbers(),gameState.getIndexedGameLocation(),world -> {
            gameState.setPhase(GamePhase.RUNNING);
            playerList.forEach(player -> {
                Player operatablePlayer = player.getPlayer();
                try {
                    gameState.setPlayerRespawn(operatablePlayer.getUniqueId(), 0,true);
                    operatablePlayer.setGameMode(GameMode.SURVIVAL);
                    operatablePlayer.getInventory().clear();
                    operatablePlayer.getEnderChest().clear();
                    operatablePlayer.getActivePotionEffects().clear();
                    if(gameSettings.isSaturation()) operatablePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION,-1,0,false,false,true));
                    if(gameSettings.isNightVision()) operatablePlayer.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,-1,0,false,false,true));
                    Bukkit.getScheduler().runTaskLater(this,task->{
                        operatablePlayer.showTitle(Title.title(Component.text("GO",NamedTextColor.GOLD,TextDecoration.BOLD),Component.empty()));
                        operatablePlayer.sendMessage(Component.translatable("luckygames.message.hint"));
                    },10);
                } catch (IllegalArgumentException e) {
                    worldGame.sendHereAsSpectator(player,true);
                }
            });
            Bukkit.getScheduler().runTaskLater(this,task->{
                worldLobby.reset();
            },100);
        });
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
                    if(executor instanceof Player){
                        worldLobby.sendHereThenSetRespawn((Player) executor);
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
                    UUID executorUUID = executor.getUniqueId();
                    if(executor == null){
                        sender.sendMessage(Component.text("Can't process"));
                        return Command.SINGLE_SUCCESS;
                    }else{
                        if(luckyBlock.getLuckyEvents().getPlayerOngoingChallenges().getOngoingChallenges().isEmpty()){
                            sender.sendMessage(Component.translatable("luckygames.message.nothing_to_show"));
                        }else {
                            for (Map.Entry<UUID, PlayerChallengeDesc> entry : luckyBlock.getLuckyEvents().getPlayerOngoingChallenges().getOngoingChallenges().entrySet()) {
                                sender.sendMessage(Component.text("[", NamedTextColor.YELLOW)
                                        .append(Component.text(Bukkit.getOfflinePlayer(entry.getKey()).getName(), NamedTextColor.AQUA))
                                        .append(Component.text("] ", NamedTextColor.YELLOW))
                                        .append(entry.getValue().getChallenge(entry.getKey().equals(executorUUID))));
                            }
                        }
                        return Command.SINGLE_SUCCESS;
                    }
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

    private void wrapUpGame(){
        gameState.setPhase(GamePhase.WAITING);
        Bukkit.getOnlinePlayers().forEach(player -> {
            Player operatablePlayer = player.getPlayer();
            worldLobby.sendHereThenSetRespawn(operatablePlayer);
            player.setGameMode(GameMode.SURVIVAL);
        });
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

    private void onEpilogue(){
        World gameWorld = Bukkit.getWorld(worldGame.getWorldKey());
        assert(gameWorld != null);
        gameWorld.setGameRule(GameRules.PVP,false);
        Bukkit.getServer().sendMessage(Component.translatable("luckygames.message.everyone_has_finished"));
        Bukkit.getServer().sendMessage(Component.translatable("luckygames.message.get_ready_for_the_match"));
        Bukkit.getScheduler().runTaskLater(this,task->{
            Location placeLocation = gameState.getIndexedGameLocation().getLocation(0,gameState.getThisGameSettings().getLastIndex()).clone().add(new Vector(60,-20,0));
            gameStructures.getArena().placeFast(placeLocation,true,block -> {
                block.setType(Material.AIR);
            });
            List<Player> attendingPlayers = new ArrayList<>();
            Iterator<Map.Entry<UUID,Integer>> iterator = gameState.getAllPlayerTeamAttribution().entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry<UUID,Integer> entry = iterator.next();
                Player player = Bukkit.getPlayer(entry.getKey());
                if(player == null){
                    gameState.addPlayerRankEntry(new PlayerRankEntry(Bukkit.getOfflinePlayer(entry.getKey()).getName(),entry.getValue()));
                    iterator.remove();
                }else{
                    attendingPlayers.add(player);
                    player.teleport(placeLocation.clone().add(new Vector(PluginRandom.randomGenerator.nextFloat(),0,PluginRandom.randomGenerator.nextFloat())));
                    player.setGameMode(GameMode.ADVENTURE);
                }
            }
            Audience attendingPlayerAudiences = Audience.audience(attendingPlayers);
            attendingPlayerAudiences.sendMessage(Component.translatable("luckygames.message.match_beginning_in_5_seconds"));
            Bukkit.getScheduler().runTaskTimer(this, new Consumer<BukkitTask>() {
                int countdown = 5;
                @Override
                public void accept(BukkitTask bukkitTask) {
                    if(countdown == 0){
                        attendingPlayerAudiences.showTitle(Title.title(Component.translatable("luckygames.message.match_begin"),Component.translatable("luckygames.message.be_the_last_one_standing")));
                        Bukkit.getServer().sendMessage(Component.translatable("luckygames.message.match_begin"));
                        Bukkit.getServer().sendMessage(Component.translatable("luckygames.message.be_the_last_one_standing"));
                        gameWorld.setGameRule(GameRules.PVP,true);
                        attendingPlayers.forEach(player -> player.setGameMode(GameMode.SURVIVAL));
                        bukkitTask.cancel();
                        return;
                    }
                    attendingPlayerAudiences.showTitle(Title.title(Component.text(countdown,countdown > 3?NamedTextColor.GREEN:countdown>1?NamedTextColor.YELLOW:NamedTextColor.RED, TextDecoration.BOLD),Component.empty()));
                    countdown--;
                }
            }, 20, 20);
            gameState.setPhase(GamePhase.EPILOGUE);
        },80);
    }

    private void onFinish(){
        gameState.setPhase(GamePhase.FINISHED);
        Bukkit.getScheduler().runTaskLater(this,task->{
            for (Map.Entry<UUID, Integer> entry : gameState.getAllPlayerTeamAttribution().entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if(player != null) player.showTitle(Title.title(Component.translatable("luckygames.message.victory"),Component.text("LUCKY!",NamedTextColor.GREEN)));
                gameState.addPlayerRankEntry(new PlayerRankEntry(Bukkit.getOfflinePlayer(entry.getKey()).getName(),entry.getValue()));
            }
            TextComponent.Builder playerRank = Component.text();
            int rank = 1;
            for(PlayerRankEntry entry: gameState.getPlayerRankEntries().reversed()){
                TextColor textColor = NamedTextColor.GRAY;
                switch (rank){
                    case 1:
                        textColor = NamedTextColor.YELLOW;
                        break;
                    case 2:
                        textColor = NamedTextColor.BLUE;
                        break;
                    case 3:
                        textColor = NamedTextColor.GREEN;
                        break;
                    default:
                        break;
                }
                playerRank.append(Component.text("#",textColor));
                playerRank.append(Component.text(rank,textColor));
                playerRank.append(Component.space());
                playerRank.append(Component.text("["));
                playerRank.append(Component.text(entry.getTeam(),textColor));
                playerRank.append(Component.text("] "));
                playerRank.append(Component.text(entry.getName(),NamedTextColor.AQUA));
                playerRank.append(Component.newline());
                rank++;
            }
            Bukkit.getServer().sendMessage(Component.translatable("luckygames.message.game_ended",playerRank.build()));
        },2);
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent e){
        if(e.getType() == ServerLoadEvent.LoadType.STARTUP){
            worldLobby.reset();
            gameState.setPhase(GamePhase.WAITING);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        if(gameState.getPhase() == GamePhase.WAITING || gameState.getPhase() == GamePhase.PREPARING){
            worldLobby.sendHereThenSetRespawn(player);
            player.setGameMode(GameMode.SURVIVAL);
        }else if(gameState.getPhase() == GamePhase.RUNNING || gameState.getPhase() == GamePhase.EPILOGUE){
            if(player.getWorld().getKey().equals(worldGame.getWorldKey())){
                if(GamePlayer.getPlayerTeam(player) >= 0){
                    if(gameState.getThisGameSettings().isSaturation()) player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION,-1,0,false,false,true));
                    if(gameState.getThisGameSettings().isNightVision()) player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,-1,0,false,false,true));
                }
                return;
            };
            if(GamePlayer.getPlayerTeam(player) < 0){
                worldGame.sendHereAsSpectator(player,true);
            }
        }else{
            if(player.getWorld().getKey().equals(worldGame.getWorldKey())) return;
            worldGame.sendHereAsSpectator(player,true);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerPostRespawnEvent e){
        Player player = e.getPlayer();
        if(gameState.getPhase() == GamePhase.WAITING || gameState.getPhase() == GamePhase.PREPARING){
            player.setGameMode(GameMode.SURVIVAL);
            if(!e.getRespawnLocation().getWorld().getKey().equals(worldLobby.getWorldKey())) worldLobby.sendHere(player);
        }else if(gameState.getPhase() == GamePhase.RUNNING || gameState.getPhase() == GamePhase.EPILOGUE){
            if(e.getRespawnLocation().getWorld().getKey().equals(worldGame.getWorldKey())) {
                if(GamePlayer.getPlayerTeam(player) < 0) player.setGameMode(GameMode.SPECTATOR);
                else {
                    if(gameState.getThisGameSettings().isSaturation()) player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION,-1,0,false,false,true));
                    if(gameState.getThisGameSettings().isNightVision()) player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,-1,0,false,false,true));
                    player.setGameMode(GameMode.SURVIVAL);
                }
            }else{
                if(GamePlayer.getPlayerTeam(player) < 0) worldGame.sendHereAsSpectator(player,true);
                else gameState.setPlayerRespawn(player.getUniqueId(),0,true);
            }
        }else{
            worldGame.sendHereAsSpectator(player,true);
        }
    }
}
