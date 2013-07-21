package com.zachsthings.liftplates;

import com.zachsthings.liftplates.config.ConfigurationBase;
import com.zachsthings.liftplates.config.Setting;
import com.zachsthings.liftplates.specialblock.SpecialBlock;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author zml2008
 */
public class LiftPlatesConfig extends ConfigurationBase implements Listener {
    /**
     * Whether to treat a bunch of pressure plate lifts next to each other
     * with the same base block type as the same lift
     */
    @Setting("recursive-lifts") public boolean recursiveLifts = true;
    /**
     * Allow configuration of which block types that have special functionality
     */
    @Setting("special-blocks") private Map<String, String> rawSpecialBlocks = new HashMap<String, String>();

    /**
     * The maximum distance from the triggered pressure plate to look for blocks of the same type
     */
    @Setting("max-lift-size") public int maxLiftSize = 5;

    /**
     * How many blocks tall the lift should be. Setting to below 2 will prevent lifts from functioning,
     * since pressure plates will not be considered part of the lift when trying to move the lift.
     */
    @Setting("lift-height") public int liftHeight = 2;

    public Map<Material, SpecialBlock> specialBlocks = new HashMap<Material, SpecialBlock>();
    Set<SpecialBlock> storedSpecialBlocks;

    public void load(ConfigurationSection section) {
        super.load(section);

        storedSpecialBlocks = new HashSet<SpecialBlock>();

        for (Map.Entry<String, String> entry : rawSpecialBlocks.entrySet()) {
            SpecialBlock block = SpecialBlock.byName(entry.getKey());
            Material mat = entry.getValue() == null ? null : Material.matchMaterial(entry.getValue());
            if (block != null) {
                if (mat != null) specialBlocks.put(mat, block);
                storedSpecialBlocks.add(block);
            }
        }

        boolean changed = false;
        for (SpecialBlock type : SpecialBlock.getAll()) {
            if (!storedSpecialBlocks.contains(type)) {
                specialBlocks.put(type.getDefaultType(), type);
                storedSpecialBlocks.add(type);
                changed = true;
            }
        }

        if (changed) {
            save(section);
        }
    }

    public void save(ConfigurationSection section) {
        rawSpecialBlocks.clear();
        for (Map.Entry<Material, SpecialBlock> entry : specialBlocks.entrySet()) {
            rawSpecialBlocks.put(entry.getValue().getName(), entry.getKey().name());
        }

        super.save(section);
    }
}
