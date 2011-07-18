package au.com.gaiaresources.bdrs.servlet.jsp.tag;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import au.com.gaiaresources.bdrs.model.theme.Theme;
import au.com.gaiaresources.bdrs.model.theme.ThemeElement;
import au.com.gaiaresources.bdrs.service.template.TemplateService;

/**
 * Performs the rendering of themable blocks by delegating templates to the
 * template service if a template exists, or falling back to a specified default
 * implementation.
 */
@SuppressWarnings("serial")
@Configurable
public class GetThemeTemplateTag extends TagSupport {
    
    private Formatter formatter = new Formatter();
    
    /**
     * The key of the theme element containing the name of the template to use.
     */
    private String key;
    /**
     * The default implementation of this block. The fallback must be a relative
     * url to a JSP template within the web context.
     */
    private String fallback;
    
    private Logger log = Logger.getLogger(this.getClass());

    @SuppressWarnings("unchecked")
    public int doEndTag() {

        try {
            Map<String, ThemeElement> themeMap = getAttribute("themeMap", Map.class);
            ThemeElement themeElement = themeMap.get(getKey());
            Theme theme = getAttribute("theme", Theme.class);

            if (theme == null || themeElement == null) {
                log.warn("Cannot find Theme or ThemeElement. Rendering fallback template.");
                log.warn("Theme = " + theme);
                log.warn("key = " + getKey());
                log.warn("ThemeElement = " + themeElement);
                renderFallback();
            } else {

                TemplateService templateService = getBean(TemplateService.class);

                String path = new File(Theme.THEME_DIR_PROCESSED,
                        themeElement.getCustomValue()).getPath();
                templateService.mergeTemplate(theme, path, getAttributes(theme), pageContext.getOut());
            }
        } catch (Exception ve) {
            log.error(ve);
            log.error(ve.getMessage());
            renderFallback();
        }
        return EVAL_PAGE;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFallback() {
        return fallback;
    }

    public void setFallback(String fallback) {
        this.fallback = fallback;
    }

    private void renderFallback() {
        try {
            pageContext.include(getFallback());
        } catch (ServletException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }
    }

    private Map<String, Object> getAttributes(Theme theme) {
        Map<String, Object> attributeMap = new HashMap<String, Object>();

        getAttributes(PageContext.APPLICATION_SCOPE, attributeMap);
        getAttributes(PageContext.SESSION_SCOPE, attributeMap);
        getAttributes(PageContext.REQUEST_SCOPE, attributeMap);
        getAttributes(PageContext.PAGE_SCOPE, attributeMap);

        attributeMap.put("pageContext", pageContext);
        attributeMap.put("formatter", formatter);

        return attributeMap;
    }

    @SuppressWarnings("unchecked")
    private void getAttributes(int scope, Map<String, Object> attributeMap) {
        Enumeration<String> attrNames = pageContext.getAttributeNamesInScope(scope);
        for (String name = attrNames.nextElement(); attrNames.hasMoreElements(); name = attrNames.nextElement()) {
            attributeMap.put(name, pageContext.findAttribute(name));
        }

    }

    @SuppressWarnings("unchecked")
    private <T> T getAttribute(String key, Class<T> klass) {
        Object obj = pageContext.findAttribute(key);
        return obj == null ? null : (T) obj;
    }

    private <T> T getBean(Class<T> klass) {
        ServletContext servletContext = pageContext.getServletContext();
        ApplicationContext ac = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        return ac.getBean(klass);
    }
}
