package au.com.gaiaresources.bdrs.model.survey;

/**
 * Enum for defining the action to be taken after posting
 * a survey form.
 *
 */
public enum SurveyFormSubmitAction {
    
    MY_SIGHTINGS("Redirect to My Sightings"),
    STAY_ON_FORM("Stay on the current form");
    
    private String name;
    
    /**
     * Create a new enum object
     * @param name Name of the object
     */
    private SurveyFormSubmitAction(String name) {
        this.name = name;
    }
    
    /**
     * Gets the name of the enum
     * @return name of the enum
     */
    public String getName() {
        return this.name;
    }
}
