package io.github.mtykk.luckygames;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.entity.boat.AcaciaBoat;
import org.bukkit.entity.boat.OakBoat;
import org.bukkit.entity.boat.OakChestBoat;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public abstract class SpawnLuckyEvents extends GenericLuckyEvents {

    @Override
    protected void classDefaultOnStart(Player player, Location blockLocation){
        ReusableParticleBuilders.POOF_PARTICLE_BUILDER.location(blockLocation).spawn();
    }

    @Override
    protected void classDefaultOnFinish(Player player, Location blockLocation){}

    protected Vector generateRandomSpawnOffset(){
        return new Vector(PluginRandom.randomGenerator.nextFloat()-0.5f,0,PluginRandom.randomGenerator.nextFloat()-0.5f);
    }

    protected void registerSimpleEntity(EntityType entityType,int minAmount,int maxAmount){
        registerGenericEvent((player, blockLocation)->{
            blockLocation.getWorld().spawnEntity(blockLocation.add(generateRandomSpawnOffset()),entityType,true);
        },minAmount,maxAmount);
    }
    protected void registerSimpleEntity(EntityType entityType,int amount){
        registerGenericEvent((player, blockLocation)->{
            blockLocation.getWorld().spawnEntity(blockLocation.add(generateRandomSpawnOffset()),entityType,true);
        },amount,amount);
    }
    protected  void registerSimpleEntity(EntityType entityType){
        registerSimpleEntity(entityType,1);
    }
}

class UnluckySpawnLuckyEvents extends SpawnLuckyEvents{
    UnluckySpawnLuckyEvents(){
        registerSimpleEntity(EntityType.ZOMBIE,1,2);
        registerSimpleEntity(EntityType.DROWNED);
        registerSimpleEntity(EntityType.CREEPER);
        registerSimpleEntity(EntityType.VINDICATOR);
        registerSimpleEntity(EntityType.SLIME);
        registerSimpleEntity(EntityType.ENDERMAN);
        registerSimpleEntity(EntityType.SKELETON);
        registerSimpleEntity(EntityType.PARCHED);
        registerSimpleEntity(EntityType.MAGMA_CUBE);
        registerSimpleEntity(EntityType.PIGLIN);
        registerSimpleEntity(EntityType.PIGLIN_BRUTE);
        registerSimpleEntity(EntityType.ZOMBIFIED_PIGLIN);
        registerSimpleEntity(EntityType.WITHER_SKELETON);
        registerSimpleEntity(EntityType.HUSK);
        registerSimpleEntity(EntityType.SILVERFISH);
        registerSimpleEntity(EntityType.ENDERMITE);
        registerSimpleEntity(EntityType.WITCH);
        registerSimpleEntity(EntityType.STRAY);
        registerSimpleEntity(EntityType.BOGGED);
        registerSimpleEntity(EntityType.SHULKER);

        registerGenericEvent((player, blockLocation)->{
            blockLocation.getWorld().spawn(blockLocation, Zombie.class, CreatureSpawnEvent.SpawnReason.CUSTOM,zombie -> {
                EntityEquipment equipment = zombie.getEquipment();
                equipment.setItemInMainHand(new ItemStack(Material.DIAMOND_AXE,1));
                equipment.setItemInMainHandDropChance(0f);
            });
        });

        registerGenericEvent((player, blockLocation)->{
            blockLocation.getWorld().spawn(blockLocation, Zombie.class, CreatureSpawnEvent.SpawnReason.CUSTOM,zombie -> {
                Zombie zombiePassenger = blockLocation.getWorld().spawn(blockLocation, Zombie.class,CreatureSpawnEvent.SpawnReason.CUSTOM,false, z -> {
                    z.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SPEAR));
                    z.getEquipment().setItemInMainHandDropChance(0f);
                });
                zombie.addPassenger(zombiePassenger);
                zombie.setSilent(true);
            });
        });

        registerGenericEvent((player, blockLocation)->{
            blockLocation.getWorld().spawn(blockLocation, Phantom.class, CreatureSpawnEvent.SpawnReason.CUSTOM, phantom -> {
                Zombie zombiePassenger = blockLocation.getWorld().spawn(blockLocation, Zombie.class,CreatureSpawnEvent.SpawnReason.CUSTOM,false, z -> {
                    z.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SPEAR));
                    z.getEquipment().setItemInMainHandDropChance(0f);
                });
                phantom.addPassenger(zombiePassenger);
            });
        });

        registerGenericEvent((player, blockLocation)->{
            blockLocation.getWorld().spawn(blockLocation, Slime.class, CreatureSpawnEvent.SpawnReason.CUSTOM, slime -> {
                MagmaCube magmaPassenger = blockLocation.getWorld().spawn(blockLocation, MagmaCube.class,CreatureSpawnEvent.SpawnReason.CUSTOM,false, m -> {
                    m.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SPEAR));
                    m.getEquipment().setItemInMainHandDropChance(0f);
                });
                slime.addPassenger(magmaPassenger);
            });
        });

        registerGenericEvent((player, blockLocation)->{
            blockLocation.getWorld().spawn(blockLocation, Chicken.class, CreatureSpawnEvent.SpawnReason.CUSTOM, chicken -> {
                Zombie zombiePassenger = blockLocation.getWorld().spawn(blockLocation, Zombie.class,CreatureSpawnEvent.SpawnReason.CUSTOM,false, z -> {
                    z.getEquipment().setItemInMainHand(new ItemStack(Material.WOODEN_SPEAR));
                    z.getEquipment().setItemInMainHandDropChance(0f);
                    z.setAgeLock(true);
                    z.setBaby();
                });
                chicken.addPassenger(zombiePassenger);
            });
        });

        registerGenericEvent((player, blockLocation)->{
            blockLocation.getWorld().spawn(blockLocation, Breeze.class, CreatureSpawnEvent.SpawnReason.CUSTOM,breeze -> {
                breeze.setHealth(15);
            });
        });
    }
}

