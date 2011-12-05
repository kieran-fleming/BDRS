package au.com.gaiaresources.bdrs.controller.report.python.model;

import java.util.Map;

import net.sf.json.JSONArray;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.record.ScrollableRecords;
import au.com.gaiaresources.bdrs.model.record.impl.AdvancedRecordFilter;
import au.com.gaiaresources.bdrs.model.record.impl.RecordFilter;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

/**
 * Represents a facade over the {@link RecordDAO} ensuring that any data
 * retrieved using this facade is readonly.
 */
public class PyRecordDAO {
    private Logger log = Logger.getLogger(getClass());
    
    private User accessor;
    private RecordDAO recordDAO;
   
    /**
     * Creates a new instance.
     * 
     * @param user the user accessing data.
     * @param recordDAO retrieves record related data.
     */
    public PyRecordDAO(User user, RecordDAO recordDAO) {
        this.accessor = user;
        this.recordDAO = recordDAO;
    }
    
    /**
     * Returns all records for the specified survey.
     * 
     * @param surveyId the primary key of the survey containing the desired records.
     * @param includeTaxon true if the serialized record should contain a serialized taxon, or false if only a primary key is required.
     * @param includeLocation true if the serialized record should contain a serialized location, or false if only a primary key is required.
     * @return a JSON serialized representation of the records contained by the specified survey.
     */
    public String getRecordsForSurvey(int surveyId, boolean includeTaxon, boolean includeLocation) {
        RecordFilter filter = new AdvancedRecordFilter();
        filter.setSurveyPk(surveyId);
        filter.setAccessor(accessor);
        
        ScrollableRecords sr = recordDAO.getScrollableRecords(filter);
        
        int count = 0;
        Session sesh = RequestContextHolder.getContext().getHibernate();
        JSONArray array = new JSONArray();
        while (sr.hasMoreElements()) {
            Record rec = sr.nextElement();
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
            
            array.add(recFlatten);
            
            // evict to ensure garbage collection
            if (++count % ScrollableRecords.RECORD_BATCH_SIZE == 0) {
                sesh.clear();
            }
        }
        return array.toString();
    }
}
