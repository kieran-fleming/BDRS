package au.com.gaiaresources.bdrs.model.record.impl;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.persistence.Transient;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.ForeignKey;
import org.hibernatespatial.GeometryUserType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.QueryCriteria;
import au.com.gaiaresources.bdrs.db.QueryOperation;
import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.db.impl.HqlQuery;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.db.impl.QueryPaginator;
import au.com.gaiaresources.bdrs.db.impl.SortingCriteria;
import au.com.gaiaresources.bdrs.geometry.GeometryBuilder;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.record.RecordVisibility;
import au.com.gaiaresources.bdrs.model.record.ScrollableRecords;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.db.DeleteCascadeHandler;
import au.com.gaiaresources.bdrs.service.db.DeletionService;
import au.com.gaiaresources.bdrs.util.Pair;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

@Repository
public class RecordDAOImpl extends AbstractDAOImpl implements RecordDAO {
    private Logger log = Logger.getLogger(getClass());
    
    @Autowired
    private GeometryBuilder geometryBuilder;
    
    @Autowired
    private DeletionService delService;
    
    @PostConstruct
    public void init() throws Exception {
        delService.registerDeleteCascadeHandler(Record.class, new DeleteCascadeHandler() {
            @Override
            public void deleteCascade(PersistentImpl instance) {
                delete((Record)instance);
            }
        });
        delService.registerDeleteCascadeHandler(AttributeValue.class, new DeleteCascadeHandler() {
            @Override
            public void deleteCascade(PersistentImpl instance) {
                delete((AttributeValue)instance);
            }
        });
    }

    @Override
    public Record createRecord(Location userLocation,
            IndicatorSpecies species, Date when, Long time, String notes,
            Boolean firstAppearance, Boolean lastAppearance, String behaviour,
            String habitat, Integer number,
            Map<Attribute, Object> attributes) {
        Date lastDate = when;
        Long lastTime = time;
        return this.createRecord(null, userLocation, species, when, time, lastDate, lastTime, notes,
                firstAppearance, lastAppearance, behaviour,
                habitat, number, attributes);

    }
    @Override
    public Record createRecord(Location userLocation, IndicatorSpecies species,
            Date when, Long time, Date lastDate, Long lastTime, String notes,
            Boolean firstAppearance, Boolean lastAppearance, String behaviour,
            String habitat, Integer number, Map<Attribute, Object> attributes) {
        return this.createRecord(null, userLocation, species, when, time, lastDate, lastTime, notes,
                firstAppearance, lastAppearance, behaviour,
                habitat, number, attributes);
    }

