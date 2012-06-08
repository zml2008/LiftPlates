package com.zachsthings.liftplates.commands;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

/**
 * Very barebones commands setup intended only for LiftPlates (Look at the WorldEdit command
 * system for a more general solution). Since LiftPlates is not a very command-focused
 * plugin, this system works fine for us.
 *<br />
 * Interesting stuff:<br />
 * * Implementation of (theoretically) infinite depth of child commands<br />
 * * Allow using CommandExceptions to display errors<br />
 * * Better usage text for children<br />
 */
public abstract class ChildrenCommandExecutor implements CommandExecutor {
    private Map<String, ChildrenCommandExecutor> children = new HashMap<String, ChildrenCommandExecutor>();
    public final boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        if (children.size() > 0) {
            if (arguments.length < 1) {
                sender.sendMessage(ChatColor.RED + "No subcommand specified!");
                sender.sendMessage(ChatColor.RED + "Usage: " + label + " " + StringUtils.join(children.keySet(), '|'));
                return false;
            }
            ChildrenCommandExecutor child = children.get(arguments[0]);
            if (child != null) {
                child.onCommand(sender, command, arguments[0],
                        (String[]) ArrayUtils.subarray(arguments, 1, arguments.length));
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Unknown subcommand specified");
                sender.sendMessage(ChatColor.RED + "Usage: " + label + " " + StringUtils.join(children.keySet(), '|'));
            }
        } else {
            try {
                return execute(sender, command, arguments);
            } catch (CommandException e) {
                sender.sendMessage(ChatColor.RED + e.getMessage());
            }
        }
        return true;
    }

    public void addChild(String name, ChildrenCommandExecutor child) {
        children.put(name, child);
    }

    /**
     * Actually execute this command
     *
     * @param sender The CommandSender who called this command
     * @param command The base command this was called from
     * @param arguments The arguments passed to this level of the command
     * @return Whether execution was successful and arguments should not be displayed
     * @throws CommandException when an error occurs while executing command
     */
    protected abstract boolean execute(CommandSender sender, Command command, String[] arguments) throws CommandException;
}
