package io.github.mtykk.luckygames;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
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
            if(worldDimensionFolder.exists()){
                FileHelper.deleteDir(worldDimensionFolder);
            }
            try(InputStream worldResource = overridableResource.getResource("dimensions/lobby.zip")){
                FileHelper.unzip(worldResource,worldDimensionFolder);
            }catch (IOException e){
                e.printStackTrace();
            }
            Bukkit.getScheduler().runTask(plugin,taskMain->{
                WorldCreator worldCreator = new WorldCreator(worldKey);
                worldCreator.type(WorldType.FLAT);
                worldCreator.generateStructures(false);
                worldCreator.generatorSettings("{\"layers\":[],\"biome\":\"plains\"}");
                World world = worldCreator.createWorld();

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

                world.getChunkAtAsync(introDisplayCoord.getBlockX(), introDisplayCoord.getBlockZ(), (Consumer<Chunk>) chunk -> world.spawn(new Location(world,introDisplayCoord.getX(),introDisplayCoord.getY(),introDisplayCoord.getZ()), TextDisplay.class, CreatureSpawnEvent.SpawnReason.CUSTOM, text->{
                    text.setBillboard(Display.Billboard.CENTER);
                    text.setInvulnerable(true);
                    text.setAlignment(TextDisplay.TextAlignment.CENTER);
                    text.text(Component.translatable("luckygames.message.intro",
                            Component.text(luckyEventCounts[0]),
                            Component.text(luckyEventCounts[1]),
                            Component.text(luckyEventCounts[2]),
                            Component.text(luckyEventCounts[3]),
                            Component.text(luckyEventCounts[0]+luckyEventCounts[1]+luckyEventCounts[2]+luckyEventCounts[3]),
                            Component.text(plugin.getPluginMeta().getVersion())));
                }));
                world.getChunkAtAsync(guideDisplayCoord.getBlockX(), guideDisplayCoord.getBlockZ(), (Consumer<Chunk>) chunk -> world.spawn(new Location(world,guideDisplayCoord.getX(),guideDisplayCoord.getY(),guideDisplayCoord.getZ()), TextDisplay.class, CreatureSpawnEvent.SpawnReason.CUSTOM, text->{
                    text.setBillboard(Display.Billboard.CENTER);
                    text.setInvulnerable(true);
                    text.setAlignment(TextDisplay.TextAlignment.CENTER);
                    text.text(Component.translatable("luckygames.message.guide"));
                }));
                world.getChunkAtAsync(ruleDisplayCoord.getBlockX(), ruleDisplayCoord.getBlockZ(),(Consumer<Chunk>)chunk-> {world.spawn(new Location(world, ruleDisplayCoord.getX(), ruleDisplayCoord.getY(), ruleDisplayCoord.getZ()), TextDisplay.class, CreatureSpawnEvent.SpawnReason.CUSTOM, text -> {
                        text.setBillboard(Display.Billboard.CENTER);
                        text.setInvulnerable(true);
                        text.setAlignment(TextDisplay.TextAlignment.CENTER);
                        text.text(Component.translatable("luckygames.message.rule",Component.text(LuckyGames.getInstance().getGameSettings().isKeepInventory()),Component.text(LuckyGames.getInstance().getGameSettings().isSaturation()),Component.text(LuckyGames.getInstance().getGameSettings().isNightVision())));
                    });
                });
            });
        });
    }

    public void reset(){
        World overworld = Bukkit.getWorld(NamespacedKey.minecraft("overworld"));
        if(overworld == null) throw new IllegalStateException("Overworld not found, though normally not needed.");
        {
            World world = Bukkit.getWorld(worldKey);
            if (world != null) {
                //World exists
                world.getPlayers().forEach(player -> {
                    player.sendMessage(Component.translatable("luckygames.warning.resetting_your_world"));
                    player.teleport(overworld.getSpawnLocation());
                });
                Bukkit.getScheduler().runTaskTimer(plugin, task -> {
                    if (!Bukkit.isTickingWorlds()) {
                        Bukkit.unloadWorld(world, false);
                        put();
                        task.cancel();
                    }
                }, 1, 0);
                return;
            }
        }
        put();
    }

    public void sendHere(Player player) throws IllegalStateException{
        if(player.getWorld().getKey().equals(worldKey)) return;
        World world = Bukkit.getWorld(worldKey);
        if(world == null){
            player.teleport(Bukkit.getWorld(NamespacedKey.minecraft("overworld")).getSpawnLocation());
            player.sendMessage(Component.translatable("luckygames.message.lobby_unavailable"));
            throw new IllegalStateException("Trying to send a player to an unavailable lobby");
        }else{
            player.teleport(world.getSpawnLocation());
        }
    }
    public void sendHereThenSetRespawn(Player player) throws IllegalStateException{
        sendHere(player);
        World world = Bukkit.getWorld(worldKey);
        if(world != null) player.setRespawnLocation(world.getSpawnLocation(),true);
        else throw new IllegalStateException("Can't set a player's respawn to a non-existent lobby");
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
