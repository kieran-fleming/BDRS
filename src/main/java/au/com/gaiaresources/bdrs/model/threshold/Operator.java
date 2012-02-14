package au.com.gaiaresources.bdrs.model.threshold;

import java.io.IOException;
import java.io.Writer;

import au.com.gaiaresources.bdrs.json.JSONEnum;
import au.com.gaiaresources.bdrs.json.JSONEnumUtil;
import au.com.gaiaresources.bdrs.service.threshold.SimpleTypeOperator;

/**
 * Operations that may be applied to determine if an object matches a 
 * {@link Condition}  
 */
public enum Operator implements SimpleTypeOperator, JSONEnum {
    EQUALS("is equal to"),
    CONTAINS("contains");
    
    private String displayText;

    Operator(String displayText) {
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
