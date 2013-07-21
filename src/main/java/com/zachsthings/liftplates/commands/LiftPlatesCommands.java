package com.zachsthings.liftplates.commands;

import com.zachsthings.liftplates.LiftPlatesPlugin;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 * @author zml2008
 */
public class LiftPlatesCommands extends ChildrenCommandExecutor {
    public LiftPlatesCommands(LiftPlatesPlugin plugin) {
        addChild("version", new VersionCommand(plugin));
        addChild("reload", new ReloadCommand(plugin));
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String[] arguments) throws CommandException {
        throw new UnsupportedOperationException("A child command is required");
    }

    public static class VersionCommand extends ChildrenCommandExecutor {
        private final Plugin plugin;

        public VersionCommand(Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean execute(CommandSender sender, Command command, String[] arguments) throws CommandException {
			if (!sender.hasPermission("liftplates.version")) {
				throw new CommandException("You do not have permission for /liftplates version");
			}
            PluginDescriptionFile desc = plugin.getDescription();
            sender.sendMessage(ChatColor.BLUE + desc.getName() + " version " + desc.getVersion());
            sender.sendMessage(ChatColor.BLUE + "Written by " + StringUtils.join(desc.getAuthors(), ", "));
            return true;
        }
    }

    public static class ReloadCommand extends ChildrenCommandExecutor {
        private final LiftPlatesPlugin plugin;

        public ReloadCommand(LiftPlatesPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean execute(CommandSender sender, Command command, String[] arguments) throws CommandException {
			if (!sender.hasPermission("liftplates.reload")) {
				throw new CommandException("You do not have permission for /liftplates reload");
			}

            try {
                plugin.reload();
                sender.sendMessage(ChatColor.BLUE + plugin.getName() + " successfully reloaded");
            } catch (Throwable t) {
                sender.sendMessage(ChatColor.RED + "Error while reloading " + plugin.getName() + ": " + t.getMessage());
                sender.sendMessage(ChatColor.DARK_RED + "See console for more details");
                t.printStackTrace();
            }
            return true;
        }
    }
}
