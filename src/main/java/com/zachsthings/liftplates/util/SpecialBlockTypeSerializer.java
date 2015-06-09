package com.zachsthings.liftplates.util;

import com.google.common.reflect.TypeToken;
import com.zachsthings.liftplates.specialblock.SpecialBlock;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.InvalidTypeException;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

/**
 * Type serializer for SpecialBlock instances
 */
public class SpecialBlockTypeSerializer implements TypeSerializer {

    @Override
    public boolean isApplicable(TypeToken<?> type) {
        return SpecialBlock.class.isAssignableFrom(type.getRawType());
    }

    @Override
    public Object deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        if (!isApplicable(type)) {
            throw new InvalidTypeException(type);
        }
        return SpecialBlock.byName(value.getString());
    }

    @Override
    public void serialize(TypeToken<?> type, Object obj, ConfigurationNode value) throws ObjectMappingException {
        if (!isApplicable(type)) {
            throw new InvalidTypeException(type);
        }
        value.setValue(((SpecialBlock) obj).getName());
    }
}
