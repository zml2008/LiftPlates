package ninja.leaping.liftplates.specialblock;

import ninja.leaping.liftplates.Lift;
import ninja.leaping.liftplates.LiftContents;
import ninja.leaping.liftplates.MoveResult;
import ninja.leaping.liftplates.LiftRunner;
import org.spongepowered.api.block.BlockType;

/**
 * This special block delays the elevator for the given amount of cycles ({@link LiftRunner#RUN_FREQUENCY} ticks)
 */
public class DelaySpecialBlock extends SpecialBlock {
    private final int cycles;
    public DelaySpecialBlock(BlockType type, int cycles) {
        super("Delay" + cycles, type);
        this.cycles = cycles;
    }

    @Override
    public MoveResult liftActed(Lift lift, LiftContents contents) {
        return new MoveResult(MoveResult.Type.DELAY, cycles);
    }
}
