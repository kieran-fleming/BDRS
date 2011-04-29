package au.com.gaiaresources.bdrs.message;

import org.springframework.validation.FieldError;

/**
 * 
 * @author Tim Carpenter
 *
 */
public class FieldMessage extends FieldError {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FieldMessage(String objectName, String fieldName, Object badValue, String code, Object[] args) {
        super(objectName, fieldName, badValue, false, new String[] {code}, args, code);
    }
}
