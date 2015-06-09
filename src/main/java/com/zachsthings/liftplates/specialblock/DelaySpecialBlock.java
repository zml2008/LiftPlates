package com.zachsthings.liftplates.specialblock;

import com.zachsthings.liftplates.Lift;
import com.zachsthings.liftplates.LiftContents;
import com.zachsthings.liftplates.MoveResult;
import org.spongepowered.api.block.BlockType;

/**
 * This special block delays the elevator for the given amount of cycles ({@link com.zachsthings.liftplates.LiftRunner#RUN_FREQUENCY} ticks)
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
