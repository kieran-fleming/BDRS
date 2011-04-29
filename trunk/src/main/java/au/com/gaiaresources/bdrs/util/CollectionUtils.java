package au.com.gaiaresources.bdrs.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class CollectionUtils {
    private CollectionUtils() { }
    
    @SuppressWarnings("unchecked")
    public static <T> T[] nullSafeFor(T[] a) {
        if (a != null) {
            return a;
        }
        return (T[]) new Object[0];
    }
    
    public static <T> Collection<T> nullSafeFor(Collection<T> c) {
        if (c == null) {
            return new ArrayList<T>();
        }
        return c;
    }
    
    public static int size(Collection<?> c) {
        return c != null ? c.size() : 0;
    }
    
    public static <K, V> Map<K, V> createMap(K[] keys, V[] values) {
        Map<K, V> m = new HashMap<K, V>();
        for (int i = 0; i < Math.min(keys.length, values.length); i++) {
            m.put(keys[i], values[i]);
        }
        return m;
    }
}
