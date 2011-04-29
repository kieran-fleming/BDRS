package au.com.gaiaresources.bdrs.controller.record;

public enum ValidationType {

    STRING,
    REQUIRED_BLANKABLE_STRING,
    REQUIRED_NONBLANK_STRING,

    REQUIRED_TIME,
    
    INTEGER, 
    REQUIRED_INTEGER,
    PRIMARYKEY, 
    REQUIRED_POSITIVE_INT,
    // LT = less than
    REQUIRED_POSITIVE_LESSTHAN,
    
    DOUBLE,
    REQUIRED_DOUBLE,
    REQUIRED_DEG_LONGITUDE,
    REQUIRED_DEG_LATITUDE,
    
    DATE,
    REQUIRED_DATE, 
    REQUIRED_HISTORICAL_DATE, 
    
    REQUIRED_TAXON;
    
}
