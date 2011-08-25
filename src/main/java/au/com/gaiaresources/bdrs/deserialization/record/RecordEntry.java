package au.com.gaiaresources.bdrs.deserialization.record;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.vividsolutions.jts.geom.Geometry;

public class RecordEntry {
    
    Map<String, String[]> dataMap;
    Map<String, MultipartFile> fileMap;
    
    Geometry geometry = null;
    String description = "";

    /**
     * 
     * @param dataMap - cannot be null
     * @param fileMap - can be null
     */
    public RecordEntry(Map<String, String[]> dataMap, Map<String, MultipartFile> fileMap) {
        if (dataMap == null) {
            throw new IllegalArgumentException("dataMap cannot be null");
        }
        if (fileMap == null) {
            throw new IllegalArgumentException("fileMap cannot be null");
        }
        this.dataMap = dataMap;
        this.fileMap = fileMap;
    }
    
    public RecordEntry(Map<String, String[]> dataMap) {
        this(dataMap, new HashMap<String, MultipartFile>());
    }
    
    public Map<String, String[]> getDataMap() {
        return this.dataMap;
    }
    
    public Map<String, MultipartFile> getFileMap() {
        return this.fileMap;
    }
    
    /**
     * Returns the first value in the array - just to keep the code 
     * cleaner in the client.
     * 
     * @param key
     * @return
     */
    public String getValue(String key) {
        String[] value = dataMap.get(key);
        if (value == null || value.length == 0) {
            return null;
        }
        return value[0];
    }
    
    /**
     * Some methods of record entry may produce a geometry object directly.
     * In this case populate this field and use it when populating the 
     * actual record object.
     * 
     * May be null. This indicates that the geometry is indicated in the
     * parameter map, possibly by long/lat or WKT string.
     * 
     * @return
     */
    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
    
    /**
     * Some piece of context information about the RecordEntry.
     * For example the shapefile object id, or spreadsheet row number
     * 
     * @return
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
