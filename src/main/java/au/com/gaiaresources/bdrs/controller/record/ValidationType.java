package au.com.gaiaresources.bdrs.controller.record;

import java.io.IOException;
import java.io.Writer;

import au.com.gaiaresources.bdrs.json.JSONEnum;
import au.com.gaiaresources.bdrs.json.JSONEnumUtil;

public enum ValidationType implements JSONEnum {

    STRING,
    REQUIRED_BLANKABLE_STRING,
    REQUIRED_NONBLANK_STRING,
    
    HTML,
    
    BARCODE,
    REQUIRED_BARCODE,

    REGEX,
    REQUIRED_REGEX,
    
    TIME,
    REQUIRED_TIME,
    
    INTEGER, 
    REQUIRED_INTEGER,
    INTEGER_RANGE,
    REQUIRED_INTEGER_RANGE,
    PRIMARYKEY, 
    REQUIRED_POSITIVE_INT,
    // LT = less than
    POSITIVE_LESSTHAN,
    REQUIRED_POSITIVE_LESSTHAN,
    
    DOUBLE,
    REQUIRED_DOUBLE,
    REQUIRED_DEG_LONGITUDE,
    REQUIRED_DEG_LATITUDE,
    DEG_LONGITUDE,
    DEG_LATITUDE,
    
    DATE,
    REQUIRED_DATE, 
    REQUIRED_HISTORICAL_DATE,
    BLANKABLE_HISTORICAL_DATE,
    
    
    REQUIRED_TAXON,
    TAXON, 
    DATE_WITHIN_RANGE,
    REQUIRED_DATE_WITHIN_RANGE;
    
    @Override
    public void writeJSONString(Writer out) throws IOException {
        JSONEnumUtil.writeJSONString(out, this);
    }

    @Override
    public String toJSONString() {
        return JSONEnumUtil.toJSONString(this);
    }
}
