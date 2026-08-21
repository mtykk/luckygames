package io.github.mtykk.luckygames;

import org.bukkit.Location;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.structure.Structure;
import org.bukkit.util.Vector;

public abstract class StructureLuckyEvents extends GenericLuckyEvents{
    @Override
    protected void classDefaultOnStart(Player player, Location blockLocation){}
    @Override
    protected void classDefaultOnFinish(Player player, Location blockLocation){}
}

class UnluckyStructureLuckyEvents extends StructureLuckyEvents{
    UnluckyStructureLuckyEvents(){
        registerGenericEvent((player, blockLocation) -> {
            Structure structure = LuckyGames.getInstance().getServer().getStructureManager().getStructure(PluginStructures.WAXED_OXIDIZED_WATERLOGGED_PRISON.getStructureKey());
            structure.place(blockLocation.clone().add(new Vector(-1,0,-1)),false, StructureRotation.NONE, Mirror.NONE,0,1.0f,PluginRandom.randomGenerator);
            player.teleport(blockLocation.clone().add(new Vector(0.5,0,0.5)), PlayerTeleportEvent.TeleportCause.PLUGIN);
        });
        registerGenericEvent((player, blockLocation) -> {
            Structure structure = LuckyGames.getInstance().getServer().getStructureManager().getStructure(PluginStructures.WATERLOGGED_PRISON.getStructureKey());
            structure.place(blockLocation.clone().add(new Vector(-1,0,-1)),false, StructureRotation.NONE, Mirror.NONE,0,1.0f,PluginRandom.randomGenerator);
            player.teleport(blockLocation.clone().add(new Vector(0.5,0,0.5)), PlayerTeleportEvent.TeleportCause.PLUGIN);
        });
        registerGenericEvent((player, blockLocation) -> {
            Structure structure = LuckyGames.getInstance().getServer().getStructureManager().getStructure(PluginStructures.FORGOTTEN_SHELTER.getStructureKey());
            structure.place(blockLocation.clone().add(new Vector(-2,0,-2)),true, StructureRotation.NONE, Mirror.NONE,0,1.0f,PluginRandom.randomGenerator);
            player.teleport(blockLocation.clone().add(new Vector(-0.5,0,-0.5)), PlayerTeleportEvent.TeleportCause.PLUGIN);
        });
        registerGenericEvent((player, blockLocation) -> {
            Structure structure = LuckyGames.getInstance().getServer().getStructureManager().getStructure(PluginStructures.FORGOTTEN_SHOOTING_GALLERY.getStructureKey());
            structure.place(blockLocation.clone().add(new Vector(-2,0,-2)),true, StructureRotation.NONE, Mirror.NONE,0,1.0f,PluginRandom.randomGenerator);
            player.teleport(blockLocation.clone().add(new Vector(-0.5,0,-0.5)), PlayerTeleportEvent.TeleportCause.PLUGIN);
        });
        registerGenericEvent((player, blockLocation) -> {
            Structure structure = LuckyGames.getInstance().getServer().getStructureManager().getStructure(PluginStructures.FORGOTTEN_PORTAL.getStructureKey());
            structure.place(blockLocation.clone().add(new Vector(-2,0,-2)),true, StructureRotation.NONE, Mirror.NONE,0,1.0f,PluginRandom.randomGenerator);
        });
        registerGenericEvent((player, blockLocation) -> {
            Structure structure = LuckyGames.getInstance().getServer().getStructureManager().getStructure(PluginStructures.PRISON.getStructureKey());
            structure.place(blockLocation.clone().add(new Vector(-1,0,-1)),false, StructureRotation.NONE, Mirror.NONE,0,1.0f,PluginRandom.randomGenerator);
            player.teleport(blockLocation.clone().add(new Vector(0.5,0,0.5)), PlayerTeleportEvent.TeleportCause.PLUGIN);
        });
        registerGenericEvent((player, blockLocation) -> {
            Structure structure = LuckyGames.getInstance().getServer().getStructureManager().getStructure(PluginStructures.PRIVACY_PROTECTION.getStructureKey());
            structure.place(blockLocation.clone().add(new Vector(-1,0,-1)),false, StructureRotation.NONE, Mirror.NONE,0,1.0f,PluginRandom.randomGenerator);
            player.teleport(blockLocation.clone().add(new Vector(0.5,0,0.5)), PlayerTeleportEvent.TeleportCause.PLUGIN);
        });
        registerGenericEvent((player, blockLocation) -> {
            Structure structure = LuckyGames.getInstance().getServer().getStructureManager().getStructure(PluginStructures.DOOR_TRAP.getStructureKey());
            structure.place(blockLocation.clone().add(new Vector(-1,0,-1)),false, StructureRotation.NONE, Mirror.NONE,0,1.0f,PluginRandom.randomGenerator);
            player.teleport(blockLocation.clone().add(new Vector(0.5,0,0.5)), PlayerTeleportEvent.TeleportCause.PLUGIN);
        });
        registerGenericEvent((player, blockLocation) -> {
            Structure structure = LuckyGames.getInstance().getServer().getStructureManager().getStructure(PluginStructures.PISTON_TRAP.getStructureKey());
            structure.place(blockLocation.clone().add(new Vector(-1,0,-1)),false, StructureRotation.NONE, Mirror.NONE,0,1.0f,PluginRandom.randomGenerator);
            player.teleport(blockLocation.clone().add(new Vector(0.5,1,0.5)), PlayerTeleportEvent.TeleportCause.PLUGIN);
        });
        registerGenericEvent((player, blockLocation) -> {
            Structure structure = LuckyGames.getInstance().getServer().getStructureManager().getStructure(PluginStructures.MINIATURE_END.getStructureKey());
            structure.place(blockLocation.clone().add(new Vector(-5,10,-4)),true, StructureRotation.NONE, Mirror.NONE,0,1.0f,PluginRandom.randomGenerator);
            player.teleport(blockLocation.clone().add(new Vector(0.5,13,0.5)), PlayerTeleportEvent.TeleportCause.PLUGIN);
        });
    }
}

