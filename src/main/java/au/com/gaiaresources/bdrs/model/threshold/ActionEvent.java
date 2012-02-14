package au.com.gaiaresources.bdrs.model.threshold;

import java.io.IOException;
import java.io.Writer;

import au.com.gaiaresources.bdrs.json.JSONEnum;
import au.com.gaiaresources.bdrs.json.JSONEnumUtil;

/**
 * Represents the database events on which threshold actions can be run.
 * @author stephanie
 */
public enum ActionEvent implements JSONEnum {
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
    
    @Override
    public void writeJSONString(Writer out) throws IOException {
        JSONEnumUtil.writeJSONString(out, this);
    }

    @Override
    public String toJSONString() {
        return JSONEnumUtil.toJSONString(this);
    }
}
