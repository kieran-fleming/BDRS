package au.com.gaiaresources.bdrs.service.bulkdata;



/**
 * Exception that is thrown when an attempt is made to add a record to a survey
 * that does not contain the specified species. 
 *
 * @author benk
 *
 */
public class InvalidSurveySpeciesException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public InvalidSurveySpeciesException() {
        super();
    }

    public InvalidSurveySpeciesException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public InvalidSurveySpeciesException(String arg0) {
        super(arg0);
    }

    public InvalidSurveySpeciesException(Throwable arg0) {
        super(arg0);
    }
}