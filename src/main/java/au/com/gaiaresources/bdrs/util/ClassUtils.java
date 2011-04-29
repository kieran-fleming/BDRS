package au.com.gaiaresources.bdrs.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.springframework.beans.BeanUtils;

public final class ClassUtils {
    /**
     * Get the setter method for a property.
     * @param clazz The class to extract the method from.
     * @param propertyName The name of the property.
     * @return <code>Method</code>
     * @throws NoSuchMethodException The method does not exist.
     * @throws NoSuchFieldException The property does not exist as a field.
     */
    public static Method getSetterMethod(Class<?> clazz, String propertyName) 
                                         throws NoSuchMethodException, NoSuchFieldException 
    {
        Class<?> propertyClass = getPropertyClass(clazz, propertyName);
        return getSetterMethod(clazz, propertyName, propertyClass);
    }
    
    /**
     * Get the setter method for a property.
     * @param clazz The class to extract the method from.
     * @param propertyName The name of the property.
     * @param propertyClass The class of the property.
     * @return <code>Method</code>
     * @throws NoSuchMethodException The method does not exist.
     */
    public static Method getSetterMethod(Class<?> clazz, String propertyName, Class<?> propertyClass) {
        String methodName = getSetterName(propertyName);
        return getMethod(clazz, methodName, new Class[] {propertyClass});
    }
    
    /**
     * Get the getter method for a property.
     * @param clazz The class to extract the method from.
     * @param propertyName The name of the property.
     * @return <code>Method</code>
     * @throws NoSuchMethodException The method does not exist.
     */
    public static Method getGetterMethod(Class<?> clazz, String propertyName) 
                                         throws NoSuchMethodException 
    {
        Method getter = getMethod(clazz, getGetterName(propertyName, false), null);
        if (getter == null) {
            getter = getMethod(clazz, getGetterName(propertyName, true), null);
        }
        if (getter == null) {
            throw new NoSuchMethodException("Failed to find getter method for " + propertyName 
                                          + " on class " + clazz);
        }
        return getter;
    }
    
    /**
     * Get the class of a property.
     * @param clazz The <code>Class</code> that owns the property.
     * @param propertyName The name of the property.
     * @return <code>Class</code>.
     * @throws NoSuchFieldException If the property does not exist.
     */
    public static Class<?> getPropertyClass(Class<?> clazz, String propertyName) throws NoSuchFieldException {
        Class<?> c = clazz;
        Field field = getField(clazz, propertyName);
        try {
            field = c.getDeclaredField(propertyName);
        } catch (NoSuchFieldException nsfe) {
            //org.springframework.util.ClassUtils.get
        }
        return field.getType();
    }
    
    @SuppressWarnings("unchecked")
    public static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        java.lang.reflect.Field f = null;
        try {
            f = clazz.getDeclaredField(fieldName);
            return f;
        } catch (NoSuchFieldException nsfe) {
            List<Class<?>> superClasses = (List<Class<?>>) org.apache.commons.lang.ClassUtils.getAllSuperclasses(clazz);
            for (Class<?> c : superClasses) {
                if (!c.equals(Object.class)) {
                    try {
                        f = c.getDeclaredField(fieldName);
                        return f;
                    } catch (NoSuchFieldException nsfe2) {
                        // Ignore
                        // getLogger().debug("Field " + fieldName + " not found in " + c.getName());
                    }
                }
            }
            throw nsfe;
        }
    }
    
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>[] args) {
        return BeanUtils.findMethod(clazz, methodName, args);
    }
    
    /**
     * Get the name of the setter for a property.
     * @param propertyName The name of the property.
     * @return <code>String</code>.
     */
    public static String getSetterName(String propertyName) {
        return "set" + propertyCaseChange(propertyName);
    }
    
    /**
     * Get the name of the getter method for a property.
     * @param propertyName The name of the property.
     * @param useIs Should "is" be used as the prefix, e.g. for boolean properties.
     * @return <code>String</code>.
     */
    public static String getGetterName(String propertyName, boolean useIs) {
        return (useIs ? "is" : "get") + propertyCaseChange(propertyName);
    }
    
    private static String propertyCaseChange(String propertyName) {
        return propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }
}
