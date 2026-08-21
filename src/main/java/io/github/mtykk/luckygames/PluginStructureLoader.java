package io.github.mtykk.luckygames;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;

import java.io.IOException;
import java.io.InputStream;

class PluginStructureLoader {
    static void loadStructures(JavaPlugin plugin,OverridableResource overridableResource){
        StructureManager structureManager = plugin.getServer().getStructureManager();
        for(PluginStructures structureKey : PluginStructures.values()){
            try(InputStream structureResource = overridableResource.getResource("structures/"+structureKey.getStructureKey().getNamespace()+"/"+structureKey.getStructureKey().getKey()+".nbt")){
                if (structureResource == null) throw new IOException();
                Structure structure = structureManager.loadStructure(structureResource);
                structureManager.registerStructure(structureKey.getStructureKey(),structure);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
