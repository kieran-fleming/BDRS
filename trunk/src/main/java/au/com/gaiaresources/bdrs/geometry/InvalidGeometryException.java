package au.com.gaiaresources.bdrs.geometry;

public class InvalidGeometryException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidGeometryException(String message, Throwable cause) {
        super(message, cause);
    }
}
