package com.zachsthings.liftplates.specialblock;

import com.zachsthings.liftplates.Lift;
import com.zachsthings.liftplates.LiftContents;
import com.zachsthings.liftplates.MoveResult;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 * This special block stops the lift util its associated pressure plate is retriggered
 */
public class StationSpecialBlock extends SpecialBlock {
    public StationSpecialBlock() {
        super("Station", Material.GOLD_BLOCK);
    }

    protected StationSpecialBlock(String name, Material type) {
        super(name, type);
    }

    @Override
    public MoveResult liftActed(Lift lift, LiftContents contents) {
        return new MoveResult(MoveResult.Type.STOP);
    }

    @Override
    public void plateTriggered(Lift lift, Block block) {
        if (lift.getPosition().getY() > block.getY()) {
            int diff = lift.getPosition().getY() - block.getY() - 1;
            LiftContents contents = lift.getContents();
            contents.move(BlockFace.DOWN, diff, true);
        } else if (lift.getPosition().getY() <= block.getY()) {
            int diff = block.getY() - lift.getPosition().getY() + 1;
            LiftContents contents = lift.getContents();
            contents.move(BlockFace.UP, diff, true);
        }
    }
}
