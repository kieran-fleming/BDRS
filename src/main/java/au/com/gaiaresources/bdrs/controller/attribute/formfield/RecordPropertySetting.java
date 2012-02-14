package au.com.gaiaresources.bdrs.controller.attribute.formfield;

import java.io.IOException;
import java.io.Writer;

import au.com.gaiaresources.bdrs.json.JSONEnum;
import au.com.gaiaresources.bdrs.json.JSONEnumUtil;

/**
 * Possible settings that a <code>RecordPropertyType</code> can have.
 * 
 * @author timo
 * 
 */
public enum RecordPropertySetting implements JSONEnum {
    WEIGHT, DESCRIPTION, HIDDEN, SCOPE, REQUIRED;

    @Override
    public void writeJSONString(Writer out) throws IOException {
        JSONEnumUtil.writeJSONString(out, this);
    }

    @Override
    public String toJSONString() {
        return JSONEnumUtil.toJSONString(this);
    }
}
