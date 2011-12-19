package au.com.gaiaresources.bdrs.model.record;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;

import au.com.gaiaresources.bdrs.db.TransactionDAO;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.db.impl.SortingCriteria;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.record.impl.RecordFilter;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.util.Pair;

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
	 * @param startDate
	 *            the start date (inclusive) of the records
	 * @param endDate
	 *            the end date (inclusive) of the records
	 * @return the records for the specified survey in the specified location.
	 */
	List<Record> getRecords(User user, Survey survey, Location location, Date startDate, Date endDate);

	List<Record> getRecords(Geometry withinGeom);

	List<Record> getRecords(IndicatorSpecies species);

	Integer countAllRecords();

	Integer countUniqueSpecies();

	Record getLatestRecord();

	/**
	 * Counts all of the records owned by the given user.  If you want the count 
	 * of records that the user has access to, use {@link #countAllRecords(User)} instead.
	 * @param user The owner of the records
	 * @return A count of all records owned by the user
	 */
	public Integer countRecords(User user);

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

	TypedAttributeValue updateAttribute(Integer id, BigDecimal numeric,
			String value, Date date);

	void saveRecordList(List<Record> records);

	List<Record> getRecord(User user, int groupId, int surveyId,
			int taxonGroupId, Date startDate, Date endDate,
			String speciesScientificNameSearch, int limit);

	List<Record> getRecord(User user, int groupId, int surveyId,
			int taxonGroupId, Date startDate, Date endDate,
			String speciesScientificNameSearch, int limit, boolean fetch);
	
	ScrollableRecords getScrollableRecords(User user, int groupPk, int surveyPk,
	            int taxonGroupPk, Date startDate, Date endDate, String species);
	
	ScrollableRecords getScrollableRecords(User user, int groupPk, int surveyPk,
                int taxonGroupPk, Date startDate, Date endDate, String species,
                int pageNumber, int entriesPerPage);
	
	/**
	 * Stopping the madness of too many args when filtering for records. Encapsulate all
	 * future querying in the RecordFilter object
	 * 
	 * @param recFilter
	 * @return
	 */
	ScrollableRecords getScrollableRecords(RecordFilter recFilter);
	
	/**
         * Queries for Records based on the filter arguments applying the sort
         * options specified by <code>sortCriteria</code>.
         * 
         * @param recFilter a scrollable list of records.
         * @param sortCriteria a list of column name, order type 
         * (ascending or descending) pairs to apply to the query.
         * @return a scrollable list of records.
         */
	ScrollableRecords getScrollableRecords(RecordFilter recFilter,
	            List<SortingCriteria> sortCriteria);
	
	/**
	 * Returns the number of records that match the specified filter.
	 * 
	 * Note: if you don't use AdvancedRecordCountFilter as the concrete impl
	 * of RecordFilter, this method will break in RecordDAOImpl
	 * 
	 * @param recFilter
	 * @return the number of records that match the filter.
	 */
	int countRecords(RecordFilter recFilter);
	
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

	@Deprecated
	AttributeValue saveAttributeValue(AttributeValue recAttr);
	@Deprecated
	AttributeValue updateAttributeValue(AttributeValue recAttr);

	List<Record> getRecords(String userRegistrationKey, int surveyPk,
			int locationPk);

	/**
	 * Converts form values into AttributeValues
	 * 
	 * @param r
	 *            Record that you want to attach the attributes to
	 * @param attributeMap
	 *            a Map of <Attribute, Object> containing the Attributes that
	 *            you want to store values for and the Object which is produced
	 *            by beanUtils getting the AttributeValue values from the
	 *            RecordForm
	 * @return a List of Records which then need to be attached to the record
	 *         (record.setAttributes()) and saved using the RecordDAO
	 */
	@Deprecated
	Set<AttributeValue> saveAttributeValues(Record r,
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
	 * Returns the <code>AttributeValue</code> with the specified primary key
	 * or null if one does not exist.
	 * 
	 * @param recordAttributePk
	 *            primary key of the <code>AttributeValue</code> to be
	 *            retrieved.
	 * @return the <code>AttributeValue</code> with the provided primary key or
	 *         null if one does not exist.
	 */
	@Deprecated
	AttributeValue getAttributeValue(int recordAttributePk);

	/**
	 * Returns the <code>Record</code> that contains a
	 * <code>AttributeValue</code> with the specified primary key.
	 * 
	 * @param sesh
	 *            the session to use to retrieve the <code>Record</code>.
	 * @param id
	 *            the primary key of the <code>AttributeValue</code>.
	 * @return the <code>Record</code>
	 */
	PersistentImpl getRecordForAttributeValueId(Session sesh, Integer id);

	/**
	 * Returns a list of the latest species recorded for a user.
	 * @param userPk the user
	 * @return
	 */
    List<IndicatorSpecies> getLastSpecies(int userPk, int limit);

    void delete(Record record);
    @Deprecated
    void delete(AttributeValue recAttr);
    
    PagedQueryResult<Record> search(PaginationFilter filter, Integer surveyPk, List<Integer> userId);

    /**
     * Queries all records for a list of distinct taxon groups and the number of
     * records associated with that taxon group filtered by user.
     * @param sesh the session to use for this query.
     * @param user the user who is trying to access the taxon groups 
     *             (for record visibility filtering)
     *             this should be set to null for anonymous access
     * @return the list of distinct taxon groups and record counts.
     */
    List<Pair<TaxonGroup, Long>> getDistinctTaxonGroups(Session sesh, User user);

    /**
     * Queries all records for a list of distinct surveys and the number of
     * records associated with that survey filtered by user.
     * @param sesh the session to use for this query.
     * @param user the user who is trying to access the surveys 
     *             (for record visibility filtering)
     *             this should be set to null for anonymous access
     * @return the list of distinct surveys and record counts.
     */
    List<Pair<Survey, Long>> getDistinctSurveys(Session sesh, User user);

    /**
     * Queries all records for a list of distinct months and the count of records
     * for that month filtered by user.
     * @param sesh the session to use for this query.
     * @param user the user who is trying to access the months 
     *             (for record visibility filtering)
     *             this should be set to null for anonymous access
     * @return the list of distinct months and the count of records that match.
     */
    List<Pair<Long, Long>> getDistinctMonths(Session sesh, User user);

    /**
     * Queries all records for a list of distinct years and the count of records
     * for that month filtered by user.
     * @param sesh the session to use for this query.
     * @param user the user who is trying to access the years 
     *             (for record visibility filtering)
     *             this should be set to null for anonymous access
     * @return the list of distinct months and the count of records that match.
     */
    List<Pair<Long, Long>> getDistinctYears(Session sesh, User user);
    
    /**
     * Queries all records for a list of distinct census method types
     * and the count of records for each type filtered by user
     * @param sesh the session to use for this query.
     * @param user the user who is trying to access the census method types 
     *             (for record visibility filtering)
     *             this should be set to null for anonymous access
     * @return the list of distinct census methods and count of records that match.
     */
    List<Pair<String, Long>> getDistinctCensusMethodTypes(Session sesh, User user);

    /**
     * Counts all Records which do not have a Census Method.
     * @return the number of records without a census method.
     */
    Integer countNullCensusMethodRecords();

    /**
     * Queries all records for a list of distinct attribute types
     * and the count of records for each type
     * @param sesh the session to use for this query.
     * @param user the user who is trying to access the attributes 
     *             (for record visibility filtering)
     *             this should be set to null for anonymous access
     * @return the list of distinct attribute types and count of records that match.
     */
    List<Pair<String, Long>> getDistinctAttributeTypes(Session sesh, User user,
            AttributeType[] attributeTypes);
            

    /**
     * spatial query for records
     * 
     * @param mapLayerId - the map layer id
     * @param intersectGeom - the geometry to intersect with
     * @param isPrivate - whether the record has to be private. true: publish is OWNER_ONLY
     * false: publish is anything but OWNER_ONLY. null: don't care
     * @param userId - The id of the owner of the record. If the user passed here matches the owner of the record
     * the record will be returned regardless of the isPrivate flag.
     * @return
     */
    List<Record> find(Integer[] mapLayerId, Geometry intersectGeom, Boolean isPrivate, Integer userId);
    
    /**
     * Returns a count number of records, ordered by id, starting with offset.
     * @param count
     * @param offset
     * @return
     */
    public List<Record> getRecords(int count, int offset);

    /**
     * Returns the record associated with the specified client id (such as
     * the mobile record primary key)
     * @param clientID the client identifier
     * @return the record associated with the specified client ID.
     */
    public Record getRecordByClientID(String clientID);

    /**
     * Counts all of the records that this user has access to view.
     * @param accessor the account that is accessing the records
     * @return A count of all records that the user has access to.
     */
    public Integer countAllRecords(User accessor);

    /**
     * Queries all records for a list of distinct locations and the count of records
     * for each location filtered by user.
     * @param sesh the session to use for this query.
     * @param user the user who is trying to access the locations 
     *             (for record visibility filtering)
     *             this should be set to null for anonymous access
     * @param limit an optional limit to the number of results that are returned,
     *              ignored if it is less than 0
     * @return the list of distinct months and the count of records that match.
     */
    public List<Pair<Location, Long>> getDistinctLocations(Session sesh, User user,
            int limit);

    /**
     * Queries all records for a list of distinct users and the count of records
     * for each user.
     * @param sesh the session to use for this query.
     * @param user the user who is trying to access the user records 
     *             (for record visibility filtering)
     *             this should be set to null for anonymous access
     * @return the list of distinct users and the count of records that match.
     */
    public List<Pair<User, Long>> getDistinctUsers(Session sesh, User user);

    /**
     * Queries all records for an attribute with the given attributeName.
     * @param sesh the session to use for this query.
     * @param user the user who is trying to access the records
     *             (for record visibility filtering)
     *             this should be set to null for anonymous access
     * @param attributeName the name of the attribute for the query
     * @param limit the number of results to return
     * @return a list of distinct attribute value names and the count of records that match each one.
     */
    public List<Pair<String, Long>> getDistinctAttributeValues(Session sesh,
            User user, String attributeName, int limit);
}