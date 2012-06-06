package com.zachsthings.liftplates;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zml2008
 */
public class LiftManager {
    private final LiftPlatesPlugin plugin;
    private final YamlConfiguration store;
    private final File storeFile;

    /**
     * The lifts stored in this LiftManager
     */
    private Map<Vector, Lift> lifts = new HashMap<Vector, Lift>();

    public LiftManager(LiftPlatesPlugin plugin) {
        this.plugin = plugin;
        store = new YamlConfiguration();
        storeFile = new File(plugin.getDataFolder(), "lifts.yml");
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

        for (Object lift : store.getList("lifts")) {
            if (!(lift instanceof Lift)) {

            }
        }
    }

    public void save() {
        try {
            store.save(storeFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Error while saving lifts configuration: " + e.getMessage());
        }
    }
}
