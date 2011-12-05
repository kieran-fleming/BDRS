package au.com.gaiaresources.bdrs.model.threshold;

/**
 * Represents the database events on which threshold actions can be run.
 * @author stephanie
 */
public enum ActionEvent {
    /**
     * On creation or update of a class for which a threshold is set.
     */
    CREATE_AND_UPDATE("On Create and Update"),
    /**
     * On creation of a class for which a threshold is set.
     */
    CREATE("On Create"),
    /**
     * On update of a class for which a threshold is set.
     */
    UPDATE("On Update");

    private String displayText;

    /**
     * Creates a new ActionEvent.
     * 
     * @param displayText
     *            the text to display when the action event is rendered to users.
     */
    ActionEvent(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return displayText;
    }
}