class NormalSpawnLuckyEvents extends SpawnLuckyEvents{
    NormalSpawnLuckyEvents() {
        registerSimpleEntity(EntityType.OAK_BOAT);
        registerSimpleEntity(EntityType.ACACIA_BOAT);
        registerGenericEvent((player, blockLocation)->{
            blockLocation.getWorld().spawn(blockLocation, OakBoat.class, CreatureSpawnEvent.SpawnReason.CUSTOM, boat -> {
                Sheep sheepPassenger = blockLocation.getWorld().spawn(blockLocation, Sheep.class,CreatureSpawnEvent.SpawnReason.CUSTOM,false, s -> {
                    s.setColor(DyeColor.PINK);
                });
                boat.addPassenger(sheepPassenger);
            });
        });
        registerGenericEvent((player, blockLocation)->{
            blockLocation.getWorld().spawn(blockLocation, AcaciaBoat.class, CreatureSpawnEvent.SpawnReason.CUSTOM, boat -> {
                Sheep sheepPassenger = blockLocation.getWorld().spawn(blockLocation, Sheep.class,CreatureSpawnEvent.SpawnReason.CUSTOM,false, s -> {
                    s.setColor(DyeColor.YELLOW);
                });
                boat.addPassenger(sheepPassenger);
            });
        });
        registerGenericEvent((player, blockLocation)->{
            blockLocation.getWorld().spawn(blockLocation, OakChestBoat.class, CreatureSpawnEvent.SpawnReason.CUSTOM, boat -> {
                Inventory inv = boat.getInventory();
                inv.addItem(new ItemStack(Material.GOLDEN_PICKAXE,1));
                inv.addItem(new ItemStack(Material.GOLDEN_HOE,1));
                inv.addItem(new ItemStack(Material.GOLDEN_HORSE_ARMOR,1));
                inv.addItem(new ItemStack(Material.GOLDEN_NAUTILUS_ARMOR,1));
            });
        });
        registerGenericEvent((player, blockLocation)->{
            blockLocation.getWorld().spawn(blockLocation, OakChestBoat.class, CreatureSpawnEvent.SpawnReason.CUSTOM, boat -> {
                Inventory inv = boat.getInventory();
                inv.addItem(new ItemStack(Material.SADDLE,1));
                inv.addItem(new ItemStack(Material.WHITE_HARNESS,1));
            });
        });

        registerSimpleEntity(EntityType.SHEEP,1,3);
        registerSimpleEntity(EntityType.SKELETON_HORSE);
        registerSimpleEntity(EntityType.HORSE);
        registerSimpleEntity(EntityType.PIG,1,2);
        registerSimpleEntity(EntityType.COW,1,2);
        registerSimpleEntity(EntityType.CHICKEN,1,4);
        registerSimpleEntity(EntityType.DONKEY);
        registerSimpleEntity(EntityType.MULE);
        registerSimpleEntity(EntityType.ALLAY);
        registerSimpleEntity(EntityType.PARROT);
        registerSimpleEntity(EntityType.POLAR_BEAR);
    }
}

