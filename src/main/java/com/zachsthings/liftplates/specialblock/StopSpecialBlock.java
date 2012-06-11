package com.zachsthings.liftplates.specialblock;

import com.zachsthings.liftplates.Lift;
import com.zachsthings.liftplates.LiftContents;
import com.zachsthings.liftplates.MoveResult;
import org.bukkit.Material;

/**
 * This special block prevents the lift from moving farther in the given direction
 */
public class StopSpecialBlock extends StationSpecialBlock {
    private final Lift.Direction direction;
    public StopSpecialBlock(Lift.Direction direction, Material type) {
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
