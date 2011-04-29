package au.com.gaiaresources.bdrs.servlet.jsp.tag;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import au.com.gaiaresources.bdrs.model.content.ContentDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.template.TemplateService;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

@Configurable
public class GetContentTag extends TagSupport {

    private static final long serialVersionUID = 1L;
    private String key;
    
    private Logger log = Logger.getLogger(this.getClass());

    public int doEndTag() throws JspException {
        
        // unfortunately we can't use spring for dependency injection in a tag
        // as spring does not manage these classes.
        // so...we'll use ApplicationContext.getBean like a DAO factory 
        ApplicationContext ac = WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext());
        ContentDAO contentDAO = ac.getBean(ContentDAO.class);
        TemplateService templateService = ac.getBean(TemplateService.class);
       
        String value = contentDAO.getContentValue(key);
        if (value == null) {
            value = new String("Error: Could not fetch content for key: " + key + ". Inform the webmaster");
        }
        
        Portal portal = RequestContextHolder.getContext().getPortal();
        User currentUser = RequestContextHolder.getContext().getUser();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("portalName", portal.getName());
        if (currentUser != null) {
            params.put("currentUserFirstName", currentUser.getFirstName());
            params.put("currentUserLastName", currentUser.getLastName());
        }
        
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
