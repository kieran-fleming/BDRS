package au.com.gaiaresources.bdrs.servlet.jsp.tag;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.theme.Theme;
import au.com.gaiaresources.bdrs.model.theme.ThemeDAO;
import au.com.gaiaresources.bdrs.model.theme.ThemeElement;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.template.TemplateService;
import au.com.gaiaresources.bdrs.service.theme.ThemeService;
import au.com.gaiaresources.bdrs.servlet.BdrsPluginFacade;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

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
    
    private Logger log = Logger.getLogger(this.getClass());
    
    @SuppressWarnings("unchecked")
    public int doEndTag() {
        
        try {
            Map<String, ThemeElement> themeMap = getAttribute("themeMap", Map.class);
            ThemeElement themeElement = themeMap == null ? null : themeMap.get(getKey());
            Theme theme = getAttribute("theme", Theme.class);

            TemplateService templateService = getBean(TemplateService.class);
            Map<String, Object> templateParams = new HashMap<String, Object>();
            String themeElemVal;
            if (theme == null || themeElement == null) {
                log.warn("Cannot find Theme or ThemeElement. Rendering fallback template.");
                log.warn("Theme = " + theme);
                log.warn("key = " + getKey());
                log.warn("ThemeElement = " + themeElement);
                // get the default theme and theme element of the same name
                ThemeDAO themeDAO = getBean(ThemeDAO.class);
                Portal portal = RequestContextHolder.getContext().getPortal();
                theme = themeDAO.getDefaultTheme(portal);
                themeElement = themeDAO.getThemeElement(theme.getId(), getKey());
                //templateParams.putAll(ContentService.getContentParams(portal, null, null));
                themeElemVal = themeElement.getDefaultValue();
            } else {
                themeElemVal = themeElement.getCustomValue();
            }
            templateParams.putAll(getAttributes(theme));
            
            Portal portal = RequestContextHolder.getContext().getPortal();
            User currentUser = RequestContextHolder.getContext().getUser();
            String requestUrl = RequestContextHolder.getContext().getRequestPath();
            
            // Access the bdrs api through 'bdrs' in velocity
            templateParams.put("bdrs", new BdrsPluginFacade(portal, requestUrl, currentUser));
            
            File templateFile = new File(ThemeService.getThemeDirectory(theme), themeElemVal);
            String path = templateFile.getPath();
            templateService.mergeTemplate(theme, path, templateParams, pageContext.getOut());
        } catch (Exception ve) {
            log.error("Problem loading template", ve);
            try {
                pageContext.setAttribute("templateKey", getKey());
                pageContext.include("/WEB-INF/jsp/bdrs/templateError.jsp");
            } catch (Exception e) {
                log.error("Problem loading error template", e);
            }
        }
        return EVAL_PAGE;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
