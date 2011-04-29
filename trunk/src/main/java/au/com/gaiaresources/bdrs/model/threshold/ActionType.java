package au.com.gaiaresources.bdrs.model.threshold;

/**
 * Represents the types of actions that can be taken.
 */
public enum ActionType {
    /**
     * Send an email.
     */
    EMAIL_NOTIFICATION("Send an email to"),
    /**
     * Set the held property of the record to <code>true</code>.
     */
    HOLD_RECORD("Hold Record");

    private String displayText;

    /**
     * Creates a new ActionType.
     * 
     * @param displayText
     *            the name to display when the action type is rendered to users.
     */
    ActionType(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return displayText;
    }
}
