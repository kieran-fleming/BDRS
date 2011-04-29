package au.com.gaiaresources.bdrs.servlet.jsp.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import au.com.gaiaresources.bdrs.security.RecaptchaService;

public class RecaptchaTag extends TagSupport {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public int doStartTag() throws JspException {
        String sessionID = pageContext.getSession().getId();
        RecaptchaService.getInstance().start(sessionID);
        try {
            RecaptchaService.getInstance().render(sessionID, pageContext.getOut());
        } catch (IOException ioe) {
            throw new JspException("Failed to render reCAPTCHA", ioe);
        }
        return SKIP_BODY;
    }
    
    @Override
    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }
}
