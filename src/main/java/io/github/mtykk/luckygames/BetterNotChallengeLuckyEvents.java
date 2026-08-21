package io.github.mtykk.luckygames;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import io.papermc.paper.event.inventory.ItemCraftedEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.UUID;
import java.util.function.Consumer;

public class BetterNotChallengeLuckyEvents extends GenericLuckyEvents{
    @Override
    protected void classDefaultOnStart(Player player,Location blockLocation){
        ReusableParticleBuilders.GREEN_EFFECT_SPREAD_PARTICLE_BUILDER.count(32).location(player.getLocation()).spawn();
    }
    @Override
    protected void classDefaultOnFinish(Player player,Location blockLocation){}
    private final static boolean hideGoldToChallenger = true;
    private PlayerOngoingChallenges playerOngoingChallengesInstance;

    protected final Consumer<UUID> onFail = uuid -> {
        Player player = Bukkit.getPlayer(uuid);
        if(player.isConnected()){
            World world = player.getWorld();
            ReusableParticleBuilders.DAMAGE_PARTICLE_BUILDER.location(player.getLocation()).spawn();
            world.spawn(player.getLocation(), LightningStrike.class, CreatureSpawnEvent.SpawnReason.CUSTOM, lightningStrike -> {
                lightningStrike.setFlashCount(32);
            });
        }
        playerOngoingChallengesInstance.getOngoingChallenges().get(uuid).broadcastChallengeEnd(ReusableChallengeOutcomeTypes.FAIL);
        playerOngoingChallengesInstance.removeOngoingChallenge(uuid);
    };

    protected final Consumer<UUID> onTimeout = uuid -> {
        Player player = Bukkit.getPlayer(uuid);
        if(player.isConnected()){
            LuckyGames.getInstance().getLuckyBlockInstance().getLuckyEvents().getLuckyEventsRegistry(2).get(0).run(player,player.getLocation().toBlockLocation());
        }
        playerOngoingChallengesInstance.getOngoingChallenges().get(uuid).broadcastChallengeEnd(ReusableChallengeOutcomeTypes.DONE);
        playerOngoingChallengesInstance.removeOngoingChallenge(uuid);
    };

