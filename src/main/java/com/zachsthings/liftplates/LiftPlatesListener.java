package com.zachsthings.liftplates;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Optional;
import com.zachsthings.liftplates.specialblock.SpecialBlockRegisterEvent;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.manipulator.tileentity.SignData;
import org.spongepowered.api.entity.EntityInteractionTypes;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.block.BlockRedstoneUpdateEvent;
import org.spongepowered.api.event.entity.player.PlayerBreakBlockEvent;
import org.spongepowered.api.event.entity.player.PlayerInteractBlockEvent;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains the event listeners. These mostly call other bits of the code.
 * @author zml2008
 */
public class LiftPlatesListener {
    public static final Pattern LIFT_SIGN_PATTERN = Pattern.compile("\\[lift:([^]]+)]");
    private final LiftPlatesPlugin plugin;

    public LiftPlatesListener(LiftPlatesPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onPressPlate(PlayerInteractBlockEvent event) {
        if (event.getInteractionType() == EntityInteractionTypes.USE) {
            Optional<TileEntity> state = event.getBlock().getTileEntity();
            if (state.isPresent() && state.get() instanceof Sign) {
                Sign sign = (Sign) state.get();
                Optional<SignData> data = sign.getData();
                if (data.isPresent()) {
                    Matcher match = LIFT_SIGN_PATTERN.matcher(Texts.toPlain(data.get().getLine(0)));
                    if (match.matches()) {
                        final String action = match.group(1);
                        // TODO: Handle lift signs if I still want to use them
                    }
                }
            }
        }
    }

    @Subscribe(order = Order.LAST)
    public void onBlockBreak(PlayerBreakBlockEvent event) {
        final Location block = event.getBlock();
        if (plugin.getLiftManager(block.getExtent()).getLift(block) != null) {
            plugin.getLiftManager(block.getExtent()).removeLift(block.getBlockPosition());
        } else {
            final Vector3i above = block.getPosition().add(Direction.UP.toVector3d()).toInt();
            plugin.getGame().getSyncScheduler().runTaskAfter(plugin, new Runnable() {
                public void run() {
                    if (block.getExtent().getBlockType(above) == BlockTypes.AIR &&
                            plugin.getLiftManager(block.getExtent()).getLift(above) != null) {
                        plugin.getLiftManager(block.getExtent()).removeLift(above);
                    }
                }
            }, 1L);
        }
    }

    @Subscribe
    public void onSpecialBlockRegister(SpecialBlockRegisterEvent event) {
        LiftPlatesConfig config = plugin.getConfiguration();
        if (!config.specialBlocks.containsValue(event.getRegisteredBlock())) {
            config.specialBlocks.put(event.getRegisteredBlock().getDefaultType(), event.getRegisteredBlock());
            try {
                config.save();
            } catch (IOException e) {
                // Ignore, oh well
            }
        }
    }

    @Subscribe
    public void onPlateToggle(BlockRedstoneUpdateEvent event) {
        System.out.println("Toggling plate!");
        if (LiftUtil.isPressurePlate(event.getBlock().getType())) {
            if (event.getNewSignalStrength() > 0) { // Turning on
                plugin.getLiftRunner().plateTriggered(event.getBlock());
            }
        }
    }
}
