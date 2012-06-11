package com.zachsthings.liftplates;

import com.zachsthings.liftplates.SpecialBlock;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author zml2008
 */
public class LiftRunner implements Runnable {
    public static final long RUN_FREQUENCY = 10;
    private final LiftPlatesPlugin plugin;
    private final Map<String, PlayerState> playerStates = new HashMap<String, PlayerState>();
    private final Map<Lift, LiftState> movingLifts = new HashMap<Lift, LiftState>();

    public LiftRunner(LiftPlatesPlugin plugin) {
        this.plugin = plugin;
    }

    public class PlayerState {
        private final Player player;
        private boolean onPlate;

        private PlayerState(Player player) {
            this.player = player;
        }

        public void triggeredPlate() {
            onPlate = true;
        }

        void update() {
            if (onPlate && !LiftUtil.isPressurePlate(player.getLocation().getBlock().getType())) {
                onPlate = false;
            }
        }

        public boolean isOnPlate() {
            return onPlate;
        }

        public Player getPlayer() {
            return player;
        }
    }

    public PlayerState getState(Player player) {
        PlayerState state = playerStates.get(player.getName());
        if (state == null) {
            state = new PlayerState(player);
            playerStates.put(player.getName(), state);
        }
        return state;
    }

    public void clear(Player player) {
        playerStates.remove(player.getName());
    }

    private static class LiftState {
        public int delay = 1;
        public boolean specialBlocksTriggered = true;
        public LiftContents contents;
    }

    public void run() {
        for (PlayerState state : playerStates.values()) {
            state.update();
            Location plateLoc = state.getPlayer().getLocation();

            if (state.isOnPlate()) {
                Lift lift = plugin.getLiftManager(plateLoc.getWorld()).getLift(plateLoc);
                if (lift != null) {
                    if (!movingLifts.containsKey(lift)) {
                        movingLifts.put(lift, new LiftState());
                    } else {
                        LiftState liftState = movingLifts.get(lift);
                        if (liftState.delay == -1) {
                            liftState.delay = 1;
                        }
                    }
                } else {
                    lift = plugin.detectLift(plateLoc, true);
                    if (lift != null) {
                        Block bukkitBlock = plateLoc.getBlock().getRelative(BlockFace.DOWN);
                        SpecialBlock block = lift.getSpecialBlock(bukkitBlock.getType());
                        if (block != null) {
                            block.plateTriggered(lift, bukkitBlock);
                        }
                    }
                }
            }
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

            if (!hasPlayers) {
                i.remove();
                continue;
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
