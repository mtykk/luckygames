package io.github.mtykk.luckygames;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.*;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * Instances of this class is not meant to be registered directly to the lucky events registry, extend it and add custom items insteam
 */
public abstract class LootLuckyEvents implements LuckyRunnable{
    private final List<ItemLoot> possibleItemsLootList = new ArrayList<>();

    abstract void lootEffect(Player player, Location blockLocation);

    public int getWeight(){
        return possibleItemsLootList.size();
    }

    public void run(Player player, Location blockLocation){
        ItemLoot itemLoot = RandomPicker.randomPick(possibleItemsLootList);
        ItemStack itemStack = itemLoot.getAmountRandomizedItemStack();
        blockLocation.getWorld().dropItem(blockLocation, itemStack);
        lootEffect(player, blockLocation);
    }

    /**
     * Register a stack of materials
     * @param material Material
     */
    protected void registerSimpleItemLoot(Material material){
        registerItemLoot(new ItemLoot(new ItemStack(material), material.getMaxStackSize(), material.getMaxStackSize()));
    }

    /**
     * Register a set amount of materials
     * @param material Material
     * @param amount Amount
     */
    protected void registerSimpleItemLoot(Material material, int amount){
        registerItemLoot(new ItemLoot(new ItemStack(material), amount, amount));
    }

    /**
     * Register a ranging amount of materials
     * @param material Material
     * @param minAmount Minimum amount
     * @param maxAmount Maximum amount
     */
    protected void registerSimpleItemLoot(Material material, int minAmount, int maxAmount){
        registerItemLoot(new ItemLoot(new ItemStack(material), minAmount, maxAmount));
    }

    protected void registerSimplePotionLoot(Material potionType, PotionEffectType potionEffectType, int duration, int amplifier, boolean ambient,boolean particles,boolean icon,int minAmount, int maxAmount){
        ItemStack potion = new ItemStack(potionType);
        PotionEffect effect = new PotionEffect(potionEffectType,duration,amplifier,ambient,particles,icon);
        PotionContents contents = PotionContents.potionContents().addCustomEffect(effect).build();
        potion.setData(DataComponentTypes.POTION_CONTENTS,contents);
        registerItemLoot(new ItemLoot(potion,minAmount,maxAmount));
    }

    protected void registerItemLoot(ItemLoot itemLoot){
        possibleItemsLootList.add(itemLoot);
    }
}

class NormalLootLuckyEvents extends LootLuckyEvents{
    @Override
    void lootEffect(Player player, Location blockLocation){
        ReusableParticleBuilders.GREEN_EFFECT_SPREAD_PARTICLE_BUILDER.count(64).location(blockLocation).spawn();
    }

