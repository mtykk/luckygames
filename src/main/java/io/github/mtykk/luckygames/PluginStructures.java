package io.github.mtykk.luckygames;

import org.bukkit.NamespacedKey;

public enum PluginStructures {
    WAXED_OXIDIZED_WATERLOGGED_PRISON(new NamespacedKey("luckygames","waxed_oxidized_waterlogged_prison")),
    WATERLOGGED_PRISON(new NamespacedKey("luckygames","waterlogged_prison")),
    FORGOTTEN_SHELTER(new NamespacedKey("luckygames","forgotten_shelter")),
    FORGOTTEN_SHOOTING_GALLERY(new NamespacedKey("luckygames","forgotten_shooting_gallery")),
    FORGOTTEN_PORTAL(new NamespacedKey("luckygames","forgotten_portal")),
    MINIATURE_END(new NamespacedKey("luckygames","miniature_end")),
    PRISON(new NamespacedKey("luckygames","prison")),
    PRIVACY_PROTECTION(new NamespacedKey("luckygames","privacy_protection")),
    DOOR_TRAP(new NamespacedKey("luckygames","door_trap")),
    PISTON_TRAP(new NamespacedKey("luckygames","piston_trap")),
    LODESTONE_TERRACE(new NamespacedKey("luckygames","lodestone_terrace")),
    FOOD_SHOP(new NamespacedKey("luckygames","food_shop")),
    POND(new NamespacedKey("luckygames","pond")),
    ENCHANTING_TABLE(new NamespacedKey("luckygames","enchanting_table")),
    LUCKY_ISLAND(new NamespacedKey("luckygames","lucky_island")),
    CHECKPOINT(new NamespacedKey("luckygames","checkpoint")),
    START(new NamespacedKey("luckygames","start")),
    ARENA(new NamespacedKey("luckygames","arena"));

    private final NamespacedKey structureKey;
    PluginStructures(NamespacedKey key){
        structureKey = key;
    }

    public NamespacedKey getStructureKey(){
        return structureKey;
    }
}
