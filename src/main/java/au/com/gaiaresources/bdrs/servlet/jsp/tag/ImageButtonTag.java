package au.com.gaiaresources.bdrs.servlet.jsp.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public abstract class ImageButtonTag extends TagSupport {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String url;
    
    @Override
    public int doStartTag() throws JspException {
        JspWriter out = pageContext.getOut();
        try {
            HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
            if (url.startsWith("javascript=")) {
                out.println("<img src=\"" + req.getContextPath() + getImageUrl() + "\""
                          + " onclick=\"" + url.replaceAll("javascript=", "") + "\"" +   "/>");
            } else {
                out.println("<a href=\"" + getUrl() + "\">" 
                          + "<img src=\"" + req.getContextPath() + getImageUrl() + "\"/>");
            }
        } catch (Exception e) {
            throw new JspException("Failed to write content.", e);
        }
        return SKIP_BODY;
    }
    
    @Override
    public int doEndTag() throws JspException {
        JspWriter out = pageContext.getOut();
        try {
            if (getUrl() != null && !getUrl().startsWith("javascript=")) {
                out.println("</a>");
            }
        } catch (Exception e) {
            throw new JspException("Failed to write content.", e);
        }
        return EVAL_PAGE;
    }
    
    /**
     * Get the URL of the image to display, expected to be relative to the servlet context.
     * Should start with a /.
     * @return {@link String}
     */
    protected abstract String getImageUrl();
    
    public void setUrl(String url) {
        this.url = url;
    }
    public String getUrl() {
        return url;
    }
}