    BetterNotChallengeLuckyEvents(PlayerOngoingChallenges playerOngoingChallenges){
        playerOngoingChallengesInstance = playerOngoingChallenges;
        registerGenericEvent((player, blockLocation) -> {
            UUID playerUUID = player.getUniqueId();
            if(playerOngoingChallenges.hasOngoingChallenge(playerUUID)) return;
            PlayerChallengeDesc challengeDesc = new PlayerChallengeDesc(playerUUID,Component.translatable("luckyblock.message.better_not_modal"),
                    Component.translatable("luckyblock.message.challenge_content.sneak_for_3_seconds"),
                    2400,
                    new Consumer<UUID>() {
                        int counter = 0;
                        @Override
                        public void accept(UUID uuid) {
                            Player playerChecking = Bukkit.getPlayer(uuid);
                            try {
                                if (playerChecking.isSneaking()) counter++;
                                else counter = 0;
                            } catch (RuntimeException e) {}
                            if(counter > 60) onFail.accept(uuid);
                        }
                    },
                    null,
                    onTimeout,
                    onFail,
                    null
                    );
            playerOngoingChallengesInstance.setOngoingChallenge(playerUUID,challengeDesc);
            challengeDesc.broadcastChallengeBegin(Component.translatable("luckyblock.message.2minutes"),hideGoldToChallenger);
            challengeDesc.enableBossBar();
        });

        registerGenericEvent((player, blockLocation) -> {
            UUID playerUUID = player.getUniqueId();
            if(playerOngoingChallenges.hasOngoingChallenge(playerUUID)) return;
            PlayerChallengeDesc challengeDesc = new PlayerChallengeDesc(playerUUID, Component.translatable("luckyblock.message.better_not_modal"),
                    Component.translatable("luckyblock.message.challenge_content.jump_twice"),
                    900,
                    null,
                    new Listener() {
                        UUID targetPlayerUUID = playerUUID;
                        int playerJumpCount = 0;
                        @EventHandler
                        public void onPlayerJump(PlayerJumpEvent e){
                            if(e.getPlayer().getUniqueId() == targetPlayerUUID) {
                                playerJumpCount++;
                                if(playerJumpCount >= 2) onFail.accept(targetPlayerUUID);
                            }
                        }
                    },
                    onTimeout,
                    onFail,
                    null
            );
            playerOngoingChallengesInstance.setOngoingChallenge(playerUUID,challengeDesc);
            challengeDesc.broadcastChallengeBegin(Component.translatable("luckyblock.message.45seconds"),hideGoldToChallenger);
            challengeDesc.enableBossBar();
        });

        registerGenericEvent((player, blockLocation) -> {
            UUID playerUUID = player.getUniqueId();
            if(playerOngoingChallenges.hasOngoingChallenge(playerUUID)) return;
            PlayerChallengeDesc challengeDesc = new PlayerChallengeDesc(playerUUID, Component.translatable("luckyblock.message.better_not_modal"),
                    Component.translatable("luckyblock.message.challenge_content.stargaze_for_5_seconds"),
                    2400,
                    new Consumer<UUID>() {
                        int counter = 0;
                        @Override
                        public void accept(UUID uuid) {
                            Player playerChecking = Bukkit.getPlayer(uuid);
                            try{
                                if(playerChecking.getPitch() < -50f) counter++;
                                else counter = 0;
                            }catch(RuntimeException e){}
                            if(counter>100) onFail.accept(uuid);
                        }
                    },
                    null,
                    onTimeout,
                    onFail,
                    null
            );
            playerOngoingChallengesInstance.setOngoingChallenge(playerUUID,challengeDesc);
            challengeDesc.broadcastChallengeBegin(Component.translatable("luckyblock.message.2minutes"),hideGoldToChallenger);
            challengeDesc.enableBossBar();
        });

        registerGenericEvent((player, blockLocation) -> {
            UUID playerUUID = player.getUniqueId();
            if(playerOngoingChallenges.hasOngoingChallenge(playerUUID)) return;
            PlayerChallengeDesc challengeDesc = new PlayerChallengeDesc(playerUUID, Component.translatable("luckyblock.message.better_not_modal"),
                    Component.translatable("luckyblock.message.challenge_content.respawn"),
                    3600,
                    null,
                    new Listener() {
                        UUID targetPlayerUUID = playerUUID;
                        @EventHandler
                        public void onPlayerRespawn(PlayerRespawnEvent e){
                            if(e.getPlayer().getUniqueId() == targetPlayerUUID) onFail.accept(targetPlayerUUID);
                        }
                    },
                    onTimeout,
                    onFail,
                    null
            );
            playerOngoingChallengesInstance.setOngoingChallenge(playerUUID,challengeDesc);
            challengeDesc.broadcastChallengeBegin(Component.translatable("luckyblock.message.3minutes"),hideGoldToChallenger);
            challengeDesc.enableBossBar();
        });

        registerGenericEvent((player, blockLocation) -> {
            UUID playerUUID = player.getUniqueId();
            if(playerOngoingChallenges.hasOngoingChallenge(playerUUID)) return;
            PlayerChallengeDesc challengeDesc = new PlayerChallengeDesc(playerUUID, Component.translatable("luckyblock.message.better_not_modal"),
                    Component.translatable("luckyblock.message.challenge_content.die"),
                    3600,
                    null,
                    new Listener() {
                        UUID targetPlayerUUID = playerUUID;
                        @EventHandler
                        public void onPlayerDeath(PlayerDeathEvent e){
                            if(e.getPlayer().getUniqueId() == targetPlayerUUID) onFail.accept(targetPlayerUUID);
                        }
                    },
                    onTimeout,
                    onFail,
                    null
            );
            playerOngoingChallengesInstance.setOngoingChallenge(playerUUID,challengeDesc);
            challengeDesc.broadcastChallengeBegin(Component.translatable("luckyblock.message.3minutes"),hideGoldToChallenger);
            challengeDesc.enableBossBar();
        });

        registerGenericEvent((player, blockLocation) -> {
            UUID playerUUID = player.getUniqueId();
            if(playerOngoingChallenges.hasOngoingChallenge(playerUUID)) return;
            PlayerChallengeDesc challengeDesc = new PlayerChallengeDesc(playerUUID, Component.translatable("luckyblock.message.better_not_modal"),
                    Component.translatable("luckyblock.message.challenge_content.kill_player"),
                    2400,
                    null,
                    new Listener() {
                        UUID targetPlayerUUID = playerUUID;
                        @EventHandler
                        public void onPlayerDeath(PlayerDeathEvent e){
                            try {
                                if (e.getDamageSource().getCausingEntity().getType() == EntityType.PLAYER && e.getDamageSource().getCausingEntity().getUniqueId() == targetPlayerUUID)
                                    onFail.accept(targetPlayerUUID);
                            }catch(RuntimeException exception){}
                        }
                    },
                    onTimeout,
                    onFail,
                    null
            );
            playerOngoingChallengesInstance.setOngoingChallenge(playerUUID,challengeDesc);
            challengeDesc.broadcastChallengeBegin(Component.translatable("luckyblock.message.2minutes"),hideGoldToChallenger);
            challengeDesc.enableBossBar();
        });

        registerGenericEvent((player, blockLocation) -> {
            UUID playerUUID = player.getUniqueId();
            if(playerOngoingChallenges.hasOngoingChallenge(playerUUID)) return;
            PlayerChallengeDesc challengeDesc = new PlayerChallengeDesc(playerUUID, Component.translatable("luckyblock.message.better_not_modal"),
                    Component.translatable("luckyblock.message.challenge_content.get_above_y_100"),
                    2400,
                    new Consumer<UUID>() {
                        @Override
                        public void accept(UUID uuid) {
                            Player playerChecking = Bukkit.getPlayer(uuid);
                            try{
                                if(playerChecking.getY() > 100) onFail.accept(uuid);
                            }catch(RuntimeException e){}
                        }
                    },
                    null,
                    onTimeout,
                    onFail,
                    null
            );
            playerOngoingChallengesInstance.setOngoingChallenge(playerUUID,challengeDesc);
            challengeDesc.broadcastChallengeBegin(Component.translatable("luckyblock.message.2minutes"),hideGoldToChallenger);
            challengeDesc.enableBossBar();
        });

        registerGenericEvent((player, blockLocation) -> {
            UUID playerUUID = player.getUniqueId();
            if(playerOngoingChallenges.hasOngoingChallenge(playerUUID)) return;
            PlayerChallengeDesc challengeDesc = new PlayerChallengeDesc(playerUUID, Component.translatable("luckyblock.message.better_not_modal"),
                    Component.translatable("luckyblock.message.challenge_content.get_below_y_40"),
                    2400,
                    new Consumer<UUID>() {
                        @Override
                        public void accept(UUID uuid) {
                            Player playerChecking = Bukkit.getPlayer(uuid);
                            try{
                                if(playerChecking.getY() < 40) onFail.accept(uuid);
                            }catch(RuntimeException e){}
                        }
                    },
                    null,
                    onTimeout,
                    onFail,
                    null
            );
            playerOngoingChallengesInstance.setOngoingChallenge(playerUUID,challengeDesc);
            challengeDesc.broadcastChallengeBegin(Component.translatable("luckyblock.message.2minutes"),hideGoldToChallenger);
            challengeDesc.enableBossBar();
        });

        registerGenericEvent((player, blockLocation) -> {
            UUID playerUUID = player.getUniqueId();
            if(playerOngoingChallenges.hasOngoingChallenge(playerUUID)) return;
            PlayerChallengeDesc challengeDesc = new PlayerChallengeDesc(playerUUID, Component.translatable("luckyblock.message.better_not_modal"),
                    Component.translatable("luckyblock.message.challenge_content.open_a_furnace"),
                    2400,
                    null,
                    new Listener() {
                        UUID targetPlayerUUID = playerUUID;
                        @EventHandler
                        public void onPlayerInteract(PlayerInteractEvent e){
                            try {
                                if (e.getPlayer().getUniqueId() == targetPlayerUUID && e.getClickedBlock().getType() == Material.FURNACE)
                                    onFail.accept(playerUUID);
                            }catch (RuntimeException exception){}
                        }
                    },
                    onTimeout,
                    onFail,
                    null
            );
            playerOngoingChallengesInstance.setOngoingChallenge(playerUUID,challengeDesc);
            challengeDesc.broadcastChallengeBegin(Component.translatable("luckyblock.message.2minutes"),hideGoldToChallenger);
            challengeDesc.enableBossBar();
        });

        registerGenericEvent((player, blockLocation) -> {
            UUID playerUUID = player.getUniqueId();
            if(playerOngoingChallenges.hasOngoingChallenge(playerUUID)) return;
            PlayerChallengeDesc challengeDesc = new PlayerChallengeDesc(playerUUID, Component.translatable("luckyblock.message.better_not_modal"),
                    Component.translatable("luckyblock.message.challenge_content.stay_still_for_5_seconds"),
                    2400,
                    new Consumer<UUID>() {
                        int counter = 0;
                        Location playerLocation = null;
                        @Override
                        public void accept(UUID uuid) {
                            Player playerChecking = Bukkit.getPlayer(uuid);
                            try{
                                if(playerLocation == null || playerChecking.getLocation().distance(playerLocation) > 0.2){
                                    playerLocation = playerChecking.getLocation();
                                    counter = 0;
                                }else{
                                    counter++;
                                }
                            }catch(RuntimeException e){}
                            if(counter > 100) onFail.accept(uuid);
                        }
                    },
                    null,
                    onTimeout,
                    onFail,
                    null
            );
            playerOngoingChallengesInstance.setOngoingChallenge(playerUUID,challengeDesc);
            challengeDesc.broadcastChallengeBegin(Component.translatable("luckyblock.message.2minutes"),hideGoldToChallenger);
            challengeDesc.enableBossBar();
        });

        registerGenericEvent((player, blockLocation) -> {
            UUID playerUUID = player.getUniqueId();
            if(playerOngoingChallenges.hasOngoingChallenge(playerUUID)) return;
            PlayerChallengeDesc challengeDesc = new PlayerChallengeDesc(playerUUID, Component.translatable("luckyblock.message.better_not_modal"),
                    Component.translatable("luckyblock.message.challenge_content.craft_anything"),
                    2400,
                    null,
                    new Listener() {
                        UUID targetPlayerUUID = playerUUID;
                        @EventHandler
                        public void onItemCraft(ItemCraftedEvent e){
                            if(e.getPlayer().getUniqueId() == targetPlayerUUID) onFail.accept(playerUUID);
                        }
                    },
                    onTimeout,
                    onFail,
                    null
            );
            playerOngoingChallengesInstance.setOngoingChallenge(playerUUID,challengeDesc);
            challengeDesc.broadcastChallengeBegin(Component.translatable("luckyblock.message.2minutes"),hideGoldToChallenger);
            challengeDesc.enableBossBar();
        });

        registerGenericEvent((player, blockLocation) -> {
            UUID playerUUID = player.getUniqueId();
            if(playerOngoingChallenges.hasOngoingChallenge(playerUUID)) return;
            PlayerChallengeDesc challengeDesc = new PlayerChallengeDesc(playerUUID, Component.translatable("luckyblock.message.better_not_modal"),
                    Component.translatable("luckyblock.message.challenge_content.drop_item"),
                    2400,
                    null,
                    new Listener() {
                        UUID targetPlayerUUID = playerUUID;
                        @EventHandler
                        public void onPlayerDropItem(PlayerDropItemEvent e){
                            if(e.getPlayer().getUniqueId() == targetPlayerUUID) onFail.accept(playerUUID);
                        }
                    },
                    onTimeout,
                    onFail,
                    null
            );
            playerOngoingChallengesInstance.setOngoingChallenge(playerUUID,challengeDesc);
            challengeDesc.broadcastChallengeBegin(Component.translatable("luckyblock.message.2minutes"),hideGoldToChallenger);
            challengeDesc.enableBossBar();
        });

        registerGenericEvent((player, blockLocation) -> {
            UUID playerUUID = player.getUniqueId();
            if(playerOngoingChallenges.hasOngoingChallenge(playerUUID)) return;
            PlayerChallengeDesc challengeDesc = new PlayerChallengeDesc(playerUUID, Component.translatable("luckyblock.message.better_not_modal"),
                    Component.translatable("luckyblock.message.challenge_content.use_fishing_rod"),
                    2400,
                    null,
                    new Listener() {
                        UUID targetPlayerUUID = playerUUID;
                        @EventHandler
                        public void onPlayerFish(PlayerFishEvent e){
                            if(e.getPlayer().getUniqueId() == targetPlayerUUID) onFail.accept(playerUUID);
                        }
                    },
                    onTimeout,
                    onFail,
                    null
            );
            playerOngoingChallengesInstance.setOngoingChallenge(playerUUID,challengeDesc);
            challengeDesc.broadcastChallengeBegin(Component.translatable("luckyblock.message.2minutes"),hideGoldToChallenger);
            challengeDesc.enableBossBar();
        });
    }
}
