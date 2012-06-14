package com.zachsthings.liftplates;

import com.zachsthings.liftplates.specialblock.SpecialBlock;
import com.zachsthings.liftplates.util.WorldPoint;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author zml2008
 */
public class LiftRunner implements Runnable {
    public static final long RUN_FREQUENCY = 10;
    private final LiftPlatesPlugin plugin;
    private final Map<Lift, LiftState> movingLifts = new HashMap<Lift, LiftState>();
    private Set<WorldPoint> triggeredPoints = new LinkedHashSet<WorldPoint>();

    public LiftRunner(LiftPlatesPlugin plugin) {
        this.plugin = plugin;
    }

    public void plateTriggered(WorldPoint loc) {
        triggeredPoints.add(loc);
    }

    private static class LiftState {
        public int delay = 1;
        public boolean specialBlocksTriggered = true;
        public LiftContents contents;
        public boolean playerCaused = false;
    }

    public void run() {
        for (Iterator<WorldPoint> i = triggeredPoints.iterator(); i.hasNext();) {
            WorldPoint point = i.next();
            if (point.getBlock().getBlockPower() == 0) {
                i.remove();
                continue;
            }
            Lift lift = plugin.getLiftManager(point.getWorld()).getLift(point.toPoint());
            if (lift != null) {
                if (!movingLifts.containsKey(lift)) {
                    LiftState liftState = new LiftState();
                    movingLifts.put(lift, liftState);
                } else {
                    LiftState liftState = movingLifts.get(lift);
                    if (liftState.delay == -1) {
                        liftState.delay = 1;
                    }
                }
            } else {
                lift = plugin.detectLift(point, true);
                if (lift != null) {
                    Block bukkitBlock = point.getBlock().getRelative(BlockFace.DOWN);
                    SpecialBlock block = lift.getSpecialBlock(bukkitBlock.getType());
                    if (block != null) {
                        block.plateTriggered(lift, bukkitBlock);
                    }
                }
            }
            if (LiftUtil.isPressurePlate(point.getBlock().getType())) {
                point.getBlock().setData((byte) 0);
            }
            i.remove();
        }

        for (Iterator<Map.Entry<Lift, LiftState>> i = movingLifts.entrySet().iterator(); i.hasNext();) {
            Map.Entry<Lift, LiftState> entry = i.next();
            LiftState state = entry.getValue();
            if (state.contents == null) {
                state.contents = entry.getKey().getContents();
            } else {
                state.contents.update();
            }
                boolean hasPlayers = false;
                for (Entity entity : state.contents.getEntities()) {
                    if (entity instanceof Player) {
                        hasPlayers = true;
                        break;
                    }
                }

                if (!hasPlayers && state.playerCaused) {
                    i.remove();
                    continue;
                } else {
                    state.playerCaused = hasPlayers;
                }
            if (--state.delay == 0) {
                MoveResult result = state.contents.move(!state.specialBlocksTriggered);
                switch (result.getType()) {
                    case DELAY:
                        state.specialBlocksTriggered = false;
                        state.delay += result.getAmount();
                        break;
                    case CONTINUE:
                        state.delay = 1;
                        state.specialBlocksTriggered = true;
                        break;
                    case STOP:
                        state.contents = null;
                        state.delay = -1;
                        state.specialBlocksTriggered = false;
                        break;
                    case BLOCK:
                        i.remove();
                        break;
                }
            }
        }


    }
}
