package com.zachsthings.liftplates.commands;

import com.zachsthings.liftplates.Lift;
import com.zachsthings.liftplates.LiftManager;
import com.zachsthings.liftplates.LiftPlatesPlugin;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListLiftsCommand extends ChildrenCommandExecutor {
    private final LiftPlatesPlugin plugin;

    public ListLiftsCommand(LiftPlatesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String[] arguments) throws CommandException {
        World world;
        if (arguments.length >= 1) {
            world = plugin.getServer().getWorld(arguments[0]);
            if (world == null) {
                throw new CommandException("Unknown world: " + arguments[0]);
            }
        } else if (sender instanceof Player) {
            world = ((Player) sender).getWorld();
        } else {
            throw new CommandException("Sender must be a player or world must be specified!");
        }

        LiftManager manager = plugin.getLiftManager(world);
        for (Lift lift : manager.getLifts()) {
            sender.sendMessage(ChatColor.BLUE + "Lift at " + lift.getPosition() + " moving " + lift.getDirection());
        }
        return true;
    }
}
