package au.com.gaiaresources.bdrs.json;


/**
 * Simple wrapper around JSON related exceptions that
 * are thrown if there has been an error parsing or accessing a JSON type.
 */
public class JSONException extends RuntimeException {

    private static final long serialVersionUID = -7985078322018385365L;

    /**
     * Creates a new instance.
     */
    public JSONException() {
        super();
    }

    /**
     * @see {@link Exception#Exception(String)} 
     */
    public JSONException(String message) {
        super(message);
    }

    /**
     * @see {@link Exception#Exception(Throwable)} 
     */
    public JSONException(Throwable t) {
        super(t);
    }

    /**
     * @see {@link Exception#Exception(String, Throwable)} 
     */
    public JSONException(String message, Throwable cause) {
        super(message, cause);
    }
}
