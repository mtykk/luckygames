package io.github.mtykk.luckygames;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Note: Deletion of challenge entries and challenge ending broadcasts should be done in finalization methods(onTimeout, onFail, onAccomplish, etc.)
 * onTimeout, onFail, onAccomplish could be null, checker implementations are not confined to calling the stored methods
 */
public class PlayerOngoingChallenges {
    private final Map<UUID,PlayerChallengeDesc> ongoingChallenges = new ConcurrentHashMap<>();

    PlayerOngoingChallenges(){
        Bukkit.getServer().getScheduler().runTaskTimer(LuckyGames.getInstance(),task->{
            if (ongoingChallenges.isEmpty()) return;
            for(Entry<UUID,PlayerChallengeDesc> i:ongoingChallenges.entrySet()){
                UUID playerUUID = i.getKey();
                Player player = Bukkit.getPlayer(playerUUID);
                if (player == null || !player.isConnected()) continue;
                PlayerChallengeDesc playerChallengeDesc = i.getValue();
                int ticksLeft = playerChallengeDesc.reduceTicksLeft();
                if (ticksLeft <= 0) { // The challenge reaches a timeout.
                    playerChallengeDesc.runTimeoutEvent();
                    continue;
                }
                playerChallengeDesc.runPeriodicChecker();
            }
        }, 1, 1);

        Bukkit.getServer().getScheduler().runTaskTimer(LuckyGames.getInstance(),task->{
            if (ongoingChallenges.isEmpty()) return;
            for(Entry<UUID,PlayerChallengeDesc> i:ongoingChallenges.entrySet()) {
                PlayerChallengeDesc playerChallengeDesc = i.getValue();
                playerChallengeDesc.updateBossBar();
            }
        }, 1, 20);
    }

    public Map<UUID, PlayerChallengeDesc> getOngoingChallenges() {
        return ongoingChallenges;
    }
    public boolean hasOngoingChallenge(UUID uuid){
        return ongoingChallenges.containsKey(uuid);
    }
    public boolean setOngoingChallenge(UUID uuid, PlayerChallengeDesc playerChallengeDesc) {
        return ongoingChallenges.putIfAbsent(uuid, playerChallengeDesc) == null;
    }

    public boolean removeOngoingChallenge(UUID uuid) {
        PlayerChallengeDesc removed = ongoingChallenges.remove(uuid);
        if (removed == null) return false;
        removed.gracefullyClean();
        return true;
    }

    public void clear() {
        for (UUID uuid : List.copyOf(ongoingChallenges.keySet())) {
            removeOngoingChallenge(uuid);
        }
    }

    public int count(){
        return ongoingChallenges.size();
    }
}
