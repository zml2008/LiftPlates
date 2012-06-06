package com.zachsthings.liftplates;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * @author zml2008
 */
public class LiftPlatesListener {
    private final LiftPlatesPlugin plugin;

    public LiftPlatesListener(LiftPlatesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPressPlate(PlayerInteractEvent event) {
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

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        /*
        If pressureplate, remove registrtion for column
        if above pressureplate, schedule delayed task and if above is no longer pressureplate, remove registration
         */
    }
}
