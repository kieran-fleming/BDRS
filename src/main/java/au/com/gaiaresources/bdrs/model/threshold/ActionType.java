package au.com.gaiaresources.bdrs.model.threshold;

import java.io.IOException;
import java.io.Writer;

import au.com.gaiaresources.bdrs.json.JSONEnum;
import au.com.gaiaresources.bdrs.json.JSONEnumUtil;

/**
 * Represents the types of actions that can be taken.
 */
public enum ActionType implements JSONEnum {
    /**
     * Send an email.
     */
    EMAIL_NOTIFICATION("Send an email to"),
    /**
     * Send a moderation email.
     */
    MODERATION_EMAIL_NOTIFICATION("Send a moderation email"),
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
    
    @Override
    public void writeJSONString(Writer out) throws IOException {
        JSONEnumUtil.writeJSONString(out, this);
    }

    @Override
    public String toJSONString() {
        return JSONEnumUtil.toJSONString(this);
    }
}
