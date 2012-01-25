package au.com.gaiaresources.bdrs.controller.report.python.model;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.ScrollableRecords;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

/**
 * Acts as a wrapper around {@link ScrollableRecords} returning JSON encoded
 * strings rather than {@link Record}s.
 */
public class PyScrollableRecords implements Enumeration<String>{

    private boolean includeLocation;
    private boolean includeTaxon;
    private boolean includeAttributeValues;
    
    private ScrollableRecords scrollableRecords;
    
    private int count = 0;
    

    /**
     * Creates a new instance.
     * 
     * @param scrollableRecords the base enumeration to be wrapped.
     * @param includeTaxon true if the serialized record should contain a serialized taxon, or false if only a primary key is required.
     * @param includeLocation true if the serialized record should contain a serialized location, or false if only a primary key is required.
     */
    public PyScrollableRecords(ScrollableRecords scrollableRecords,
            boolean includeTaxon, boolean includeLocation) {
        
        this(scrollableRecords, includeTaxon, includeLocation, false);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param scrollableRecords the base enumeration to be wrapped.
     * @param includeTaxon true if the serialized record should contain a serialized taxon, or false if only a primary key is required.
     * @param includeLocation true if the serialized record should contain a serialized location, or false if only a primary key is required.
     * @param includeAttributeValues true if the serialized record should contain serialized attribute values, or false if only a primary key is required.
     */
    public PyScrollableRecords(ScrollableRecords scrollableRecords,
            boolean includeTaxon, boolean includeLocation,
            boolean includeAttributeValues) {
        
        this.scrollableRecords = scrollableRecords;
        this.includeTaxon = includeTaxon;
        this.includeLocation = includeLocation;
        this.includeAttributeValues = includeAttributeValues;
    }

    /* (non-Javadoc)
     * @see java.util.Enumeration#hasMoreElements()
     */
    @Override
    public boolean hasMoreElements() {
        return scrollableRecords.hasMoreElements();
    }

    /* (non-Javadoc)
     * @see java.util.Enumeration#nextElement()
     */
    @Override
    public String nextElement() {
        Record rec = scrollableRecords.nextElement();
        Map<String, Object> recFlatten = rec.flatten();
        
        if(includeTaxon) {
            IndicatorSpecies species = rec.getSpecies();
            if(species != null) {
                recFlatten.put("species", species.flatten());
            }
        }
        
        if(includeLocation) {
            Location loc = rec.getLocation();
            if(loc != null) {
                recFlatten.put("location", loc.flatten());
            }
        }
        
        if(includeAttributeValues) {
            List<Map<String, Object>> attrValList = new ArrayList<Map<String, Object>>();
            for(AttributeValue attrVal : rec.getAttributes()) {
                attrValList.add(attrVal.flatten());
            }
            recFlatten.put("attributes", attrValList);
        }
        
        String jsonStr = JSONObject.fromObject(recFlatten).toString();
        
        // evict to ensure garbage collection
        if (++count % ScrollableRecords.RECORD_BATCH_SIZE == 0) {
            RequestContextHolder.getContext().getHibernate().clear();
        }
        
        return jsonStr;
    }
}
