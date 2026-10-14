package io.github.mtykk.luckygames;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class WorldLobby {
    private final JavaPlugin plugin;
    private NamespacedKey worldKey = null;
    private OverridableResource overridableResource = null;
    private File worldDimensionFolder = null;
    private final Vector spawnCoord;
    private final float spawnYaw;
    private final Vector introDisplayCoord;
    private final Vector guideDisplayCoord;
    private static Vector ruleDisplayCoord;
    private int[] luckyEventCounts = null;
    private volatile boolean resetInProgress;
    WorldLobby(@NotNull JavaPlugin plugin,@NotNull OverridableResource overridableResource,int[] luckyEventCounts){
        this.plugin = plugin;
        this.overridableResource = overridableResource;
        this.luckyEventCounts = luckyEventCounts;
        worldKey = new NamespacedKey(plugin,"lobby");
        worldDimensionFolder = new File(Bukkit.getServer().getLevelDirectory().toFile(),"dimensions/"+worldKey.getNamespace()+"/"+worldKey.getKey());
        FileConfiguration config = plugin.getConfig();
        spawnCoord = new Vector(config.getDouble("lobby.spawn.x"),config.getDouble("lobby.spawn.y"),config.getDouble("lobby.spawn.z"));
        spawnYaw = (float)config.getDouble("lobby.spawn.yaw");
        introDisplayCoord = new Vector(config.getDouble("lobby.intro-display.x"),config.getDouble("lobby.intro-display.y"),config.getDouble("lobby.intro-display.z"));
        guideDisplayCoord = new Vector(config.getDouble("lobby.guide-display.x"),config.getDouble("lobby.guide-display.y"),config.getDouble("lobby.guide-display.z"));
        ruleDisplayCoord = new Vector(config.getDouble("lobby.rule-display.x"),config.getDouble("lobby.rule-display.y"),config.getDouble("lobby.rule-display.z"));
    }

    /**
     * Unzips the world and registers it
     * Always make sure to call this method after unloading the world
     */
    private void put(){
        Bukkit.getScheduler().runTaskAsynchronously(plugin,taskAsync->{
            try (InputStream worldResource = overridableResource.getResource("dimensions/lobby.zip")) {
                if (worldResource == null) throw new IOException("Missing dimensions/lobby.zip resource");
                if (worldDimensionFolder.exists()) FileHelper.deleteDir(worldDimensionFolder);
                FileHelper.unzip(worldResource, worldDimensionFolder);
            } catch (IOException | RuntimeException e) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Failed to prepare the lobby world", e);
                resetInProgress = false;
                return;
            }
            try {
                Bukkit.getScheduler().runTask(plugin, taskMain -> {
                    try {
                        WorldCreator worldCreator = new WorldCreator(worldKey);
                        worldCreator.type(WorldType.FLAT);
                        worldCreator.generateStructures(false);
                        worldCreator.generatorSettings("{\"layers\":[],\"biome\":\"plains\"}");
                        World world = worldCreator.createWorld();
                        if (world == null) {
                            plugin.getLogger().severe("Paper failed to create the lobby world");
                            resetInProgress = false;
                            return;
                        }

                        world.setGameRule(GameRules.ADVANCE_TIME,false);
                        world.setGameRule(GameRules.ADVANCE_WEATHER,false);
                        world.setGameRule(GameRules.ALLOW_ENTERING_NETHER_USING_PORTALS,false);
                        world.setGameRule(GameRules.KEEP_INVENTORY,true);
                        world.setGameRule(GameRules.TNT_EXPLODES,false);
                        world.setGameRule(GameRules.SPAWN_MOBS,false);
                        world.setGameRule(GameRules.SPAWN_MONSTERS,false);
                        world.setGameRule(GameRules.SPAWN_PATROLS,false);
                        world.setGameRule(GameRules.SPAWN_PHANTOMS,false);
                        world.setGameRule(GameRules.SPAWN_WANDERING_TRADERS,false);
                        world.setGameRule(GameRules.SPAWN_WARDENS,false);
                        world.setGameRule(GameRules.PROJECTILES_CAN_BREAK_BLOCKS,false);
                        world.setGameRule(GameRules.MOB_GRIEFING,false);
                        world.setDifficulty(Difficulty.NORMAL);
                        world.setSpawnLocation(spawnCoord.getBlockX(),spawnCoord.getBlockY(),spawnCoord.getBlockZ(),spawnYaw);

                        AtomicInteger pendingDisplays = new AtomicInteger(3);
                        spawnTextDisplay(world, introDisplayCoord, pendingDisplays, text -> {
                            text.setBillboard(Display.Billboard.CENTER);
                            text.setInvulnerable(true);
                            text.setAlignment(TextDisplay.TextAlignment.CENTER);
                            text.text(Component.translatable("luckygames.message.intro",
                                    Component.text(luckyEventCounts[0]),
                                    Component.text(luckyEventCounts[1]),
                                    Component.text(luckyEventCounts[2]),
                                    Component.text(luckyEventCounts[3]),
                                    Component.text(luckyEventCounts[0] + luckyEventCounts[1] + luckyEventCounts[2] + luckyEventCounts[3]),
                                    Component.text(plugin.getPluginMeta().getVersion())));
                        });
                        spawnTextDisplay(world, guideDisplayCoord, pendingDisplays, text -> {
                            text.setBillboard(Display.Billboard.CENTER);
                            text.setInvulnerable(true);
                            text.setAlignment(TextDisplay.TextAlignment.CENTER);
                            text.text(Component.translatable("luckygames.message.guide"));
                        });
                        spawnTextDisplay(world, ruleDisplayCoord, pendingDisplays, text -> {
                            text.setBillboard(Display.Billboard.CENTER);
                            text.setInvulnerable(true);
                            text.setAlignment(TextDisplay.TextAlignment.CENTER);
                            text.text(Component.translatable("luckygames.message.rule",
                                    Component.text(LuckyGames.getInstance().getGameSettings().isKeepInventory()),
                                    Component.text(LuckyGames.getInstance().getGameSettings().isSaturation()),
                                    Component.text(LuckyGames.getInstance().getGameSettings().isNightVision())));
                        });
                    } catch (RuntimeException exception) {
                        resetInProgress = false;
                        plugin.getLogger().log(java.util.logging.Level.SEVERE, "Failed to create the lobby world", exception);
                    }
                });
            } catch (RuntimeException exception) {
                resetInProgress = false;
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Could not schedule lobby world creation", exception);
            }
        });
    }

    private void spawnTextDisplay(World world, Vector coordinate, AtomicInteger pendingDisplays,
                                  Consumer<TextDisplay> initializer) {
        try {
            Location displayLocation = new Location(world, coordinate.getX(), coordinate.getY(), coordinate.getZ());
            world.getChunkAtAsync(displayLocation, (Consumer<Chunk>) chunk -> {
                try {
                    if (chunk == null) {
                        plugin.getLogger().warning("Could not load lobby display chunk at " + coordinate);
                        return;
                    }
                    world.spawn(displayLocation, TextDisplay.class, CreatureSpawnEvent.SpawnReason.CUSTOM, initializer);
                } catch (RuntimeException exception) {
                    plugin.getLogger().log(java.util.logging.Level.WARNING, "Could not spawn a lobby text display", exception);
                } finally {
                    if (pendingDisplays.decrementAndGet() == 0) resetInProgress = false;
                }
            });
        } catch (RuntimeException exception) {
            plugin.getLogger().log(java.util.logging.Level.WARNING, "Could not schedule a lobby display chunk", exception);
            if (pendingDisplays.decrementAndGet() == 0) resetInProgress = false;
        }
    }

    public void reset() {
        if (resetInProgress) return;
        resetInProgress = true;
        try {
            World overworld = Bukkit.getWorld(NamespacedKey.minecraft("overworld"));
            if (overworld == null) throw new IllegalStateException("Overworld not found, though normally not needed.");
            World world = Bukkit.getWorld(worldKey);
            if (world != null) {
                world.getPlayers().forEach(player -> {
                    player.sendMessage(Component.translatable("luckygames.warning.resetting_your_world"));
                    player.teleport(overworld.getSpawnLocation());
                });
                Bukkit.getScheduler().runTaskTimer(plugin, task -> {
                    if (Bukkit.isTickingWorlds()) return;
                    task.cancel();
                    try {
                        if (!Bukkit.unloadWorld(world, false)) {
                            plugin.getLogger().severe("Could not unload the lobby world; leaving its files untouched");
                            resetInProgress = false;
                            return;
                        }
                        put();
                    } catch (RuntimeException exception) {
                        resetInProgress = false;
                        plugin.getLogger().log(java.util.logging.Level.SEVERE, "Could not reset the lobby world", exception);
                    }
                }, 1, 1);
                return;
            }
            put();
        } catch (RuntimeException exception) {
            resetInProgress = false;
            throw exception;
        }
    }

    public void sendHere(Player player) throws IllegalStateException{
        if(player.getWorld().getKey().equals(worldKey)) return;
        World world = Bukkit.getWorld(worldKey);
        if(world == null){
            World overworld = Bukkit.getWorld(NamespacedKey.minecraft("overworld"));
            if (overworld != null) player.teleport(overworld.getSpawnLocation());
            player.sendMessage(Component.translatable("luckygames.message.lobby_unavailable"));
            throw new IllegalStateException("Trying to send a player to an unavailable lobby");
        } else if (!player.teleport(world.getSpawnLocation())) {
            player.sendMessage(Component.translatable("luckygames.message.lobby_teleport_failed"));
            throw new IllegalStateException("Teleport to the lobby was cancelled");
        }
    }
    public void sendHereThenSetRespawn(Player player) throws IllegalStateException {
        sendHere(player);
        World world = Bukkit.getWorld(worldKey);
        if (world != null) player.setRespawnLocation(world.getSpawnLocation(), true);
        else throw new IllegalStateException("Can't set a player's respawn to a non-existent lobby");
    }

    public void sendHereThenSetRespawnWhenAvailable(Player player) {
        if (!resetInProgress && Bukkit.getWorld(worldKey) != null) {
            try {
                sendHereThenSetRespawn(player);
            } catch (IllegalStateException exception) {
                plugin.getLogger().warning("Could not send " + player.getName() + " to the lobby: " + exception.getMessage());
            }
            return;
        }

        try {
            Bukkit.getScheduler().runTaskTimer(plugin, new Consumer<BukkitTask>() {
                private int waitedTicks;

                @Override
                public void accept(BukkitTask task) {
                    if (!player.isOnline()) {
                        task.cancel();
                        return;
                    }
                    LuckyGames luckyGames = LuckyGames.getInstance();
                    if (luckyGames == null) {
                        task.cancel();
                        return;
                    }
                    GamePhase phase = luckyGames.getGameState().getPhase();
                    if (phase != GamePhase.WAITING && phase != GamePhase.PREPARING
                            && phase != GamePhase.FINISHED) {
                        task.cancel();
                        return;
                    }
                    if (!resetInProgress) {
                        World world = Bukkit.getWorld(worldKey);
                        if (world != null) {
                            task.cancel();
                            try {
                                sendHereThenSetRespawn(player);
                            } catch (IllegalStateException exception) {
                                plugin.getLogger().warning("Could not send " + player.getName() + " to the lobby: " + exception.getMessage());
                            }
                            return;
                        }
                    }
                    if (++waitedTicks >= 1200) {
                        task.cancel();
                        player.sendMessage(Component.translatable("luckygames.message.lobby_unavailable"));
                        plugin.getLogger().warning("Lobby did not become available for joining player " + player.getName());
                    }
                }
            }, 1, 1);
        } catch (RuntimeException exception) {
            player.sendMessage(Component.translatable("luckygames.message.lobby_unavailable"));
            plugin.getLogger().log(java.util.logging.Level.WARNING, "Could not schedule lobby arrival for " + player.getName(), exception);
        }
    }

    public NamespacedKey getWorldKey() {
        return worldKey;
    }

    public static void updateRuleDisplay(GameSettings gameSettings){
        World world = Bukkit.getWorld(new NamespacedKey("luckygames","lobby"));
        if(world == null) return;
        Collection<TextDisplay> textDisplays = world.getNearbyEntitiesByType(TextDisplay.class,new Location(world, ruleDisplayCoord.getX(), ruleDisplayCoord.getY(), ruleDisplayCoord.getZ()),0.5);
        if(textDisplays.isEmpty()) return;
        for(TextDisplay textDisplay: textDisplays){
            textDisplay.text(Component.translatable("luckygames.message.rule",Component.text(gameSettings.isKeepInventory()),Component.text(gameSettings.isSaturation()),Component.text(gameSettings.isNightVision())));
        }
    }
}
