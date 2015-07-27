package ninja.leaping.liftplates.util;

import com.google.common.reflect.TypeToken;
import ninja.leaping.liftplates.specialblock.SpecialBlock;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.InvalidTypeException;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

/**
 * Type serializer for SpecialBlock instances
 */
public class SpecialBlockTypeSerializer implements TypeSerializer<SpecialBlock> {
    @Override
    public SpecialBlock deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        return SpecialBlock.byName(value.getString());
    }

    @Override
    public void serialize(TypeToken<?> type, SpecialBlock obj, ConfigurationNode value) throws ObjectMappingException {
        value.setValue(obj.getName());
    }
}
