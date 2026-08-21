package io.github.mtykk.luckygames;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class BlockProtector implements Listener {
    private final JavaPlugin plugin;
    private final Set<BlockIntLocation> protectedLocation = new HashSet<>();
    BlockProtector(JavaPlugin plugin){
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }

    public boolean isProtected(BlockIntLocation blockIntLocation){
        return protectedLocation.contains(blockIntLocation);
    }
    public boolean isProtected(Location blockLocation){
        return isProtected(BlockIntLocation.of(blockLocation));
    }
    public boolean isProtected(Block block){
        return isProtected(block.getLocation());
    }
    public void clear(){
        protectedLocation.clear();
    }
    public void protect(BlockIntLocation blockIntLocation){
        protectedLocation.add(blockIntLocation);
    }
    public void protect(Location blockLocation){
        protect(BlockIntLocation.of(blockLocation));
    }
    public void protect(Block block){
        protect(block.getLocation());
    }

    public void unprotect(BlockIntLocation blockIntLocation){
        protectedLocation.remove(blockIntLocation);
    }
    public void unprotect(Location blockLocation){
        unprotect(BlockIntLocation.of(blockLocation));
    }
    public void unprotect(Block block){
        unprotect(block.getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e){
        if(isProtected(e.getBlock())){
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e){
        e.blockList().removeIf(this::isProtected);
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e){
        e.blockList().removeIf(this::isProtected);
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent e){
        if(e.getBlocks().stream().anyMatch(this::isProtected)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    public void onBlockPistonRetrace(BlockPistonRetractEvent e){
        if(e.getBlocks().stream().anyMatch(this::isProtected)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent e){
        if(isProtected(e.getToBlock())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent e){
        if(isProtected(e.getBlock())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    public void onEntityBreakDoor(EntityBreakDoorEvent e){
        if(isProtected(e.getBlock())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent e){
        if(isProtected(e.getBlock())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent e){
        if(isProtected(e.getBlock())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    public void onEntityBlockForm(EntityBlockFormEvent e){
        if(isProtected(e.getBlock())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent e){
        if(isProtected(e.getBlock())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent e){
        if(isProtected(e.getBlock())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e){
        if(isProtected(e.getBlockPlaced())) e.setCancelled(true);
    }
}
