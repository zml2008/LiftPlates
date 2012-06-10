package com.zachsthings.liftplates;

import com.zachsthings.liftplates.util.Point;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * @author zml2008
 */
public class LiftManager {
    private final LiftPlatesPlugin plugin;
    private final World world;
    private final YamlConfiguration store;
    private final File storeFile;

    /**
     * The lifts stored in this LiftManager
     */
    private Map<Point, Lift> lifts = new HashMap<Point, Lift>();

    public LiftManager(LiftPlatesPlugin plugin, World world) {
        this.plugin = plugin;
        this.world = world;
        store = new YamlConfiguration();
        storeFile = new File(plugin.getDataFolder(), "lifts-" + world.getName() + ".yml");
    }

    public Lift getLift(Point point) {
        return lifts.get(point);
    }

    public Lift getLift(Location loc) {
        if (!world.equals(loc.getWorld())) {
            throw new IllegalArgumentException("Location with mismatched world provided to world-specific LiftManager");
        }
        return lifts.get(new Point(loc));
    }

    public Lift getOrAddLift(Point point) {
        Lift lift = lifts.get(point);
        if (lift == null && canPlaceLift(point)) {
            lift = new Lift(point);
            lift.setManager(this);
            lifts.put(point, lift);
            save();
        }
        return lift;
    }

    public boolean removeLift(Point point) {
        return lifts.remove(point) != null;
    }

    void updateLiftLocations() {
        Set<Lift> mismatchedLocations = new HashSet<Lift>();
        for (Iterator<Map.Entry<Point, Lift>> i = lifts.entrySet().iterator(); i.hasNext();) {
            Map.Entry<Point, Lift> entry = i.next();
            if (!entry.getKey().equals(entry.getValue().getPosition())) {
                mismatchedLocations.add(entry.getValue());
                i.remove();
            }
        }

        for (Lift lift : mismatchedLocations) {
            lifts.put(lift.getPosition(), lift);
        }
    }

    public boolean canPlaceLift(Point point) {
        return LiftUtil.isPressurePlate(point.getBlock(world).getType());
    }

    public Collection<Lift> getLifts() {
        return lifts.values();
    }

    public World getWorld() {
        return world;
    }

    public LiftPlatesPlugin getPlugin() {
        return plugin;
    }

    public void load() {
        try {
            store.load(storeFile);
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            plugin.getLogger().warning("Error while loading lifts configuration: " + e.getMessage());
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            plugin.getLogger().warning("Error while loading lifts configuration: " + e.getMessage());
            e.printStackTrace();
        }
        List<?> objects = store.getList("lifts");
        if (objects == null) {
            System.out.println("No 'lifts' list in the configuration!");
            return;
        }

        for (Object obj : objects) {
            if (!(obj instanceof Lift)) {
                continue;
            }
            Lift lift = (Lift) obj;
            if (!canPlaceLift(lift.getPosition())) { // The lift block has been removed since the last load
                System.out.println(lift.getPosition().getBlock(world));
                System.out.println("Cannot load lift at " + lift.getPosition());
                continue;
            }
            lift.setManager(this);
            lifts.put(lift.getPosition(), lift);
        }
    }

    public void save() {
        store.set("lifts", new ArrayList<Lift>(this.lifts.values()));
        try {
            store.save(storeFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Error while saving lifts configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
