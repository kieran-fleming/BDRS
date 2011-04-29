package au.com.gaiaresources.bdrs.service.bulkdata;



/**
 * Exception that is thrown when attempting to bulk save Records that
 * is missing required data.
 *
 * @author benk
 *
 */
public class MissingDataException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public MissingDataException() {
        super();
    }

    public MissingDataException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public MissingDataException(String arg0) {
        super(arg0);
    }

    public MissingDataException(Throwable arg0) {
        super(arg0);
    }
}