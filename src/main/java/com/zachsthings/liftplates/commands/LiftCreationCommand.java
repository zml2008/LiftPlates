package com.zachsthings.liftplates.commands;

import com.zachsthings.liftplates.Lift;
import com.zachsthings.liftplates.LiftPlatesPlugin;
import com.zachsthings.liftplates.LiftUtil;
import com.zachsthings.liftplates.util.Point;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The command used to create a new lift
 */
public class LiftCreationCommand extends ChildrenCommandExecutor {
    private final LiftPlatesPlugin plugin;

    public LiftCreationCommand(LiftPlatesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String[] arguments) throws CommandException {
        if (arguments.length < 1) {
            throw new CommandException("Not enough arguments! Usage: /" + command.getName() + " [location] <up|down>");
        }
        if (arguments.length > 2) {
            throw new CommandException("Too many arguments! Usage: /" + command.getName() + " [location] <up|down>");
        }
        Lift.Direction direction;
        try {
            direction = Lift.Direction.valueOf(arguments[arguments.length - 1].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CommandException("Unknown direction: " + arguments[arguments.length - 1]);
        }
        Location loc;
        if (arguments.length > 1) {
            loc = LiftUtil.matchLocation(sender, arguments[0]);
        } else if (sender instanceof Player) {
            loc = ((Player) sender).getLocation();
        } else {
            throw new CommandException("Player or specified location required to get a lift!");
        }

        Lift lift = plugin.getLiftManager(loc.getWorld()).getOrAddLift(new Point(loc));
        if (lift == null) {
            throw new CommandException("No pressure plate at the specified location!");
        }

        lift.setDirection(direction);

        sender.sendMessage(ChatColor.BLUE + "Lift successfully created!");
        return true;
    }

}
