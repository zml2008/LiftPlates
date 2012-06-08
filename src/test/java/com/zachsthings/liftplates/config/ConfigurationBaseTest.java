package com.zachsthings.liftplates.config;

import org.bukkit.configuration.MemoryConfiguration;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author zml2008
 */
public class ConfigurationBaseTest {
    private static final String STRING_KEY = "some-string";
    private static final String STRING_VALUE = "ohaithar";
    private static final String MAP_KEY = "a-map";
    private static final Map<String, Object> MAP_VALUE = createMap();

    private static Map<String, Object> createMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", "b");
        map.put("c", 5);
        map.put("d", true);
        return map;
    }

    public static class TestConfig extends ConfigurationBase {
        @Setting(STRING_KEY) public String stringValue;
        @Setting(MAP_KEY) public Map<String, Object> mapValue;
    }

    @Test
    public void testLoad() {
        MemoryConfiguration bukkit = new MemoryConfiguration();
        bukkit.set(STRING_KEY, STRING_VALUE);
        bukkit.set(MAP_KEY, MAP_VALUE);

        TestConfig config = new TestConfig();
        config.load(bukkit);

        assertEquals(config.stringValue, STRING_VALUE);
        assertEquals(config.mapValue, MAP_VALUE);
    }

    @Test
    public void testSave() {
        TestConfig config = new TestConfig();
        config.stringValue = STRING_VALUE;
        config.mapValue = MAP_VALUE;

        MemoryConfiguration bukkit = new MemoryConfiguration();

        config.save(bukkit);

        assertEquals(STRING_VALUE, bukkit.get(STRING_KEY));
        assertEquals(MAP_VALUE, bukkit.getConfigurationSection(MAP_KEY).getValues(true));
    }
}
