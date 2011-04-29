package au.com.gaiaresources.bdrs.servlet.jsp.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;

@Configurable
public class GetManagedFileTag extends TagSupport {

    private static final long serialVersionUID = 1L;
    private String uuid;
    private String var; 
    
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(this.getClass());
    
    public int doEndTag() throws JspException {
        
        // unfortunately we can't use spring for dependency injection in a tag
        // as spring does not manage these classes.
        // so...we'll use ApplicationContext.getBean like a DAO factory 
        ApplicationContext ac = WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext());
        ManagedFileDAO managedFileDAO = ac.getBean(ManagedFileDAO.class);
       
        ManagedFile mf = managedFileDAO.getManagedFile(uuid);
        log.info(uuid);
        log.info(var);
        
        pageContext.setAttribute(var, mf);

        return EVAL_PAGE;
    }

    public String geUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }
}
