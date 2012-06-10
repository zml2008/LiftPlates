package com.zachsthings.liftplates;

import com.zachsthings.liftplates.commands.LiftCreationCommand;
import com.zachsthings.liftplates.commands.LiftPlatesCommands;
import com.zachsthings.liftplates.commands.ListLiftsCommand;
import com.zachsthings.liftplates.util.Point;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author zml2008
 */
public class LiftPlatesPlugin extends JavaPlugin {

    static {
        ConfigurationSerialization.registerClass(Lift.class);
        ConfigurationSerialization.registerClass(Point.class);
    }
    private LiftRunner liftRunner;
    private final Map<UUID, LiftManager> liftManagers = new HashMap<UUID, LiftManager>();
    private LiftPlatesConfig config = new LiftPlatesConfig();

    @Override
    public void onEnable() {
        config.load(getConfig());
        saveConfig();

        getServer().getPluginManager().registerEvents(new LiftPlatesListener(this), this);

        safeSetExecutor("liftplates", new LiftPlatesCommands(this));
        safeSetExecutor("lift", new LiftCreationCommand(this));
        safeSetExecutor("lslifts", new ListLiftsCommand(this));

        liftRunner = new LiftRunner(this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, liftRunner, LiftRunner.RUN_FREQUENCY, LiftRunner.RUN_FREQUENCY);
    }

    @Override
    public void onDisable() {
        for (LiftManager manager : liftManagers.values()) {
            manager.save();
        }
    }

    private boolean safeSetExecutor(String command, CommandExecutor executor) {
        PluginCommand cmd = getCommand(command);
        if (cmd != null) {
            cmd.setExecutor(executor);
        }
        return cmd != null;
    }

    public void reload() {
        reloadConfig();
        config.load(getConfig());
        for (LiftManager manager : liftManagers.values()) {
            manager.load();
        }
    }

    public LiftRunner getLiftRunner() {
        return liftRunner;
    }

    public LiftPlatesConfig getConfiguration() {
        return config;
    }

    public LiftManager getLiftManager(World world) {
        Validate.notNull(world);
        LiftManager manager = liftManagers.get(world.getUID());
        if (manager == null) {
            manager = new LiftManager(this, world);
            manager.load();
            liftManagers.put(world.getUID(), manager);
        }
        return manager;
    }

    public Lift detectLift(Location loc, boolean nearby) {
        Validate.notNull(loc);
        Point point = new Point(loc);
        Lift lift = getLiftManager(loc.getWorld()).getLift(point);
        if (lift == null && nearby) {
            return detectLift(loc.getWorld(), point, config.maxLiftSize * config.maxLiftSize, point, new ArrayList<Point>(10));
        }
        return lift;
    }


    private Lift detectLift(World world, Point orig, int maxDistSq, Point loc, List<Point> visited) {
        if (orig.distanceSquared(loc) > maxDistSq) {
            return null;
        }

        Lift lift = getLiftManager(world).getLift(loc);
        if (lift != null) {
            return lift;
        }

        visited.add(loc);
        for (BlockFace face : LiftUtil.NSEW_FACES) {
            Point newLoc = loc.modify(face);
            if (visited.contains(newLoc)) {
                continue;
            }

            lift = detectLift(world, orig, maxDistSq, newLoc, visited);
            if (lift != null) {
                return lift;
            }
        }
        return null;
    }
}
