package au.com.gaiaresources.bdrs.model.record;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;

import au.com.gaiaresources.bdrs.db.TransactionDAO;
import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.user.User;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public interface RecordDAO extends TransactionDAO {
	Record createRecord(Location userLocation, IndicatorSpecies species,
			Date when, Long time, String notes, Boolean firstAppearance,
			Boolean lastAppearance, String behaviour, String habitat,
			Integer number, Map<Attribute, Object> attributes);

	Record createRecord(Survey survey, Location userLocation,
			IndicatorSpecies species, Date when, Long time, String notes,
			Boolean firstAppearance, Boolean lastAppearance, String behaviour,
			String habitat, Integer number, Map<Attribute, Object> attributes);

	Record createRecord(Survey survey, Point point, User user,
			IndicatorSpecies species, Date when, Long time, String notes,
			Boolean firstAppearance, Boolean lastAppearance, String behaviour,
			String habitat, Integer number, Map<Attribute, Object> attributes);

	Record createRecord(Location userLocation, IndicatorSpecies species,
			Date when, Long time, Date lastDate, Long lastTime, String notes,
			Boolean firstAppearance, Boolean lastAppearance, String behaviour,
			String habitat, Integer number, Map<Attribute, Object> attributes);

	Record createRecord(Survey survey, Location userLocation,
			IndicatorSpecies species, Date when, Long time, Date lastDate,
			Long lastTime, String notes, Boolean firstAppearance,
			Boolean lastAppearance, String behaviour, String habitat,
			Integer number, Map<Attribute, Object> attributes);

	Record createRecord(Survey survey, Point point, User user,
			IndicatorSpecies species, Date when, Long time, Date lastDate,
			Long lastTime, String notes, Boolean firstAppearance,
			Boolean lastAppearance, String behaviour, String habitat,
			Integer number, Map<Attribute, Object> attributes);

	List<Record> getRecords(Survey survey, Set<User> users);

	List<Record> getRecords(User user);

	List<Record> getRecords(Location userLocation);

	/**
	 * Retrieves the records for the specified survey in the specified location.
	 * 
	 * @param user
	 *            the person who created the records.
	 * @param survey
	 *            the containing survey for the records to be retrieved.
	 * @param location
	 *            the location of the records to be retrieved.
	 * @return the records for the specified survey in the specified location.
	 */
	List<Record> getRecords(User user, Survey survey, Location location);

	List<Record> getRecords(Geometry withinGeom);

	List<Record> getRecords(IndicatorSpecies species);

	Integer countAllRecords();

	Integer countUniqueSpecies();

	Record getLatestRecord();

	Integer countRecords(User user);

	Integer countRecordsForSpecies(IndicatorSpecies species);

	Integer countSpecies(User user);

	/**
	 * Count the number of records entered for the locations created by a user.
	 * 
	 * @param user
	 *            {@link User}
	 * @return {@link Map}
	 */
	Map<Location, Integer> countLocationRecords(User user);

	Record getRecord(Session sesh, Integer id);

	Record getRecord(Integer id);

	void deleteById(Integer id);

	Record saveRecord(Record r);

	Record updateRecord(Record r);

	AttributeValue updateAttribute(Integer id, BigDecimal numeric,
			String value, Date date);

	void saveRecordList(List<Record> records);

	List<Record> getRecord(int userId, int groupId, int surveyId,
			int taxonGroupId, Date startDate, Date endDate,
			String speciesScientificNameSearch, int limit);

	List<Record> getRecord(int userId, int groupId, int surveyId,
			int taxonGroupId, Date startDate, Date endDate,
			String speciesScientificNameSearch, int limit, boolean fetch);

	/**
	 * Returns a list of dates when a record was made for the specified
	 * scientific name fragment.
	 * 
	 * @param scientificNameSearch
	 *            the fragment of the scientific name.
	 */
	List<Date> getRecordDatesByScientificNameSearch(String scientificNameSearch);

	/**
	 * Retrieves records that were created by the specified user ordered by when
	 * they were created.
	 * 
	 * @param user
	 *            The creator of the records
	 * @param scientificNameSearch
	 *            the fragment of the scientific name.
	 * @param limit
	 *            The maximum number of records to return
	 * @return records created by the specified user.
	 */
	List<Record> getLatestRecords(User user, String scientificNameSearch,
			int limit);

	RecordAttribute saveRecordAttribute(RecordAttribute recAttr);

	List<Record> getRecords(String userRegistrationKey, int surveyPk,
			int locationPk);

	/**
	 * Converts form values into RecordAttributes
	 * 
	 * @param r
	 *            Record that you want to attach the attributes to
	 * @param attributeMap
	 *            a Map of <Attribute, Object> containing the Attributes that
	 *            you want to store values for and the Object which is produced
	 *            by beanUtils getting the RecordAttribute values from the
	 *            RecordForm
	 * @return a List of Records which then need to be attached to the record
	 *         (record.setAttributes()) and saved using the RecordDAO
	 */
	Set<RecordAttribute> saveRecordAttributes(Record r,
			Map<Attribute, Object> attributeMap);

	/**
	 * Finds potential duplicates for records based on configurable distance,
	 * time and record properties
	 * 
	 * @param record
	 * @param extendMetres
	 *            - how far to buffer around the record
	 * @param calendarField
	 *            - Integer eg Calendar.MINUTE - the Calendar field type to
	 *            buffer time by (other options include Calendar.SECOND,
	 *            Calendar.HOUR
	 * @param extendTime
	 *            - Amount of time to buffer by
	 * @param excludeRecordIds
	 *            - an array of record ids that you want to specifically exclude
	 *            from search
	 * @return
	 */
	HashSet<Record> getDuplicateRecords(Record record, double extendMetres,
			int calendarField, int extendTime, Integer[] excludeRecordIds,
			Integer[] includeRecordIds);

	Metadata getRecordMetadataForKey(Record record, String metadataKey);

	/**
	 * Returns the <code>RecordAttribute</code> with the specified primary key
	 * or null if one does not exist.
	 * 
	 * @param recordAttributePk
	 *            primary key of the <code>RecordAttribute</code> to be
	 *            retrieved.
	 * @return the <code>RecordAttribute</code> with the provided primary key or
	 *         null if one does not exist.
	 */
	RecordAttribute getRecordAttribute(int recordAttributePk);

	/**
	 * Returns the <code>Record</code> that contains a
	 * <code>RecordAttribute</code> with the specified primary key.
	 * 
	 * @param sesh
	 *            the session to use to retrieve the <code>Record</code>.
	 * @param id
	 *            the primary key of the <code>RecordAttribute</code>.
	 * @return the <code>Record</code>
	 */
	PersistentImpl getRecordForRecordAttributeId(Session sesh, Integer id);

	/**
	 * Returns a list of the latest species recorded for a user.
	 * @param userPk the user
	 * @return
	 */
    List<IndicatorSpecies> getLastSpecies(int userPk, int limit);

    void delete(Record record);
    void delete(RecordAttribute recAttr);
}