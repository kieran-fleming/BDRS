package au.com.gaiaresources.bdrs.db;

public enum QueryOperation {
    EQUAL, 
    NOT_EQUAL,
    LIKE,
    ILIKE,
    LESS_THAN, 
    LESS_THAN_OR_EQUAL, 
    GREATER_THAN, 
    GREATER_THAN_OR_EQUAL,
    IS_NULL, 
    IS_NOT_NULL, 
    IN, 
    NOT_IN,
    BETWEEN,
    // Spatial
    WITHIN,
    CONTAINS,
    INTERSECTS;
}
