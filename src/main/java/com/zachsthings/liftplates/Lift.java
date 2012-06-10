package com.zachsthings.liftplates;

import com.zachsthings.liftplates.util.BlockQueue;
import com.zachsthings.liftplates.util.IntPairKey;
import com.zachsthings.liftplates.util.Point;
import org.apache.commons.lang.Validate;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.material.Button;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author zml2008
 */
@SerializableAs("LiftPlates-Lift")
public class Lift implements ConfigurationSerializable {

    public static enum Direction {
        UP (BlockFace.UP),
        DOWN(BlockFace.DOWN),
        ;

        private final BlockFace face;

        private Direction(BlockFace face) {
            this.face = face;
        }

        /**
         * Gets the BlockFace this direction corresponds to. This can be used with {@link LiftContents#move(BlockFace, boolean)}.
         *
         * @return this direction's BlockFace.
         */
        public BlockFace getFace() {
            return face;
        }
    }
    /**
     * The LiftManager this Lift is located in. Required for a complete Lift object
     */
    private LiftManager manager;

    private Direction direction = Direction.UP;

    /**
     * The position of this lift (pressure plate) in the world
     */
    private Point position;

    public Lift(Point position) {
        this.position = position;
    }

    /**
     * Return which direction the attached lift will move when triggered by an attached
     * pressure plate
     *
     * @return The trigger direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Sets the direction this lift will travel in
     *
     * @param direction The lift's direction
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    /**
     * Gets the current position of this lift
     * @see #position
     * @return This lift's position
     */
    public Point getPosition() {
        return position;
    }

    void setPosition(Point position) {
        this.position = position;
    }

    /**
     * Set the manager attached to this Lift.
     *
     * @param manager The manager to set
     */
    protected void setManager(LiftManager manager) {
        Validate.notNull(manager);
        if (this.manager != null) {
            throw new IllegalStateException("A manager (" + manager + ") has already been set for the lift " + this);
        }
        this.manager = manager;
    }

    LiftManager getManager() {
        if (this.manager == null) {
            throw new IllegalStateException("This lift has not yet been attached!");
        }
        return this.manager;
    }

    // -- Motion methods

    /**
     * Return the blocks that will be moved with this elevator
     *
     * @return The blocks that this lift will move
     */
    public LiftContents getContents() {
        Set<Point> blocks = new HashSet<Point>();
        Set<SpecialBlock> specialBlocks = new HashSet<SpecialBlock>();
        Point location = position.setY(position.getY() - 1);
        travelBlocks(location, location, blocks, new HashSet<Point>(), specialBlocks);

        Set<Entity> entities = new HashSet<Entity>();
        Set<Long> chunks = new HashSet<Long>();
        for (Point loc : blocks) {
            chunks.add(IntPairKey.key(loc.getX() >> 4, loc.getZ() >> 4));
        }

        for (long chunkCoord : chunks) {
            Chunk chunk = manager.getWorld().getChunkAt(IntPairKey.key1(chunkCoord), IntPairKey.key2(chunkCoord));
            for (Entity entity : chunk.getEntities()) {
                Location entLoc = entity.getLocation();
                if (blocks.contains(new Point(entLoc))) {
                    entities.add(entity);
                }
            }
        }

        return new LiftContents(this, specialBlocks, blocks, entities);
    }

    /**
     * Go through the blocks, checking for valid ones
     * The way this works is:
     * <ul>
     *     <li>Adds the current location to the list of valid locations</li>
     *     <li>Checks if the current block is within {@link LiftPlatesConfig#maxLiftSize}</li>
     *     <li>Makes sure the current block is of the same type and data as the central block</li>
     *     <li>If large lifts are not enabled in the configuration, also checks that the block above is a pressureplate</li>
     *     (If any of the previous conditions are not met, the method does not continue)
     *     <li>Adds the current location to the list of valid locations</li>
     *     <li>Runs the method on {@link LiftUtil#NSEW_FACES}, excluding locations that have already been visited, with the same sets of valid and visited locations</li>
     * </ul>
     *
     * @param start The origin block
     * @param current The current block
     * @param validLocations The list of already travelled and valid locations
     * @param visited The list of already travelled (not necessarily valid) locations
     */
    private void travelBlocks(Point start, Point current, Set<Point> validLocations, Set<Point> visited, Set<SpecialBlock> specialBlocks) {
        visited.add(current);

        LiftPlatesConfig config = manager.getPlugin().getConfiguration();
        final int maxDist = config.maxLiftSize * config.maxLiftSize;
        if (start.distanceSquared(current) > maxDist) { // Too far away
            SpecialBlock block = getSpecialBlock(current.getBlock(manager.getWorld()).getType());
            if (block != null) {
                specialBlocks.add(block);
            }
            return;
        }

        Block currentBlock = current.getBlock(manager.getWorld());
        Block startBlock = start.getBlock(manager.getWorld());
        if (currentBlock.getTypeId() != startBlock.getTypeId()
                || currentBlock.getData() != startBlock.getData()) { // Different block type
            SpecialBlock block = getSpecialBlock(currentBlock.getType());
            if (block != null) {
                specialBlocks.add(block);
            }
            return;
        }

        if (!config.recursiveLifts
                && !LiftUtil.isPressurePlate(current.modify(BlockFace.UP).getBlock(manager.getWorld()).getType())) { // Not a pressure plate
            SpecialBlock block = getSpecialBlock(currentBlock.getType());
            if (block != null) {
                specialBlocks.add(block);
            }
            return;
        }

        validLocations.add(current);

        for (int i = 1; i < config.liftHeight; ++i) {
            validLocations.add(current.setY(current.getY() + i));
        }

        for (BlockFace face : LiftUtil.NSEW_FACES) {
            Point newLoc = current.modify(face);
            if (visited.contains(newLoc)) {
                continue;
            }

            travelBlocks(start, newLoc, validLocations, visited, specialBlocks);
        }
    }

    public SpecialBlock getSpecialBlock(Material mat) {
        SpecialBlock block = manager.getPlugin().getConfiguration().specialBlocks.get(mat);
        // TODO: Per-lift special block overrides
        return block;
    }

    // -- Dinnerconfig Serialization methods

    public Map<String, Object> serialize() {
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("position", position);
        ret.put("direction", direction.name());
        return ret;
    }

    public static Lift deserialize(Map<String, Object> data) {
        Point position = (Point) data.get("position");
        Direction direction = Direction.valueOf(String.valueOf(data.get("direction")));
        Lift lift = new Lift(position);
        lift.direction = direction;
        return lift;
    }
}
