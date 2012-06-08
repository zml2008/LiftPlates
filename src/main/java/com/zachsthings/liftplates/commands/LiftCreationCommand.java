package com.zachsthings.liftplates.commands;

import com.zachsthings.liftplates.Lift;
import com.zachsthings.liftplates.LiftPlatesPlugin;
import com.zachsthings.liftplates.LiftUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

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
            throw new CommandException("Not enough arguments! Usage: /" + command.getName() + " <up|down> [pos]");
        }
        if (arguments.length > 2) {
            throw new CommandException("Too many arguments! Usage; /" + command.getName() + " <up|down> [pos]");
        }
        Lift.Direction direction;
        try {
            direction = Lift.Direction.valueOf(arguments[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CommandException("Unknown direction: " + arguments[0]);
        }
        Location loc;
        if (arguments.length > 1) {
            loc = matchLocation(sender, arguments[1]);
        } else if (sender instanceof Player) {
            loc = ((Player) sender).getLocation();
        } else {
            throw new CommandException("Player or specified location required to get a lift!");
        }

        Lift lift = plugin.getLiftManager(loc.getWorld()).getOrAddLift(LiftUtil.toBlockVector(loc));
        if (lift == null) {
            throw new CommandException("No pressure plate at the specified location!");
        }

        lift.setDirection(direction);

        sender.sendMessage("Lift successfully created!");
        return true;
    }

    private Location matchLocation(CommandSender sender, String testString) throws CommandException {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (testString.equalsIgnoreCase("target")) {
                return player.getTargetBlock(null, 300).getLocation();
            } else if (testString.equalsIgnoreCase("pos")
                    || testString.equalsIgnoreCase("cur")
                    || testString.equalsIgnoreCase("current")) {
                return player.getLocation();
            }
        }
        String[] worldSplit = testString.split(":");
        World world;
        if (worldSplit.length == 1) {
            if (!(sender instanceof Player)) {
                throw new CommandException("No player or world provided for location!");
            }
            world = ((Player) sender).getWorld();
        } else {
            world = plugin.getServer().getWorld(worldSplit[0]);
            if (world == null) {
                throw new CommandException("Unknown world specified: " + worldSplit[0]);
            }
        }

        String[] pointSplit = worldSplit[worldSplit.length - 1].split(",");
        if (pointSplit.length != 3) {
            throw new CommandException("The third dimension, do you get it? Three points are required to specify a location");
        }
        int x = Integer.parseInt(pointSplit[0]);
        int y = Integer.parseInt(pointSplit[1]);
        int z = Integer.parseInt(pointSplit[2]);
        return new Location(world, x, y, z);

    }

}
