package com.zachsthings.liftplates;

import com.zachsthings.liftplates.util.IntPairKey;
import org.apache.commons.lang.Validate;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
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
    private BlockVector position;

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
     * Gets the current position of this lift
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

    // -- Motion methods

    /**
     * Return the blocks that will be moved with this elevator
     *
     * @return The blocks that this lift will move
     */
    public Set<Location> getBlocks() {
        Set<Location> blocks = new HashSet<Location>();
        Location location = position.toLocation(manager.getWorld());
        blocks.add(location);
        travelBlocks(location, location, blocks, new HashSet<Location>());
        return blocks;
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
    private void travelBlocks(Location start, Location current, Set<Location> validLocations, Set<Location> visited) {
        visited.add(current);

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

        for (int i = 1; i < config.liftHeight; ++i) {
            validLocations.add(current.clone().add(0, i, 0));
        }

        for (BlockFace face : LiftUtil.NSEW_FACES) {
            Location newLoc = LiftUtil.mod(current, face);
            if (visited.contains(newLoc)) {
                continue;
            }

            travelBlocks(start, newLoc, validLocations, visited);
        }
    }

    /**
     * Move the lift and all the blocks the lift is composed of in the given direction
     *
     * @param direction The direction to move in.
     * @return Whether the lift could be successfully moved.
     *      This will return false if the lift tries to move to an already occupied position.
     */
    public boolean move(BlockFace direction) {
        // Get blocks
        Map<Location, MaterialData> blockChanges = new TreeMap<Location, MaterialData>(LiftUtil.LOCATION_Y_COMPARE);
        Set<Location> blocks = getBlocks();
        Set<Long> chunks = new HashSet<Long>();

        // Move
        for (Location loc : blocks) {
            Block oldBlock = loc.getBlock();
            Location newLoc = LiftUtil.mod(loc, direction);
            Block newBlock = newLoc.getBlock();
            blockChanges.put(newLoc, oldBlock.getType().getNewData(oldBlock.getData()));
            if (!blockChanges.containsKey(loc)) {
                blockChanges.put(loc, new MaterialData(Material.AIR));
            }

            if (!newBlock.isEmpty() && !blocks.contains(newLoc)) {
                return false;
            }

            // Update the location of any lifts in the moving blocks
            // TODO: Update tile entity data (needs n.m.s code) and call LiftMoveEvent to allow other plugins to move their objects
            // This will need a more complete object to store block data (old location, new location, type, data, tile entity data)
            BlockVector vec = loc.toVector().toBlockVector();
            Lift testLift = manager.getLift(vec);
            if (testLift != null) {
                testLift.position = vec;
            }

            chunks.add(IntPairKey.key(oldBlock.getChunk().getX(), oldBlock.getChunk().getZ()));
        }

        for (long chunkCoord : chunks) {
            Chunk chunk = manager.getWorld().getChunkAt(IntPairKey.key1(chunkCoord), IntPairKey.key2(chunkCoord));
            for (Entity entity : chunk.getEntities()) {
                Location entLoc = entity.getLocation();
                if (blocks.contains(entLoc)) {
                    entity.teleport(entLoc.add(direction.getModX(),
                            direction.getModY(), direction.getModZ()), PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            }
        }

        for (Map.Entry<Location, MaterialData> entry : blockChanges.entrySet()) {
            Block block = entry.getKey().getBlock();
            block.setTypeIdAndData(entry.getValue().getItemTypeId(), entry.getValue().getData(), true);
        }

        return true;
    }

    // -- Dinnerconfig Serialization methods

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
