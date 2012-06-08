package com.zachsthings.liftplates;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains the event listeners. These mostly call other bits of the code.
 * @author zml2008
 */
public class LiftPlatesListener implements Listener {
    public static final Pattern LIFT_SIGN_PATTERN = Pattern.compile("\\[lift:([^\\]]+)\\]");
    private final LiftPlatesPlugin plugin;

    public LiftPlatesListener(LiftPlatesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPressPlate(PlayerInteractEvent event) {
        switch (event.getAction()) {
            case PHYSICAL:
                // Add player to pressure plate triggered list in LiftPlatesState
                // This will have them processed for pressure plate usage in the next tick
                // A time counter will also be added so that we can disable the pressure
                // plate state after the player has been off a pressure plate for a certain amount of time
                break;
            case LEFT_CLICK_BLOCK:
                BlockState state = event.getClickedBlock().getState();
                if (state instanceof Sign) {
                    Sign sign = (Sign) state;
                    Matcher match = LIFT_SIGN_PATTERN.matcher(sign.getLine(0));
                    if (match.matches()) {
                        final String action = match.group(1);
                        // TODO: Handle lift signs if I still want to use them
                    }
                }
                break;
        }
        /*

        check if a registered column
        if so start moving upwards at speed configurable in config
        continue until player steps off pressure plates
        how to handle up/down?
        pressure plate calls repeatedly, so we can keep moving a player up until the last trigger time is from a while ago.
        or if they're no longer standing on a pressureplate.

        also check for callsigns ([lift:up/down]) and bring the correct lift to the same level as the player
         */
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        final Location block = event.getBlock().getLocation();
        if (LiftUtil.isPressurePlate(event.getBlock().getType())) {
            plugin.getLiftManager(block.getWorld()).removeLift(block.toVector().toBlockVector());
        } else {
            final Location above = LiftUtil.mod(block, BlockFace.UP);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    if (!LiftUtil.isPressurePlate(above.getBlock().getType())) {
                        plugin.getLiftManager(block.getWorld()).removeLift(LiftUtil.toBlockVector(above));
                    }
                }
            }, 1L);
        }
    }
}
