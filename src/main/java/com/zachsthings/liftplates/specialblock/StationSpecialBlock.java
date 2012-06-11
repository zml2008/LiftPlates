package com.zachsthings.liftplates.specialblock;

import com.zachsthings.liftplates.Lift;
import com.zachsthings.liftplates.LiftContents;
import com.zachsthings.liftplates.MoveResult;
import org.bukkit.Bukkit;
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
        CallLift call = new CallLift(block, lift);
        call.taskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(
                Bukkit.getServer().getPluginManager().getPlugin("LiftPlates"),
                call, 0, CallLift.RUN_FREQUENCY);
    }

    private static class CallLift implements Runnable {
        public static final long RUN_FREQUENCY = 2;
        private int taskId;
        private final BlockFace direction;
        private final Block target;
        private final Lift lift;
        private int diff;
        private LiftContents contents;

        public CallLift(Block target, Lift lift) {
            this.target = target;
            this.lift = lift;
            if (lift.getPosition().getY() > target.getY()) {
                direction = BlockFace.DOWN;
                diff = lift.getPosition().getY() - target.getY() - 1;
            } else if (lift.getPosition().getY() <= target.getY()) {
                direction = BlockFace.UP;
                diff = target.getY() - lift.getPosition().getY() + 1;
            } else {
                direction = null;
                diff = 0;
            }
            this.contents = lift.getContents();
        }

        public void run() {
            contents.update();
            contents.move(direction, true);
            if (--diff == 0) {
                Bukkit.getServer().getScheduler().cancelTask(taskId);
            }
        }
    }
}
