package ninja.leaping.liftplates.specialblock;

import com.flowpowered.math.vector.Vector3i;
import ninja.leaping.liftplates.Lift;
import ninja.leaping.liftplates.LiftContents;
import ninja.leaping.liftplates.LiftRunner;
import ninja.leaping.liftplates.MoveResult;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.service.scheduler.Task;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

import java.util.HashSet;
import java.util.Set;

/**
 * This special block stops the lift util its associated pressure plate is re-triggered
 */
public class StationSpecialBlock extends SpecialBlock {
    private final Set<Location> activeBlocks = new HashSet<Location>();
    public StationSpecialBlock() {
        super("Station", BlockTypes.GOLD_BLOCK);
    }

    protected StationSpecialBlock(String name, BlockType type) {
        super(name, type);
    }

    @Override
    public MoveResult liftActed(Lift lift, LiftContents contents) {
        return new MoveResult(MoveResult.Type.STOP);
    }

    @Override
    public void plateTriggered(Lift lift, Location block) {
        if (!activeBlocks.contains(block)) {
            activeBlocks.add(block);
            CallLift call = new CallLift(block, lift);
            call.task = lift.getPlugin().getGame().getScheduler().getTaskBuilder()
                    .execute(call)
                    .interval(LiftRunner.RUN_FREQUENCY)
                    .submit(lift.getPlugin());
        }
    }

    private class CallLift implements Runnable {
        private Task task;
        private final Direction direction;
        private final Location target;
        private final Lift lift;
        private Vector3i nearestLiftBlock;

        public CallLift(Location target, Lift lift) {
            this.target = target;
            this.lift = lift;
            LiftContents contents = lift.getContents();

            Vector3i blockLoc = target.getBlockPosition();
            final int requiredY = lift.getPosition().getY() - 1;
            Vector3i nearestLoc = null;
            int distance = Integer.MAX_VALUE;

            for (Vector3i loc : contents.getBlocks()) {
                if (loc.getY() == requiredY) {
                    if (loc.distanceSquared(blockLoc) < distance) {
                        nearestLoc = loc;
                    }
                }
            }

            if (nearestLoc == null) {
                throw new IllegalStateException("No nearest location found from the lift!");
            }
            this.nearestLiftBlock = nearestLoc;

            Direction liftDirection = lift.getDirection();

            // Calculate the distances to travel and restrict them to directions the lift can move in normal operation
            Vector3i delta = blockLoc.sub(nearestLoc).mul(liftDirection.toVector3d().toInt().abs());
            delta.div(delta.getX() == 0 ? 1 : Math.abs(delta.getX()),
                    delta.getY() == 0 ? 1 : Math.abs(delta.getY()),
                    delta.getZ() == 0 ? 1 : Math.abs(delta.getZ()));

            Direction moveFace = Direction.getClosest(delta.toDouble()); // TODO: Does this get what I want?
            /*for (Direction face : Direction.values()) {
                if (face.toVector3d().toInt().equals(delta)) {
                    moveFace = face;
                    break;
                }
            }*/


            if (moveFace == null) {
                throw new IllegalArgumentException("No BlockFace for direction that lift is supposed to move (" + delta + "!");
            } else if (moveFace == Direction.NONE) {

            }
            direction = moveFace;
        }

        public void run() {
            lift.getPlugin().getLiftRunner().stopLift(lift);
            final Vector3i blockLoc = target.getBlockPosition();
            LiftContents contents = lift.getContents();
            final int requiredY = lift.getPosition().getY() - 1;
            Vector3i nearestLoc = null;
            int distance = Integer.MAX_VALUE;

            for (Vector3i loc : contents.getBlocks()) {
                if (loc.getY() == requiredY) {
                    if (loc.distanceSquared(blockLoc) < distance) {
                        nearestLoc = loc;
                    }
                }
            }

            if (nearestLoc == null) {
                throw new IllegalStateException("No nearest location found from the lift!");
            }

            this.nearestLiftBlock = nearestLoc;
            final Vector3i delta = nearestLiftBlock.sub(blockLoc).mul(direction.toVector3d().toInt());

            contents.update();
            MoveResult result = delta.lengthSquared() == 0 ? new MoveResult(MoveResult.Type.STOP) :
                    contents.move(direction, true);
            if ((result.getType() == MoveResult.Type.STOP
                    || result.getType() == MoveResult.Type.BLOCK) && this.task != null) {
                task.cancel();
                this.task = null;
                activeBlocks.remove(target);
            }
        }
    }
}
