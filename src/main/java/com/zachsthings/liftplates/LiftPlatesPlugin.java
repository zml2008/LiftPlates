package com.zachsthings.liftplates;

import com.zachsthings.liftplates.commands.LiftPlatesCommands;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
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
    private LiftPlatesState plateState;
    private final Map<UUID, LiftManager> liftManagers = new HashMap<UUID, LiftManager>();
    private LiftPlatesConfig config = new LiftPlatesConfig();
    @Override
    public void onEnable() {
        config.load(getConfig());
        saveConfig();

        getServer().getPluginManager().registerEvents(new LiftPlatesListener(this), this);

        getCommand("liftplates").setExecutor(new LiftPlatesCommands(this));

        plateState = new LiftPlatesState(this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, plateState, LiftPlatesState.RUN_FREQUENCY, LiftPlatesState.RUN_FREQUENCY);
    }

    public void reload() {
        reloadConfig();
        config.load(getConfig());
        for (LiftManager manager : liftManagers.values()) {
            manager.load();
        }
    }

    public LiftPlatesState getPlateState() {
        return plateState;
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
        Lift lift = getLiftManager(loc.getWorld()).getLift(loc);
        if (lift == null && nearby) {
            return detectLift(loc, config.maxLiftSize * config.maxLiftSize, loc, new ArrayList<Location>(10));
        }
        return lift;
    }


    private Lift detectLift(Location orig, int maxDistSq, Location loc, List<Location> visited) {
        if (orig.distanceSquared(loc) > maxDistSq) {
            return null;
        }

        Lift lift = getLiftManager(loc.getWorld()).getLift(loc);
        if (lift != null) {
            return lift;
        }

        visited.add(loc);
        for (BlockFace face : LiftUtil.NSEW_FACES) {
            Location newLoc = LiftUtil.mod(loc, face);
            if (visited.contains(newLoc)) {
                continue;
            }

            lift = detectLift(orig, maxDistSq, newLoc, visited);
            if (lift != null) {
                return lift;
            }
        }
        return null;
    }
}
