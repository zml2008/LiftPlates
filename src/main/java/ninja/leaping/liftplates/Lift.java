package ninja.leaping.liftplates;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import ninja.leaping.liftplates.specialblock.SpecialBlock;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.World;

import java.util.HashSet;
import java.util.Set;

/**
 * @author zml2008
 */
@ConfigSerializable
public class Lift {
    public static final ObjectMapper<Lift> MAPPER;

    static {
        try {
            MAPPER = ObjectMapper.forClass(Lift.class);
        } catch (ObjectMappingException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * The LiftManager this Lift is located in. Required for a complete Lift object
     */
    private LiftManager manager;

    @Setting
    private Direction direction = Direction.UP;

    /**
     * The position of this lift (pressure plate) in the world
     */
    @Setting
    private Vector3i position;

    public Lift() {
    }

    public Lift(Vector3i position) {
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
    public Vector3i getPosition() {
        return position;
    }

    void setPosition(Vector3i position) {
        this.position = position;
    }

    /**
     * Set the manager attached to this Lift.
     *
     * @param manager The manager to set
     */
    protected void setManager(LiftManager manager) {
        Preconditions.checkNotNull(manager, "manager");
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

    public LiftPlatesPlugin getPlugin() {
        return getManager().getPlugin();
    }

    /**
     * Return the blocks that will be moved with this elevator
     *
     * @return The blocks that this lift will move
     */
    public LiftContents getContents() {
        Set<Vector3i> blocks = new HashSet<Vector3i>();
        Set<Vector3i> edgeBlocks = new HashSet<Vector3i>();
        Vector3i location = position.add(0, -1, 0);
        travelBlocks(location, location, blocks, new HashSet<Vector3i>(), edgeBlocks);

        return new LiftContents(this, edgeBlocks, blocks);
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
    private void travelBlocks(Vector3i start, Vector3i current, Set<Vector3i> validLocations, Set<Vector3i> visited, Set<Vector3i> edgeBlocks) {
        visited.add(current);

        LiftPlatesConfig config = manager.getPlugin().getConfiguration();
        final int maxDist = config.maxLiftSize * config.maxLiftSize;
        if (start.distanceSquared(current) > maxDist) { // Too far away
            edgeBlocks.add(current);
            return;
        }

        World world = manager.getWorld();
        world.getFullBlock(start).getRelative(Direction.UP);
        if (!world.getBlock(start).equals(world.getBlock(current))) { // Different block type
            edgeBlocks.add(current);
            return;
        }

        if (!config.recursiveLifts
                && !LiftUtil.isPressurePlate(world.getBlockType(current.add(0, 1, 0)))) { // Not a pressure plate
            edgeBlocks.add(current);
            return;
        }

        validLocations.add(current);

        for (int i = 1; i < config.liftHeight; ++i) {
            validLocations.add(current.add(0, i, 0));
        }

        for (Direction face : LiftUtil.NSEW_FACES) {
            Vector3i newLoc = current.add(face.toVector3d().toInt());
            if (visited.contains(newLoc)) {
                continue;
            }

            travelBlocks(start, newLoc, validLocations, visited, edgeBlocks);
        }
    }

    public SpecialBlock getSpecialBlock(BlockType mat) {
        SpecialBlock block = manager.getPlugin().getConfiguration().specialBlocks.get(mat);
        // TODO: Per-lift special block overrides
        return block;
    }
}
