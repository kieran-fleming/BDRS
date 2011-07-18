package au.com.gaiaresources.bdrs.service.bulkdata;


/**
 * Exception that is thrown when attempting to bulk save Records that
 * contain references to other records or other census methods (or anything
 * else in the future) that cause the bulk upload to fail.
 *
 * @author alow
 *
 */
public class DataReferenceException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DataReferenceException() {
        super();
    }

    public DataReferenceException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public DataReferenceException(String arg0) {
        super(arg0);
    }

    public DataReferenceException(Throwable arg0) {
        super(arg0);
    }
}
