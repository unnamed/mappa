package team.unnamed.mappa.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface TypeUtils {
    Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS = newPrimitiveWrapperMap();

    static Map<Class<?>, Class<?>> newPrimitiveWrapperMap() {
        Map<Class<?>, Class<?>> map = new HashMap<>();
        map.put(boolean.class, Boolean.class);
        map.put(byte.class, Byte.class);
        map.put(char.class, Character.class);
        map.put(double.class, Double.class);
        map.put(float.class, Float.class);
        map.put(int.class, Integer.class);
        map.put(long.class, Long.class);
        map.put(short.class, Short.class);
        return Collections.unmodifiableMap(map);
    }

    static Class<?> primitiveToWrapper(Class<?> clazz) {
        if (!clazz.isPrimitive()) {
            return clazz;
        }

        return PRIMITIVES_TO_WRAPPERS.get(clazz);
    }
}
