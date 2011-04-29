package au.com.gaiaresources.bdrs.servlet.jsp.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Strip HTML Code string. 
 * @author Kehan Harman
 */
public class StripHtml extends TagSupport {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private String html;


	@Override
    public int doStartTag() throws JspException {
        JspWriter out = pageContext.getOut();
        
        try {
            out.write(html.replaceAll("\\<.*?>",""));
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
