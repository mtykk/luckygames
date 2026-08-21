package io.github.mtykk.luckygames;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Utility class, convenience only
 */
public class GamePlayer {
    /**
     * Retrieves the team attribute of a player
     * @param player Player
     * @return -1 for no team, an integer from [0,32] represents the team.
     */
    public static int getPlayerTeam(Player player){
        return LuckyGames.getInstance().getGameState().getPlayerTeamAttribution(player);
    }
    public static int getPlayerTeam(UUID playerUUID){
        return LuckyGames.getInstance().getGameState().getPlayerTeamAttribution(playerUUID);
    }
}
