package au.com.gaiaresources.bdrs.message;

import org.springframework.validation.ObjectError;

public class Message extends ObjectError {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String OBJECT_NAME = "Request";
    /**
     * Create a new instance of the ObjectError class.
     * @param objectName the name of the affected object
     * @param defaultMessage the default message to be used to resolve this message
     */
    public Message(String defaultMessage) {
        super(OBJECT_NAME, new String[] {defaultMessage}, null, defaultMessage);
    }

    /**
     * Create a new instance of the ObjectError class.
     * @param objectName the name of the affected object
     * @param codes the codes to be used to resolve this message
     * @param arguments the array of arguments to be used to resolve this message
     * @param defaultMessage the default message to be used to resolve this message
     */
    public Message(String code, Object[] arguments, String defaultMessage) {
        super(OBJECT_NAME, new String[] {code}, arguments, defaultMessage);
    }
    
    /**
     * Create a new instance of the ObjectError class.
     * 
     * @param code - the code used to resolve the message. is also used for the
     * default message
     * @param arguments - the array of arguments to be used to resolve this message 
     */
    public Message(String code, Object[] arguments) {
        super(OBJECT_NAME, new String[] {code}, arguments, code);
    }
    
    public String toString() {
        return resolvableToString();
    }
}
