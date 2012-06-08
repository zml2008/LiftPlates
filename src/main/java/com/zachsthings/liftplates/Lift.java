package com.zachsthings.liftplates;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.material.MaterialData;
import org.bukkit.util.BlockVector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author zml2008
 */
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
         * Gets the BlockFace this direction corresponds to. This can be used with {@link Lift#move(BlockFace)}.
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
    private final BlockVector position;

    public Lift(BlockVector position) {
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
     * @see #position
     * @return This lift's position
     */
    public BlockVector getPosition() {
        return position;
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

    // - Motion methods

    /**
     * Return the blocks that will be moved with this elevator
     * @return The blocks that this lift will move
     */
    public Set<Location> getBlocks() {
        Set<Location> blocks = new HashSet<Location>();
        Location location = position.toLocation(manager.getWorld());
        blocks.add(location);
        travelBlocks(location, location, blocks);
        return blocks;
    }

    private void travelBlocks(Location start, Location current, Set<Location> validLocations) {
        LiftPlatesConfig config = manager.getPlugin().getConfiguration();
        final int maxDist = config.maxLiftSize * config.maxLiftSize;
        if (start.distanceSquared(current) > maxDist) { // Too far away
            return;
        }

        Block currentBlock = current.getBlock();
        Block startBlock = start.getBlock();
        if (currentBlock.getTypeId() != startBlock.getTypeId()
                || currentBlock.getData() != startBlock.getData()) { // Different block type
            return;
        }

        if (!config.recursiveLifts && !LiftUtil.isPressurePlate(LiftUtil.mod(current, BlockFace.UP).getBlock().getType())) { // Not a pressure plate
            return;
        }

        validLocations.add(current);

        for (BlockFace face : LiftUtil.NSEW_FACES) {
            Location newLoc = LiftUtil.mod(current, face);
            if (validLocations.contains(newLoc)) {
                continue;
            }

            travelBlocks(start, newLoc, validLocations);
        }
    }

    /**
     * Move the lift and all the blocks the lift is composed of in the given direction
     *
     * @param direction The direction to move in
     */
    public void move(BlockFace direction) {
        Map<Location, MaterialData> blockChanges = new TreeMap<Location, MaterialData>(LiftUtil.LOCATION_Y_COMPARE);
        Set<Location> blocks = getBlocks();
        for (Location loc : blocks) {
            Block oldBlock = loc.getBlock();
            Location newLoc = LiftUtil.mod(loc, direction);
            blockChanges.put(newLoc, oldBlock.getType().getNewData(oldBlock.getData()));
            if (!blockChanges.containsKey(loc)) {
                blockChanges.put(loc, new MaterialData(Material.AIR));
            }
        }

        for (Map.Entry<Location, MaterialData> entry : blockChanges.entrySet()) {
            Block block = entry.getKey().getBlock();
            block.setTypeIdAndData(entry.getValue().getItemTypeId(), entry.getValue().getData(), true);
        }
    }

    // - Dinnerconfig Serialization methods

    public Map<String, Object> serialize() {
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("position", position);
        ret.put("direction", direction.name());
        return ret;
    }

    private static Lift deserialize(Map<String, Object> data) {
        BlockVector position = (BlockVector) data.get("position");
        Direction direction = Direction.valueOf(String.valueOf(data.get("direction")));
        Lift lift = new Lift(position);
        lift.direction = direction;
        return lift;
    }
}
