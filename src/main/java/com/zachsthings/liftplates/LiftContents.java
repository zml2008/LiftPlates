package com.zachsthings.liftplates;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Optional;
import com.zachsthings.liftplates.specialblock.SpecialBlock;
import com.zachsthings.liftplates.util.BlockQueue;
import com.zachsthings.liftplates.util.IntPairKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.manipulator.block.PoweredData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The contents of a {@link Lift}. This can be calculated whenever the lift is moved.
 */
public class LiftContents {
    private final Lift lift;
    private Set<SpecialBlock> specialBlocks;
    private Set<Vector3i> locations;
    private Set<Entity> entities;
    private Set<Vector3i> edgeBlocks;
    private Set<Lift> lifts;

    public LiftContents(Lift lift, Set<Vector3i> edgeBlocks, Set<Vector3i> locations) {
        this.lift = lift;
        this.edgeBlocks = Collections.unmodifiableSet(edgeBlocks);
        this.locations = Collections.unmodifiableSet(locations);
    }

    public Set<SpecialBlock> getSpecialBlocks() {
        if (this.specialBlocks == null) {
            update();
        }
        return specialBlocks;
    }

    public Set<Vector3i> getEdgeBlocks() {
        return edgeBlocks;
    }

    public Set<Vector3i> getBlocks() {
        return locations;
    }

    public Set<Lift> getLifts() {
        if (this.lifts == null) {
            update();
        }
        return this.lifts;
    }

    public Set<Entity> getEntities() {
        if (this.entities == null) {
            update();
        }
        return entities;
    }

    public void update() {
        Set<Entity> entities = new HashSet<Entity>();
        Set<Long> chunks = new HashSet<Long>();
        for (Vector3i loc : locations) {
            chunks.add(IntPairKey.key(loc.getX() >> 4, loc.getZ() >> 4));
        }
        for (long key : chunks) {
            Optional<Chunk> chunk = lift.getManager().getWorld().getChunk(IntPairKey.key1(key), 0, IntPairKey.key2(key));
            if (chunk.isPresent()) {
                for (Entity entity : chunk.get().getEntities()) {
                    if (locations.contains(entity.getLocation().getBlockPosition())) {
                        entities.add(entity);
                    }
                }
            }
        }
        this.entities = Collections.unmodifiableSet(entities);

        Set<SpecialBlock> specialBlocks = new HashSet<SpecialBlock>();

        for (Vector3i loc : getEdgeBlocks()) {
            SpecialBlock block = lift.getSpecialBlock(lift.getManager().getWorld().getBlockType(loc));
            if (block != null) {
                specialBlocks.add(block);
            }
        }
        this.specialBlocks = Collections.unmodifiableSet(specialBlocks);

        Set<Lift> lifts = new HashSet<Lift>();
        for (Vector3i loc : getBlocks()) {
            Lift testLift = lift.getManager().getLift(loc);
            if (testLift != null) {
                lifts.add(testLift);
            }
        }

        this.lifts = Collections.unmodifiableSet(lifts);
    }

    /**
     *
     * Moves the lift in its default direction
     *
     * @see #move(org.spongepowered.api.util.Direction, boolean)
     * @param ignoreSpecialBlocks Whether special blocks should have an
     *     effect on the motion of the lift
     * @return Whether the motion was successful
     */
    public MoveResult move(boolean ignoreSpecialBlocks) {
        return move(lift.getDirection(), ignoreSpecialBlocks);
    }

    /**
     * Move the elevator in its default direction not ignoring special blocks
     * @return The result of the elevator's motion attempt
     */
    public MoveResult move() {
        return move(false);
    }

    public MoveResult move(Direction face) {
        return move(face, false);
    }

    /**
     * Move the lift and all the blocks the lift is composed of in the given direction
     *
     * @param direction The direction to move in.
     * @return Whether the lift could be successfully moved.
     *      This will return false if the lift tries to move to an already occupied position.
     */
    public MoveResult move(Direction direction, boolean ignoreSpecialBlocks) { // We might want to cache the data used in here for elevator trips. Will
    // reduce server load
        if (this.entities == null) {
            this.update();
        }
        MoveResult.Type type = MoveResult.Type.CONTINUE;
        int amount = 0;
        // Get blocks
        BlockQueue removeBlocks = new BlockQueue(lift.getManager().getWorld(), BlockQueue.BlockOrder.TOP_DOWN);
        BlockQueue addBlocks = new BlockQueue(lift.getManager().getWorld(), BlockQueue.BlockOrder.BOTTOM_UP) {
            @Override
            protected BlockState modifyBlockState(BlockState input) {
                if (LiftUtil.isPressurePlate(input.getType())
                        || input.getType() == BlockTypes.STONE_BUTTON
                        || input.getType() == BlockTypes.WOODEN_BUTTON) {
                    input = input.withoutData(PoweredData.class).get();
                } else if (input.getType() == BlockTypes.REDSTONE_TORCH) {
                    input = input.withData(lift.getPlugin().getGame().getRegistry().getManipulatorRegistry().getBuilder(PoweredData.class).get()
                            .create()).get();
                }
                return super.modifyBlockState(input);
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

        Set<Vector3i> locations = new HashSet<Vector3i>();

        // Move
        for (Vector3i loc : getBlocks()) {
            BlockState oldBlock = lift.getManager().getWorld().getBlock(loc);
            Vector3i newLoc = loc.add(direction.toVector3d().toInt());
            locations.add(newLoc);
            BlockType newBlock = lift.getManager().getWorld().getBlockType(newLoc);

            if (newBlock != BlockTypes.AIR && !getBlocks().contains(newLoc)) {
                type = MoveResult.Type.BLOCK;
                break;
            }

            addBlocks.set(newLoc, oldBlock);
            removeBlocks.set(loc, BlockTypes.AIR.getDefaultState());

        }

        if (type != MoveResult.Type.CONTINUE) {
            return new MoveResult(type, amount);
        }

        // Update the location of any lifts in the moving blocks
        // TODO: Call LiftMoveEvent to allow other plugins to move their objects
        for (Lift lift : getLifts()) {
            lift.setPosition(lift.getPosition().add(direction.toVector3d().toInt()));
        }

        removeBlocks.apply();
        for (Entity entity : getEntities()) {
            Location newLocation = entity.getLocation().add(direction.toVector3d().getX(), Math.max(-0.5, direction.toVector3d().getY()), direction
                    .toVector3d().getZ());
            //if (!(entity instanceof LivingEntity) || direction.getModY() > 0 ) {
            //    newLocation.add(0, Math.max(-0.5, direction.getModY()), 0);
            //}
            entity.setLocation(newLocation);

        }

        addBlocks.apply();
        lift.getManager().updateLiftLocations();

        this.locations = Collections.unmodifiableSet(locations);

        Set<Vector3i> edgeBlocks = new HashSet<Vector3i>();
        for (Vector3i edgeBlock : getEdgeBlocks()) {
            edgeBlocks.add(edgeBlock.add(direction.toVector3d().toInt()));
        }
        this.edgeBlocks = Collections.unmodifiableSet(edgeBlocks);

        return new MoveResult(type, amount);
    }
}
