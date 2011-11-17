package au.com.gaiaresources.bdrs.servlet.jsp.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringEscapeUtils;

import au.com.gaiaresources.bdrs.util.StringUtils;

/**
 * Tag to check if an HTML String is valid and print the String when valid and 
 * escape the String if not valid to avoid breaking the entire page with invalid 
 * HTML.
 * 
 * @author stephanie
 */
public class ValidateHtml extends TagSupport {

    private String html;
    
    @Override
    public int doStartTag() throws JspException {
        JspWriter out = pageContext.getOut();
        
        try {
            String tidied = StringUtils.validateHtml(html);
            if (tidied == null) {
                // invalid hmtl, escape before adding to page
                html = StringEscapeUtils.escapeHtml(html);
            } else {
                html = tidied;
            }
            
            out.write(html);
        } catch (Exception e) {
            throw new JspException("Failed to escape html " + html, e);
        }
        
        return SKIP_BODY;
    }
    
    @Override
    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }
    
    
    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

}
