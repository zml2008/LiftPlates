package ninja.leaping.liftplates;

import com.flowpowered.math.vector.Vector3i;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author zml2008
 */
public class LiftManager {
    private final LiftPlatesPlugin plugin;
    private final World world;
    private ConfigurationNode store;
    private final ConfigurationLoader<ConfigurationNode> loader;

    /**
     * The lifts stored in this LiftManager
     */
    private Map<Vector3i, Lift> lifts = new HashMap<Vector3i, Lift>();

    public LiftManager(LiftPlatesPlugin plugin, World world) {
        this.plugin = plugin;
        this.world = world;
        loader = GsonConfigurationLoader.builder().setFile(new File(plugin.getConfigurationFolder(), "lifts-" + world.getUniqueId() + ".json")).build();
    }

    public Lift getLift(Vector3i point) {
        return lifts.get(point);
    }

    public Lift getLift(Location loc) {
        if (!world.equals(loc.getExtent())) {
            throw new IllegalArgumentException("Location with mismatched world provided to world-specific LiftManager");
        }
        return lifts.get(loc.getBlockPosition());
    }

    public Lift getOrAddLift(Vector3i point) {
        Lift lift = lifts.get(point);
        if (lift == null && canPlaceLift(point)) {
            lift = new Lift(point);
            lift.setManager(this);
            lifts.put(point, lift);
            save();
        }
        return lift;
    }

    public boolean removeLift(Vector3i point) {
        return lifts.remove(point) != null;
    }

    void updateLiftLocations() {
        Set<Lift> mismatchedLocations = new HashSet<Lift>();
        for (Iterator<Map.Entry<Vector3i, Lift>> i = lifts.entrySet().iterator(); i.hasNext();) {
            Map.Entry<Vector3i, Lift> entry = i.next();
            if (!entry.getKey().equals(entry.getValue().getPosition())) {
                mismatchedLocations.add(entry.getValue());
                i.remove();
            }
        }

        for (Lift lift : mismatchedLocations) {
            lifts.put(lift.getPosition(), lift);
        }
    }

    public boolean canPlaceLift(Vector3i point) {
        return LiftUtil.isPressurePlate(world.getBlockType(point));
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
            store = loader.load();
        } catch (IOException e) {
            plugin.getLogger().warn("Error while loading lifts configuration: " + e.getMessage(), e);
            return;
        }
        ConfigurationNode objects = store.getNode("lifts");
        if (objects.isVirtual()) {
            System.out.println("No 'lifts' list in the configuration!");
            return;
        }

        for (ConfigurationNode child : objects.getChildrenList()) {
            Lift lift;
            try {
                lift = Lift.MAPPER.bindToNew().populate(child);
            } catch (ObjectMappingException e) {
                plugin.getLogger().warn("Unable to load lift at " + Arrays.toString(child.getPath()) + " in world " + getWorld().getName(), e);
                continue;
            }
            if (!canPlaceLift(lift.getPosition())) { // The lift block has been removed since the last load
                plugin.getLogger().warn("Cannot load lift at " + lift.getPosition() + "! Did the world change?");
                continue;
            }
            lift.setManager(this);
            lifts.put(lift.getPosition(), lift);
        }
    }

    public void save() {
        ConfigurationNode lifts = store.getNode("lifts");
        lifts.setValue(null);
        for (Lift lift : this.lifts.values()) {
            try {
                Lift.MAPPER.bind(lift).serialize(lifts.getAppendedNode());
            } catch (ObjectMappingException e) {
                plugin.getLogger().warn("Unable to serializer lift at position " + lift.getPosition() + ", it may have to be recreated!");
            }
        }

        try {
            loader.save(store);
        } catch (IOException e) {
            plugin.getLogger().warn("Error while saving lifts configuration", e);
        }
    }
}
