package io.github.mtykk.luckygames;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.structure.Structure;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class GameStructure {
    private final BlockProtector protector;
    private final Structure structure;
    private final int sizeX;
    private final Vector offset;

    /**
     * Create a protectable structure
     * @param structure The structure
     * @param protector The protector which can be used to protect the structure
     */
    GameStructure(Structure structure,BlockProtector protector){
        this.structure = structure;
        this.protector = protector;
        sizeX = structure.getSize().getBlockX();
        Location markerLocation = structure.getPalettes().getFirst().getBlocks().stream().filter(blockState -> blockState.getType().equals(Material.SPONGE)).findFirst().get().getLocation();
        offset = new Vector(-markerLocation.getBlockX(),-markerLocation.getBlockY(),-markerLocation.getBlockZ());
    }

    /**
     * Place the structure with the marker precisely at the given location
     * @param location Center location
     * @param protect Whether the structure be protected
     * @param markerBlockOperation The operation for the marker block (A sponge)
     */
    public void place(Location location, boolean protect,@Nullable Consumer<Block> markerBlockOperation){
        structure.place(location.clone().add(offset),
                false,
                StructureRotation.NONE,
                Mirror.NONE,
                0,
                1.0f,
                PluginRandom.randomGenerator,
                List.of((region, x, y, z, current, state) -> {
                    Location blockLocation = new Location(location.getWorld(),x,y,z);
                    if(protect) {
                        if (current.getType() != Material.AIR && current.getType() != Material.SPONGE && current.getType() != Material.WATER && current.getType() != Material.LAVA) protector.protect(blockLocation);
                    }
                    return current;
                }),
                List.of()
                );
        if(markerBlockOperation != null){
            markerBlockOperation.accept(location.getBlock());
        }
    }
    public void placeFast(Location location,boolean protect,@Nullable Consumer<Block> markerBlockOperation){
        Location corner1 = location.clone().add(offset);
        int cornerX = corner1.getBlockX();
        int cornerY = corner1.getBlockY();
        int cornerZ = corner1.getBlockZ();
        UUID worldUUID = location.getWorld().getUID();
        structure.place(corner1,
                false,
                StructureRotation.NONE,
                Mirror.NONE,
                0,
                1.0f,
                PluginRandom.randomGenerator);
        if(markerBlockOperation != null){
            markerBlockOperation.accept(location.getBlock());
        }
        if(protect){
            for(BlockState block:structure.getPalettes().getFirst().getBlocks()){
                if(block.getType() != Material.AIR && block.getType() != Material.SPONGE && block.getType() != Material.WATER && block.getType() != Material.LAVA){
                    protector.protect(new BlockIntLocation(worldUUID,cornerX+block.getX(),cornerY+block.getY(),cornerZ+block.getZ()));
                }
            }
        }
    }

    public BlockVector getSize(){
        return structure.getSize();
    }

    public int getSizeX() {
        return sizeX;
    }

    public Structure getStructure() {
        return structure;
    }

    /**
     * Get the distance of the marker to the border on positive X axis
     * @return The distance
     */
    public int getSizeXP(){
        return sizeX-offset.getBlockX()-1;
    }
    /**
     * Get the distance of the marker to the border on negative X axis
     * @return The distance
     */
    public int getSizeXN(){
        return offset.getBlockX();
    }

    public Vector getOffset() {
        return offset;
    }
}