    @Override
    public Record createRecord(Survey survey, Location userLocation,
            IndicatorSpecies species, Date when, Long time, String notes,
            Boolean firstAppearance, Boolean lastAppearance, String behaviour,
            String habitat, Integer number,
            Map<Attribute, Object> attributes) {
        Date lastDate = when;
        Long lastTime = time;
        return this.createRecord(survey, userLocation, species, when, time, lastDate, lastTime, notes,
                firstAppearance, lastAppearance, behaviour,
                habitat, number, attributes);

    }
    @Override
    public Record createRecord(Survey survey, Location userLocation,
            IndicatorSpecies species, Date when, Long time, Date lastDate,
            Long lastTime, String notes, Boolean firstAppearance,
            Boolean lastAppearance, String behaviour, String habitat,
            Integer number, Map<Attribute, Object> attributes) {
        Record r = new Record();
        r.setSurvey(survey);
        r.setLocation(userLocation);
        r.setUser(userLocation.getUser());
        r.setSpecies(species);
        r.setWhen(when);
        r.setTime(time);
        r.setLastDate(lastDate);
        r.setLastTime(lastTime);
        r.setNotes(notes);
        r.setFirstAppearance(firstAppearance);
        r.setLastAppearance(lastAppearance);
        r.setBehaviour(behaviour);
        r.setHabitat(habitat);
        r.setNumber(number);

        r = save(r);

        r.setAttributes(this.saveAttributeValues(r, attributes));

        return update(r);
    }
    @Override
    @SuppressWarnings("unchecked")
    public List<Record> getRecords(Survey survey, Set<User> users) {
        if(users.isEmpty()) {
            return Collections.emptyList();
        }

        Query q = getSession().createQuery("select r from Record r where r.user in (:users) and r.survey = :survey");
        q.setParameterList("users", users);
        q.setParameter("survey", survey);
        return q.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Record> getLatestRecords(User user, String scientificNameSearch, int limit) {

        Map<String,Object> paramMap = new HashMap<String, Object>();
        StringBuilder builder = new StringBuilder();
        builder.append("select r from Record r where r.id > 0");

        if(scientificNameSearch != null && !scientificNameSearch.isEmpty()) {
            // Otherwise you will simply see all records for all species
            builder.append(" and (UPPER(r.species.commonName) like UPPER('%" + StringEscapeUtils.escapeSql(scientificNameSearch) +
                    "%') or UPPER(r.species.scientificName) like UPPER ('%" + StringEscapeUtils.escapeSql(scientificNameSearch) + "%'))");
        }
        if(user != null) {
            // Used if the admin wants to see all records
            builder.append(" and r.user = :user");
            paramMap.put("user", user);
        }

        builder.append(" order by r.when desc");

        Query q = getSession().createQuery(builder.toString());
        for(Map.Entry<String,Object> entry: paramMap.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }
        if(limit > 0) {
            q.setMaxResults(limit);
        }

        return q.list();
    }

    @Override
    public List<Record> getRecords(User user) {
        return find("from Record r where r.user=?", user);
    }

    @Override
    public List<Record> getRecords(User user, Survey survey, Location location, Date startDate, Date endDate) {
        return find("from Record r where r.location = ? and r.survey = ? and r.user = ? and r.when >= ? and r.when <= ?", 
                    new Object[] {location, survey, user, startDate, endDate});
    }
    
    @Override
    public List<Record> getRecords(Location userLocation) {
        return find("from Record r where r.location = ?", userLocation);
    }


    @Override
    public List<Record> getRecords(Geometry withinGeom) {
        return newQueryCriteria(Record.class).add("location", "location",
                QueryOperation.WITHIN, withinGeom).run();
    }

    @Override
    public List<Record> getRecords(IndicatorSpecies species) {
        return newQueryCriteria(Record.class).add("species",
                QueryOperation.EQUAL, species).run();
    }

    @Override
    public Integer countUniqueSpecies() {
        Query q = getSession().createQuery("select count(distinct r.species) from Record r");
        Integer count = Integer.parseInt(q.list().get(0).toString(),10);
        return count;
    }

    @Override
    public Integer countRecords(User user) {
        RecordFilter filter = new CountRecordFilter();
        filter.setUser(user);
        Query q = filter.getRecordQuery(getSession());
        Integer count = Integer.parseInt(q.list().get(0).toString(),10);
        return count;
    }

    @Override
    public Integer countAllRecords(User accessor) {
        RecordFilter filter = new CountRecordFilter();
        filter.setAccessor(accessor);
        Query q = filter.getRecordQuery(getSession());
        Integer count = Integer.parseInt(q.list().get(0).toString(),10);
        return count;
    }
    
    @Override
    public Integer countAllRecords() {
        Query q = getSession().createQuery("select count(*) from Record");
        Integer count = Integer.parseInt(q.list().get(0).toString(),10);
        return count;
    }
    
    @Override
    public Integer countNullCensusMethodRecords() {
        RecordFilter filter = new CountRecordFilter();
        filter.setCensusMethod(null);
        Query q = filter.getRecordQuery(getSession());
        Integer count = Integer.parseInt(q.list().get(0).toString(),10);
        return count;
    }

    @Override
    public Integer countRecordsForSpecies(IndicatorSpecies species) {
        RecordFilter filter = new CountRecordFilter();
        filter.setSpecies(species);
        Query q = filter.getRecordQuery(getSession());
        Integer count = Integer.parseInt(q.list().get(0).toString(),10);
        return count;
    }

    @Override
    public Record getLatestRecord() {
        List results = find("select r from Record r where r.updatedAt != null order by r.updatedAt desc", new Object[0], 1);
        if(results.isEmpty()) {
            return null;
        } else {
            return (Record)results.get(0);
        }
    }

    @Override
    public Integer countSpecies(User user) {
        return newQueryCriteria(Record.class).add("user",
                QueryOperation.EQUAL, user).countDistinct("species");
    }

    @Override
    public Map<Location, Integer> countLocationRecords(User user) {
        return newQueryCriteria(Record.class).add("user",
                QueryOperation.EQUAL, user).groupByAndCount("location");
    }

    @Override
    public Record getRecord(Integer id) {
        return getByID(Record.class, id);
    }

    @Override
    public Record getRecord(Session sesh, Integer id) {
        return (Record)sesh.get(Record.class, id);
    }
    @Override
    public void deleteById(Integer id) {
        Session sesh = getSession();
        Object ob = sesh.get(Record.class, id);
        if (ob != null)
            sesh.delete(ob);
        else {
            Record record = getRecord(id);
            sesh.delete(record);
        }
    }

    @Override
    public Record createRecord(Survey survey, Point location, User user,
            IndicatorSpecies species, Date when, Long time, Date lastDate,
            Long lastTime, String notes, Boolean firstAppearance,
            Boolean lastAppearance, String behaviour, String habitat,
            Integer number, Map<Attribute, Object> attributes) {
        Record r = new Record();
        r.setSurvey(survey);
        r.setPoint(location);
        r.setUser(user);
        r.setSpecies(species);
        r.setWhen(when);
        r.setTime(time);
        r.setLastDate(lastDate);
        r.setLastTime(lastTime);
        r.setNotes(notes);
        r.setFirstAppearance(firstAppearance);
        r.setLastAppearance(lastAppearance);
        r.setBehaviour(behaviour);
        r.setHabitat(habitat);
        r.setNumber(number);
        Set<AttributeValue> atts = this.saveAttributeValues(r, attributes);
        r.setAttributes(atts);
        save(r);
        return r;
    }
    @Override
    public  Set<AttributeValue> saveAttributeValues(Record r, Map<Attribute, Object> attributeMap){
        Set<AttributeValue> atts = new HashSet<AttributeValue>();
        for (Map.Entry<Attribute, Object> attValue : attributeMap
                .entrySet()) {
            if (attValue != null) {
                AttributeValue attribute = new AttributeValue();
                attribute.setAttribute(attValue
                        .getKey());
                // attribute.setRecord(r);
                switch (attValue.getKey().getType()) {
                case IMAGE:
                case FILE:
//                    attrFile = fileAttributeMap.get(attValue.getKey());
//                    // attrFile will always have size zero unless the file
//                    // is changed. If there is already a file, but the
//                    // record is updated, without changing the file input,
//                    // addAttribute will be true but attrFile will
//                    // have size zero.
//                    boolean addAttribute;
//                    addAttribute =  attValue.getValue() != null;
//                    if(addAttribute && attrFile != null && attrFile.getSize() > 0) {
//                        attribute.setStringValue(attrFile.getOriginalFilename());
//                        
//                    }
                case HTML:
                case HTML_COMMENT:
                case HTML_HORIZONTAL_RULE:
                case STRING:
                case STRING_WITH_VALID_VALUES:
                case BARCODE:
                case REGEX:
                case TEXT:
                    attribute.setStringValue((String) attValue.getValue());
                    break;
                case DATE:
                    attribute.setDateValue((Date) attValue.getValue());
                    break;
                case DECIMAL:
                    attribute.setNumericValue((BigDecimal) attValue.getValue());
                    break;
                case INTEGER:
                case INTEGER_WITH_RANGE:
                    attribute.setNumericValue(new BigDecimal((Integer) attValue
                            .getValue()));
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Invalid data type for attribute "
                                    + attValue.getKey());
                }
                save(attribute);
                atts.add(attribute);
            }
        }
        return atts;
        
    }
    @Override
    public Record createRecord(Survey survey, Point location, User user,
            IndicatorSpecies species, Date when, Long time, String notes,
            Boolean firstAppearance, Boolean lastAppearance, String behaviour,
            String habitat, Integer number,
            Map<Attribute, Object> attributes) {
        Date lastDate = when;
        Long lastTime = time;
        return this.createRecord(survey, location, user, species, when, time, lastDate, lastTime,
                notes, firstAppearance, lastAppearance, behaviour, habitat, number, attributes);

    }

