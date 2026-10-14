package io.github.mtykk.luckygames;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import io.papermc.paper.event.inventory.ItemCraftedEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
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
        if (!playerOngoingChallengesInstance.hasOngoingChallenge(uuid)) return;
        try {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isConnected()) {
                World world = player.getWorld();
                ReusableParticleBuilders.DAMAGE_PARTICLE_BUILDER.location(player.getLocation()).spawn();
                world.spawn(player.getLocation(), LightningStrike.class, CreatureSpawnEvent.SpawnReason.CUSTOM, lightningStrike -> {
                    lightningStrike.setFlashCount(32);
                });
            }
        } finally {
            finishChallenge(uuid, ReusableChallengeOutcomeTypes.FAIL);
        }
    };

    protected final Consumer<UUID> onTimeout = uuid -> {
        if (!playerOngoingChallengesInstance.hasOngoingChallenge(uuid)) return;
        try {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isConnected()) {
                LuckyGames.getInstance().getLuckyBlockInstance().getLuckyEvents().getLuckyEventsRegistry(2).get(0)
                        .run(player, player.getLocation().toBlockLocation());
            }
        } finally {
            finishChallenge(uuid, ReusableChallengeOutcomeTypes.DONE);
        }
    };

    private void finishChallenge(UUID uuid, Component outcome) {
        PlayerChallengeDesc challenge = playerOngoingChallengesInstance.getOngoingChallenges().get(uuid);
        if (challenge == null) return;
        try {
            challenge.broadcastChallengeEnd(outcome);
        } finally {
            playerOngoingChallengesInstance.removeOngoingChallenge(uuid);
        }
    }

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
                            if (playerChecking == null || !playerChecking.isConnected()) return;
                            if (playerChecking.isSneaking()) counter++;
                            else counter = 0;
                            if (counter >= 60) onFail.accept(uuid);
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
                            if(e.getPlayer().getUniqueId().equals(targetPlayerUUID)) {
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
                            if (playerChecking == null || !playerChecking.isConnected()) return;
                            if (playerChecking.getPitch() < -50f) counter++;
                            else counter = 0;
                            if (counter >= 100) onFail.accept(uuid);
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
                            if(e.getPlayer().getUniqueId().equals(targetPlayerUUID)) onFail.accept(targetPlayerUUID);
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
                            if(e.getPlayer().getUniqueId().equals(targetPlayerUUID)) onFail.accept(targetPlayerUUID);
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
                        public void onPlayerDeath(PlayerDeathEvent e) {
                            Entity causingEntity = e.getDamageSource().getCausingEntity();
                            if (causingEntity != null && causingEntity.getType() == EntityType.PLAYER
                                    && causingEntity.getUniqueId().equals(targetPlayerUUID)
                                    && !e.getPlayer().getUniqueId().equals(targetPlayerUUID)) {
                                onFail.accept(targetPlayerUUID);
                            }
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
                            if (playerChecking != null && playerChecking.isConnected() && playerChecking.getY() > 100) {
                                onFail.accept(uuid);
                            }
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
                            if (playerChecking != null && playerChecking.isConnected() && playerChecking.getY() < 40) {
                                onFail.accept(uuid);
                            }
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
                        @EventHandler(ignoreCancelled = true)
                        public void onInventoryOpen(InventoryOpenEvent e) {
                            InventoryType inventoryType = e.getInventory().getType();
                            if (e.getPlayer().getUniqueId().equals(targetPlayerUUID)
                                    && (inventoryType == InventoryType.FURNACE
                                    || inventoryType == InventoryType.BLAST_FURNACE
                                    || inventoryType == InventoryType.SMOKER)) {
                                onFail.accept(playerUUID);
                            }
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
                            if (playerChecking == null || !playerChecking.isConnected()) return;
                            Location currentLocation = playerChecking.getLocation();
                            if (playerLocation == null
                                    || !currentLocation.getWorld().equals(playerLocation.getWorld())
                                    || currentLocation.distanceSquared(playerLocation) > 0.04) {
                                playerLocation = currentLocation;
                                counter = 0;
                            } else {
                                counter++;
                            }
                            if (counter > 100) onFail.accept(uuid);
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
                        @EventHandler(ignoreCancelled = true)
                        public void onItemCraft(ItemCraftedEvent e){
                            if(e.getPlayer().getUniqueId().equals(targetPlayerUUID)) onFail.accept(playerUUID);
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
                        @EventHandler(ignoreCancelled = true)
                        public void onPlayerDropItem(PlayerDropItemEvent e){
                            if(e.getPlayer().getUniqueId().equals(targetPlayerUUID)) onFail.accept(playerUUID);
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
                        @EventHandler(ignoreCancelled = true)
                        public void onPlayerFish(PlayerFishEvent e){
                            if(e.getPlayer().getUniqueId().equals(targetPlayerUUID)) onFail.accept(playerUUID);
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
