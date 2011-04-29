package au.com.gaiaresources.bdrs.servlet.jsp.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Clean Sql Code
 * @author Kehan Harman
 */
public class CleanSqlTag extends TagSupport {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private String sql;


	@Override
    public int doStartTag() throws JspException {
        JspWriter out = pageContext.getOut();
        
        try {
            /**
             * Replace all single and double quotes with an escaped version (add's \ before the quote).
             */
            out.write(sql.replaceAll("([\"'])", "\\\\$1"));
        } catch (Exception e) {
            throw new JspException("Failed to escape sql " + sql, e);
        }
        
        return SKIP_BODY;
    }
    
    @Override
    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

    
    public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}
}
