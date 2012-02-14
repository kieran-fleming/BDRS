package au.com.gaiaresources.bdrs.db;

import java.io.IOException;
import java.io.Writer;

import au.com.gaiaresources.bdrs.json.JSONEnum;
import au.com.gaiaresources.bdrs.json.JSONEnumUtil;

public enum QueryOperation implements JSONEnum {
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

    @Override
    public void writeJSONString(Writer out) throws IOException {
        JSONEnumUtil.writeJSONString(out, this);
    }

    @Override
    public String toJSONString() {
        return JSONEnumUtil.toJSONString(this);
    }
}
