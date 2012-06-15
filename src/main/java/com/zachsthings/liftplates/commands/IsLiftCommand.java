package com.zachsthings.liftplates.commands;

import com.zachsthings.liftplates.Lift;
import com.zachsthings.liftplates.LiftPlatesPlugin;
import com.zachsthings.liftplates.LiftUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

/**
 * A command that tells a player whether a certain location has a lift
 */
public class IsLiftCommand extends ChildrenCommandExecutor {
    private final LiftPlatesPlugin plugin;

    public IsLiftCommand(LiftPlatesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String[] arguments) throws CommandException {
        if (arguments.length < 1) {
            throw new CommandException("Not enough arguments! Usage: /" + command.getLabel() + " <location>");
        }
        Location testLocation = LiftUtil.matchLocation(sender, arguments[0]);
        Lift lift = plugin.getLiftManager(testLocation.getWorld()).getLift(testLocation);
        if (lift == null) {
            throw new CommandException("There is no lift at the specified location!");
        } else {
            sender.sendMessage(ChatColor.BLUE + "There is a lift at "
                    + LiftUtil.toPrettyString(lift.getPosition()) + " moving " + lift.getDirection());
        }
        return true;
    }
}
