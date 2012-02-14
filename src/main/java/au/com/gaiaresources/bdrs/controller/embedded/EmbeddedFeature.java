package au.com.gaiaresources.bdrs.controller.embedded;

import java.io.IOException;
import java.io.Writer;

import au.com.gaiaresources.bdrs.json.JSONEnum;
import au.com.gaiaresources.bdrs.json.JSONEnumUtil;

public enum EmbeddedFeature implements JSONEnum {
    LATEST_STATISTICS("Latest Statistics"),
    IMAGE_SLIDESHOW("Image Slideshow");
    
    private String name;

    EmbeddedFeature(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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
