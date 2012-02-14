package au.com.gaiaresources.bdrs.service.user;

import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;

import au.com.gaiaresources.bdrs.json.JSONEnum;
import au.com.gaiaresources.bdrs.json.JSONEnumUtil;

public class UserMetaData {
    
    public static final String TRUE = "true";
    // really we will consider anything not "TRUE" to be false
    public static final String FALSE = "false";
    
    public enum UserMetaDataType implements JSONEnum
    {
        Boolean,
        String;
        
        @Override
        public void writeJSONString(Writer out) throws IOException {
            JSONEnumUtil.writeJSONString(out, this);
        }

        @Override
        public String toJSONString() {
            return JSONEnumUtil.toJSONString(this);
        }
    }
    
    Logger log = Logger.getLogger(getClass());
    
    private String key;
    private String displayName;
    private UserMetaDataType type;
    private String validation;
    private String value;
    
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    public UserMetaDataType getType() {
        return type;
    }
    public void setType(UserMetaDataType type) {
        this.type = type;
    }
    public void setType(String t) throws IllegalArgumentException{
        String tlower = t.toLowerCase();
        if ("boolean".equals(tlower))
        {
            this.type = UserMetaDataType.Boolean;
        }
        else if ("string".equals(tlower))
        {
            this.type = UserMetaDataType.String;
        }
        else
        {
            this.type = UserMetaDataType.String;
            String k = key != null ? key : ""; 
            log.warn("Setting key: '" + k + "' to the default type String. Type value from xml file is: " + t);
        }
    }
    public String getValidation() {
        return validation;
    }
    public void setValidation(String validation) {
        this.validation = validation;
    }
    public String getValue() {
        return this.value;
    }
    public void setValue(String value) {
        if (this.type == null) {
            throw new IllegalStateException("UserMetaData.type must be set before setting UserMetaData.value");
        }
        switch (getType())
        {
        case Boolean:
            this.value = TRUE.equals(value) ? TRUE : FALSE;
            break;
        case String:
            this.value = value;
            break;
        }
    }
    
    
}
