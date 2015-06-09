package com.zachsthings.liftplates.specialblock;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.zachsthings.liftplates.Lift;
import com.zachsthings.liftplates.LiftContents;
import com.zachsthings.liftplates.MoveResult;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

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

    private final String name;
    private final BlockType type;

    public SpecialBlock(String name, BlockType type) {
        this.name = name;
        this.type = type;
    }

    public static <T extends SpecialBlock> T register(T block) {
        BY_NAME.put(block.getName().toLowerCase(), block);
        Map<String, Object> map = Maps.newHashMap();
        map.put("registeredBlock", block);
        SpecialBlockRegisterEvent event = SpongeEventFactory.createEvent(SpecialBlockRegisterEvent.class, map);
        //Bukkit.getServer().getPluginManager().callEvent(new SpecialBlockRegisterEvent(block)); // TODO: Events
        return block;
    }

    public String getName() {
        return name;
    }

    public BlockType getDefaultType() {
        return type;
    }

    public abstract MoveResult liftActed(Lift lift, LiftContents contents);

    /**
     * Called when a plate on top of {@code block} is triggered
     *
     * @param lift The nearest lift
     * @param block The block that is of this special block type
     */
    public void plateTriggered(Lift lift, Location block) {}

    public static SpecialBlock byName(String name) {
        Preconditions.checkNotNull(name, "name");
        return BY_NAME.get(name.toLowerCase());
    }

    public static Collection<SpecialBlock> getAll() {
        return Collections.unmodifiableCollection(BY_NAME.values());
    }

    public static void registerDefaults() {
        // Stops the lift for 2 cycles
        register(new DelaySpecialBlock(BlockTypes.IRON_BLOCK, 2));
        // This special block stops the lift util its associated pressure plate is retriggered
        register(new StationSpecialBlock());
        // Prevents the lift from moving up beyond the current block
        register(new StopSpecialBlock(Direction.UP, BlockTypes.OBSIDIAN));
        // Prevents the lift from moving down beyond the current block
        register(new StopSpecialBlock(Direction.DOWN, BlockTypes.ICE));
    }
}