    @Override
    public List<Date> getRecordDatesByScientificNameSearch(
            String scientificNameSearch) {
        StringBuilder builder = new StringBuilder();
        builder.append("select r.when from Record r where r.id > 0");

        if(scientificNameSearch != null && !scientificNameSearch.isEmpty()) {
            builder.append(" and (UPPER(r.species.commonName) like UPPER('%" + StringEscapeUtils.escapeSql(scientificNameSearch) +
                    "%') or UPPER(r.species.scientificName) like UPPER ('%" + StringEscapeUtils.escapeSql(scientificNameSearch) + "%'))");
        }
        builder.append(" order by r.when");
        Query q = getSession().createQuery(builder.toString());
        return q.list();
    }
    /**
     * Warning: untested and currently unused method!
     * {@inheritDoc}
     */
    @Override
    public HashSet<Record> getDuplicateRecords(Record record, double bufferMetre, int calendarField, int extendTime, Integer[] excludeRecordIds, Integer[] includeRecordIds){
        Calendar calendar = Calendar.getInstance();
        
        if(Calendar.FIELD_COUNT < calendarField ){
            throw new ArrayIndexOutOfBoundsException(calendarField);
        }
//        calendar.add
        calendar.setTime(record.getWhen());
        calendar.add(calendarField, -extendTime);
        Date timeFrom = calendar.getTime();
        calendar.setTime(record.getWhen());
        calendar.add(calendarField, extendTime);
        Date timeUntil = calendar.getTime();
        Point point;
        if(record.getPoint()!=null){
            point = record.getPoint();
        }else if(record.getLocation()!= null){
            point = record.getLocation().getLocation().getCentroid();
        }
        else{
            log.warn("Record Needs to have a point or a location associated with it");
            return new HashSet<Record>();
        }
        Geometry buffer = geometryBuilder.bufferInM(point, bufferMetre);
        QueryCriteria<Record> queryCriterea = newQueryCriteria(Record.class).add("point", QueryOperation.INTERSECTS, buffer)
            .add("when", QueryOperation.GREATER_THAN_OR_EQUAL, timeFrom)
            .add("when", QueryOperation.LESS_THAN_OR_EQUAL, timeUntil)
            .add("id", QueryOperation.NOT_EQUAL, record.getId());
        if (record.getSpecies() != null) {
            queryCriterea.add("species", QueryOperation.EQUAL, record.getSpecies());
        } else {
            queryCriterea.add("species", QueryOperation.IS_NULL, record.getSpecies());
        }
        if(excludeRecordIds != null && excludeRecordIds.length > 0) {
            queryCriterea.add("id", QueryOperation.NOT_IN, (Object[])excludeRecordIds);
        }
        if(includeRecordIds!= null && includeRecordIds.length > 0) {
            queryCriterea.add("id", QueryOperation.IN, (Object[])includeRecordIds);
        }
        return new HashSet<Record>(queryCriterea.run());
    }