class LuckySpawnLuckyEvents extends SpawnLuckyEvents{
    LuckySpawnLuckyEvents() {
        registerSimpleEntity(EntityType.PANDA);

        registerGenericEvent((player, blockLocation)->{
            blockLocation.getWorld().spawn(blockLocation, Horse.class, CreatureSpawnEvent.SpawnReason.CUSTOM, horse -> {
                horse.setColor(Horse.Color.WHITE);
                horse.setTamed(true);
                horse.getEquipment().setItem(EquipmentSlot.SADDLE,new ItemStack(Material.SADDLE));
                horse.getEquipment().setItem(EquipmentSlot.BODY,new ItemStack(Material.DIAMOND_HORSE_ARMOR));
                horse.setJumpStrength(1);
                AttributeInstance speedAttribute = horse.getAttribute(Attribute.MOVEMENT_SPEED);
                if (speedAttribute != null) {speedAttribute.setBaseValue(0.3375);}
            });
        });
        registerGenericEvent((player, blockLocation)->{
            blockLocation.getWorld().spawn(blockLocation, IronGolem.class, CreatureSpawnEvent.SpawnReason.CUSTOM, golem -> {
                golem.setPlayerCreated(true);
            });
        });

        registerGenericEvent((player, blockLocation)->{
            player.give(new ItemStack(Material.COOKED_BEEF,3));
            blockLocation.getWorld().spawn(blockLocation, Wolf.class, CreatureSpawnEvent.SpawnReason.CUSTOM, wolf -> {
                wolf.setTamed(true);
                wolf.setAdult();
                wolf.setVariant(Wolf.Variant.SNOWY);
                wolf.setOwner(player);
            });
        });
        registerGenericEvent((player, blockLocation)->{
            player.give(new ItemStack(Material.COOKED_BEEF,3));
            blockLocation.getWorld().spawn(blockLocation, Wolf.class, CreatureSpawnEvent.SpawnReason.CUSTOM, wolf -> {
                wolf.setTamed(true);
                wolf.setAdult();
                wolf.setVariant(Wolf.Variant.ASHEN);
                wolf.setOwner(player);
            });
        });
        registerGenericEvent((player, blockLocation)->{
            player.give(new ItemStack(Material.COOKED_BEEF,3));
            blockLocation.getWorld().spawn(blockLocation, Wolf.class, CreatureSpawnEvent.SpawnReason.CUSTOM, wolf -> {
                wolf.setTamed(true);
                wolf.setAdult();
                wolf.setVariant(Wolf.Variant.PALE);
                wolf.setOwner(player);
            });
        });

        registerGenericEvent((player, blockLocation) -> {
            blockLocation.getWorld().spawn(blockLocation, Villager.class, CreatureSpawnEvent.SpawnReason.CUSTOM, villager -> {
                List<MerchantRecipe> recipes = new ArrayList<>();
                {
                    ItemStack i = new ItemStack(Material.GOLDEN_SPEAR,1);
                    i.addUnsafeEnchantment(Enchantment.LUNGE,10);
                    i.addUnsafeEnchantment(Enchantment.LOOTING,5);
                    MerchantRecipe r = new MerchantRecipe(i,0,2,true,5,0,0,0,false);
                    r.addIngredient(new ItemStack(Material.GOLD_INGOT,7));
                    recipes.add(r);
                }
                {
                    ItemStack i = new ItemStack(Material.GOLDEN_SPEAR,1);
                    i.addUnsafeEnchantment(Enchantment.LUNGE,16);
                    MerchantRecipe r = new MerchantRecipe(i,0,1,true,5,0,0,0,false);
                    r.addIngredient(new ItemStack(Material.GOLD_INGOT,7));
                    recipes.add(r);
                }
                {
                    ItemStack i = new ItemStack(Material.GOLDEN_AXE,1);
                    i.addUnsafeEnchantment(Enchantment.SHARPNESS,29);
                    ItemMeta meta = i.getItemMeta();
                    if(meta instanceof Damageable){
                        ((Damageable) meta).setDamage(Material.GOLDEN_AXE.getMaxDurability()/2);
                    }
                    i.setItemMeta(meta);
                    MerchantRecipe r = new MerchantRecipe(i,0,1,true,5,0,0,0,false);
                    r.addIngredient(new ItemStack(Material.GOLD_INGOT,10));
                    recipes.add(r);
                }
                villager.setProfession(Villager.Profession.WEAPONSMITH);
                villager.setAware(false);
                villager.setRecipes(recipes);
            });
        });

        registerGenericEvent((player, blockLocation) -> {
            blockLocation.getWorld().spawn(blockLocation, Villager.class, CreatureSpawnEvent.SpawnReason.CUSTOM, villager -> {
                List<MerchantRecipe> recipes = new ArrayList<>();
                {
                    ItemStack i = new ItemStack(Material.WOODEN_SPEAR,1);
                    i.addUnsafeEnchantment(Enchantment.LUNGE,10);
                    i.addUnsafeEnchantment(Enchantment.LOOTING,5);
                    MerchantRecipe r = new MerchantRecipe(i,0,5,true,5,0,0,0,false);
                    r.addIngredient(new ItemStack(Material.OAK_PLANKS,8));
                    recipes.add(r);
                }
                {
                    ItemStack i = new ItemStack(Material.WOODEN_SPEAR,1);
                    i.addUnsafeEnchantment(Enchantment.LUNGE,16);
                    MerchantRecipe r = new MerchantRecipe(i,0,1,true,5,0,0,0,false);
                    r.addIngredient(new ItemStack(Material.OAK_PLANKS,16));
                    recipes.add(r);
                }
                {
                    ItemStack i = new ItemStack(Material.WOODEN_AXE,1);
                    i.addUnsafeEnchantment(Enchantment.SHARPNESS,27);
                    ItemMeta meta = i.getItemMeta();
                    if(meta instanceof Damageable){
                        ((Damageable) meta).setDamage(Material.WOODEN_AXE.getMaxDurability()/2);
                    }
                    i.setItemMeta(meta);
                    MerchantRecipe r = new MerchantRecipe(i,0,1,true,5,0,0,0,false);
                    r.addIngredient(new ItemStack(Material.OAK_PLANKS,32));
                    recipes.add(r);
                }
                villager.setProfession(Villager.Profession.WEAPONSMITH);
                villager.setAware(false);
                villager.setRecipes(recipes);
            });
        });

        registerGenericEvent((player, blockLocation) -> {
            blockLocation.getWorld().spawn(blockLocation, Villager.class, CreatureSpawnEvent.SpawnReason.CUSTOM, villager -> {
                List<MerchantRecipe> recipes = new ArrayList<>();
                {
                    ItemStack i = new ItemStack(Material.TRIDENT,1);
                    i.addUnsafeEnchantment(Enchantment.LOYALTY,3);
                    i.addUnsafeEnchantment(Enchantment.CHANNELING,1);
                    i.addUnsafeEnchantment(Enchantment.IMPALING,5);
                    MerchantRecipe r = new MerchantRecipe(i,0,3,true,5,0,0,0,false);
                    r.addIngredient(new ItemStack(Material.EMERALD,8));
                    recipes.add(r);
                }
                {
                    ItemStack i = new ItemStack(Material.CROSSBOW,1);
                    i.addUnsafeEnchantment(Enchantment.POWER,4);
                    i.addUnsafeEnchantment(Enchantment.PIERCING,3);
                    i.addUnsafeEnchantment(Enchantment.MULTISHOT,1);
                    i.addUnsafeEnchantment(Enchantment.QUICK_CHARGE,3);
                    i.addUnsafeEnchantment(Enchantment.INFINITY,1);
                    MerchantRecipe r = new MerchantRecipe(i,0,1,true,5,0,0,0,false);
                    r.addIngredient(new ItemStack(Material.EMERALD,15));
                    recipes.add(r);
                }
                {
                    ItemStack i = new ItemStack(Material.BOW,1);
                    i.addUnsafeEnchantment(Enchantment.POWER,4);
                    i.addUnsafeEnchantment(Enchantment.PUNCH,2);
                    i.addUnsafeEnchantment(Enchantment.INFINITY,1);
                    MerchantRecipe r = new MerchantRecipe(i,0,1,true,5,0,0,0,false);
                    r.addIngredient(new ItemStack(Material.EMERALD,10));
                    recipes.add(r);
                }
                villager.setProfession(Villager.Profession.WEAPONSMITH);
                villager.setAware(false);
                villager.setRecipes(recipes);
            });
        });

        registerGenericEvent((player, blockLocation) -> {
            blockLocation.getWorld().spawn(blockLocation, Villager.class, CreatureSpawnEvent.SpawnReason.CUSTOM, villager -> {
                List<MerchantRecipe> recipes = new ArrayList<>();
                {
                    ItemStack i = new ItemStack(Material.GOLDEN_APPLE,1);
                    MerchantRecipe r = new MerchantRecipe(i,0,5,true,5,0,0,0,false);
                    r.addIngredient(new ItemStack(Material.GOLD_INGOT,5));
                    recipes.add(r);
                }
                {
                    ItemStack i = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE,1);
                    MerchantRecipe r = new MerchantRecipe(i,0,2,true,5,0,0,0,false);
                    r.addIngredient(new ItemStack(Material.GOLD_BLOCK,5));
                    recipes.add(r);
                }
                villager.setProfession(Villager.Profession.FARMER);
                villager.setAware(false);
                villager.setRecipes(recipes);
            });
        });

