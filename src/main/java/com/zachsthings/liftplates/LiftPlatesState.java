package com.zachsthings.liftplates;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zml2008
 */
public class LiftPlatesState implements Runnable {
    public static final long RUN_FREQUENCY = 10;
    private final LiftPlatesPlugin plugin;
    private final Map<String, PlayerState> playerStates = new HashMap<String, PlayerState>();

    public LiftPlatesState(LiftPlatesPlugin plugin) {
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

    public void run() {
        for (PlayerState state : playerStates.values()) {
            state.update();

            if (state.isOnPlate()) {
                System.out.println("Player " + state.getPlayer().getName() + " is on pressureplate at " + state.getPlayer().getLocation());
                Lift lift = plugin.getLiftManager(state.getPlayer().getWorld()).getLift(state.getPlayer().getLocation());
                if (lift != null) {
                    lift.move(lift.getDirection().getFace());
                }
            }
        }
    }
}
