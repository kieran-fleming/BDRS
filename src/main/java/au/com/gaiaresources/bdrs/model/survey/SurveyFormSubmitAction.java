package au.com.gaiaresources.bdrs.model.survey;

import java.io.IOException;
import java.io.Writer;

import au.com.gaiaresources.bdrs.json.JSONEnum;
import au.com.gaiaresources.bdrs.json.JSONEnumUtil;

/**
 * Enum for defining the action to be taken after posting
 * a survey form.
 *
 */
public enum SurveyFormSubmitAction implements JSONEnum {
    
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
    
    @Override
    public void writeJSONString(Writer out) throws IOException {
        JSONEnumUtil.writeJSONString(out, this);
    }

    @Override
    public String toJSONString() {
        return JSONEnumUtil.toJSONString(this);
    }
}
