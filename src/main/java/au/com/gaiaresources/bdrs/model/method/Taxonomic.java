package au.com.gaiaresources.bdrs.model.method;

import java.io.IOException;
import java.io.Writer;

import au.com.gaiaresources.bdrs.json.JSONEnum;
import au.com.gaiaresources.bdrs.json.JSONEnumUtil;

public enum Taxonomic implements JSONEnum {
    TAXONOMIC("Taxonomic"), 
    NONTAXONOMIC("Non Taxonomic"), 
    OPTIONALLYTAXONOMIC("Optionally Taxonomic");

    private String name;

    private Taxonomic(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
