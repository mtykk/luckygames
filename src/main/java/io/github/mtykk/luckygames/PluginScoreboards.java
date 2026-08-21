package io.github.mtykk.luckygames;

import fr.mrmicky.fastboard.adventure.FastBoard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PluginScoreboards implements Listener {
    private final Map<UUID,FastBoard> boards = new HashMap<>();
    private final JavaPlugin plugin;
    private final GameState gameState;
    private final PlayerOngoingChallenges playerOngoingChallenges;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yy/MM/dd");

    PluginScoreboards(JavaPlugin plugin,GameState gameState,PlayerOngoingChallenges playerOngoingChallenges){
        this.plugin = plugin;
        this.gameState = gameState;
        this.playerOngoingChallenges = playerOngoingChallenges;

        Bukkit.getPluginManager().registerEvents(this,plugin);

        Bukkit.getScheduler().runTaskTimer(plugin,task->{
            for(Map.Entry<UUID,FastBoard> board: boards.entrySet()){
                updateBoard(board);
                Player player = Bukkit.getPlayer(board.getKey());
                if(player != null){
                    int playerTeam = GamePlayer.getPlayerTeam(player);
                    if(playerTeam >= 0) player.playerListName(Component.text("[",NamedTextColor.YELLOW).append(Component.text(playerTeam,NamedTextColor.AQUA)).append(Component.text("] ",NamedTextColor.YELLOW)).append(Component.text(player.getName(),NamedTextColor.AQUA)));
                    else player.playerListName(Component.text(player.getName(),NamedTextColor.GRAY));
                }
            }
        },0,20);
    }

    private void updateBoard(Map.Entry<UUID,FastBoard> boardEntry){
        FastBoard board = boardEntry.getValue();
        UUID playerUUID = boardEntry.getKey();
        Date now = new Date();
        String gamePhaseTranslationKey = switch (gameState.getPhase()) {
            case WAITING -> "luckygames.message.waiting";
            case PREPARING -> "luckygames.message.preparing";
            case RUNNING -> "luckygames.message.running";
            case EPILOGUE -> "luckygames.message.epilogue";
            case FINISHED -> "luckygames.message.finished";
        };
        board.updateLines(
                Component.text(dateFormat.format(now),NamedTextColor.GRAY, TextDecoration.BOLD),
                Component.empty(),
                Component.translatable("luckygames.message.in_progress").append(Component.translatable(gamePhaseTranslationKey)),
                Component.empty(),
                Component.translatable("luckygames.message.players_left").append(Component.text(gameState.getAllPlayerTeamAttribution().size(),NamedTextColor.AQUA)),
                Component.translatable("luckygames.message.active_challenges").append(Component.text(playerOngoingChallenges.count(),NamedTextColor.AQUA)),
                GamePlayer.getPlayerTeam(playerUUID) >= 0 ? Component.translatable("luckygames.message.your_team").append(Component.text(GamePlayer.getPlayerTeam(playerUUID),NamedTextColor.AQUA)) : Component.empty()
        );
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        FastBoard board = new FastBoard(player);
        board.updateTitle(Component.text("LuckyGames", NamedTextColor.GOLD, TextDecoration.BOLD));
        boards.put(player.getUniqueId(),board);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        FastBoard board = boards.remove(player.getUniqueId());
        if(board != null){
            board.delete();
        }
    }
}
