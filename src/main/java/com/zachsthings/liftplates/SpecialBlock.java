package com.zachsthings.liftplates;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;

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
     * Stops the lift for 2 cycles ({@link LiftPlatesState#RUN_FREQUENCY} ticks)
     */
    public static final SpecialBlock PAUSE = new SpecialBlock("Pause", Material.IRON_BLOCK) {
        @Override
        public MoveResult liftActed(LiftSnapshot lift) {
            return new MoveResult(MoveResult.Type.DELAY, 2);
        }
    };

    /**
     * This special block stops the lift util its associated pressure plate is retriggered
     */
    public static final SpecialBlock STATION = new SpecialBlock("Station", Material.GOLD_BLOCK) {
        @Override
        public MoveResult liftActed(LiftSnapshot lift) {
            return new MoveResult(MoveResult.Type.STOP);
        }
    };

    /**
     * Prevents the lift from moving up beyond the current block
     */
    public static final SpecialBlock STOP_UP = new SpecialBlock("StopUp", Material.OBSIDIAN) {
        @Override
        public MoveResult liftActed(LiftSnapshot lift) {
            if (lift.getLift().getDirection() == Lift.Direction.UP) {
                return new MoveResult(MoveResult.Type.BLOCK);
            } else {
                return new MoveResult(MoveResult.Type.CONTINUE);
            }
        }
    };

    /**
     * Prevents the lift from moving up beyond the current block
     */
    public static final SpecialBlock STOP_DOWN = new SpecialBlock("StopDown", Material.ICE) {
        @Override
        public MoveResult liftActed(LiftSnapshot lift) {
            if (lift.getLift().getDirection() == Lift.Direction.DOWN) {
                return new MoveResult(MoveResult.Type.BLOCK);
            } else {
                return new MoveResult(MoveResult.Type.CONTINUE);
            }
        }
    };

    private final String name;
    private final Material type;

    public SpecialBlock(String name, Material type) {
        this.name = name;
        this.type = type;
        BY_NAME.put(name.toLowerCase(), this);
    }

    public String getName() {
        return name;
    }

    public Material getDefaultType() {
        return type;
    }

    public abstract MoveResult liftActed(LiftSnapshot lift);

    public static SpecialBlock byName(String name) {
        Validate.notNull(name);
        return BY_NAME.get(name.toLowerCase());
    }

    public static Collection<SpecialBlock> getAll() {
        return Collections.unmodifiableCollection(BY_NAME.values());
    }
}
