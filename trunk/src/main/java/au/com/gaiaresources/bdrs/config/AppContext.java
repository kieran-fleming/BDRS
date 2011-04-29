package au.com.gaiaresources.bdrs.config;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class AppContext implements ApplicationContextAware {
    private static ApplicationContext context;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
    
    public static ApplicationContext getApplicationContext() {
        return context;
    }
    
    @SuppressWarnings("unchecked")
    public static <C> C getBean(Class<C> clazz) {
        Map<?, ?> beans = context.getBeansOfType(clazz);
        if (beans.size() == 1) {
            return (C) beans.entrySet().iterator().next().getValue();
        }
        throw new IllegalStateException("Unique bean of class " + clazz.getName() 
                                      + " not found, " + beans.size() + " were found.");
    }
}
