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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Contains everything needed for the game to run
 */
public class GameState implements Listener{
    private GamePhase phase;
    private GameSettings thisGameSettings;
    private final IndexedGameLocation indexedGameLocation = new IndexedGameLocation();
    private final List<PlayerRankEntry> playerRankEntries = new ArrayList<>();
    private int teamNumbers;
    private int finishedLaneCount;
    private final List<PlayerGameState> playerGameStates = new ArrayList<>();
    private final JavaPlugin plugin;
    private final HashMap<UUID,Integer> playerTeamAttribution = new HashMap<>();
    private final Runnable runEpilogue;
    private final Runnable runFinish;

    GameState(JavaPlugin plugin,Runnable onEpilogue,Runnable onFinish){
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
    public boolean setPlayerVisitedLocation(int lane,int index) throws IllegalArgumentException{
        if(lane < 0 || lane >= teamNumbers) throw new IllegalArgumentException("No such lane");
        PlayerGameState gameState = playerGameStates.get(lane);
        if(index == 0){
            gameState.locationVisited[0] = true;
            return true;
        }else if(!thisGameSettings.isCheckpoint(index)){
            gameState.locationVisited[index] = true;
            return true;
        }else{
            for(int i = 0;i < index;i++){
                if((!thisGameSettings.isCheckpoint(i)) && !gameState.locationVisited[i]) return false;
            }
            gameState.locationVisited[index] = true;
            return true;
        }
    }

    /**
     * Mark a location index for a player as visited
     * @param playerUUID UUID of the player
     * @param index The index to be marked
     * @return true if successfully set, false if the player is not allowed to do so
     * @throws IllegalArgumentException If the player is not in the game
     */
    public boolean setPlayerVisitedLocation(UUID playerUUID,int index) throws IllegalArgumentException{
        return setPlayerVisitedLocation(GamePlayer.getPlayerTeam(Bukkit.getPlayer(playerUUID)),index);
    }

    public boolean isPlayerVisited(int lane,int index) throws IllegalArgumentException{
        if(lane < 0 || lane >= teamNumbers) throw new IllegalArgumentException("No such lane");
        PlayerGameState gameState = playerGameStates.get(lane);
        return gameState.locationVisited[index];
    }

    public boolean isPlayerVisited(UUID playerUUID,int index) throws IllegalArgumentException{
        return isPlayerVisited(GamePlayer.getPlayerTeam(Bukkit.getPlayer(playerUUID)),index);
    }

    public void setPlayerRespawn(UUID playerUUID,int index,boolean teleport) throws IllegalArgumentException{
        int playerTeam = GamePlayer.getPlayerTeam(Bukkit.getPlayer(playerUUID));
        if(playerTeam < 0) throw new IllegalArgumentException("This player is not in the game");
        PlayerGameState gameState = playerGameStates.get(playerTeam);
        gameState.respawnLocationIndex = index;
        Player player = Bukkit.getPlayer(playerUUID);
        Location targetLocation = indexedGameLocation.getLocation(playerTeam,index);
        if(player != null){
            player.setRespawnLocation(targetLocation,true);
            if(teleport) player.teleport(targetLocation);
        }
        else plugin.getLogger().warning("Failed to set the location with an index of "+index+" as UUID("+playerUUID+")'s default game spawn");
    }

    public IndexedGameLocation getIndexedGameLocation() {
        return indexedGameLocation;
    }

    public int getTeamNumbers() {
        return teamNumbers;
    }

    public void setTeamNumbers(int teamNumbers) {
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

    public void setPlayerTeamAttribution(Player player, int team){
        playerTeamAttribution.put(player.getUniqueId(),team);
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

    public void initPlayerGameStates(){
        int indexCount = thisGameSettings.getLastIndex()+1;
        playerGameStates.clear();
        for(int i = 0;i < teamNumbers;i++){
            playerGameStates.add(new PlayerGameState(indexCount));
        }
        finishedLaneCount = 0;
        playerRankEntries.clear();
    }

    //Listen for player checkpoint events
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.PHYSICAL)) {
            Block block = e.getClickedBlock();
            if (block.getType() != Material.LIGHT_WEIGHTED_PRESSURE_PLATE) return;
            PersistentDataContainer pdc = new CustomBlockData(block, plugin);
            if (pdc.has(GameIndexDataKey.MARKER_LANE_ATTRIBUTION) && pdc.has(GameIndexDataKey.MARKER_INDEX)) {
                Player player = e.getPlayer();
                int checkpointAttribution = pdc.get(GameIndexDataKey.MARKER_LANE_ATTRIBUTION, PersistentDataType.INTEGER);
                int checkpointIndex = pdc.get(GameIndexDataKey.MARKER_INDEX, PersistentDataType.INTEGER);
                if (checkpointAttribution != GamePlayer.getPlayerTeam(player)) {
                    player.sendActionBar(Component.translatable("luckygames.message.not_your_checkpoint"));
                    return;
                }
                if(isPlayerVisited(checkpointAttribution,checkpointIndex)) return;
                if (setPlayerVisitedLocation(player.getUniqueId(), checkpointIndex)) {
                    setPlayerRespawn(player.getUniqueId(), checkpointIndex,false);
                    player.sendActionBar(Component.translatable("luckygames.message.progress_saved"));
                    ReusableParticleBuilders.GREEN_EFFECT_SPREAD_PARTICLE_BUILDER.count(32).location(player.getLocation()).spawn();
                    Bukkit.getScheduler().runTaskLater(plugin,task->{
                        block.setType(Material.AIR);
                    },1);
                    if(thisGameSettings.isLastCheckpoint(checkpointIndex)){
                        player.showTitle(Title.title(Component.translatable("luckygames.message.finish_line_reached"),Component.empty()));
                        finishedLaneCount++;
                        Bukkit.getServer().sendMessage(Component.translatable("luckygames.message.reached_the_finish_line",Component.text(player.getName())));
                        if(finishedLaneCount >= teamNumbers) runEpilogue.run();
                    }
                } else {
                    player.sendActionBar(Component.translatable("luckygames.message.could_not_save_progress_lucky_block_not_cleared"));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e){
        PersistentDataContainer pdc = new CustomBlockData(e.getBlock(),plugin);
        if(pdc.has(GameIndexDataKey.MARKER_INDEX) && pdc.has(GameIndexDataKey.MARKER_LANE_ATTRIBUTION)){
            int markerIndex = pdc.get(GameIndexDataKey.MARKER_INDEX,PersistentDataType.INTEGER);
            int markerLaneAttribution = pdc.get(GameIndexDataKey.MARKER_LANE_ATTRIBUTION,PersistentDataType.INTEGER);
            setPlayerVisitedLocation(markerLaneAttribution,markerIndex);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
        if(phase == GamePhase.EPILOGUE){
            Player player = e.getPlayer();
            UUID playerUUID = player.getUniqueId();
            if(playerTeamAttribution.containsKey(playerUUID)){
                int team = playerTeamAttribution.get(playerUUID);
                playerTeamAttribution.remove(playerUUID);
                playerRankEntries.add(new PlayerRankEntry(player.getName(),team));
                Bukkit.getServer().sendMessage(Component.translatable("luckygames.message.player_final_death",Component.text(player.getName())));
                if(playerTeamAttribution.size() <= 1){
                    runFinish.run();
                }
            }
        }
    }
}
