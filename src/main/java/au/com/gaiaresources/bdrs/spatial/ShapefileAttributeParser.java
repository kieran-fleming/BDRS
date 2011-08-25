package au.com.gaiaresources.bdrs.spatial;

import java.util.Map;

import au.com.gaiaresources.bdrs.deserialization.record.AttributeParser;

public class ShapefileAttributeParser extends AttributeParser {

    @Override
    public String getTimeValue(String timeKey, String timeHourKey,
            String timeMinuteKey, Map<String, String[]> parameterMap) {
        
        if (timeKey == null) {
            throw new IllegalArgumentException("arg timeKey cannot be null");
        }
        if (parameterMap == null) {
            throw new IllegalArgumentException("arg parmeterMap cannot be null");
        }
        return getParameter(parameterMap, timeKey);
    }
}
