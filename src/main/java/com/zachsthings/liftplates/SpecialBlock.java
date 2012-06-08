package com.zachsthings.liftplates;

import org.bukkit.Material;

/**
 * @author zml2008
 */
public enum SpecialBlock {
    PAUSE(Material.IRON_BLOCK),
    STATION(Material.GOLD_BLOCK),
    ;
    private final Material type;

    private SpecialBlock(Material type) {
        this.type = type;
    }

    public Material getDefaultType() {
        return type;
    }
}
