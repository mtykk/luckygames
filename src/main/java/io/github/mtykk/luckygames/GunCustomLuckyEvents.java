package io.github.mtykk.luckygames;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.zombie_striker.qg.api.QualityArmory;

public class GunCustomLuckyEvents extends LootLuckyEvents{
    @Override
    void lootEffect(Player player, Location blockLocation){
        ReusableParticleBuilders.GREEN_EFFECT_SPREAD_PARTICLE_BUILDER.count(64).location(blockLocation).spawn();
    }

    protected void registerQaItem(String itemName) throws IllegalArgumentException{
        registerQaItem(itemName,1);
    }
    protected void registerQaItem(String itemName,int minAmount,int maxAmount) throws IllegalArgumentException{
        ItemStack item = QualityArmory.getCustomItemAsItemStack(itemName);
        if(item == null) throw new IllegalArgumentException("No such item");
        registerItemLoot(new ItemLoot(item,minAmount,maxAmount));
    }
    protected void registerQaItem(String itemName,int amount) throws IllegalArgumentException{
        registerQaItem(itemName,amount,amount);
    }
}

class WeaponCustomLuckyEvents extends GunCustomLuckyEvents{
    WeaponCustomLuckyEvents(){
        registerQaItem("aa12");
        registerQaItem("ak47");
        registerQaItem("ak47u");
        registerQaItem("alienneedler");
        registerQaItem("arcgun9");
        registerQaItem("asval");
        registerQaItem("auto9");
        registerQaItem("awp");
        registerQaItem("awpasiimov");
        registerQaItem("barrett");
        registerQaItem("ctar21");
        registerQaItem("cz75");
        registerQaItem("dp27");
        registerQaItem("dragunov");
        registerQaItem("enfield");
        registerQaItem("famas");
        registerQaItem("fatman");
        registerQaItem("flamer");
        registerQaItem("flintlockpistol");
        registerQaItem("fnfal");
        registerQaItem("galil");
        registerQaItem("glock");
        registerQaItem("kar98k");
        registerQaItem("m16");
        registerQaItem("m4a1sburst");
        registerQaItem("m79");
        registerQaItem("minigun");
        registerQaItem("musket");
        registerQaItem("p30silenced");
        registerQaItem("pancorjackhammer");
        registerQaItem("pulserifle");
        registerQaItem("rpg");
        registerQaItem("ump");
        registerQaItem("uzi");
        registerQaItem("vera");

        registerQaItem("molotov");
        registerQaItem("incendarygrenade");
        registerQaItem("grenade");
        registerQaItem("smokegrenade");
        registerQaItem("medkit");
        registerQaItem("proxymine");
        registerQaItem("ammobag");

        registerQaItem("ncrhelmet");
        registerQaItem("ushanka");
        registerQaItem("skimask");
        registerQaItem("assaulthelmet");

        registerQaItem("LightSaberWhite");
    }
}

class AmmoCustomLuckyEvents extends GunCustomLuckyEvents{
    AmmoCustomLuckyEvents(){
        registerQaItem("762",48,64);
        registerQaItem("556",48,64);
        registerQaItem("fuel",20,40);
        registerQaItem("fusion_cell",25,50);
        registerQaItem("50bmg",10,20);
        registerQaItem("rocket",3,5);
        registerQaItem("40mm",12,24);
        registerQaItem("mininuke");
        registerQaItem("shell",6,10);
        registerQaItem("9mm",40,64);
        registerQaItem("musketball",20,32);
    }
}