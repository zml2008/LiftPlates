package ninja.leaping.liftplates.specialblock;

import ninja.leaping.liftplates.Lift;
import ninja.leaping.liftplates.LiftContents;
import ninja.leaping.liftplates.MoveResult;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.util.Direction;

/**
 * This special block prevents the lift from moving farther in the given direction
 */
public class StopSpecialBlock extends StationSpecialBlock {
    private final Direction direction;
    public StopSpecialBlock(Direction direction, BlockType type) {
        super("Stop" + direction, type);
        this.direction = direction;
    }

    @Override
    public MoveResult liftActed(Lift lift, LiftContents contents) {
        if (lift.getDirection() == direction) {
            return new MoveResult(MoveResult.Type.BLOCK);
        } else {
            return new MoveResult(MoveResult.Type.CONTINUE);
        }
    }
}
