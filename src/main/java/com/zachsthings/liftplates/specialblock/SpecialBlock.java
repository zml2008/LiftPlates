package com.zachsthings.liftplates.specialblock;

import com.zachsthings.liftplates.Lift;
import com.zachsthings.liftplates.LiftContents;
import com.zachsthings.liftplates.MoveResult;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;

/**
 * A list of the built-in special blocks.
 * When choosing the default block type for a new special block, remember a few things:
 * <ul>
 *     <li>The block type should not be a common building material. Common building materials
 *     will often be placed next to lifts while building structures</li>
 *     <li>The block type should be easily obtainable if the special block has a basic effect,
 *     less easily obtainable (but never naturally unobtainable) if the effect is fancier</li>
 * </ul>
 * @author zml2008
 */
public abstract class SpecialBlock {
    private static final Map<String, SpecialBlock> BY_NAME = new HashMap<String, SpecialBlock>();

    /**
     * Stops the lift for 2 cycles
     */
    public static final SpecialBlock PAUSE = register(new DelaySpecialBlock(Material.IRON_BLOCK, 2));

    /**
     * This special block stops the lift util its associated pressure plate is retriggered
     */
    public static final SpecialBlock STATION = register(new StationSpecialBlock());

    /**
     * Prevents the lift from moving up beyond the current block
     */
    public static final SpecialBlock STOP_UP = register(new StopSpecialBlock(Lift.Direction.UP, Material.OBSIDIAN));

    /**
     * Prevents the lift from moving down beyond the current block
     */
    public static final SpecialBlock STOP_DOWN = register(new StopSpecialBlock(Lift.Direction.DOWN, Material.ICE));

    private final String name;
    private final Material type;

    public SpecialBlock(String name, Material type) {
        this.name = name;
        this.type = type;
    }

    public static <T extends SpecialBlock> T register(T block) {
        BY_NAME.put(block.getName().toLowerCase(), block);
        Bukkit.getServer().getPluginManager().callEvent(new SpecialBlockRegisterEvent(block));
        return block;
    }

    public String getName() {
        return name;
    }

    public Material getDefaultType() {
        return type;
    }

    public abstract MoveResult liftActed(Lift lift, LiftContents contents);

    /**
     * Called when a plate on top of {@code block} is triggered
     *
     * @param lift The nearest lift
     * @param block The block that is of this special block type
     */
    public void plateTriggered(Lift lift, Block block) {}

    public static SpecialBlock byName(String name) {
        Validate.notNull(name);
        return BY_NAME.get(name.toLowerCase());
    }

    public static Collection<SpecialBlock> getAll() {
        return Collections.unmodifiableCollection(BY_NAME.values());
    }
}
