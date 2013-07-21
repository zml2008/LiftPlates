package com.zachsthings.liftplates;

import com.zachsthings.liftplates.specialblock.SpecialBlock;
import com.zachsthings.liftplates.util.BlockQueue;
import com.zachsthings.liftplates.util.IntPairKey;
import com.zachsthings.liftplates.util.NMSTileEntityInterface;
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
    private Set<SpecialBlock> specialBlocks;
    private Set<Point> locations;
    private Set<Entity> entities;
    private Set<Point> edgeBlocks;
    private Set<Lift> lifts;

    public LiftContents(Lift lift, Set<Point> edgeBlocks, Set<Point> locations) {
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

    public Set<Point> getEdgeBlocks() {
        return edgeBlocks;
    }

    public Set<Point> getBlocks() {
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

        Set<SpecialBlock> specialBlocks = new HashSet<SpecialBlock>();

        for (Point loc : getEdgeBlocks()) {
            SpecialBlock block = lift.getSpecialBlock(loc.getBlock(lift.getManager().getWorld()).getType());
            if (block != null) {
                specialBlocks.add(block);
            }
        }
        this.specialBlocks = Collections.unmodifiableSet(specialBlocks);

        Set<Lift> lifts = new HashSet<Lift>();
        for (Point loc : getBlocks()) {
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
     * @see #move(org.bukkit.block.BlockFace, boolean)
     * @param ignoreSpecialBlocks Whether special blocks should have an
     *     effect on the motion of the lift
     * @return Whether the motion was successful
     */
    public MoveResult move(boolean ignoreSpecialBlocks) {
        return move(lift.getDirection().getFace(), ignoreSpecialBlocks);
    }

    /**
     * Move the elevator in its default direction not ignoring special blocks
     * @return The result of the elevator's motion attempt
     */
    public MoveResult move() {
        return move(false);
    }

    public MoveResult move(BlockFace face) {
        return move(face, false);
    }

    /**
     * Move the lift and all the blocks the lift is composed of in the given direction
     *
     * @param direction The direction to move in.
     * @return Whether the lift could be successfully moved.
     *      This will return false if the lift tries to move to an already occupied position.
     */
    public MoveResult move(BlockFace direction, boolean ignoreSpecialBlocks) { // We might want to cache the data used in here for elevator trips. Will reduce server load
        if (this.entities == null) {
            this.update();
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

        Set<Point> locations = new HashSet<Point>();

        // Move
        for (Point loc : getBlocks()) {
            Block oldBlock = loc.getBlock(lift.getManager().getWorld());
			NMSTileEntityInterface.TEntWrapper tentData = NMSTileEntityInterface.getData(oldBlock);
            Point newLoc = loc.modify(direction);
            locations.add(newLoc);
            Block newBlock = newLoc.getBlock(lift.getManager().getWorld());

            if (!newBlock.isEmpty() && !getBlocks().contains(newLoc)) {
                type = MoveResult.Type.BLOCK;
                break;
            }

            addBlocks.set(newLoc, oldBlock.getType().getNewData(oldBlock.getData()), tentData);
            removeBlocks.set(loc, new MaterialData(Material.AIR));

        }

        if (type != MoveResult.Type.CONTINUE) {
            return new MoveResult(type, amount);
        }

        // Update the location of any lifts in the moving blocks
        // TODO: Update tile entity data (needs n.m.s code) and call LiftMoveEvent to allow other plugins to move their objects
        // This will need a more complete object to store block data (old location, new location, type, data, tile entity data)
        for (Lift lift : getLifts()) {
            lift.setPosition(lift.getPosition().modify(direction));
        }

        removeBlocks.apply();
        for (Entity entity : getEntities()) {
            Location newLocation = entity.getLocation().add(direction.getModX(), 0, direction.getModZ() );
            if (!(entity instanceof LivingEntity) || direction.getModY() > 0 ) {
                newLocation.add(0, direction.getModY(), 0);
            }
            entity.teleport(newLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);

        }

        addBlocks.apply();
        lift.getManager().updateLiftLocations();

        this.locations = Collections.unmodifiableSet(locations);
        Set<Point> edgeBlocks = new HashSet<Point>();
        for (Point edgeBlock : getEdgeBlocks()) {
            edgeBlocks.add(edgeBlock.modify(direction));
        }
        this.edgeBlocks = Collections.unmodifiableSet(edgeBlocks);

        return new MoveResult(type, amount);
    }
}
