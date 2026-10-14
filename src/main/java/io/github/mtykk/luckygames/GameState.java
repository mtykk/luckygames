package io.github.mtykk.luckygames;

import com.jeff_media.customblockdata.CustomBlockData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Contains everything needed for the game to run
 */
public class GameState implements Listener{
    private GamePhase phase = GamePhase.WAITING;
    private GameSettings thisGameSettings;
    private final IndexedGameLocation indexedGameLocation = new IndexedGameLocation();
    private final List<PlayerRankEntry> playerRankEntries = new ArrayList<>();
    private int teamNumbers;
    private int finishedLaneCount;
    private final List<PlayerGameState> playerGameStates = new ArrayList<>();
    private final LuckyGames plugin;
    private final HashMap<UUID,Integer> playerTeamAttribution = new HashMap<>();
    private final Runnable runEpilogue;
    private final Runnable runFinish;

    GameState(LuckyGames plugin, Runnable onEpilogue, Runnable onFinish) {
        runEpilogue = onEpilogue;
        runFinish = onFinish;
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }


    /**
     * Mark a location index for a team as visited
     * @param lane The lane (same as the team number)
     * @param index The location index
     * @return true if successfully set, false if the player is not allowed to do so
     * @throws IllegalArgumentException If the lane does not exist
     */
    public boolean setPlayerVisitedLocation(int lane, int index) throws IllegalArgumentException {
        if (lane < 0 || lane >= playerGameStates.size()) {
            throw new IllegalArgumentException("No such lane");
        }
        PlayerGameState laneState = playerGameStates.get(lane);
        if (index < 0 || index >= laneState.locationVisited.length) {
            throw new IllegalArgumentException("No such location index");
        }
        if (thisGameSettings == null) {
            throw new IllegalStateException("The game has not been initialized");
        }

        if (index == 0) {
            laneState.locationVisited[0] = true;
            return true;
        }
        if (!thisGameSettings.isCheckpoint(index)) {
            laneState.locationVisited[index] = true;
            return true;
        }
        for (int i = 0; i < index; i++) {
            if (!thisGameSettings.isCheckpoint(i) && !laneState.locationVisited[i]) return false;
        }
        laneState.locationVisited[index] = true;
        return true;
    }

    /**
     * Mark a location index for a player as visited
     * @param playerUUID UUID of the player
     * @param index The index to be marked
     * @return true if successfully set, false if the player is not allowed to do so
     * @throws IllegalArgumentException If the player is not in the game
     */
    public boolean setPlayerVisitedLocation(UUID playerUUID, int index) throws IllegalArgumentException {
        return setPlayerVisitedLocation(getPlayerTeamAttribution(playerUUID), index);
    }

    public boolean isPlayerVisited(int lane, int index) throws IllegalArgumentException {
        if (lane < 0 || lane >= playerGameStates.size()) {
            throw new IllegalArgumentException("No such lane");
        }
        boolean[] visited = playerGameStates.get(lane).locationVisited;
        if (index < 0 || index >= visited.length) {
            throw new IllegalArgumentException("No such location index");
        }
        return visited[index];
    }

    public boolean isPlayerVisited(UUID playerUUID, int index) throws IllegalArgumentException {
        return isPlayerVisited(getPlayerTeamAttribution(playerUUID), index);
    }

    public void setPlayerRespawn(UUID playerUUID, int index, boolean teleport) throws IllegalArgumentException {
        if (!trySetPlayerRespawn(playerUUID, index, teleport)) {
            plugin.getLogger().warning("Teleport to respawn location " + index + " was cancelled for player " + playerUUID);
        }
    }

