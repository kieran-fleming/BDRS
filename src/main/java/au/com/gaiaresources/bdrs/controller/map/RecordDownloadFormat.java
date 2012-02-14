package au.com.gaiaresources.bdrs.controller.map;

import java.io.IOException;
import java.io.Writer;

import au.com.gaiaresources.bdrs.json.JSONEnum;
import au.com.gaiaresources.bdrs.json.JSONEnumUtil;

public enum RecordDownloadFormat implements JSONEnum {
    KML,
    SHAPEFILE,
    XLS;
    
    @Override
    public void writeJSONString(Writer out) throws IOException {
        JSONEnumUtil.writeJSONString(out, this);
    }

    @Override
    public String toJSONString() {
        return JSONEnumUtil.toJSONString(this);
    }
}
