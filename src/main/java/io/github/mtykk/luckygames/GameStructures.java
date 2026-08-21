package io.github.mtykk.luckygames;

import org.bukkit.Bukkit;

public class GameStructures {
    private final GameStructure island;
    private final GameStructure start;
    private final GameStructure checkpoint;
    private final GameStructure arena;
    private final int maxZ;

    GameStructures(BlockProtector protector){
        island = new GameStructure(Bukkit.getStructureManager().getStructure(PluginStructures.LUCKY_ISLAND.getStructureKey()),protector);
        start = new GameStructure(Bukkit.getStructureManager().getStructure(PluginStructures.START.getStructureKey()),protector);
        checkpoint = new GameStructure(Bukkit.getStructureManager().getStructure(PluginStructures.CHECKPOINT.getStructureKey()),protector);
        arena = new GameStructure(Bukkit.getStructureManager().getStructure(PluginStructures.ARENA.getStructureKey()),protector);
        maxZ = Math.max(island.getSize().getBlockZ(),Math.max(start.getSize().getBlockZ(),checkpoint.getSize().getBlockZ()));
    }

    /**
     * Maximum Z value of the sizes of island, start and checkpoint
     * @return Maximum Z
     */
    public int getMaxZ() {
        return maxZ;
    }

    public GameStructure getStart() {
        return start;
    }

    public GameStructure getCheckpoint() {
        return checkpoint;
    }

    public GameStructure getIsland() {
        return island;
    }

    public GameStructure getArena(){
        return arena;
    }
}
