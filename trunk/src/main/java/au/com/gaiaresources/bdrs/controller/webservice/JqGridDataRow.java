package au.com.gaiaresources.bdrs.controller.webservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JqGridDataRow {
    private Integer id;
    private Map<String, String> values;
    
    public JqGridDataRow(Integer id) {
        this.values = new HashMap<String, String>();
        this.id = id;
        this.addValue("id", id.toString());
    }
    
    public JqGridDataRow addValue(String key, String value) {
        values.put(key, value);
        return this;
    }
    
    public Integer getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public Map<String, String> getValueMap() {
        return this.values;
    }
}