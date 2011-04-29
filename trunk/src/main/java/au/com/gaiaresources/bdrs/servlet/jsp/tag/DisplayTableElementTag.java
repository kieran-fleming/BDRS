package au.com.gaiaresources.bdrs.servlet.jsp.tag;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

public class DisplayTableElementTag extends DisplayElementTag {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    protected void start(TYPE type, Object value) throws IOException {
        JspWriter out = pageContext.getOut();
        
        if (value instanceof Number) {
            out.write("<td style=\"text-align: right;\">");
        } else {
            out.write("<td>");
        }
    }
    
    @Override
    protected void end(TYPE type) throws IOException {
        JspWriter out = pageContext.getOut();
        out.write("</td>");
    }
}
