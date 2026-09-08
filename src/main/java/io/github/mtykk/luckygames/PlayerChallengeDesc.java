package io.github.mtykk.luckygames;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBarViewer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;


import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PlayerChallengeDesc {
    private final UUID playerUUID; //UUID of the player taking the challenge
    private final Component modal;
    private final Component gold;
    private int ticksLeft;
    private final int ticksTotal;
    @Nullable
    private final Consumer<UUID> periodicChecker;
    @Nullable
    private final Listener listenerChecker;
    @Nullable
    private final Consumer<UUID> timeoutEvent;
    @Nullable
    private final Consumer<UUID> onAccomplish;
    @Nullable
    private final Consumer<UUID> onFail;
    @Nullable
    private BossBar bossBar = null;

    PlayerChallengeDesc(UUID _playerUUID,Component _modal,Component _gold,int _ticksLeft,@Nullable Consumer<UUID> _periodicChecker,@Nullable Listener _listenerChecker,@Nullable Consumer<UUID> _timeoutEvent,@Nullable Consumer<UUID> _onAccomplish,@Nullable Consumer<UUID> _onFail) throws IllegalArgumentException{
        if(_listenerChecker == null && _periodicChecker == null) throw new IllegalArgumentException("No checker specified");
        this.playerUUID = _playerUUID;
        this.modal = _modal;
        this.gold = _gold;
        this.ticksLeft = _ticksLeft;
        this.periodicChecker = _periodicChecker;
        this.listenerChecker = _listenerChecker;
        this.timeoutEvent = _timeoutEvent;
        this.onAccomplish = _onAccomplish;
        this.onFail = _onFail;

        this.ticksTotal= this.ticksLeft;

        if(this.listenerChecker != null){
            Bukkit.getPluginManager().registerEvents(this.listenerChecker,LuckyGames.getInstance());
        }
    }

    public Component getModal() {
        return modal;
    }
    public Component getGold() {
        return gold;
    }

    public int getTicksLeft() {
        return ticksLeft;
    }
    public void runTimeoutEvent(){
        if (timeoutEvent != null) {timeoutEvent.accept(playerUUID);}
    }
    public void runPeriodicChecker(){
        if (periodicChecker != null) {periodicChecker.accept(playerUUID);}
    }
    public int reduceTicksLeft(){
        if(ticksLeft <= 0) return -1;
        ticksLeft--;
        return ticksLeft;
    }

    private Component generateRemainingTime(){
        return Component.translatable("luckyblock.message.time_remaining",Argument.component("time_remaining",Component.text(ticksLeft/20,NamedTextColor.AQUA).append(Component.text(" s",NamedTextColor.AQUA))));
    }
    public void enableBossBar() throws IllegalStateException{
        Player player = Bukkit.getPlayer(playerUUID);
        if(player == null) throw new IllegalStateException("The player does not exist");
        bossBar = BossBar.bossBar(generateRemainingTime(),1.0f, BossBar.Color.YELLOW,BossBar.Overlay.PROGRESS);
        player.showBossBar(bossBar);
    }
    public void updateBossBar(){
        if(bossBar == null) return;
        for(BossBarViewer viewer: bossBar.viewers()){
            if(viewer instanceof Player player){
                if(!player.isConnected()){
                    Player target = Bukkit.getPlayer(playerUUID);
                    if(target != null){
                        target.showBossBar(bossBar);
                        bossBar.removeViewer(player);
                    }
                }
            }
        }
        bossBar.name(generateRemainingTime());
        bossBar.progress(((float)ticksLeft/ticksTotal));
    }
    public void deleteBossBar() throws IllegalStateException{
        if(bossBar == null) return;
        Player player = Bukkit.getPlayer(playerUUID);
        if(player == null) throw new IllegalStateException("The player does not exist");
        player.hideBossBar(bossBar);
        bossBar = null;
    }

    /**
     * Gracefully clean this challenge entry
     * This method does:
     * 1. Unregister the event listener (if any)
     * 2. Clear the boss bar (if any)
     */
    public void gracefullyClean(){
        deleteBossBar();
        if(listenerChecker == null) return;
        HandlerList.unregisterAll(listenerChecker);
    }
    public void broadcastChallengeBegin(Component duration,boolean hideGoldToChallenger){
        Player player = Bukkit.getPlayer(playerUUID);
        if(player == null) return;
        Component fullChallengeDescription = Component.text().append(modal).append(Component.text(" ")).append(gold).append(Component.text(" ")).append(Component.translatable("luckyblock.message.challenge_duration", Argument.component("challenge_duration",duration))).build();
        Component mysteryChallengeDescription = Component.text().append(modal).append(Component.text(" ")).append(Component.text("not this time", NamedTextColor.GOLD, TextDecoration.OBFUSCATED)).append(Component.text(" ")).append(Component.translatable("luckyblock.message.challenge_duration", Argument.component("challenge_duration", duration))).build();

        player.showTitle(Title.title(Component.translatable("luckyblock.message.trigger_challenge_self"), hideGoldToChallenger ? mysteryChallengeDescription : fullChallengeDescription));
        player.sendMessage(Component.text().append(Component.translatable("luckyblock.message.trigger_challenge_self")).append(Component.text(" ")).append(hideGoldToChallenger ? mysteryChallengeDescription : fullChallengeDescription));

        Audience otherPlayers = Audience.audience(
                Bukkit.getServer().getOnlinePlayers().stream().filter(p -> !p.equals(player)).collect(Collectors.toList())
        );
        otherPlayers.sendMessage(Component.translatable("luckyblock.message.trigger_challenge_others",Argument.component("player",Component.text(player.getName())),Argument.component("challenge_description",fullChallengeDescription)));
    }

    public void broadcastChallengeEnd(Component challengeOutcome){
        Player player = Bukkit.getPlayer(playerUUID);
        Component fullChallengeDescription = Component.text().append(modal).append(Component.text(" ")).append(gold).build();

        if(player != null) player.showTitle(Title.title(Component.translatable("luckyblock.message.challenge_finish_self",Argument.component("outcome",challengeOutcome)),fullChallengeDescription));

        Bukkit.getServer().sendMessage(Component.translatable("luckyblock.message.challenge_finish_others",Argument.component("outcome",challengeOutcome),Argument.component("player",Component.text(Objects.requireNonNullElse(Bukkit.getOfflinePlayer(playerUUID).getName(),"E"))),Argument.component("challenge_description",fullChallengeDescription)));
    }

    public Component getChallenge(boolean hideGold){
        return hideGold ? Component.text().append(modal).append(Component.text(" ")).append(Component.text("not this time", NamedTextColor.GOLD, TextDecoration.OBFUSCATED)).append(Component.text(" ")).append(Component.translatable("luckyblock.message.challenge_duration", Argument.component("challenge_duration",Component.text(ticksLeft/20 +"s",NamedTextColor.AQUA)))).build() :
                Component.text().append(modal).append(Component.text(" ")).append(gold).append(Component.text(" ")).append(Component.translatable("luckyblock.message.challenge_duration", Argument.component("challenge_duration",Component.text(ticksLeft/20 +"s",NamedTextColor.AQUA)))).build();
    }

    public Component getBasicChallenge(boolean hideGold){
        return hideGold ? Component.text().append(modal).append(Component.text(" ")).append(Component.text("not this time", NamedTextColor.GOLD, TextDecoration.OBFUSCATED)).build() :
                Component.text().append(modal).append(Component.text(" ")).append(gold).build();
    }
}
