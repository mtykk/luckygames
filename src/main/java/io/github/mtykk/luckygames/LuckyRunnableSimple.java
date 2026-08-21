package io.github.mtykk.luckygames;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * An interface that represents an event to be run when a lucky block is opened
 * Unlike LuckyRunnable, this does not contain a weight, meaning that implementations of this instance should contain only one event
 */
@FunctionalInterface
public interface LuckyRunnableSimple {
    void run(Player player, Location blockLocation);
}