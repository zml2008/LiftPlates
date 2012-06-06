package com.zachsthings.liftplates;

import com.zachsthings.liftplates.config.ConfigurationBase;
import com.zachsthings.liftplates.config.Setting;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zml2008
 */
public class LiftPlatesConfig extends ConfigurationBase {
    /**
     * Whether to treat a bunch of pressure plate lifts next to each other
     * with the same base block type as the same lift
     */
    @Setting("recursive-lifts") public boolean recursiveLifts = true;
    /**
     * Allow configuration of which block types that have special functionality
     */
    @Setting("special-blocks") public Map<String, String> rawSpecialBlocks = new HashMap<String, String>();

    /**
     * The maximum distance from the triggered pressure plate to look for blocks of the same type
     */
    @Setting("max-lift-size") public int maxLiftSize = 5;

    public Map<SpecialBlock, Material> specialBlocks = new HashMap<SpecialBlock, Material>();

    public void load(ConfigurationSection section) {
        super.load(section);

        for (Map.Entry<String, String> entry : rawSpecialBlocks.entrySet()) {
            SpecialBlock block = SpecialBlock.valueOf(entry.getKey().toUpperCase()); // TODO: Correctly handle unknown types
            Material mat = Material.matchMaterial(entry.getValue());
            if (block != null && mat != null) {
                specialBlocks.put(block, mat);
            }
        }

        boolean changed = false;
        for (SpecialBlock type : SpecialBlock.values()) {
            if (!specialBlocks.containsKey(type)) {
                specialBlocks.put(type, type.getDefaultType());
                changed = true;
            }
        }

        if (changed) {
            save(section);
        }
    }

    public void save(ConfigurationSection section) {
        rawSpecialBlocks.clear();
        for (Map.Entry<SpecialBlock, Material> entry : specialBlocks.entrySet()) {
            rawSpecialBlocks.put(entry.getKey().name(), entry.getValue().name());
        }

        super.save(section);
    }
}