    @Override
    public List<Record> getRecord(User user, int groupId, int surveyId,
            int taxonGroupId, Date startDate, Date endDate,
            String speciesScientificNameSearch, int limit) {
        return getRecord(user, groupId, surveyId,
                taxonGroupId, startDate, endDate,
                speciesScientificNameSearch, limit, false);
    }
    
    private Query getRecordQuery(RecordFilter recFilter) {
        List<SortingCriteria> sortCriteria = Collections.emptyList(); 
        return this.getRecordQuery(recFilter, sortCriteria);
    }
    

    
    @Override
    public int countRecords(RecordFilter recFilter) {
        Query q = recFilter.getRecordQuery(getSession());
        return Integer.parseInt(q.list().get(0).toString(), 10);
    }

    @Override
    public ScrollableRecords getScrollableRecords(User user, int groupPk,
            int surveyPk, int taxonGroupPk, Date startDate, Date endDate,
            String species) {
        RecordFilter filter = new AdvancedRecordFilter();
        filter.setUser(user);
        filter.setGroupPk(groupPk);
        filter.setSurveyPk(surveyPk);
        filter.setTaxonGroupPk(taxonGroupPk);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setSpeciesSearch(species);
        
        // There is no limit passed in. If there was however, remember not to 
        // use it in the RecordFilter but on the ScrollableRecords instead.
        
        Query q = getRecordQuery(filter);
        return new ScrollableRecordsImpl(q);
    }
    
    @Override
    public ScrollableRecords getScrollableRecords(User user, int groupPk,
            int surveyPk, int taxonGroupPk, Date startDate, Date endDate,
            String species, int pageNumber, int entriesPerPage) {
        RecordFilter filter = new AdvancedRecordFilter();
        filter.setUser(user);
        filter.setGroupPk(groupPk);
        filter.setSurveyPk(surveyPk);
        filter.setTaxonGroupPk(taxonGroupPk);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setSpeciesSearch(species);
        
        Query q = getRecordQuery(filter);
        return new ScrollableRecordsImpl(q, pageNumber, entriesPerPage);
    }
    
    @Override
    public ScrollableRecords getScrollableRecords(RecordFilter recFilter, List<SortingCriteria> sortCriteria) {
        // Scrollable Records cannot have a max limit set.
        Integer pageNumber = recFilter.getPageNumber();
        Integer entriesPerPage = recFilter.getEntriesPerPage();
        recFilter.setPageNumber(null);
        recFilter.setEntriesPerPage(null);
        
        Query q = getRecordQuery(recFilter, sortCriteria);
        
        recFilter.setPageNumber(pageNumber);
        recFilter.setEntriesPerPage(entriesPerPage);
        
        if (pageNumber != null && entriesPerPage != null) {
            return new ScrollableRecordsImpl(q, pageNumber, entriesPerPage);
        } else {
            return new ScrollableRecordsImpl(q);
        }
    }
    
    private Query getRecordQuery(RecordFilter recFilter,
            List<SortingCriteria> sortCriteria) {
        Query q = recFilter.getRecordQuery(getSession(), sortCriteria);

        return q;
    }