    NormalLootLuckyEvents(){
        //Building blocks
        registerSimpleItemLoot(Material.WHITE_WOOL);
        registerSimpleItemLoot(Material.BLACK_WOOL);
        registerSimpleItemLoot(Material.PINK_WOOL);
        registerSimpleItemLoot(Material.GREEN_WOOL);
        registerSimpleItemLoot(Material.YELLOW_WOOL);

        registerSimpleItemLoot(Material.OAK_PLANKS);
        registerSimpleItemLoot(Material.OAK_LOG,16);
        registerSimpleItemLoot(Material.ACACIA_PLANKS);
        registerSimpleItemLoot(Material.ACACIA_LOG,16);
        registerSimpleItemLoot(Material.DARK_OAK_PLANKS);
        registerSimpleItemLoot(Material.DARK_OAK_LOG,16);
        registerSimpleItemLoot(Material.SPRUCE_PLANKS);
        registerSimpleItemLoot(Material.SPRUCE_LOG,16);
        registerSimpleItemLoot(Material.OAK_SAPLING,8);

        registerSimpleItemLoot(Material.END_STONE,4,16);
        registerSimpleItemLoot(Material.COBBLESTONE,32,48);

        registerSimpleItemLoot(Material.ICE,4,8);
        registerSimpleItemLoot(Material.BLUE_ICE,4,8);
        registerSimpleItemLoot(Material.PACKED_ICE,4,8);

        registerSimpleItemLoot(Material.DIRT,16);
        registerSimpleItemLoot(Material.SAND,16);
        registerSimpleItemLoot(Material.GRASS_BLOCK,16);
        registerSimpleItemLoot(Material.POPPY,2);
        registerSimpleItemLoot(Material.GLASS,32);
        registerSimpleItemLoot(Material.CRAFTING_TABLE,4);
        registerSimpleItemLoot(Material.CRAFTER,4);

        registerSimpleItemLoot(Material.RAIL,32);
        registerSimpleItemLoot(Material.ACTIVATOR_RAIL,1,8);
        registerSimpleItemLoot(Material.POWERED_RAIL,1,8);

        registerSimpleItemLoot(Material.SCAFFOLDING,16,32);

        //Equipments
        registerSimpleItemLoot(Material.WOODEN_SPEAR);
        registerSimpleItemLoot(Material.SHIELD);
        registerSimpleItemLoot(Material.BUCKET,1);
        registerSimpleItemLoot(Material.AXOLOTL_BUCKET,1);
        registerSimpleItemLoot(Material.SALMON_BUCKET,1);
        registerSimpleItemLoot(Material.TADPOLE_BUCKET,1);
        registerSimpleItemLoot(Material.COD_BUCKET,1);
        registerSimpleItemLoot(Material.MILK_BUCKET,1);
        registerSimpleItemLoot(Material.PUFFERFISH_BUCKET,1);
        registerSimpleItemLoot(Material.TROPICAL_FISH_BUCKET,1);
        registerSimpleItemLoot(Material.WATER_BUCKET,1);
        registerSimpleItemLoot(Material.LAVA_BUCKET,1);
        registerSimpleItemLoot(Material.POWDER_SNOW_BUCKET,1);
        registerSimpleItemLoot(Material.FISHING_ROD,1);
        {
            ItemStack enchanted = new ItemStack(Material.FISHING_ROD);
            enchanted.addUnsafeEnchantment(Enchantment.LURE,3);
            enchanted.addUnsafeEnchantment(Enchantment.LUCK_OF_THE_SEA,3);
            enchanted.addUnsafeEnchantment(Enchantment.UNBREAKING,3);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.GOLDEN_APPLE);
            enchanted.addUnsafeEnchantment(Enchantment.UNBREAKING,3);
            enchanted.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("Not what you want?"))));
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.GOLDEN_APPLE);
            enchanted.addUnsafeEnchantment(Enchantment.INFINITY,1);
            enchanted.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("Not this enchantment?"))));
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        registerSimpleItemLoot(Material.BOW);
        registerSimpleItemLoot(Material.CROSSBOW);
        registerSimpleItemLoot(Material.ARROW,48,64);
        registerSimpleItemLoot(Material.SPECTRAL_ARROW,48,64);
        registerSimpleItemLoot(Material.FIREWORK_ROCKET,48,64);
        registerSimpleItemLoot(Material.SHEARS);
        registerSimpleItemLoot(Material.FLINT_AND_STEEL,1);
        registerSimpleItemLoot(Material.FIRE_CHARGE,1,3);
        registerSimpleItemLoot(Material.BUNDLE,1);
        registerSimpleItemLoot(Material.TNT,1,4);
        registerSimpleItemLoot(Material.TNT_MINECART,1);
        registerSimpleItemLoot(Material.MINECART,1);
        registerSimpleItemLoot(Material.WIND_CHARGE,8,16);
        registerSimpleItemLoot(Material.SNOWBALL,8,16);
        registerSimpleItemLoot(Material.ENDER_PEARL,1,3);

        registerSimpleItemLoot(Material.COPPER_INGOT,16,48);
        registerSimpleItemLoot(Material.COPPER_HELMET);
        registerSimpleItemLoot(Material.COPPER_CHESTPLATE);
        registerSimpleItemLoot(Material.COPPER_LEGGINGS);
        registerSimpleItemLoot(Material.COPPER_BOOTS);
        registerSimpleItemLoot(Material.COPPER_SPEAR);
        registerSimpleItemLoot(Material.COPPER_AXE);
        registerSimpleItemLoot(Material.COPPER_PICKAXE);
        registerSimpleItemLoot(Material.COPPER_HOE);
        registerSimpleItemLoot(Material.COPPER_SWORD);
        registerSimpleItemLoot(Material.COPPER_SHOVEL);
        registerSimpleItemLoot(Material.WAXED_OXIDIZED_COPPER_GOLEM_STATUE,1);
        registerSimpleItemLoot(Material.WAXED_OXIDIZED_CUT_COPPER_STAIRS,1,8);

        registerSimpleItemLoot(Material.IRON_INGOT,8,24);
        registerSimpleItemLoot(Material.IRON_HELMET);
        registerSimpleItemLoot(Material.IRON_CHESTPLATE);
        registerSimpleItemLoot(Material.IRON_LEGGINGS);
        registerSimpleItemLoot(Material.IRON_BOOTS);
        registerSimpleItemLoot(Material.IRON_SPEAR);
        registerSimpleItemLoot(Material.IRON_AXE);
        registerSimpleItemLoot(Material.IRON_PICKAXE);
        registerSimpleItemLoot(Material.IRON_HOE);
        registerSimpleItemLoot(Material.IRON_SWORD);
        registerSimpleItemLoot(Material.IRON_SHOVEL);

        registerSimpleItemLoot(Material.GOLD_INGOT,7,18);
        registerSimpleItemLoot(Material.GOLDEN_HELMET);
        registerSimpleItemLoot(Material.GOLDEN_CHESTPLATE);
        registerSimpleItemLoot(Material.GOLDEN_LEGGINGS);
        registerSimpleItemLoot(Material.GOLDEN_BOOTS);
        registerSimpleItemLoot(Material.GOLDEN_SPEAR);
        registerSimpleItemLoot(Material.GOLDEN_AXE);
        registerSimpleItemLoot(Material.GOLDEN_PICKAXE);
        registerSimpleItemLoot(Material.GOLDEN_HOE);
        registerSimpleItemLoot(Material.GOLDEN_SWORD);
        registerSimpleItemLoot(Material.GOLDEN_SHOVEL);

        registerSimpleItemLoot(Material.TURTLE_HELMET);
        registerSimpleItemLoot(Material.LEATHER_BOOTS);

        //Foods
        registerSimpleItemLoot(Material.BAKED_POTATO,8,16);
        registerSimpleItemLoot(Material.APPLE,8,32);
        registerSimpleItemLoot(Material.GOLDEN_CARROT,8,16);
        registerSimpleItemLoot(Material.CHORUS_FRUIT,4,16);
        registerSimpleItemLoot(Material.DRIED_KELP,4,16);
        registerSimpleItemLoot(Material.COOKED_BEEF,4,16);
        registerSimpleItemLoot(Material.COOKED_CHICKEN,4,16);
        registerSimpleItemLoot(Material.COOKED_COD,4,16);
        registerSimpleItemLoot(Material.COOKED_MUTTON,4,16);
        registerSimpleItemLoot(Material.COOKED_PORKCHOP,4,16);
        registerSimpleItemLoot(Material.COOKED_RABBIT,4,16);
        registerSimpleItemLoot(Material.COOKED_SALMON,4,16);
        registerSimpleItemLoot(Material.PUMPKIN_PIE,4,16);
        registerSimpleItemLoot(Material.GOLDEN_APPLE,1);

        //Others
        registerSimpleItemLoot(Material.BONE,4,7);
        registerSimpleItemLoot(Material.COAL,8,20);
        registerSimpleItemLoot(Material.REDSTONE_BLOCK,4,16);
        registerSimpleItemLoot(Material.PIGLIN_HEAD,1);
        registerSimpleItemLoot(Material.DRAGON_HEAD,1);
        registerSimpleItemLoot(Material.STRING,4,16);
        registerSimpleItemLoot(Material.SLIME_BLOCK,1,4);
        registerSimpleItemLoot(Material.HONEY_BLOCK,1,4);
        registerSimpleItemLoot(Material.DECORATED_POT,1);
        registerSimpleItemLoot(Material.NAUTILUS_SPAWN_EGG,1);

        //Potions
        registerSimplePotionLoot(Material.SPLASH_POTION,PotionEffectType.SLOW_FALLING,300,0,false,true,true,1,1);
        registerSimplePotionLoot(Material.SPLASH_POTION,PotionEffectType.LEVITATION,200,0,false,true,true,1,1);
        registerSimplePotionLoot(Material.SPLASH_POTION,PotionEffectType.INSTANT_DAMAGE,20,2,false,true,true,1,2);
        registerSimplePotionLoot(Material.SPLASH_POTION,PotionEffectType.POISON,200,0,false,true,true,1,1);
        registerSimplePotionLoot(Material.SPLASH_POTION,PotionEffectType.NAUSEA,200,0,false,true,true,1,1);
        registerSimplePotionLoot(Material.SPLASH_POTION,PotionEffectType.WEAKNESS,160,0,false,true,true,1,1);
        registerSimplePotionLoot(Material.SPLASH_POTION,PotionEffectType.WITHER,200,0,false,true,true,1,1);
        {
            ItemStack banner = new ItemStack(Material.WHITE_BANNER,1);
            banner.setData(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.HEAD).build());
            registerItemLoot(new ItemLoot(banner,1,1));
        }

    }
}

