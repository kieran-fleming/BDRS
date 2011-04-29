package au.com.gaiaresources.bdrs.model.threshold;

import au.com.gaiaresources.bdrs.service.threshold.SimpleTypeOperator;

/**
 * Operations that may be applied to determine if an object matches a 
 * {@link Condition}  
 */
public enum Operator implements SimpleTypeOperator {
    EQUALS("is equal to"),
    CONTAINS("contains");
    
    private String displayText;

    Operator(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return displayText;
    }
}