    @Override
    public ScrollableRecords getScrollableRecords(RecordFilter recFilter) {
        List<SortingCriteria> sortCriteria = Collections.emptyList();
        return getScrollableRecords(recFilter, sortCriteria);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Record> getRecord(User user, int groupId, int surveyId,
            int taxonGroupId, Date startDate, Date endDate,
            String speciesScientificNameSearch, int limit, boolean fetch) {
        
        RecordFilter filter = new AdvancedRecordFilter();
        filter.setUser(user);
        filter.setGroupPk(groupId);
        filter.setSurveyPk(surveyId);
        filter.setTaxonGroupPk(taxonGroupId);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setSpeciesSearch(speciesScientificNameSearch);
        filter.setFetch(fetch);
        filter.setEntriesPerPage(limit);
        
        Query q = getRecordQuery(filter);
        
        return q.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Record> getRecords(String userRegistrationKey, int surveyPk,
            int locationPk) {
        StringBuilder builder = new StringBuilder();
        builder.append(" select r");
        builder.append(" from Record r");
        builder.append(" where r.survey.id = :surveyPk and");
        builder.append("       r.location.id = :locationPk and");
        builder.append("       r.user.registrationKey = :regKey");
        builder.append(" order by r.when");

        Query q = getSession().createQuery(builder.toString());
        q.setParameter("regKey", userRegistrationKey);
        q.setParameter("surveyPk", surveyPk);
        q.setParameter("locationPk", locationPk);

        return q.list();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public TypedAttributeValue updateAttribute(Integer id, BigDecimal numeric,
            String value, Date date) {
        AttributeValue att = getByID(AttributeValue.class, id);
        att.setStringValue(value);
        att.setNumericValue(numeric);
        att.setDateValue(date);
        return update(att);
    }

    @Override
    public Record saveRecord(Record r) {
        return save(r);
    }

    @Override
    public AttributeValue saveAttributeValue(AttributeValue recAttr) {
        return save(recAttr);
    }
    
    @Override
    public AttributeValue updateAttributeValue(AttributeValue recAttr) {
        return update(recAttr);
    }
    
    @Override
    public void saveRecordList(List<Record> records) {
        for(Record r : records) {
            save(r);
        }
    }

    @Override
    public Record updateRecord(Record r) {
        return update(r);
    }
    
    @Override
    public  Metadata getRecordMetadataForKey(Record record, String metadataKey){
        for(Metadata md: record.getMetadata()){
            if (md.getKey().equals(metadataKey)){
                return md;
            }
        }
        Metadata md = new Metadata();
        md.setKey(metadataKey);
        return md;
    }
    
    @Override
    public AttributeValue getAttributeValue(int recordAttributePk) {
        return getByID(AttributeValue.class, recordAttributePk);
    }
    
    @Override
    public PersistentImpl getRecordForAttributeValueId(
            Session sesh, Integer id) {
        
        String queryString = "select distinct r from Record r left join r.attributes a where a.id = :id";
        Query q = sesh.createQuery(queryString);
        q.setParameter("id", id);
        
        List<Record> records = q.list();
        
        if(records.isEmpty()) {
            return null;
        } else {
            if(records.size() > 1) {
                log.warn("Multiple records found. Returning the first");

            }
            return records.get(0);
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<IndicatorSpecies> getLastSpecies(int userPk, int limit) {
    	List<IndicatorSpecies> species = new ArrayList<IndicatorSpecies>();
    	StringBuilder queryString = new StringBuilder("select distinct i, r.when from Record as r join r.species as i ");
    	if (userPk != 0) {
    		queryString.append("where r.user.id = " + userPk);
    	}
    	queryString.append(" order by r.when desc");
    	Query q = getSession().createQuery(queryString.toString());
    	q.setMaxResults(limit);
    	List<Object[]> resultSet = q.list();
    	for (Object[] result : resultSet) {
    		species.add((IndicatorSpecies)result[0]);
    	}
    	return species;
    }
    
    @Override
    public void delete(Record record) {
        Set<AttributeValue> attributeList = new HashSet<AttributeValue>(record.getAttributes());
        record.getAttributes().clear();
        record = save(record);
        
        DeleteCascadeHandler cascadeHandler = 
            delService.getDeleteCascadeHandlerFor(AttributeValue.class);
        for(AttributeValue recAttr : attributeList) {
            recAttr = saveAttributeValue(recAttr);
            cascadeHandler.deleteCascade(recAttr);
        }
        deleteByQuery(record);
    }
    
    @Override
    public void delete(AttributeValue recordAttribute) {
        deleteByQuery(recordAttribute);
    }
    
    @Override
    public PagedQueryResult<Record> search(PaginationFilter filter, Integer surveyPk, List<Integer> userIdList) {
        HqlQuery q;
        String sortTargetAlias = "r";
        q = new HqlQuery("select r from Record r");
        
        if (surveyPk != null) {
            q.join("r.survey", "survey");
            q.and(Predicate.eq("survey.id", surveyPk));
        }
        if (userIdList != null) {
            if (userIdList.size() > 0) {
                q.join("r.user", "user");
                q.and(Predicate.in("user.id", userIdList.toArray()));
            } else {
                // return empty result
                q.and(Predicate.eq("r.id", 0));
            }
        }
        return new QueryPaginator<Record>().page(this.getSession(), q.getQueryString(), q.getParametersValue(), filter, sortTargetAlias);
    }
    
    @Override
    public List<Pair<TaxonGroup, Long>> getDistinctTaxonGroups(Session sesh, User user) {
        StringBuilder b = new StringBuilder();
        b.append(" select g, count(r)");
        b.append(" from Record as r join r.species as s join s.taxonGroup as g");
        // add user visibility parameters here
        if (user != null) {
            if (!user.isAdmin()) {
                b.append(" where r.user.id = "+user.getId());
            }
        } else {
            b.append(" where r.recordVisibility = :vis and r.held = :held");
        }
        b.append(" group by g.id");
        for(PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(TaxonGroup.class)) {
            if(!"class".equals(pd.getName()) && 
                !"id".equals(pd.getName()) && 
                (pd.getReadMethod().getAnnotation(Transient.class) == null) &&
                !(Iterable.class.isAssignableFrom((pd.getReadMethod().getReturnType())))) {
                b.append(", g."+pd.getName());
            }
        }
        b.append(" order by g.weight asc, g.name asc");
        
        if(sesh == null) {
            sesh = super.getSessionFactory().getCurrentSession();
        }
        Query q = sesh.createQuery(b.toString());
        if (user == null) {
            q.setParameter("vis", RecordVisibility.PUBLIC);
            q.setParameter("held", false);
        }
        // Should get back a list of Object[]
        // Each Object[] has 2 items. Object[0] == taxon group, Object[1] == record count
        List<Pair<TaxonGroup, Long>> results = 
            new ArrayList<Pair<TaxonGroup, Long>>();
        for(Object rowObj : q.list()) {
            Object[] row = (Object[])rowObj;
            results.add(new Pair<TaxonGroup, Long>((TaxonGroup)row[0], (Long)row[1]));
        }
        return results;
    }

    @Override
    public List<Pair<Location, Long>> getDistinctLocations(Session sesh, User user, int limit) {
        StringBuilder b = new StringBuilder();
        b.append(" select l, count(r)");
        b.append(" from Record as r join r.location as l");
        // add user visibility parameters here
        if (user != null) {
            if (!user.isAdmin()) {
                b.append(" where r.user.id = "+user.getId());
            }
        } else {
            b.append(" where r.recordVisibility = :vis and r.held = :held");
        }
        b.append(" group by l.id");
        for(PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(Location.class)) {
            if(!"class".equals(pd.getName()) && 
                !"id".equals(pd.getName()) && 
                (pd.getReadMethod().getAnnotation(Transient.class) == null) &&
                !(Iterable.class.isAssignableFrom((pd.getReadMethod().getReturnType())))) {
                b.append(", l."+pd.getName());
            }
        }
        b.append(" order by 2 desc, l.weight asc, l.name asc");

        
        if(sesh == null) {
            sesh = super.getSessionFactory().getCurrentSession();
        }
        Query q = sesh.createQuery(b.toString());
        if (user == null) {
            q.setParameter("vis", RecordVisibility.PUBLIC);
            q.setParameter("held", false);
        }
        if(limit > 0) {
            q.setMaxResults(limit);
        }
        
        // Should get back a list of Object[]
        // Each Object[] has 2 items. Object[0] == location, Object[1] == record count
        List<Pair<Location, Long>> results = 
            new ArrayList<Pair<Location, Long>>();
        for(Object rowObj : q.list()) {
            Object[] row = (Object[])rowObj;
            results.add(new Pair<Location, Long>((Location)row[0], (Long)row[1]));
        }
        return results;
    }
    
    @Override
    public List<Pair<User, Long>> getDistinctUsers(Session sesh, User user) {
        StringBuilder b = new StringBuilder();
        b.append(" select u, count(r)");
        b.append(" from Record as r join r.user as u");
        // add user visibility parameters here
        if (user != null) {
            if (!user.isAdmin()) {
                b.append(" where r.user.id = "+user.getId()+" or (r.recordVisibility = :vis and r.held = :held)");
            }
        } else {
            b.append(" where r.recordVisibility = :vis and r.held = :held");
        }
        b.append(" group by u.id");
        for(PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(User.class)) {
            if(!"class".equals(pd.getName()) && 
                !"id".equals(pd.getName()) && 
                (pd.getReadMethod().getAnnotation(ForeignKey.class) == null) && // ignore other table joins
                (pd.getReadMethod().getAnnotation(Transient.class) == null || // ignore transients 
                        "active".equals(pd.getName())) &&                     // except active
                !(Iterable.class.isAssignableFrom((pd.getReadMethod().getReturnType())))) 
            {
                b.append(", u."+pd.getName());
            }
        }
        b.append(" order by 2 desc, u.name asc");

        
        if(sesh == null) {
            sesh = super.getSessionFactory().getCurrentSession();
        }
        Query q = sesh.createQuery(b.toString());
        if (user == null || !user.isAdmin()) {
            q.setParameter("vis", RecordVisibility.PUBLIC);
            q.setParameter("held", false);
        }
        // Should get back a list of Object[]
        // Each Object[] has 2 items. Object[0] == location, Object[1] == record count
        List<Pair<User, Long>> results = 
            new ArrayList<Pair<User, Long>>();
        for(Object rowObj : q.list()) {
            Object[] row = (Object[])rowObj;
            results.add(new Pair<User, Long>((User)row[0], (Long)row[1]));
        }
        return results;
    }
    
    @Override
    public List<Pair<Survey, Long>> getDistinctSurveys(Session sesh, User user) {
        StringBuilder b = new StringBuilder();
        b.append(" select s, count(r)");
        b.append(" from Record as r join r.survey as s");
        // add user visibility parameters here
        if (user != null) {
            if (!user.isAdmin()) {
                b.append(" where r.user.id = "+user.getId());
            }
        } else {
            b.append(" where r.recordVisibility = :vis and r.held = :held");
        }
        b.append(" group by s.id");
        for(PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(Survey.class)) {
            if(!"class".equals(pd.getName()) && 
                !"id".equals(pd.getName()) && 
                (pd.getReadMethod().getAnnotation(Transient.class) == null) &&
                !(Iterable.class.isAssignableFrom((pd.getReadMethod().getReturnType())))) {
                b.append(", s."+pd.getName());
            }
        }
        b.append(" order by s.weight asc, s.name asc");
        
        if(sesh == null) {
            sesh = super.getSessionFactory().getCurrentSession();
        }
        Query q = sesh.createQuery(b.toString());
        if (user == null) {
            q.setParameter("vis", RecordVisibility.PUBLIC);
            q.setParameter("held", false);
        }
        // Should get back a list of Object[]
        // Each Object[] has 2 items. Object[0] == taxon group, Object[1] == record count
        List<Pair<Survey, Long>> results = 
            new ArrayList<Pair<Survey, Long>>();
        for(Object rowObj : q.list()) {
            Object[] row = (Object[])rowObj;
            results.add(new Pair<Survey, Long>((Survey)row[0], (Long)row[1]));
        }
        return results;
    }
    
    @Override
    public List<Pair<Long, Long>> getDistinctMonths(Session sesh, User user) {
        StringBuilder b = new StringBuilder();
        b.append(" select distinct month(r.when), count(r)");
        b.append(" from Record as r");
        // add user visibility parameters here
        if (user != null) {
            if (!user.isAdmin()) {
                b.append(" where r.user.id = "+user.getId());
            }
        } else {
            b.append(" where r.recordVisibility = :vis and r.held = :held");
        }
        b.append(" group by month(r.when)");
        b.append(" order by month(r.when) asc");
        if(sesh == null) {
            sesh = super.getSessionFactory().getCurrentSession();
        }
        Query q = sesh.createQuery(b.toString());
        if (user == null) {
            q.setParameter("vis", RecordVisibility.PUBLIC);
            q.setParameter("held", false);
        }
        
        // Should get back a list of Object[]
        List<Pair<Long, Long>> results = 
            new ArrayList<Pair<Long, Long>>();
        for(Object rowObj : q.list()) {
            Object[] row = (Object[])rowObj;
            if (row[0] != null && row[1] != null) {
            	// Month is zero based so we need to subtract by one.
                results.add(new Pair<Long, Long>(Long.parseLong(row[0].toString()), (Long)row[1]));
            }
        }
        return results;
    }
    
    
    @Override
    public List<Pair<Long, Long>> getDistinctYears(Session sesh, User user) {
        StringBuilder b = new StringBuilder();
        b.append(" select distinct year(r.when), count(r)");
        b.append(" from Record as r");
        // add user visibility parameters here
        if (user != null) {
            if (!user.isAdmin()) {
                b.append(" where r.user.id = "+user.getId());
            }
        } else {
            b.append(" where r.recordVisibility = :vis and r.held = :held");
        }
        b.append(" group by year(r.when)");
        b.append(" order by year(r.when) asc");
        
        if(sesh == null) {
            sesh = super.getSessionFactory().getCurrentSession();
        }
        Query q = sesh.createQuery(b.toString());
        if (user == null) {
            q.setParameter("vis", RecordVisibility.PUBLIC);
            q.setParameter("held", false);
        }
        
        // Should get back a list of Object[]
        List<Pair<Long, Long>> results = 
            new ArrayList<Pair<Long, Long>>();
        for(Object rowObj : q.list()) {
            Object[] row = (Object[])rowObj;
            if (row[0] != null && row[1] != null) {
	            // Month is zero based so we need to subtract by one.
	            results.add(new Pair<Long, Long>(Long.parseLong(row[0].toString()), (Long)row[1]));
            }
        }
        return results;
    }

    @Override
    public List<Pair<String, Long>> getDistinctCensusMethodTypes(Session sesh, User user) {
        StringBuilder b = new StringBuilder();
        b.append(" select distinct r.censusMethod.type, count(r)");
        b.append(" from Record as r");
        // add user visibility parameters here
        if (user != null) {
            if (!user.isAdmin()) {
                b.append(" where r.user.id = "+user.getId());
            }
        } else {
            b.append(" where r.recordVisibility = :vis and r.held = :held");
        }
        b.append(" group by r.censusMethod.type");
        b.append(" order by r.censusMethod.type asc");
        
        if(sesh == null) {
            sesh = super.getSessionFactory().getCurrentSession();
        }
        Query q = sesh.createQuery(b.toString());
        if (user == null) {
            q.setParameter("vis", RecordVisibility.PUBLIC);
            q.setParameter("held", false);
        }

        // Should get back a list of Object[]
        List<Pair<String, Long>> results =  new ArrayList<Pair<String, Long>>();
        for(Object rowObj : q.list()) {
            Object[] row = (Object[])rowObj;
            results.add(new Pair<String, Long>(row[0].toString(), (Long)row[1]));
        }
        return results;
    }

    @Override
    public List<Pair<String, Long>> getDistinctAttributeTypes(Session sesh, User user,
            AttributeType[] attributeTypes) {
        
        StringBuilder b = new StringBuilder();
        b.append(" select distinct a.typeCode, count(distinct r)");
        b.append(" from Record as r join r.attributes as ra join ra.attribute as a");
        b.append(" where length(trim(ra.stringValue)) > 0 and (1 = 2");
        for(AttributeType type : attributeTypes) {
            b.append(String.format(" or a.typeCode = '%s'", type.getCode()));
        }
        b.append(" )");
        // add user visibility parameters here
        if (user != null) {
            if (!user.isAdmin()) {
                b.append(" and r.user.id = "+user.getId());
            }
        } else {
            b.append(" and r.recordVisibility = :vis and r.held = :held");
        }
        b.append(" group by a.typeCode");
        b.append(" order by a.typeCode asc");
        
        if(sesh == null) {
            sesh = super.getSessionFactory().getCurrentSession();
        }
        
        Query q = sesh.createQuery(b.toString());
        if (user == null) {
            q.setParameter("vis", RecordVisibility.PUBLIC);
            q.setParameter("held", false);
        }

        // Should get back a list of Object[]
        List<Pair<String, Long>> results =  new ArrayList<Pair<String, Long>>();
        for(Object rowObj : q.list()) {
            Object[] row = (Object[])rowObj;
            results.add(new Pair<String, Long>(row[0].toString(), (Long)row[1]));
        }
        return results;
    }

    @Override
    public List<Pair<String, Long>> getDistinctAttributeValues(Session sesh, User user,
            String attributeName, int limit) {
        
        StringBuilder b = new StringBuilder();
        b.append(" select distinct ra.stringValue, count(distinct r)");
        b.append(" from Record as r join r.attributes as ra join ra.attribute as a");
        b.append(" where ");
        b.append(String.format(" a.description = '%s'", attributeName));
        // ignore empty string values
        b.append(" and ra.stringValue is not null and ra.stringValue != ''");
        
        // add user visibility parameters here
        if (user != null) {
            if (!user.isAdmin()) {
                b.append(" and r.user.id = "+user.getId());
            }
        } else {
            b.append(" and r.recordVisibility = :vis and r.held = :held");
        }
        b.append(" group by ra.stringValue");
        b.append(" order by 2 desc");
        
        if(sesh == null) {
            sesh = super.getSessionFactory().getCurrentSession();
        }
        
        Query q = sesh.createQuery(b.toString());
        if (user == null) {
            q.setParameter("vis", RecordVisibility.PUBLIC);
            q.setParameter("held", false);
        }
        if (limit > 0) {
            q.setMaxResults(limit);
        }
        
        List<Pair<String, Long>> results =  new ArrayList<Pair<String, Long>>();
        for(Object rowObj : q.list()) {
            Object[] row = (Object[])rowObj;
            results.add(new Pair<String, Long>(row[0].toString(), (Long)row[1]));
        }
        return results;
    }
    
    @Override
    public List<Record> find(Integer[] mapLayerId, Geometry intersectGeom, Boolean isPrivate, Integer userId) {
        // To avoid having an empty array which will cause an exception during the query.
        if (mapLayerId.length == 0) {
            mapLayerId = new Integer[] { 0 };
        }
        
        StringBuilder hb = new StringBuilder();
        hb.append("select distinct rec from Record rec inner join rec.survey survey where survey.id in ");
        hb.append(" (select s.id from GeoMapLayer layer inner join layer.survey s where layer.id in (:layerIds)) ");
        if (intersectGeom != null) {
            hb.append(" and intersects(:geom, rec.geometry) = true");
        }
        List<String> orSection = new LinkedList<String>();
        if (isPrivate != null) {
            if (isPrivate) {
                orSection.add("rec.recordVisibility = '" + RecordVisibility.OWNER_ONLY + "'");
            } else {
                orSection.add("rec.recordVisibility != '" + RecordVisibility.OWNER_ONLY + "'");
            }
        }
        if (userId != null) {
            orSection.add("rec.user.id = " + userId.toString());
        }
        
        if (orSection.size() > 0) {
            boolean firstItem = true;
            hb.append(" and (");
            for (String clause : orSection) {
                if (firstItem) {
                    firstItem = false;
                } else {
                    hb.append(" or ");
                }
                hb.append(clause);
            }
            hb.append(")");
        }

        Query q = getSession().createQuery(hb.toString());
        q.setParameterList("layerIds", mapLayerId);
        if (intersectGeom != null) {
            q.setParameter("geom", intersectGeom, GeometryUserType.TYPE);
        }
        return (List<Record>)q.list();
    }
    
    public List<Record> getRecords(int count, int offset) {
    	Query q = getSession().createQuery("from Record r order by r.id");
    	q.setMaxResults(count);
    	q.setFirstResult(offset);
    	return q.list();
    }

    @Override
    public Record getRecordByClientID(String clientID) {
        if(clientID == null) {
            throw new NullPointerException();
        }
        
        Session sesh = super.getSessionFactory().getCurrentSession();
        Query q = sesh.createQuery("select distinct r from Record r left join r.metadata md where md.key = :key and md.value = :value");
        q.setParameter("key", Metadata.RECORD_CLIENT_ID_KEY);
        q.setParameter("value", clientID);
        q.setMaxResults(1);
        return (Record)q.uniqueResult();
    }
    
    
}




