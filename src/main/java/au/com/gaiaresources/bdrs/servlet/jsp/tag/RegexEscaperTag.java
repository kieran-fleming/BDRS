package au.com.gaiaresources.bdrs.servlet.jsp.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import au.com.gaiaresources.bdrs.util.StringUtils;

/**
 * Tag to escape a regular expression for javascript validation.
 * 
 * For example, '\d' when stringified for validation results in 'd' which is 
 * not what you want.  This tag escapes the '\d' with another '\' so it is returned 
 * to the jsp as '\\d' and is evaluated as the regex you want: '\d'.
 * 
 * @author stephanie
 */
public class RegexEscaperTag extends TagSupport {
    private String regex;
    
    @Override
    public int doStartTag() throws JspException {
        JspWriter out = pageContext.getOut();
        
        try {
            regex = StringUtils.escapeRegex(regex);
            out.write(regex);
        } catch (Exception e) {
            throw new JspException("Failed to escape regex " + regex, e);
        }
        
        return SKIP_BODY;
    }
    
    @Override
    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }
    
    
    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }
}
