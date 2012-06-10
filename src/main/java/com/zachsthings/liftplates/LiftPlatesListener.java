package com.zachsthings.liftplates;

import com.zachsthings.liftplates.util.Point;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
                plugin.getPlateState().getState(event.getPlayer()).triggeredPlate();
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
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        final Location block = event.getBlock().getLocation();
        if (LiftUtil.isPressurePlate(event.getBlock().getType())) {
            plugin.getLiftManager(block.getWorld()).removeLift(new Point(block));
        } else {
            final Location above = LiftUtil.mod(block, BlockFace.UP);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    if (!LiftUtil.isPressurePlate(above.getBlock().getType())) {
                        plugin.getLiftManager(block.getWorld()).removeLift(new Point(above));
                    }
                }
            }, 1L);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPlateState().clear(event.getPlayer());
    }
}
