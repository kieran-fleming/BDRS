package au.com.gaiaresources.bdrs.controller.survey;

import java.io.IOException;
import java.io.Writer;

import au.com.gaiaresources.bdrs.json.JSONEnum;
import au.com.gaiaresources.bdrs.json.JSONEnumUtil;

public enum UserSelectionType implements JSONEnum {

    ALL_USERS("ALL", "All Users", "All Registered Users"),
    SELECTED_USERS("SELECTED", "A Selection of Users", "e.g. John Smith, Jane Doe");

    private String code;
    private String name;
    private String tip;

    private UserSelectionType(String code, String name, String tip) {
        this.code = code;
        this.name = name;
        this.tip = tip;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getTip() {
        return tip;
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