    /**
     * Updates a player's saved respawn point and optionally moves them there immediately.
     *
     * @return false when the requested teleport was cancelled; the respawn point is still stored
     */
    public boolean trySetPlayerRespawn(UUID playerUUID, int index, boolean teleport) throws IllegalArgumentException {
        int playerTeam = getPlayerTeamAttribution(playerUUID);
        if (playerTeam < 0 || playerTeam >= playerGameStates.size()) {
            throw new IllegalArgumentException("This player is not in the game");
        }
        PlayerGameState laneState = playerGameStates.get(playerTeam);
        if (index < 0 || index >= laneState.locationVisited.length) {
            throw new IllegalArgumentException("No such respawn location index");
        }

        Location targetLocation = indexedGameLocation.getLocation(playerTeam, index).clone();
        targetLocation.setYaw(-90);
        laneState.respawnLocationIndex = index;
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null && player.isConnected()) {
            player.setRespawnLocation(targetLocation, true);
            if (teleport) return player.teleport(targetLocation);
        } else {
            plugin.getLogger().fine("Stored respawn index " + index + " for offline player " + playerUUID);
        }
        return true;
    }

    public int getPlayerRespawnLocationIndex(UUID playerUUID) throws IllegalArgumentException {
        int playerTeam = getPlayerTeamAttribution(playerUUID);
        if (playerTeam < 0 || playerTeam >= playerGameStates.size()) {
            throw new IllegalArgumentException("This player is not in the game");
        }
        return playerGameStates.get(playerTeam).respawnLocationIndex;
    }

    public IndexedGameLocation getIndexedGameLocation() {
        return indexedGameLocation;
    }

    public int getTeamNumbers() {
        return teamNumbers;
    }

    public void setTeamNumbers(int teamNumbers) {
        if (teamNumbers < 0 || teamNumbers > GamePlayer.MAX_TEAM_COUNT) {
            throw new IllegalArgumentException("Team count must be between 0 and " + GamePlayer.MAX_TEAM_COUNT);
        }
        this.teamNumbers = teamNumbers;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public void clearPlayerTeamAttribution(){
        playerTeamAttribution.clear();
    }

    public void setPlayerTeamAttribution(Player player, int team) {
        if (player == null) throw new IllegalArgumentException("Player is required");
        if (team < 0 || team >= teamNumbers) {
            throw new IllegalArgumentException("Team index must be between 0 and " + (teamNumbers - 1));
        }
        playerTeamAttribution.put(player.getUniqueId(), team);
    }

    public int getPlayerTeamAttribution(Player player){
        if(!playerTeamAttribution.containsKey(player.getUniqueId())) return -1;
        return playerTeamAttribution.get(player.getUniqueId());
    }
    public int getPlayerTeamAttribution(UUID playerUUID){
        if(!playerTeamAttribution.containsKey(playerUUID)) return -1;
        return playerTeamAttribution.get(playerUUID);
    }

    public HashMap<UUID,Integer> getAllPlayerTeamAttribution(){
        return playerTeamAttribution;
    }

    public List<PlayerRankEntry> getPlayerRankEntries() {
        return playerRankEntries;
    }

    public void addPlayerRankEntry(PlayerRankEntry entry){
        playerRankEntries.add(entry);
    }

    /**
     * Stores a copy of the current game settings
     * @param gameSettings The game settings for the game
     */
    public void setThisGameSettings(GameSettings gameSettings){
        thisGameSettings = new GameSettings(gameSettings);
    }

    public GameSettings getThisGameSettings() {
        return thisGameSettings;
    }

    public void initPlayerGameStates() {
        if (thisGameSettings == null) throw new IllegalStateException("Game settings have not been initialized");
        int indexCount = Math.addExact(thisGameSettings.getLastIndex(), 1);
        playerGameStates.clear();
        for (int i = 0; i < teamNumbers; i++) {
            playerGameStates.add(new PlayerGameState(indexCount));
        }
        finishedLaneCount = 0;
        playerRankEntries.clear();
    }

    public boolean areAllLanesFinished() {
        return teamNumbers > 0 && finishedLaneCount >= teamNumbers;
    }

    /**
     * Remove a disconnected participant from the active roster so a quit cannot keep a race or final match open forever.
     * A runner who already reached the finish line remains eligible for the final match; the epilogue will record them
     * as absent if they have not rejoined by then.
     */
    public void handlePlayerQuit(Player player) {
        UUID playerUUID = player.getUniqueId();
        Integer team = playerTeamAttribution.get(playerUUID);
        if (team == null) return;

        if (phase == GamePhase.PREPARING) {
            if (playerTeamAttribution.remove(playerUUID) != null) {
                playerRankEntries.add(new PlayerRankEntry(player.getName(), team));
                finishedLaneCount++;
            }
            return;
        }

        if (phase == GamePhase.RUNNING) {
            if (team < 0 || team >= playerGameStates.size()) {
                playerTeamAttribution.remove(playerUUID);
                return;
            }
            int lastIndex = thisGameSettings.getLastIndex();
            if (playerGameStates.get(team).locationVisited[lastIndex]) return;
            if (playerTeamAttribution.remove(playerUUID) != null) {
                playerRankEntries.add(new PlayerRankEntry(player.getName(), team));
                finishedLaneCount++;
                if (areAllLanesFinished()) runEpilogue.run();
            }
            return;
        }

        if (phase == GamePhase.EPILOGUE && playerTeamAttribution.remove(playerUUID) != null) {
            playerRankEntries.add(new PlayerRankEntry(player.getName(), team));
            if (playerTeamAttribution.size() <= 1) scheduleFinalMatchFinishCheck();
        }
    }

    private void scheduleFinalMatchFinishCheck() {
        // Let other death events from the same tick update the roster before declaring a winner.
        Bukkit.getScheduler().runTask(plugin, task -> {
            if (phase == GamePhase.EPILOGUE && playerTeamAttribution.size() <= 1) runFinish.run();
        });
    }

    // Listen for player checkpoint events.
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (phase != GamePhase.RUNNING || e.getAction() != Action.PHYSICAL) return;
        Block block = e.getClickedBlock();
        if (block == null || block.getType() != Material.LIGHT_WEIGHTED_PRESSURE_PLATE) return;

        PersistentDataContainer pdc = new CustomBlockData(block, plugin);
        Integer checkpointAttribution = pdc.get(GameIndexDataKey.MARKER_LANE_ATTRIBUTION, PersistentDataType.INTEGER);
        Integer checkpointIndex = pdc.get(GameIndexDataKey.MARKER_INDEX, PersistentDataType.INTEGER);
        if (checkpointAttribution == null || checkpointIndex == null) return;

        Player player = e.getPlayer();
        if (checkpointAttribution != getPlayerTeamAttribution(player.getUniqueId())) {
            player.sendActionBar(Component.translatable("luckygames.message.not_your_checkpoint"));
            return;
        }
        try {
            if (isPlayerVisited(checkpointAttribution, checkpointIndex)) return;
            if (!setPlayerVisitedLocation(player.getUniqueId(), checkpointIndex)) {
                player.sendActionBar(Component.translatable("luckygames.message.could_not_save_progress_lucky_block_not_cleared"));
                return;
            }
            setPlayerRespawn(player.getUniqueId(), checkpointIndex, false);
        } catch (IllegalArgumentException invalidCheckpoint) {
            plugin.getLogger().warning("Ignoring invalid checkpoint metadata at " + block.getLocation());
            return;
        }

        player.sendActionBar(Component.translatable("luckygames.message.progress_saved"));
        ReusableParticleBuilders.GREEN_EFFECT_SPREAD_PARTICLE_BUILDER.count(32).location(player.getLocation()).spawn();
        Bukkit.getScheduler().runTaskLater(plugin, task -> block.setType(Material.AIR), 1);
        if (thisGameSettings.isLastCheckpoint(checkpointIndex)) {
            player.showTitle(Title.title(Component.translatable("luckygames.message.finish_line_reached"), Component.empty()));
            finishedLaneCount++;
            Bukkit.getServer().sendMessage(Component.translatable(
                    "luckygames.message.reached_the_finish_line", Component.text(player.getName())));
            if (areAllLanesFinished()) runEpilogue.run();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (phase != GamePhase.RUNNING || thisGameSettings == null) return;
        if (!plugin.getLuckyBlockInstance().isLuckyBlock(e.getBlock())) return;

        PersistentDataContainer pdc = new CustomBlockData(e.getBlock(), plugin);
        Integer markerIndex = pdc.get(GameIndexDataKey.MARKER_INDEX, PersistentDataType.INTEGER);
        Integer markerLaneAttribution = pdc.get(GameIndexDataKey.MARKER_LANE_ATTRIBUTION, PersistentDataType.INTEGER);
        if (markerIndex == null || markerLaneAttribution == null) return;
        if (markerIndex < 0 || markerIndex > thisGameSettings.getLastIndex()) {
            plugin.getLogger().warning("Ignoring invalid game marker metadata at " + e.getBlock().getLocation());
            return;
        }
        if (thisGameSettings.isCheckpoint(markerIndex)) return;

        try {
            setPlayerVisitedLocation(markerLaneAttribution, markerIndex);
        } catch (IllegalArgumentException invalidMarker) {
            plugin.getLogger().warning("Ignoring invalid game marker metadata at " + e.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (phase != GamePhase.EPILOGUE) return;
        Player player = e.getPlayer();
        Integer team = playerTeamAttribution.remove(player.getUniqueId());
        if (team == null) return;

        playerRankEntries.add(new PlayerRankEntry(player.getName(), team));
        Bukkit.getServer().sendMessage(Component.translatable(
                "luckygames.message.player_final_death", Component.text(player.getName())));
        if (playerTeamAttribution.size() <= 1) scheduleFinalMatchFinishCheck();
    }
}
