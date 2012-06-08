package com.zachsthings.liftplates;

import com.google.common.collect.Sets;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Set;
import java.util.WeakHashMap;

/**
 * @author zml2008
 */
public class LiftPlatesState implements Runnable {
    public static final long RUN_FREQUENCY = 10;
    private final LiftPlatesPlugin plugin;
    private final Set<Player> pressurePlayers = Sets.newSetFromMap(new WeakHashMap<Player, Boolean>());

    public LiftPlatesState(LiftPlatesPlugin plugin) {
        this.plugin = plugin;
    }

    private class PlayerState {
        private final Player player;
        private boolean travelling;

        private PlayerState(Player player) {
            this.player = player;
        }
    }

    public void run() {

    }
}
