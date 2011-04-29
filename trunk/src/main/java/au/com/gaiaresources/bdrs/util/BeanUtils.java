package au.com.gaiaresources.bdrs.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 
 * @author Tim Carpenter
 * 
 */
public final class BeanUtils {
	private BeanUtils() {
	}

	public static Object extractProperty(Object bean, String property)
			throws NoSuchFieldException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		if (property.indexOf('.') > 0) {
			int firstDot = property.indexOf('.');
			String firstProperty = property.substring(0, firstDot);
			Object o = extractProperty(bean, firstProperty);
			return extractProperty(o, property.substring(firstDot + 1));
		}
		Method m = ClassUtils.getGetterMethod(bean.getClass(), property);
		m.setAccessible(true);
		return m.invoke(bean);
	}

	public static Object injectProperty(Object bean, String property, Object arg)
			throws NoSuchFieldException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		String methodName = property.substring(0, 1).toUpperCase() + property.substring(1);
		
		Method[] methods = bean.getClass().getMethods();
		for (Method m : methods)
		{
			if (m.getName().equals("set" + methodName))
			{
				m.setAccessible(true);
				Object[] args = new Object[]{arg};
				//args[0] = arg;
				return m.invoke(bean, args);
			}
			
		}
		return null;
	}
}
