package io.github.mtykk.luckygames;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.jeff_media.customblockdata.CustomBlockData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.*;

public class LuckyBlock implements Listener {
    private static final String TEXTURE_NORMAL = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWM0OTcwZWE5MWFiMDZlY2U1OWQ0NWZjZTc2MDRkMjU1NDMxZjJlMDNhNzM3YjIyNjA4MmM0Y2NlMWFjYTFjNCJ9fX0=";
    private static final String TEXTURE_UNLUCKY = TEXTURE_NORMAL;
    private static final String TEXTURE_LUCKY = TEXTURE_NORMAL;
    private static ResolvableProfile TEXTURE_PROFILE_NORMAL, TEXTURE_PROFILE_UNLUCKY, TEXTURE_PROFILE_LUCKY;
    private static final NamespacedKey LUCKY_BLOCK_TYPE_KEY = new NamespacedKey("luckygames","lucky_block_type");
    private static final NamespacedKey LUCKY_BLOCK_ATTRIBUTION_KEY = new NamespacedKey("luckygames","lucky_block_attribution"); // Integer, representing which team(lane) the block belongs to, -1 means public.
    private static final NamespacedKey LUCKY_BLOCK_DISPLAY_KEY = new NamespacedKey("luckygames", "lucky_block_display");

    public static final String[] luckyEventTypesString = {"unlucky","normal","lucky","challenge"};
    public static final String[] luckyBlockTypesString = {"unlucky-block", "normal-block", "lucky-block", "challenge-block"};
    private final LuckyEvents luckyEvents;

