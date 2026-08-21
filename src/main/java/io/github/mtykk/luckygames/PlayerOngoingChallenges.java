package io.github.mtykk.luckygames;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
                PlayerChallengeDesc playerChallengeDesc = i.getValue();
                int ticksLeft = playerChallengeDesc.reduceTicksLeft();
                if(ticksLeft <= 0){ //The challenge reaches a timeout
                    playerChallengeDesc.runTimeoutEvent();
                }
                if(player != null && player.isConnected()){
                    playerChallengeDesc.runPeriodicChecker();
                }
                playerChallengeDesc.updateBossBar();
            }
        },100,0);
    }

    public Map<UUID, PlayerChallengeDesc> getOngoingChallenges() {
        return ongoingChallenges;
    }
    public boolean hasOngoingChallenge(UUID uuid){
        return ongoingChallenges.containsKey(uuid);
    }
    public boolean setOngoingChallenge(UUID uuid, PlayerChallengeDesc playerChallengeDesc){
        if (this.hasOngoingChallenge(uuid)) return false;
        ongoingChallenges.put(uuid,playerChallengeDesc);
        return true;
    }
    public boolean removeOngoingChallenge(UUID uuid){
        if (!this.hasOngoingChallenge(uuid)) return false;
        ongoingChallenges.get(uuid).gracefullyClean();
        ongoingChallenges.remove(uuid);
        return true;
    }

    public int count(){
        return ongoingChallenges.size();
    }
}
