package io.github.mtykk.luckygames;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.UUID;

public record BlockIntLocation(UUID world, int x, int y,int z) {
    public static BlockIntLocation of(Location location){
        return new BlockIntLocation(location.getWorld().getUID(),location.getBlockX(),location.getBlockY(),location.getBlockZ());
    }
    public static BlockIntLocation of(Block block){
        return BlockIntLocation.of(block.getLocation());
    }
}
