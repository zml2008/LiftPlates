package com.zachsthings.liftplates;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zml2008
 */
public class Lift implements ConfigurationSerializable {

    public static enum Direction {
        UP, DOWN;
    }

    /**
     * The lift's direction
     */
    private Direction dir;

    /**
     * The position of the base block of the lift
     */
    private Vector basePosition;

    /**
     * The id and damage values of the base block, used to make sure that the
     * lift is still where it should be.
     */
    private int baseId;
    private int baseData;

    public Map<String, Object> serialize() {
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("direction", dir.name());
        ret.put("base-pos", basePosition);

        Map<String, Object> baseInfo = new HashMap<String, Object>();
        baseInfo.put("id", baseId);
        baseInfo.put("data", baseData);
        ret.put("base", baseInfo);
        return ret;
    }

    private static Lift deserialize(Map<String, Object> data) {
        Direction dir = Direction.valueOf(data.get("direction").toString());
        Vector basePosition = (Vector) data.get("pase-pos");
        Map<?, ?> baseInfo = (Map<?, ?>) data.get("base");
        int baseId = (Integer) baseInfo.get("id");
        int baseData = (Integer) baseInfo.get("data");

        Lift lift = new Lift();
        lift.dir = dir;
        lift.basePosition = basePosition;
        lift.baseId = baseId;
        lift.baseData = baseData;
        return lift;
    }

    public void moveUp() {

    }
}