    LuckyBlock(){
        TEXTURE_PROFILE_NORMAL = buildProfile(TEXTURE_NORMAL);
        TEXTURE_PROFILE_LUCKY = buildProfile(TEXTURE_LUCKY);
        TEXTURE_PROFILE_UNLUCKY = buildProfile(TEXTURE_UNLUCKY);

        LuckyGames.getInstance().getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(luckyBlockBuiltCommand());
        });
        Bukkit.getPluginManager().registerEvents(this,LuckyGames.getInstance());

        luckyEvents = new LuckyEvents();
        LuckyGames.getInstance().getLogger().info("Lucky Events registered, total registrations: "+ (luckyEvents.getUnluckyEventsTotalWeight()+luckyEvents.getNormalEventsTotalWeight()+luckyEvents.getLuckyEventsTotalWeight()+luckyEvents.getChallengeEventsTotalWeight()));
        LuckyGames.getInstance().getLogger().info("Breakdown: ");
        LuckyGames.getInstance().getLogger().info("    Unlucky: "+luckyEvents.getUnluckyEventsTotalWeight());
        LuckyGames.getInstance().getLogger().info("    Normal: "+luckyEvents.getNormalEventsTotalWeight());
        LuckyGames.getInstance().getLogger().info("    Lucky: "+luckyEvents.getLuckyEventsTotalWeight());
        LuckyGames.getInstance().getLogger().info("    Challenge: "+luckyEvents.getChallengeEventsTotalWeight());
    }


    private static ResolvableProfile buildProfile(String textureB64){
        ResolvableProfile.Builder builder = ResolvableProfile.resolvableProfile();
        builder.name("textures");
        ProfileProperty textureProperty = new ProfileProperty("textures",textureB64);
        builder.addProperty(textureProperty);
        return builder.build();
    }

    private LiteralCommandNode<CommandSourceStack> luckyBlockBuiltCommand(){
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("luckyblock")
                .then(Commands.literal("place")
                        .then(Commands.argument("position", ArgumentTypes.blockPosition()).then(Commands.argument("lucky_type", new LuckyBlockTypeArgument()).then(Commands.argument("attribution",IntegerArgumentType.integer(-1, GamePlayer.MAX_TEAM_NUMBER))
                                .requires(sender -> sender.getSender().hasPermission("luckygames.luckyblock.place"))
                                .executes(ctx -> {
                                    Entity executor = ctx.getSource().getExecutor();
                                    final BlockPositionResolver blockPositionResolver = ctx.getArgument("position", BlockPositionResolver.class);
                                    final BlockPosition blockPosition = blockPositionResolver.resolve(ctx.getSource());
                                    World world = executor == null
                                            ? ctx.getSource().getLocation().getWorld()
                                            : executor.getWorld();
                                    if (world == null) {
                                        ctx.getSource().getSender().sendMessage(Component.text("Could not determine a world for the lucky block"));
                                        return 0;
                                    }

                                    final LuckyBlockTypes luckyBlockType = ctx.getArgument("lucky_type", LuckyBlockTypes.class);
                                    final int attribution = IntegerArgumentType.getInteger(ctx,"attribution");
                                    placeLuckyBlock(new Location(world,blockPosition.x(),blockPosition.y(),blockPosition.z()),luckyBlockType,attribution);
                                    return Command.SINGLE_SUCCESS;
                                })))
                        )
                );

        return command.build();
    }


    public void placeLuckyBlock(Location location, LuckyBlockTypes type, int attribution) {
        if (location == null || location.getWorld() == null) {
            throw new IllegalArgumentException("Lucky block location must have a world");
        }
        if (type == null) throw new IllegalArgumentException("Lucky block type is required");
        if (attribution < -1 || attribution > GamePlayer.MAX_TEAM_NUMBER) {
            throw new IllegalArgumentException("Invalid lucky block attribution: " + attribution);
        }
        Chunk chunk = location.getChunk();
        if (!chunk.isLoaded()){
            chunk.load();
        }

        World world = location.getWorld();

        Block block = location.getBlock();
        switch (type) {
            case UNLUCKY:
                block.setType(Material.BLACK_STAINED_GLASS,false);
                break;
            case LUCKY:
                block.setType(Material.LIME_STAINED_GLASS,false);
                break;
            case CHALLENGE:
                block.setType(Material.LIGHT_BLUE_STAINED_GLASS,false);
                break;
            default:
                block.setType(Material.YELLOW_STAINED_GLASS,false);
        }

        removeLuckyBlockDisplays(location);
        ItemDisplay blockCoreDisplay = (ItemDisplay)world.spawnEntity(location.toCenterLocation(),EntityType.ITEM_DISPLAY);
        blockCoreDisplay.getPersistentDataContainer().set(LUCKY_BLOCK_DISPLAY_KEY, PersistentDataType.BYTE, (byte) 1);
        ItemStack coreItemStack = null;
        switch (type){
            case UNLUCKY:
                coreItemStack = new ItemStack(Material.PLAYER_HEAD,1);
                coreItemStack.setData(DataComponentTypes.PROFILE,TEXTURE_PROFILE_UNLUCKY);
                break;
            case LUCKY:
                coreItemStack = new ItemStack(Material.PLAYER_HEAD,1);
                coreItemStack.setData(DataComponentTypes.PROFILE,TEXTURE_PROFILE_LUCKY);
                break;
            case CHALLENGE:
                coreItemStack = new ItemStack(Material.EMERALD,3);
                break;
            default:
                coreItemStack = new ItemStack(Material.PLAYER_HEAD,1);
                coreItemStack.setData(DataComponentTypes.PROFILE,TEXTURE_PROFILE_NORMAL);
        }
        blockCoreDisplay.setItemStack(coreItemStack);
        if (type == LuckyBlockTypes.CHALLENGE){
            blockCoreDisplay.setItemDisplayTransform(ItemDisplayTransform.GROUND);
        }else{
            blockCoreDisplay.setItemDisplayTransform(ItemDisplayTransform.HEAD);
        }
        if (type == LuckyBlockTypes.CHALLENGE){
            blockCoreDisplay.setTransformation(new Transformation(
                    new Vector3f(0,-0.1f,0),
                    new AxisAngle4f(0,0,0,1),
                    new Vector3f(1.2f,1.2f,1.2f),
                    new AxisAngle4f(0,0,0,1)
            ));
        }else{
            blockCoreDisplay.setTransformation(new Transformation(
                    new Vector3f(0,0.2f,0),
                    new AxisAngle4f(0,0,0,1),
                    new Vector3f(0.8f,0.8f,0.8f),
                    new AxisAngle4f(0,0,0,1)
            ));
        }

        PersistentDataContainer blockTags = new CustomBlockData(block, LuckyGames.getInstance());
        blockTags.set(LUCKY_BLOCK_TYPE_KEY,PersistentDataType.INTEGER,type.ordinal());
        blockTags.set(LUCKY_BLOCK_ATTRIBUTION_KEY,PersistentDataType.INTEGER,attribution);
    }

    public void placeLuckyBlock(Location location, LuckyBlockTypes type){
        placeLuckyBlock(location, type, -1);
    }

    private void removeLuckyBlockDisplays(Location location) {
        Collection<ItemDisplay> itemDisplays = location.getWorld().getNearbyEntitiesByType(
                ItemDisplay.class, location.getBlock().getLocation().add(0.5, 0.5, 0.5), 0.5);
        for (ItemDisplay itemDisplay : itemDisplays) {
            if (itemDisplay.getPersistentDataContainer().has(LUCKY_BLOCK_DISPLAY_KEY, PersistentDataType.BYTE)) {
                itemDisplay.remove();
            }
        }
    }

    public boolean isLuckyBlock(Block block){
        Material blockType = block.getType();
        if(!(blockType.equals(Material.BLACK_STAINED_GLASS) ||
                blockType.equals(Material.YELLOW_STAINED_GLASS) ||
                blockType.equals(Material.LIME_STAINED_GLASS)||
                blockType.equals(Material.LIGHT_BLUE_STAINED_GLASS))){
            return false;
        }

        PersistentDataContainer pdc = new CustomBlockData(block, LuckyGames.getInstance());
        return pdc.has(LUCKY_BLOCK_TYPE_KEY, PersistentDataType.INTEGER)
                && pdc.has(LUCKY_BLOCK_ATTRIBUTION_KEY, PersistentDataType.INTEGER);
    }

    public LuckyEvents getLuckyEvents() {
        return luckyEvents;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!isLuckyBlock(block)) return;

        Player player = event.getPlayer();
        PersistentDataContainer pdc = new CustomBlockData(block, LuckyGames.getInstance());
        Integer typeIndex = pdc.get(LUCKY_BLOCK_TYPE_KEY, PersistentDataType.INTEGER);
        Integer attribution = pdc.get(LUCKY_BLOCK_ATTRIBUTION_KEY, PersistentDataType.INTEGER);
        if (typeIndex == null || typeIndex < 0 || typeIndex >= LuckyBlockTypes.values().length || attribution == null) {
            event.setCancelled(true);
            return;
        }

        boolean hasUseAll = player.hasPermission("luckygames.luckyblock.useall");
        if (!hasUseAll && !player.hasPermission("luckygames.luckyblock.use")) {
            event.setCancelled(true);
            player.sendActionBar(Component.translatable("luckyblock.info.no_permission"));
            return;
        }

        int playerTeam = GamePlayer.getPlayerTeam(player);
        if (!hasUseAll && attribution != -1 && playerTeam != attribution) {
            player.sendActionBar(Component.translatable("luckyblock.info.not_owner"));
            event.setCancelled(true);
            return;
        }

        event.setDropItems(false);
        if (hasUseAll && attribution != -1 && playerTeam != attribution) {
            player.sendMessage(Component.translatable("luckyblock.warning.privilege_used"));
        }

        LuckyBlockTypes type = LuckyBlockTypes.values()[typeIndex];
        Location blockLocation = block.getLocation().clone();
        Bukkit.getScheduler().runTaskLater(LuckyGames.getInstance(), task -> {
            // A later listener may still cancel a HIGHEST-priority break. Only consume the
            // display/protection and run the event once the block has actually been removed.
            if (isLuckyBlock(block)) return;
            removeLuckyBlockDisplays(blockLocation);
            LuckyGames.getInstance().getBlockProtector().unprotect(blockLocation);
            if (!player.isConnected()) return;
            LuckyRunnable luckyEvent = luckyEvents.getRandomLuckyRunnable(type);
            luckyEvent.run(player, blockLocation);
        }, 0);
    }
}