class NormalStructureLuckyEvents extends StructureLuckyEvents{
    NormalStructureLuckyEvents(){
        registerGenericEvent((player, blockLocation) -> {
            Structure structure = LuckyGames.getInstance().getServer().getStructureManager().getStructure(PluginStructures.LODESTONE_TERRACE.getStructureKey());
            structure.place(blockLocation.clone().add(new Vector(-3,0,-3)),true, StructureRotation.NONE, Mirror.NONE,0,1.0f,PluginRandom.randomGenerator);
        });
        registerGenericEvent((player, blockLocation) -> {
            Structure structure = LuckyGames.getInstance().getServer().getStructureManager().getStructure(PluginStructures.FOOD_SHOP.getStructureKey());
            structure.place(blockLocation.clone().add(new Vector(-3,0,-2)),true, StructureRotation.NONE, Mirror.NONE,0,1.0f,PluginRandom.randomGenerator);
        });
        registerGenericEvent((player, blockLocation) -> {
            Structure structure = LuckyGames.getInstance().getServer().getStructureManager().getStructure(PluginStructures.POND.getStructureKey());
            structure.place(blockLocation.clone().add(new Vector(-3,0,-3)),true, StructureRotation.NONE, Mirror.NONE,0,1.0f,PluginRandom.randomGenerator);
        });
    }
}

class LuckyStructureLuckyEvents extends StructureLuckyEvents{
    LuckyStructureLuckyEvents(){
        registerGenericEvent((player, blockLocation) -> {
            Structure structure = LuckyGames.getInstance().getServer().getStructureManager().getStructure(PluginStructures.ENCHANTING_TABLE.getStructureKey());
            structure.place(blockLocation.clone().add(new Vector(-2,0,-2)),true, StructureRotation.NONE, Mirror.NONE,0,1.0f,PluginRandom.randomGenerator);
        });
    }
}