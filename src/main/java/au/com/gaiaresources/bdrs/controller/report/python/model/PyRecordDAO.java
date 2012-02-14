package au.com.gaiaresources.bdrs.controller.report.python.model;

import java.util.List;
import java.util.Map;

import au.com.gaiaresources.bdrs.json.JSONArray;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

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
     * @param user
     *            the user accessing data.
     * @param recordDAO
     *            retrieves record related data.
     */
    public PyRecordDAO(User user, RecordDAO recordDAO) {
        this.accessor = user;
        this.recordDAO = recordDAO;
    }

    /**
     * Returns all records for the specified survey.
     * 
     * @param surveyId
     *            the primary key of the survey containing the desired records.
     * @param includeTaxon
     *            true if the serialized record should contain a serialized
     *            taxon, or false if only a primary key is required.
     * @param includeLocation
     *            true if the serialized record should contain a serialized
     *            location, or false if only a primary key is required.
     * @return a JSON serialized representation of the records contained by the
     *         specified survey.
     * 
     * @deprecated If possible, use
     *             {@link #getScrollableRecordsForSurvey(int, boolean, boolean)}
     *             instead.
     */
    public String getRecordsForSurvey(int surveyId, boolean includeTaxon,
            boolean includeLocation) {
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

            if (includeTaxon) {
                IndicatorSpecies species = rec.getSpecies();
                if (species != null) {
                    recFlatten.put("species", species.flatten());
                }
            }

            if (includeLocation) {
                Location loc = rec.getLocation();
                if (loc != null) {
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

    /**
     * Returns all records for the specified survey.
     * 
     * @param surveyId
     *            the primary key of the survey containing the desired records.
     * @param includeTaxon
     *            true if the serialized record should contain a serialized
     *            taxon, or false if only a primary key is required.
     * @param includeLocation
     *            true if the serialized record should contain a serialized
     *            location, or false if only a primary key is required.
     * @return a JSON serialized representation of the records contained by the
     *         specified survey.
     */
    public PyScrollableRecords getScrollableRecordsForSurvey(int surveyId,
            boolean includeTaxon, boolean includeLocation) {
        RecordFilter filter = new AdvancedRecordFilter();
        filter.setSurveyPk(surveyId);
        filter.setAccessor(accessor);

        return new PyScrollableRecords(recordDAO.getScrollableRecords(filter),
                includeTaxon, includeLocation);
    }

    /**
     * Returns all records for the specified survey.
     * 
     * @param surveyId
     *            the primary key of the survey containing the desired records.
     * @param includeTaxon
     *            true if the serialized record should contain a serialized
     *            taxon, or false if only a primary key is required.
     * @param includeLocation
     *            true if the serialized record should contain a serialized
     *            location, or false if only a primary key is required.
     * @param includeAttributeValues
     *            true if the serialized record should contain serialized
     *            attribute values, or false if only a primary key is required.
     * @return a JSON serialized representation of the records contained by the
     *         specified survey.
     */
    public PyScrollableRecords getScrollableRecordsForSurvey(int surveyId,
            boolean includeTaxon, boolean includeLocation,
            boolean includeAttributeValues) {
        RecordFilter filter = new AdvancedRecordFilter();
        filter.setSurveyPk(surveyId);
        filter.setAccessor(accessor);

        return new PyScrollableRecords(recordDAO.getScrollableRecords(filter),
                includeTaxon, includeLocation, includeAttributeValues);
    }

    /**
     * Retrieves all records within the geometry specified.
     * 
     * @param srid
     *            the projection of the wkt string
     * @param wktFilter
     *            the wkt string that defines the geometry
     * @return a json serialized representation of the records contained by the
     *         geometry.
     */
    public String getRecordsWithinGeometry(int srid, String wktFilter) {
        WKTReader fromText = new WKTReader();
        Geometry filter = null;
        try {
            filter = fromText.read(wktFilter);
            filter.setSRID(srid);
            if(filter.isValid()) {
                return PyDAOUtil.toJSON(recordDAO.getRecordIntersect(filter)).toString();
            } else {
                log.error(String.format("WKT String does not produce a valid geometry: %s", wktFilter));
                return null;
            }
            
        } catch (ParseException e) {
            log.error(String.format("Failed to parse WKT String: %s", wktFilter), e);
            return null;
        }
    }
}
