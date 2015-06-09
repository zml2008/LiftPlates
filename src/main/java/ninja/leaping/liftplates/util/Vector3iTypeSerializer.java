package ninja.leaping.liftplates.util;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.InvalidTypeException;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

/**
 * Created by zml on 6/8/15.
 */
public class Vector3iTypeSerializer implements TypeSerializer {

    @Override
    public boolean isApplicable(TypeToken<?> typeToken) {
        return Vector3i.class.isAssignableFrom(typeToken.getRawType());
    }

    @Override
    public Object deserialize(TypeToken<?> typeToken, ConfigurationNode configurationNode) throws ObjectMappingException {
        if (!isApplicable(typeToken)) {
            throw new InvalidTypeException(typeToken);
        }
        int x = configurationNode.getNode("x").getInt();
        int y = configurationNode.getNode("y").getInt();
        int z = configurationNode.getNode("z").getInt();
        return new Vector3i(x, y, z);
    }

    @Override
    public void serialize(TypeToken<?> typeToken, Object o, ConfigurationNode configurationNode) throws ObjectMappingException {
        if (!isApplicable(typeToken)) {
            throw new InvalidTypeException(typeToken);
        }
        Vector3i vec = (Vector3i) o;
        configurationNode.getNode("x").setValue(vec.getX());
        configurationNode.getNode("y").setValue(vec.getY());
        configurationNode.getNode("z").setValue(vec.getZ());
    }
}