class LuckyLootLuckyEvents extends LootLuckyEvents{
    @Override
    void lootEffect(Player player, Location blockLocation){
        ReusableParticleBuilders.GREEN_EFFECT_SPREAD_PARTICLE_BUILDER.count(64).location(blockLocation).spawn();
    }

    LuckyLootLuckyEvents(){
        //Building blocks
        registerSimpleItemLoot(Material.OBSIDIAN,4,8);

        //Equipments
        registerSimpleItemLoot(Material.EMERALD,3,14);

        registerSimpleItemLoot(Material.DIAMOND,3,12);
        registerSimpleItemLoot(Material.DIAMOND_HELMET);
        registerSimpleItemLoot(Material.DIAMOND_CHESTPLATE);
        registerSimpleItemLoot(Material.DIAMOND_LEGGINGS);
        registerSimpleItemLoot(Material.DIAMOND_BOOTS);
        registerSimpleItemLoot(Material.DIAMOND_SPEAR);
        registerSimpleItemLoot(Material.DIAMOND_AXE);
        registerSimpleItemLoot(Material.DIAMOND_PICKAXE);
        registerSimpleItemLoot(Material.DIAMOND_SWORD);

        registerSimpleItemLoot(Material.NETHERITE_HELMET);
        registerSimpleItemLoot(Material.NETHERITE_CHESTPLATE);
        registerSimpleItemLoot(Material.NETHERITE_LEGGINGS);
        registerSimpleItemLoot(Material.NETHERITE_BOOTS);
        registerSimpleItemLoot(Material.NETHERITE_SPEAR);
        registerSimpleItemLoot(Material.NETHERITE_AXE);
        registerSimpleItemLoot(Material.NETHERITE_PICKAXE);
        registerSimpleItemLoot(Material.NETHERITE_SWORD);

        registerSimpleItemLoot(Material.TOTEM_OF_UNDYING,1);
        registerSimpleItemLoot(Material.ELYTRA,1);
        registerSimpleItemLoot(Material.ENCHANTED_GOLDEN_APPLE,1);
        registerSimpleItemLoot(Material.TRIDENT);
        registerSimpleItemLoot(Material.MACE,1);

        //Potions
        registerSimplePotionLoot(Material.SPLASH_POTION,PotionEffectType.REGENERATION,600,3,false,true,true,1,1);
        registerSimplePotionLoot(Material.SPLASH_POTION,PotionEffectType.ABSORPTION,600,2,false,true,true,1,1);
        registerSimplePotionLoot(Material.SPLASH_POTION,PotionEffectType.FIRE_RESISTANCE,1200,0,false,true,true,1,1);
        registerSimplePotionLoot(Material.SPLASH_POTION,PotionEffectType.INVISIBILITY,3600,0,false,true,true,1,1);
        registerSimplePotionLoot(Material.SPLASH_POTION,PotionEffectType.SPEED,2400,3,false,true,true,1,1);
        registerSimplePotionLoot(Material.SPLASH_POTION,PotionEffectType.SPEED,1800,4,false,true,true,1,1);
        registerSimplePotionLoot(Material.SPLASH_POTION,PotionEffectType.SPEED,1000,5,false,true,true,1,1);
        registerSimplePotionLoot(Material.SPLASH_POTION,PotionEffectType.JUMP_BOOST,1200,7,false,true,true,1,1);
        registerSimplePotionLoot(Material.SPLASH_POTION,PotionEffectType.JUMP_BOOST,2400,5,false,true,true,1,1);
        registerSimplePotionLoot(Material.SPLASH_POTION,PotionEffectType.INSTANT_HEALTH,20,3,false,true,true,1,4);
        registerSimplePotionLoot(Material.SPLASH_POTION,PotionEffectType.STRENGTH,1200,1,false,true,true,1,4);
        registerSimplePotionLoot(Material.SPLASH_POTION,PotionEffectType.HASTE,1200,3,false,true,true,1,1);

        //Courage potion
        {
            ItemStack potion = new ItemStack(Material.POTION);
            PotionContents contents = PotionContents.potionContents()
                    .addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION,2000,2,false,true,true))
                    .addCustomEffect(new PotionEffect(PotionEffectType.ABSORPTION,2000,1,false,false,true))
                    .addCustomEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST,2000,2,false,false,true))
                    .addCustomEffect(new PotionEffect(PotionEffectType.HASTE,2000,3,false,false,true))
                    .addCustomEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 2000,5,false,false,true))
                    .addCustomEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,2000,0,false,false,true))
                    .addCustomEffect(new PotionEffect(PotionEffectType.STRENGTH,2000,0,false,false,true))
                    .addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE,20,1,false,true,false))
                    .addCustomEffect(new PotionEffect(PotionEffectType.BLINDNESS,300,0,false,false,false))
                    .addCustomEffect(new PotionEffect(PotionEffectType.SLOWNESS,300,5,false,true,false))
                    .build();
            potion.setData(DataComponentTypes.POTION_CONTENTS, contents);
            potion.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build());
            potion.setData(DataComponentTypes.CUSTOM_NAME, MiniMessage.miniMessage().deserialize("<!i><b>Gather up</b> Courage"));
            potion.setData(DataComponentTypes.RARITY, ItemRarity.RARE);
            potion.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<!i>Gather up your courage with this potion"),
                    MiniMessage.miniMessage().deserialize("<!i>But at what cost?"),
                    MiniMessage.miniMessage().deserialize("<!i><b>Be careful</b> of your health"))));
            registerItemLoot(new ItemLoot(potion, 1, 2));
        }

        //Fly potion
        {
            ItemStack potion = new ItemStack(Material.POTION);
            PotionContents contents = PotionContents.potionContents()
                    .addCustomEffect(new PotionEffect(PotionEffectType.LEVITATION,200,1,false,true,true))
                    .addCustomEffect(new PotionEffect(PotionEffectType.SLOW_FALLING,600,1,false,false,true))
                    .build();
            potion.setData(DataComponentTypes.POTION_CONTENTS, contents);
            potion.setData(DataComponentTypes.CUSTOM_NAME, MiniMessage.miniMessage().deserialize("<!i>Fly potion"));
            potion.setData(DataComponentTypes.RARITY, ItemRarity.RARE);
            potion.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<!i>You can fly, gracefully, slowly"),
                    MiniMessage.miniMessage().deserialize(""),
                    MiniMessage.miniMessage().deserialize("<!i><b><dark_gray>User review:</dark_gray></b>"),
                    MiniMessage.miniMessage().deserialize("<gray>★☆☆☆☆ I pressed my spacebar twice, it simply does not work.</gray>"))));
            registerItemLoot(new ItemLoot(potion, 1, 2));
        }

        //Enchanted items
        {
            ItemStack enchanted = new ItemStack(Material.DIAMOND_SWORD);
            enchanted.addUnsafeEnchantment(Enchantment.SHARPNESS,7);
            enchanted.addUnsafeEnchantment(Enchantment.SMITE,5);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.DIAMOND_AXE);
            enchanted.addUnsafeEnchantment(Enchantment.SHARPNESS,5);
            enchanted.addUnsafeEnchantment(Enchantment.SMITE,3);
            enchanted.addUnsafeEnchantment(Enchantment.EFFICIENCY,7);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.DIAMOND);
            enchanted.addUnsafeEnchantment(Enchantment.SHARPNESS,19);
            enchanted.addUnsafeEnchantment(Enchantment.SMITE,5);
            enchanted.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<b>Independence of Ingredients?</b>"))));
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.DIAMOND_PICKAXE);
            enchanted.addUnsafeEnchantment(Enchantment.EFFICIENCY,7);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.DIAMOND_PICKAXE);
            enchanted.addUnsafeEnchantment(Enchantment.SILK_TOUCH,1);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.DIAMOND_SPEAR);
            enchanted.addUnsafeEnchantment(Enchantment.SHARPNESS,7);
            enchanted.addUnsafeEnchantment(Enchantment.SMITE,3);
            enchanted.addUnsafeEnchantment(Enchantment.LUNGE,5);
            enchanted.addUnsafeEnchantment(Enchantment.KNOCKBACK,1);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }

        {
            ItemStack enchanted = new ItemStack(Material.NETHERITE_SWORD);
            enchanted.addUnsafeEnchantment(Enchantment.SHARPNESS,7);
            enchanted.addUnsafeEnchantment(Enchantment.SMITE,5);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.NETHERITE_AXE);
            enchanted.addUnsafeEnchantment(Enchantment.SHARPNESS,5);
            enchanted.addUnsafeEnchantment(Enchantment.SMITE,4);
            enchanted.addUnsafeEnchantment(Enchantment.EFFICIENCY,7);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.NETHERITE_PICKAXE);
            enchanted.addUnsafeEnchantment(Enchantment.EFFICIENCY,7);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.NETHERITE_SPEAR);
            enchanted.addUnsafeEnchantment(Enchantment.SHARPNESS,7);
            enchanted.addUnsafeEnchantment(Enchantment.SMITE,3);
            enchanted.addUnsafeEnchantment(Enchantment.LUNGE,5);
            enchanted.addUnsafeEnchantment(Enchantment.KNOCKBACK,1);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.NETHERITE_BLOCK);
            enchanted.addUnsafeEnchantment(Enchantment.EFFICIENCY,64);
            enchanted.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<b>What am I going to mine?</b>"))));
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }

        {
            ItemStack enchanted = new ItemStack(Material.TRIDENT);
            enchanted.addUnsafeEnchantment(Enchantment.RIPTIDE,5);
            enchanted.addUnsafeEnchantment(Enchantment.IMPALING,5);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.TRIDENT);
            enchanted.addUnsafeEnchantment(Enchantment.LOYALTY,5);
            enchanted.addUnsafeEnchantment(Enchantment.IMPALING,5);
            enchanted.addUnsafeEnchantment(Enchantment.CHANNELING,1);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }

        {
            ItemStack enchanted = new ItemStack(Material.BOW);
            enchanted.addUnsafeEnchantment(Enchantment.INFINITY,1);
            enchanted.addUnsafeEnchantment(Enchantment.POWER,3);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.BOW);
            enchanted.addUnsafeEnchantment(Enchantment.PUNCH,2);
            enchanted.addUnsafeEnchantment(Enchantment.FLAME,3);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }

        {
            ItemStack enchanted = new ItemStack(Material.CROSSBOW);
            enchanted.addUnsafeEnchantment(Enchantment.MULTISHOT,1);
            enchanted.addUnsafeEnchantment(Enchantment.PIERCING,3);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.CROSSBOW);
            enchanted.addUnsafeEnchantment(Enchantment.MULTISHOT,1);
            enchanted.addUnsafeEnchantment(Enchantment.QUICK_CHARGE,5);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }

        //Enchanted equipments
        {
            ItemStack enchanted = new ItemStack(Material.DIAMOND_HELMET);
            enchanted.addUnsafeEnchantment(Enchantment.PROTECTION,4);
            enchanted.addUnsafeEnchantment(Enchantment.UNBREAKING,3);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.DIAMOND_CHESTPLATE);
            enchanted.addUnsafeEnchantment(Enchantment.PROTECTION,4);
            enchanted.addUnsafeEnchantment(Enchantment.UNBREAKING,3);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.DIAMOND_LEGGINGS);
            enchanted.addUnsafeEnchantment(Enchantment.PROTECTION,4);
            enchanted.addUnsafeEnchantment(Enchantment.UNBREAKING,3);
            enchanted.addUnsafeEnchantment(Enchantment.SWIFT_SNEAK,3);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.DIAMOND_BOOTS);
            enchanted.addUnsafeEnchantment(Enchantment.PROTECTION,4);
            enchanted.addUnsafeEnchantment(Enchantment.UNBREAKING,3);
            enchanted.addUnsafeEnchantment(Enchantment.FEATHER_FALLING,3);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }

        {
            ItemStack enchanted = new ItemStack(Material.NETHERITE_HELMET);
            enchanted.addUnsafeEnchantment(Enchantment.PROTECTION,4);
            enchanted.addUnsafeEnchantment(Enchantment.UNBREAKING,3);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.NETHERITE_CHESTPLATE);
            enchanted.addUnsafeEnchantment(Enchantment.PROTECTION,4);
            enchanted.addUnsafeEnchantment(Enchantment.UNBREAKING,3);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.NETHERITE_LEGGINGS);
            enchanted.addUnsafeEnchantment(Enchantment.PROTECTION,4);
            enchanted.addUnsafeEnchantment(Enchantment.UNBREAKING,3);
            enchanted.addUnsafeEnchantment(Enchantment.SWIFT_SNEAK,3);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.NETHERITE_BOOTS);
            enchanted.addUnsafeEnchantment(Enchantment.PROTECTION,4);
            enchanted.addUnsafeEnchantment(Enchantment.UNBREAKING,3);
            enchanted.addUnsafeEnchantment(Enchantment.FEATHER_FALLING,3);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.MACE);
            enchanted.addUnsafeEnchantment(Enchantment.DENSITY,5);
            enchanted.addUnsafeEnchantment(Enchantment.WIND_BURST,3);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
        {
            ItemStack enchanted = new ItemStack(Material.MACE);
            enchanted.addUnsafeEnchantment(Enchantment.BREACH,4);
            enchanted.addUnsafeEnchantment(Enchantment.WIND_BURST,3);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }

        //Others
        {
            ItemStack enchanted = new ItemStack(Material.DIAMOND_CHESTPLATE);
            enchanted.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<b>Could jumping twice work?</b>"))));
            enchanted.setData(DataComponentTypes.GLIDER);
            enchanted.addUnsafeEnchantment(Enchantment.PROTECTION,2);
            enchanted.addUnsafeEnchantment(Enchantment.UNBREAKING,1);
            registerItemLoot(new ItemLoot(enchanted,1,1));
        }
    }
}

class UnluckyLootLuckyEvents extends LootLuckyEvents{
    @Override
    void lootEffect(Player player, Location blockLocation){
        ReusableParticleBuilders.GREEN_EFFECT_SPREAD_PARTICLE_BUILDER.count(64).location(blockLocation).spawn();
    }

    UnluckyLootLuckyEvents(){
        registerSimpleItemLoot(Material.POISONOUS_POTATO,1);
    }
}