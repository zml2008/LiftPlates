package ninja.leaping.liftplates.util;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

/**
 * A TypeSerializer for {@link Vector3i} instances
 */
public class Vector3iTypeSerializer implements TypeSerializer<Vector3i> {

    @Override
    public Vector3i deserialize(TypeToken<?> typeToken, ConfigurationNode configurationNode) throws ObjectMappingException {
        int x = configurationNode.getNode("x").getInt();
        int y = configurationNode.getNode("y").getInt();
        int z = configurationNode.getNode("z").getInt();
        return new Vector3i(x, y, z);
    }

    @Override
    public void serialize(TypeToken<?> typeToken, Vector3i vec, ConfigurationNode configurationNode) throws ObjectMappingException {
        configurationNode.getNode("x").setValue(vec.getX());
        configurationNode.getNode("y").setValue(vec.getY());
        configurationNode.getNode("z").setValue(vec.getZ());
    }
}
