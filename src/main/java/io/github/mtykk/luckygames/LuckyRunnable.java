package io.github.mtykk.luckygames;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface LuckyRunnable extends WeightedObject {
    void run(Player player, Location blockLocation);
}