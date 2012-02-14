package au.com.gaiaresources.bdrs.controller.survey;

import java.io.IOException;
import java.io.Writer;

import au.com.gaiaresources.bdrs.json.JSONEnum;
import au.com.gaiaresources.bdrs.json.JSONEnumUtil;

public enum SpeciesListType implements JSONEnum {

    ONE_SPECIES("ONE", "A Single Species", "e.g. Carnaby&#39;s Cockatoo"),
    MANY_SPECIES("MANY", "A Selection of Species", "e.g. Carnaby&#39;s Cockatoo, Hooded Plover and Red-rumped Parrot"),
    SPECIES_GROUP("GROUP", "A Related Group of Species", "e.g The Cacatuidae family"),
    ALL_SPECIES("ALL", "All Species", "All Known Species");

    private String code;
    private String name;
    private String tip;
    
    private SpeciesListType(String code, String name, String tip) {
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

