package com.zachsthings.liftplates;

import com.zachsthings.liftplates.SpecialBlock;
import com.zachsthings.liftplates.util.BlockQueue;
import com.zachsthings.liftplates.util.IntPairKey;
import com.zachsthings.liftplates.util.Point;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.material.Button;
import org.bukkit.material.MaterialData;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The contents of a {@link Lift}. This can be calculated whenever the lift is moved.
 */
public class LiftContents {
    private final Lift lift;
    private final Set<SpecialBlock> specialBlocks;
    private final Set<Point> locations;
    private Set<Entity> entities;

    public LiftContents(Lift lift, Set<SpecialBlock> specialBlocks, Set<Point> locations, Set<Entity> entities) {
        this.lift = lift;
        this.specialBlocks = Collections.unmodifiableSet(specialBlocks);
        this.locations = Collections.unmodifiableSet(locations);
        this.entities = Collections.unmodifiableSet(entities);
    }

    public Set<SpecialBlock> getSpecialBlocks() {
        return specialBlocks;
    }

    public Set<Point> getBlocks() {
        return locations;
    }

    public Set<Entity> getEntities() {
        return entities;
    }

    public void update() {
        Set<Entity> entities = new HashSet<Entity>();
        Set<Long> chunks = new HashSet<Long>();
        for (Point loc : locations) {
            chunks.add(IntPairKey.key(loc.getX() >> 4, loc.getZ() >> 4));
        }
        for (long key : chunks) {
            Chunk chunk = lift.getManager().getWorld().getChunkAt(IntPairKey.key1(key), IntPairKey.key2(key));
            for (Entity entity : chunk.getEntities()) {
                if (locations.contains(new Point(entity.getLocation()))) {
                    entities.add(entity);
                }
            }
        }
        this.entities = Collections.unmodifiableSet(entities);
    }

    /**
     *
     * Moves the lift in its default direction
     *
     * @see #move(org.bukkit.block.BlockFace, int, boolean)
     * @param ignoreSpecialBlocks Whether special blocks should have an
     *     effect on the motion of the lift
     * @return Whether the motion was successful
     */
    public MoveResult move(boolean ignoreSpecialBlocks) {
        return move(lift.getDirection().getFace(), 1, ignoreSpecialBlocks);
    }

    /**
     * Move the elevator in its default direction not ignoring special blocks
     * @return The result of the elevator's motion attempt
     */
    public MoveResult move() {
        return move(false);
    }

    public MoveResult move(BlockFace face) {
        return move(face, 1, false);
    }

    public MoveResult move(BlockFace face, int count) {
        return move(face, count, false);
    }

    /**
     * Move the lift and all the blocks the lift is composed of in the given direction
     *
     * @param direction The direction to move in.
     * @return Whether the lift could be successfully moved.
     *      This will return false if the lift tries to move to an already occupied position.
     */
    public MoveResult move(BlockFace direction, int iterations, boolean ignoreSpecialBlocks) { // We might want to cache the data used in here for elevator trips. Will reduce server load
        if (iterations == 0) {
            return new MoveResult(MoveResult.Type.STOP);
        }
        MoveResult.Type type = MoveResult.Type.CONTINUE;
        int amount = 0;
        // Get blocks
        BlockQueue removeBlocks = new BlockQueue(lift.getManager().getWorld(), BlockQueue.BlockOrder.TOP_DOWN);
        BlockQueue addBlocks = new BlockQueue(lift.getManager().getWorld(), BlockQueue.BlockOrder.BOTTOM_UP) {
            @Override
            protected MaterialData modifyMaterialData(MaterialData input) {
                if (LiftUtil.isPressurePlate(input.getItemType())) {
                    input.setData((byte) 0x0); // Unpress any pressure plates -- these aren't updated correctly when the block is moved
                } else if (input.getItemType() == Material.STONE_BUTTON) {
                    ((Button) input).setPowered(false); // Same for buttons
                } else if (input.getItemType() == Material.REDSTONE_TORCH_OFF) {
                    input = Material.REDSTONE_TORCH_ON.getNewData(input.getData());
                }
                return super.modifyMaterialData(input);
            }
        };

        if (!ignoreSpecialBlocks) {
            for (SpecialBlock block : getSpecialBlocks()) {
                MoveResult result = block.liftActed(lift, this);
                if (result.getType().ordinal() > type.ordinal()) {
                    type = result.getType();
                }
                amount += result.getAmount();
            }
        }

        // Move
        for (Point loc : getBlocks()) {
            Block oldBlock = loc.getBlock(lift.getManager().getWorld());
            Point newLoc = loc.modify(direction, iterations);
            Block newBlock = newLoc.getBlock(lift.getManager().getWorld());

            if (!newBlock.isEmpty() && !getBlocks().contains(newLoc)) {
                type = MoveResult.Type.BLOCK;
                break;
            }

            addBlocks.set(newLoc, oldBlock.getType().getNewData(oldBlock.getData()));
            removeBlocks.set(loc, new MaterialData(Material.AIR));

        }

        if (type != MoveResult.Type.CONTINUE) {
            return new MoveResult(type, amount);
        }

        // Update the location of any lifts in the moving blocks
        // TODO: Update tile entity data (needs n.m.s code) and call LiftMoveEvent to allow other plugins to move their objects
        // This will need a more complete object to store block data (old location, new location, type, data, tile entity data)
        for (Point loc : getBlocks()) {
            Lift testLift = lift.getManager().getLift(loc);
            if (testLift != null) {
                testLift.setPosition(loc.modify(direction, iterations));
            }
        }

        removeBlocks.apply();
        for (Entity entity : getEntities()) {
            Location newLocation = entity.getLocation().add(direction.getModX() * iterations, 0, direction.getModZ() * iterations);
            if (!(entity instanceof LivingEntity) || direction.getModY() > 0 ) {
                newLocation.add(0, direction.getModY() * iterations, 0);
            }
            entity.teleport(newLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);

        }

        addBlocks.apply();
        lift.getManager().updateLiftLocations();

        return new MoveResult(MoveResult.Type.CONTINUE);
    }
}
