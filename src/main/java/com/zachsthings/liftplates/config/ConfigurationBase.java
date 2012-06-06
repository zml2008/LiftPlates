package com.zachsthings.liftplates.config;

import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author zml2008
 */
public class ConfigurationBase {

    private static List<Field> getDeclaredFieldsRecur(Class<?> clazz) {
        List<Field> fields = new ArrayList<Field>();
        do {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        } while ((clazz = clazz.getSuperclass()) != null && !Object.class.equals(clazz));
        return fields;
    }

    public void load(ConfigurationSection section) {
        for (Field field : getDeclaredFieldsRecur(getClass())) {
            field.setAccessible(true);
            if (!field.isAnnotationPresent(Setting.class)) {
                continue;
            }

            Setting setting = field.getAnnotation(Setting.class);
            Object settingVal = convertLoad(section.get(setting.value()));
            if (settingVal == null) {
                try {
                    Object val = field.get(this);
                    doSave(val, setting.value(), section);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else if (!field.getType().isAssignableFrom(settingVal.getClass())) {
                // TODO: Report error to user
            } else {
                try {
                    field.set(this, settingVal);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void save(ConfigurationSection section) {
        for (Field field : getDeclaredFieldsRecur(getClass())) {
            field.setAccessible(true);
            if (!field.isAnnotationPresent(Setting.class)) {
                continue;
            }

            Setting setting = field.getAnnotation(Setting.class);
            try {
                Object val = field.get(this);
                doSave(val, setting.value(), section);
            } catch (IllegalAccessException e) {
            }
        }
    }

    private static Object convertLoad(Object obj) {
        if (obj instanceof ConfigurationSection) {
            obj = ((ConfigurationSection) obj).getValues(true);
        }
        return obj;
    }

    private static void doSave(Object val, String path, ConfigurationSection section) {
        if (val instanceof Map<?, ?>) {
            section.createSection(path, (Map<?, ?>) val);
        } else {
            section.set(path, val);
        }
    }
}