        registerGenericEvent((player, blockLocation) -> {
            blockLocation.getWorld().spawn(blockLocation, WanderingTrader.class, CreatureSpawnEvent.SpawnReason.CUSTOM, trader -> {
                List<MerchantRecipe> recipes = new ArrayList<>();
                {
                    ItemStack i = new ItemStack(Material.DIAMOND_ORE,1);
                    MerchantRecipe r = new MerchantRecipe(i,0,5,true,5,0,0,0,false);
                    r.addIngredient(new ItemStack(Material.GOLD_INGOT,5));
                    recipes.add(r);
                }
                {
                    ItemStack i = new ItemStack(Material.ANCIENT_DEBRIS,1);
                    MerchantRecipe r = new MerchantRecipe(i,0,2,true,5,0,0,0,false);
                    r.addIngredient(new ItemStack(Material.GOLD_INGOT,5));
                    recipes.add(r);
                }
                trader.setRecipes(recipes);
            });
        });

        registerGenericEvent((player, blockLocation) -> {
            blockLocation.getWorld().spawn(blockLocation, WanderingTrader.class, CreatureSpawnEvent.SpawnReason.CUSTOM, trader -> {
                List<MerchantRecipe> recipes = new ArrayList<>();
                {
                    ItemStack i = new ItemStack(Material.DRIED_GHAST,1);
                    MerchantRecipe r = new MerchantRecipe(i,0,3,true,5,0,0,0,false);
                    r.addIngredient(new ItemStack(Material.FIRE_CHARGE,2));
                    recipes.add(r);
                }
                {
                    ItemStack i = new ItemStack(Material.WATER_BUCKET,1);
                    MerchantRecipe r = new MerchantRecipe(i,0,2,true,5,0,0,0,false);
                    r.addIngredient(new ItemStack(Material.IRON_INGOT,5));
                    recipes.add(r);
                }
                {
                    ItemStack i = new ItemStack(Material.WHITE_HARNESS,1);
                    MerchantRecipe r = new MerchantRecipe(i,0,2,true,5,0,0,0,false);
                    r.addIngredient(new ItemStack(Material.LEATHER,3));
                    recipes.add(r);
                }
                trader.setRecipes(recipes);
            });
        });
    }
}