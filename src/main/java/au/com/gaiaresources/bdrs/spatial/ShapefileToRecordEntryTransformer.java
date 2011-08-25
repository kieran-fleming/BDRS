package au.com.gaiaresources.bdrs.spatial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

import au.com.gaiaresources.bdrs.deserialization.record.RecordEntry;
import au.com.gaiaresources.bdrs.deserialization.record.RecordKeyLookup;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.util.DateFormatter;

import com.vividsolutions.jts.geom.Geometry;

public class ShapefileToRecordEntryTransformer {
    
    private Logger log = Logger.getLogger(getClass());
    
    RecordKeyLookup lookup;
    
    public ShapefileToRecordEntryTransformer(RecordKeyLookup lookup) {
        if (lookup == null) {
            throw new IllegalArgumentException("arg lookup can't be null");
        }
        this.lookup = lookup;
    }

    /**
     * 
     * @param iter
     * @param surveyIdList
     * @param censusMethodIdList
     * @return
     */
    public List<RecordEntry> shapefileFeatureToRecordEntries(Iterator<SimpleFeature> iter, List<Integer> surveyIdList, List<Integer> censusMethodIdList) {
        if (iter == null) {
            throw new IllegalArgumentException("iter cannot be null");
        }        
        if (surveyIdList == null) {
            throw new IllegalArgumentException("List<Integer>, surveyIdList cannot be null");
        }
        if (censusMethodIdList == null) {
            throw new IllegalArgumentException("List<Integer>, cenesusMethodIdList cannot be null");
        }
        
        List<RecordEntry> result = new ArrayList<RecordEntry>();
        
        while (iter.hasNext()) {
            SimpleFeature feature = iter.next();
            
            Map<String, String[]> dmap = new HashMap<String, String[]>();
            
            Collection<Property> properties = feature.getProperties();
            
            // Transfer the properties in the feature
            for (Property prop : properties) {
                
                String key = prop.getName().toString();
                
                if (dmap.get(key) == null) {
                    if (prop.getType().getBinding() == String.class) {
                        String[] value = new String[] { ((String)(prop.getValue())).trim() };
                        dmap.put(key, value);
                    } else if (prop.getType().getBinding() == Integer.class) {
                        String[] value = new String[] { ((Integer)(prop.getValue())).toString().trim() };
                        dmap.put(key, value);
                    } else if (prop.getType().getBinding() == Date.class) {
                        Date date = (Date)prop.getValue();
                        String[] value = new String[] { DateFormatter.format(date, DateFormatter.DAY_MONTH_YEAR) };
                        dmap.put(key, value);
                    } else if (prop.getType().getBinding() == Double.class) {
                        String[] value = new String[] { ((Double)(prop.getValue())).toString().trim() };
                        dmap.put(key, value);
                    } else {
                        log.error("unrecognized type, ignoring");
                        continue;
                    }
                } else {
                    log.warn("key: " + key + " already exists in data map. Ignoring new value.");
                }
            }
            
            // Handle any default values
            // If survey key is not set, set it to the default if there is one specified.
            if (!dmap.containsKey(lookup.getSurveyIdKey()) && surveyIdList.size() == 1 && surveyIdList.get(0) != null) {
                dmap.put(lookup.getSurveyIdKey(), new String[] { surveyIdList.get(0).toString() } );
                if (!dmap.containsKey(lookup.getCensusMethodIdKey()) && censusMethodIdList.size() == 1) {
                    dmap.put(lookup.getCensusMethodIdKey(), new String[] { censusMethodIdList.get(0).toString() });
                }
            } else {
                throw new IllegalStateException("Cannot have no survey set in the map feature and no default survey to set the record to.");
            }
            
            RecordEntry entry = new RecordEntry(dmap);
            entry.setGeometry((Geometry)feature.getDefaultGeometry());
            entry.setDescription("Object ID: " + feature.getID());
            
            result.add(entry);
        }
        return result;
    }
}
