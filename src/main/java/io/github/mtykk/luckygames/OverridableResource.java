package io.github.mtykk.luckygames;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class OverridableResource {
    @NotNull
    private final JavaPlugin plugin;

    OverridableResource(@NotNull JavaPlugin _plugin){
        this.plugin = _plugin;
    }

    public InputStream getResource(String path){
        File file = new File(plugin.getDataFolder(),path);
        if(file.exists()){
            try{
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                plugin.getLogger().fine("Could not open resource override " + path + "; using the bundled resource");
            }
        }
        return plugin.getResource(path);
    }
}
