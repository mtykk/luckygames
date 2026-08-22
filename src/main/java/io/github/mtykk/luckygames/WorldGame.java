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
    private final int[] luckyBlockDistribution;
    WorldGame(JavaPlugin plugin,BlockProtector protector,LuckyBlock luckyBlock,GameStructures gameStructures,int[] luckyBlockDistribution){
        this.plugin = plugin;
        this.protector = protector;
        this.luckyBlock = luckyBlock;
        this.luckyBlockDistribution = luckyBlockDistribution;
        this.gameStructures = gameStructures;
        worldKey = new NamespacedKey(plugin,"game");
        worldDimensionFolder = new File(Bukkit.getServer().getLevelDirectory().toFile(),"dimensions/"+worldKey.getNamespace()+"/"+worldKey.getKey());
    }

    private void put(GameSettings gameSettings,int lanes,IndexedGameLocation locationRecorder,Consumer<World> onResetFinish){
        Bukkit.getScheduler().runTaskAsynchronously(plugin,(Consumer<BukkitTask>)task->{
            if(worldDimensionFolder.exists()){
                FileHelper.deleteDir(worldDimensionFolder);
            }
            Bukkit.getScheduler().runTask(plugin,(Consumer<BukkitTask>) task2->{
                WorldCreator worldCreator = new WorldCreator(worldKey);
                worldCreator.type(WorldType.FLAT);
                worldCreator.generateStructures(false);
                worldCreator.generatorSettings("{\"layers\":[],\"biome\":\"plains\"}");

                World world = worldCreator.createWorld();
                world.setGameRule(GameRules.ADVANCE_TIME,false);
                world.setGameRule(GameRules.ADVANCE_WEATHER,false);
                world.setGameRule(GameRules.ALLOW_ENTERING_NETHER_USING_PORTALS,false);
                world.setGameRule(GameRules.KEEP_INVENTORY,gameSettings.isKeepInventory());
                world.setGameRule(GameRules.TNT_EXPLODES,true);
                world.setGameRule(GameRules.SPAWN_MOBS,false);
                world.setGameRule(GameRules.SPAWN_MONSTERS,false);
                world.setGameRule(GameRules.SPAWN_PATROLS,false);
                world.setGameRule(GameRules.SPAWN_PHANTOMS,false);
                world.setGameRule(GameRules.SPAWN_WANDERING_TRADERS,false);
                world.setGameRule(GameRules.SPAWN_WARDENS,false);
                world.setGameRule(GameRules.PROJECTILES_CAN_BREAK_BLOCKS,true);
                world.setGameRule(GameRules.PVP,true);
                world.setFullTime(12000);
                world.setDifficulty(Difficulty.HARD);

                generate(gameSettings,lanes,locationRecorder,onResetFinish);
            });
        });
    }

    public void reset(GameSettings gameSettings,int lanes,IndexedGameLocation locationRecorder,Consumer<World> onResetFinish) throws IllegalStateException{
        World overworld = Bukkit.getWorld(NamespacedKey.minecraft("overworld"));
        if(overworld == null) throw new IllegalStateException("Overworld not found, though normally not needed.");
        World world = Bukkit.getWorld(worldKey);
        if (world != null) {
            //World exists
            world.getPlayers().forEach(player -> {
                player.sendMessage(Component.translatable("luckygames.warning.resetting_your_world"));
                World lobby = Bukkit.getWorld(new NamespacedKey(plugin,"lobby"));
                if(lobby != null) player.teleport(lobby.getSpawnLocation());
                else player.teleport(overworld.getSpawnLocation());
            });
            Bukkit.unloadWorld(world,false);
        }
        put(gameSettings,lanes,locationRecorder,onResetFinish);
    }

    private void generate(GameSettings gameSettings, int lanes,IndexedGameLocation locationRecorder,Consumer<World> onResetFinish){
        //Generate the game world in multiple batches
        locationRecorder.setLanes(lanes);
        int itemsPerSection = gameSettings.getItemsPerSection();
        int sectionCount = gameSettings.getSectionCount();
        World gameWorld = Bukkit.getWorld(worldKey);

        Bukkit.getScheduler().runTaskTimer(plugin, new Consumer<BukkitTask>() {
            int currentLane = 0;
            int currentIndex = 0;
            int currentX = 0;
            int currentZ = 0;
            final int currentY = 70;
            @Override
            public void accept(BukkitTask task) {
                for(int i = 0;i < ITEMS_TO_GENERATE_PER_BATCH;i++){
                    if(currentIndex > (itemsPerSection+1)*sectionCount){
                        currentLane++;
                        currentIndex = 0;
                        currentX = 0;
                        currentZ += gameStructures.getMaxZ()+5; //Max Z and a buffer, buffer could be changed (tho hardcoded currently)
                    }
                    if(currentLane >= lanes){
                        onResetFinish.accept(gameWorld);
                        task.cancel();
                        break;
                    }

                    if(currentIndex == 0){
                        Location loc = new Location(gameWorld,currentX,currentY,currentZ);
                        gameStructures.getStart().place(loc,true,block -> {
                            block.setType(Material.AIR);
                            protector.protect(block);
                            protector.protect(block.getLocation().clone().add(0,1,0));
                        });
                        locationRecorder.addLocation(currentLane,loc);
                        currentX += gameStructures.getStart().getSizeXP() + 3;// Also hardcoded buffer
                    }else if(gameSettings.isCheckpoint(currentIndex)){
                        currentX += gameStructures.getCheckpoint().getSizeXN()+1;
                        Location loc = new Location(gameWorld,currentX,currentY,currentZ);
                        gameStructures.getCheckpoint().place(loc,true,block -> {
                            protector.protect(block);
                            protector.protect(block.getLocation().clone().add(0,1,0));
                            block.setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
                            PersistentDataContainer pdc = new CustomBlockData(block,plugin);
                            pdc.set(GameIndexDataKey.MARKER_LANE_ATTRIBUTION, PersistentDataType.INTEGER,currentLane);
                            pdc.set(GameIndexDataKey.MARKER_INDEX,PersistentDataType.INTEGER,currentIndex);
                        });
                        locationRecorder.addLocation(currentLane,loc);
                        currentX += gameStructures.getCheckpoint().getSizeXP() + 3;
                    }else{
                        currentX += gameStructures.getIsland().getSizeXN();
                        Location loc = new Location(gameWorld,currentX,currentY,currentZ);
                        gameStructures.getIsland().place(loc,true,block -> {
                            luckyBlock.placeLuckyBlock(block.getLocation(),WeightedRandomPicker.randomPick(Arrays.asList(LuckyBlockTypes.values()),luckyBlockDistribution),currentLane);
                            protector.protect(block);
                            PersistentDataContainer pdc = new CustomBlockData(block,plugin);
                            pdc.set(GameIndexDataKey.MARKER_LANE_ATTRIBUTION, PersistentDataType.INTEGER,currentLane);
                            pdc.set(GameIndexDataKey.MARKER_INDEX,PersistentDataType.INTEGER,currentIndex);
                        });
                        locationRecorder.addLocation(currentLane,loc);
                        currentX += gameStructures.getIsland().getSizeXP() + 2;
                    }
                    currentIndex++;
                }
            }
        }, 0, 1);
    }

    public void sendHereAsSpectator(Player player,boolean setRespawn) throws NullPointerException{
        World world = Bukkit.getWorld(worldKey);
        if(world == null) throw new NullPointerException("Game world not found");
        player.setGameMode(GameMode.SPECTATOR);
        Location spawnLocation = new Location(world,-3,75,-3);
        player.teleport(spawnLocation);
        if(setRespawn) player.setRespawnLocation(spawnLocation,true);
    }

    public NamespacedKey getWorldKey() {
        return worldKey;
    }
}
