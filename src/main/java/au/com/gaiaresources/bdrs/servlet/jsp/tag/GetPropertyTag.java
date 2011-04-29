package au.com.gaiaresources.bdrs.servlet.jsp.tag;

import au.com.gaiaresources.bdrs.util.BeanUtils;
import au.com.gaiaresources.bdrs.util.StringUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Dynamically extract a property from a bean.
 * This is an enhancement to <code>jsp:getProperty</code> as this is declared to take
 * an expression for the property name.
 * @author Tim Carpenter
 */
public class GetPropertyTag extends TagSupport {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Object bean;
    private String property;
    
    @Override
    public int doStartTag() throws JspException {
        JspWriter out = pageContext.getOut();
        
        try {
            out.write(StringUtils.toString(BeanUtils.extractProperty(bean, property)));
        } catch (Exception e) {
            throw new JspException("Failed to get property " + property + " from bean of class " 
                                 + bean.getClass().getName(), e);
        }
        
        return SKIP_BODY;
    }
    
    @Override
    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

    public Object getBean() {
        return bean;
    }
    public void setBean(Object bean) {
        this.bean = bean;
    }

    public String getProperty() {
        return property;
    }
    public void setProperty(String property) {
        this.property = property;
    }
}
