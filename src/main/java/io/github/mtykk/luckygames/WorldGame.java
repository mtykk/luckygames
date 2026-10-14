package io.github.mtykk.luckygames;

import com.jeff_media.customblockdata.CustomBlockData;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

public class WorldGame {
    private static final int ITEMS_TO_GENERATE_PER_BATCH = 5;
    private final JavaPlugin plugin;
    private final BlockProtector protector;
    private final NamespacedKey worldKey;
    private final File worldDimensionFolder;
    private final GameStructures gameStructures;
    private final LuckyBlock luckyBlock;
    WorldGame(JavaPlugin plugin,BlockProtector protector,LuckyBlock luckyBlock,GameStructures gameStructures){
        this.plugin = plugin;
        this.protector = protector;
        this.luckyBlock = luckyBlock;
        this.gameStructures = gameStructures;
        worldKey = new NamespacedKey(plugin,"game");
        worldDimensionFolder = new File(Bukkit.getServer().getLevelDirectory().toFile(),"dimensions/"+worldKey.getNamespace()+"/"+worldKey.getKey());
    }

    private void put(GameSettings gameSettings, int lanes, IndexedGameLocation locationRecorder,
                     Consumer<World> onResetFinish, Consumer<Throwable> onResetFailure) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task -> {
            try {
                if (worldDimensionFolder.exists()) FileHelper.deleteDir(worldDimensionFolder);
            } catch (IOException | RuntimeException exception) {
                Bukkit.getScheduler().runTask(plugin, mainTask -> onResetFailure.accept(exception));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, task2 -> {
                try {
                    WorldCreator worldCreator = new WorldCreator(worldKey);
                    worldCreator.type(WorldType.FLAT);
                    worldCreator.generateStructures(false);
                    worldCreator.generatorSettings("{\"layers\":[],\"biome\":\"plains\"}");

                    World world = worldCreator.createWorld();
                    if (world == null) throw new IllegalStateException("Paper failed to create the game world");
                    world.setGameRule(GameRules.ADVANCE_TIME, false);
                    world.setGameRule(GameRules.ADVANCE_WEATHER, false);
                    world.setGameRule(GameRules.ALLOW_ENTERING_NETHER_USING_PORTALS, false);
                    world.setGameRule(GameRules.KEEP_INVENTORY, gameSettings.isKeepInventory());
                    world.setGameRule(GameRules.TNT_EXPLODES, true);
                    world.setGameRule(GameRules.SPAWN_MOBS, false);
                    world.setGameRule(GameRules.SPAWN_MONSTERS, false);
                    world.setGameRule(GameRules.SPAWN_PATROLS, false);
                    world.setGameRule(GameRules.SPAWN_PHANTOMS, false);
                    world.setGameRule(GameRules.SPAWN_WANDERING_TRADERS, false);
                    world.setGameRule(GameRules.SPAWN_WARDENS, false);
                    world.setGameRule(GameRules.PROJECTILES_CAN_BREAK_BLOCKS, true);
                    world.setGameRule(GameRules.PVP, true);
                    world.setFullTime(12000);
                    world.setDifficulty(Difficulty.HARD);

                    generate(gameSettings, lanes, locationRecorder, world, onResetFinish, onResetFailure);
                } catch (RuntimeException exception) {
                    onResetFailure.accept(exception);
                }
            });
        });
    }

    public void reset(GameSettings gameSettings, int lanes, IndexedGameLocation locationRecorder,
                      Consumer<World> onResetFinish) throws IllegalStateException {
        reset(gameSettings, lanes, locationRecorder, onResetFinish, exception ->
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Could not reset the game world", exception));
    }

    public void reset(GameSettings gameSettings, int lanes, IndexedGameLocation locationRecorder,
                      Consumer<World> onResetFinish, Consumer<Throwable> onResetFailure) throws IllegalStateException {
        World overworld = Bukkit.getWorld(NamespacedKey.minecraft("overworld"));
        if (overworld == null) throw new IllegalStateException("Overworld not found, though normally not needed.");
        if (lanes < 1 || lanes > GamePlayer.MAX_TEAM_COUNT) {
            throw new IllegalArgumentException("Invalid lane count: " + lanes);
        }
        if (onResetFinish == null || onResetFailure == null) {
            throw new IllegalArgumentException("World reset callbacks are required");
        }
        World world = Bukkit.getWorld(worldKey);
        if (world != null) {
            world.getPlayers().forEach(player -> {
                player.sendMessage(Component.translatable("luckygames.warning.resetting_your_world"));
                World lobby = Bukkit.getWorld(new NamespacedKey(plugin, "lobby"));
                player.teleport(lobby != null ? lobby.getSpawnLocation() : overworld.getSpawnLocation());
            });
            Bukkit.getScheduler().runTaskTimer(plugin, task -> {
                if (Bukkit.isTickingWorlds()) return;
                task.cancel();
                try {
                    if (!Bukkit.unloadWorld(world, false)) {
                        onResetFailure.accept(new IllegalStateException("Could not unload the previous game world"));
                        return;
                    }
                    put(gameSettings, lanes, locationRecorder, onResetFinish, onResetFailure);
                } catch (RuntimeException exception) {
                    onResetFailure.accept(exception);
                }
            }, 1, 1);
            return;
        }
        put(gameSettings, lanes, locationRecorder, onResetFinish, onResetFailure);
    }

    private void generate(GameSettings gameSettings, int lanes, IndexedGameLocation locationRecorder, World gameWorld,
                         Consumer<World> onResetFinish, Consumer<Throwable> onResetFailure) {
        // Keep the old world's protection if unloading fails; its entries are stale only after
        // the replacement world has actually been created.
        protector.clear();
        locationRecorder.setLanes(lanes);
        int lastIndex = gameSettings.getLastIndex();

        Bukkit.getScheduler().runTaskTimer(plugin, new Consumer<BukkitTask>() {
            int currentLane = 0;
            int currentIndex = 0;
            int currentX = 0;
            int currentZ = 0;
            final int currentY = 70;

            @Override
            public void accept(BukkitTask task) {
                try {
                    for (int i = 0; i < ITEMS_TO_GENERATE_PER_BATCH; i++) {
                        if (currentIndex > lastIndex) {
                            currentLane++;
                            currentIndex = 0;
                            currentX = 0;
                            currentZ += gameStructures.getMaxZ() + 5;
                        }
                        if (currentLane >= lanes) {
                            task.cancel();
                            onResetFinish.accept(gameWorld);
                            return;
                        }

                        if (currentIndex == 0) {
                            Location location = new Location(gameWorld, currentX, currentY, currentZ);
                            gameStructures.getStart().place(location, true, block -> {
                                block.setType(Material.AIR);
                                protector.protect(block);
                                protector.protect(block.getLocation().clone().add(0, 1, 0));
                            });
                            locationRecorder.addLocation(currentLane, location);
                            currentX += gameStructures.getStart().getSizeXP() + 3;
                        } else if (gameSettings.isCheckpoint(currentIndex)) {
                            currentX += gameStructures.getCheckpoint().getSizeXN() + 1;
                            Location location = new Location(gameWorld, currentX, currentY, currentZ);
                            gameStructures.getCheckpoint().place(location, true, block -> {
                                protector.protect(block);
                                protector.protect(block.getLocation().clone().add(0, 1, 0));
                                block.setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
                                PersistentDataContainer pdc = new CustomBlockData(block, plugin);
                                pdc.set(GameIndexDataKey.MARKER_LANE_ATTRIBUTION, PersistentDataType.INTEGER, currentLane);
                                pdc.set(GameIndexDataKey.MARKER_INDEX, PersistentDataType.INTEGER, currentIndex);
                            });
                            locationRecorder.addLocation(currentLane, location);
                            currentX += gameStructures.getCheckpoint().getSizeXP() + 3;
                        } else {
                            currentX += gameStructures.getIsland().getSizeXN();
                            Location location = new Location(gameWorld, currentX, currentY, currentZ);
                            gameStructures.getIsland().place(location, true, block -> {
                                LuckyBlockTypes type = WeightedRandomPicker.randomPick(
                                        Arrays.asList(LuckyBlockTypes.values()), gameSettings.getLuckyBlockDistribution());
                                luckyBlock.placeLuckyBlock(block.getLocation(), type, currentLane);
                                protector.protect(block);
                                PersistentDataContainer pdc = new CustomBlockData(block, plugin);
                                pdc.set(GameIndexDataKey.MARKER_LANE_ATTRIBUTION, PersistentDataType.INTEGER, currentLane);
                                pdc.set(GameIndexDataKey.MARKER_INDEX, PersistentDataType.INTEGER, currentIndex);
                            });
                            locationRecorder.addLocation(currentLane, location);
                            currentX += gameStructures.getIsland().getSizeXP() + 2;
                        }
                        currentIndex++;
                    }
                } catch (RuntimeException exception) {
                    task.cancel();
                    onResetFailure.accept(exception);
                }
            }
        }, 0, 1);
    }

    public void sendHereAsSpectator(Player player,boolean setRespawn) throws NullPointerException{
        World world = Bukkit.getWorld(worldKey);
        if(world == null) throw new NullPointerException("Game world not found");
        player.setGameMode(GameMode.SPECTATOR);
        Location spawnLocation = new Location(world,-3,75,-3);
        if (!player.teleport(spawnLocation)) {
            plugin.getLogger().warning("Could not teleport spectator " + player.getName() + " into the game world");
        }
        if(setRespawn) player.setRespawnLocation(spawnLocation,true);
    }

    public NamespacedKey getWorldKey() {
        return worldKey;
    }
}
