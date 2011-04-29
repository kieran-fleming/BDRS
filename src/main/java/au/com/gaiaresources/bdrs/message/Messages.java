package au.com.gaiaresources.bdrs.message;

import org.springframework.validation.AbstractBindingResult;

public class Messages extends AbstractBindingResult {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Messages() {
        super("Request");
    }
    
    @Override
    protected Object getActualFieldValue(String field) {
        return null;
    }

    @Override
    public Object getTarget() {
        return "Request";
    }
}
