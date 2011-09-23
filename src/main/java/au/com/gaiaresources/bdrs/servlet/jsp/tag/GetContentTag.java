package au.com.gaiaresources.bdrs.servlet.jsp.tag;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import au.com.gaiaresources.bdrs.model.content.ContentDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.content.ContentService;
import au.com.gaiaresources.bdrs.service.template.TemplateService;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

@Configurable
public class GetContentTag extends TagSupport {

    private static final long serialVersionUID = 1L;
    private String key;
    
    // mark as transient as these are non serializable items
    transient private Logger log = Logger.getLogger(this.getClass());
    transient private ContentService contentService = new ContentService();

    public int doEndTag() throws JspException {
        
        // unfortunately we can't use spring for dependency injection in a tag
        // as spring does not manage these classes.
        // so...we'll use ApplicationContext.getBean for our factory.
        ApplicationContext ac = WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext());
        ContentDAO contentDAO = ac.getBean(ContentDAO.class);
        TemplateService templateService = ac.getBean(TemplateService.class);
        
        Portal portal = RequestContextHolder.getContext().getPortal();
        
        String requestPath = RequestContextHolder.getContext().getRequestPath();
        String value = contentService.getContent(contentDAO, portal, key, contentService.getRequestURL(requestPath));
        
        if (value == null) {
            value = "Error: Could not fetch content for key: " + key + ". Inform the webmaster";
        }
        
        User currentUser = RequestContextHolder.getContext().getUser();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("portalName", portal.getName());
        if (currentUser != null) {
            params.put("currentUserFirstName", currentUser.getFirstName());
            params.put("currentUserLastName", currentUser.getLastName());
        }
        params.put("bdrsContextPath", contentService.getContextPath(requestPath));
        
        value = templateService.evaluate(value, params);

        try {
            pageContext.getOut().print(value);
        } catch (Exception e) {
            throw new JspException(e.toString());
        }
        return EVAL_PAGE;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
