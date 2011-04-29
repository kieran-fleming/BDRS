package au.com.gaiaresources.bdrs.servlet.view;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

public class KMLViewResolver implements ViewResolver, Ordered {
    private int order;
    @Autowired
    private KMLView kmlView;
    
    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        if (KMLView.DEFAULT_VIEW_NAME.equals(viewName)) {
            return kmlView;
        }
        return null;
    }
    
    @Override
    public int getOrder() {
        return order;
    }
    public void setOrder(int order) {
        this.order = order;
    }
}
