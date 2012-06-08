package com.zachsthings.liftplates;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.BlockVector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    private Map<BlockVector, Lift> lifts = new HashMap<BlockVector, Lift>();

    public LiftManager(LiftPlatesPlugin plugin, World world) {
        this.plugin = plugin;
        this.world = world;
        store = new YamlConfiguration();
        storeFile = new File(plugin.getDataFolder(), "lifts-" + world.getName() + ".yml");
    }

    public Lift getLift(BlockVector vec) {
        return lifts.get(vec);
    }

    public Lift getLift(Location loc) {
        if (!world.equals(loc.getWorld())) {
            throw new IllegalArgumentException("Location with mismatched world provided to world-specific LiftManager");
        }
        return lifts.get(loc.toVector().toBlockVector());
    }

    public Lift getOrAddLift(BlockVector vec) {
        Lift lift = lifts.get(vec);
        if (lift == null) {
            lift = new Lift(vec);
            lift.setManager(this);
            lifts.put(vec, lift);
            save();
        }
        return lift;
    }

    public boolean removeLift(BlockVector vec) {
        return lifts.remove(vec) != null;
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
        } catch (InvalidConfigurationException e) {
            plugin.getLogger().warning("Error while loading lifts configuration: " + e.getMessage());
        }

        for (Object obj : store.getList("lifts")) {
            if (!(obj instanceof Lift)) {
                continue;
            }
            Lift lift = (Lift) obj;
            if (LiftUtil.isPressurePlate(world.getBlockAt(lift.getPosition().getBlockX(),
                    lift.getPosition().getBlockY(), lift.getPosition().getBlockZ()).getType()));
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
        }
    }
}
