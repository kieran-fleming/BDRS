package au.com.gaiaresources.bdrs.model.record.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.QueryCriteria;
import au.com.gaiaresources.bdrs.db.QueryOperation;
import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.geometry.GeometryBuilder;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordAttribute;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.db.DeleteCascadeHandler;
import au.com.gaiaresources.bdrs.service.db.DeletionService;

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
        delService.registerDeleteCascadeHandler(RecordAttribute.class, new DeleteCascadeHandler() {
            @Override
            public void deleteCascade(PersistentImpl instance) {
                delete((RecordAttribute)instance);
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

        r.setAttributes(this.saveRecordAttributes(r, attributes));

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
    public List<Record> getRecords(User user, Survey survey, Location location) {
        return find("from Record r where r.location = ? and r.survey = ? and r.user = ?", new Object[] {location, survey, user});
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
        if (user != null)
            return newQueryCriteria(Record.class).add("user",
                    QueryOperation.EQUAL, user).count();
        else
            return newQueryCriteria(Record.class).count();
    }

    @Override
    public Integer countAllRecords() {
        Query q = getSession().createQuery("select count(*) from Record");
        Integer count = Integer.parseInt(q.list().get(0).toString(),10);
        return count;
    }

    @Override
    public Integer countRecordsForSpecies(IndicatorSpecies species) {
        return newQueryCriteria(Record.class).add("species", QueryOperation.EQUAL, species).count();
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
        Set<RecordAttribute> atts = this.saveRecordAttributes(r, attributes);
        r.setAttributes(atts);
        save(r);
        return r;
    }
    @Override
    public  Set<RecordAttribute> saveRecordAttributes(Record r, Map<Attribute, Object> attributeMap){
        Set<RecordAttribute> atts = new HashSet<RecordAttribute>();
        for (Map.Entry<Attribute, Object> attValue : attributeMap
                .entrySet()) {
            if (attValue != null) {
                RecordAttribute attribute = new RecordAttribute();
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
                case STRING:
                case STRING_WITH_VALID_VALUES:
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
            point = record.getLocation().getLocation();
        }
        else{
            log.warn("Record Needs to have a point or a location associated with it");
            return new HashSet<Record>();
        }
        Geometry buffer = geometryBuilder.bufferInM(point, bufferMetre);
        QueryCriteria<Record> queryCriterea = newQueryCriteria(Record.class).add("point", QueryOperation.INTERSECTS, buffer)
            .add("when", QueryOperation.GREATER_THAN_OR_EQUAL, timeFrom)
            .add("when", QueryOperation.LESS_THAN_OR_EQUAL, timeUntil)
            .add("id", QueryOperation.NOT_EQUAL, record.getId())
            .add("species", QueryOperation.EQUAL, record.getSpecies());
        if(excludeRecordIds != null && excludeRecordIds.length > 0) {
            queryCriterea.add("id", QueryOperation.NOT_IN, (Object[])excludeRecordIds);
        }
        if(includeRecordIds!= null && includeRecordIds.length > 0) {
            queryCriterea.add("id", QueryOperation.IN, (Object[])includeRecordIds);
        }
        return new HashSet<Record>(queryCriterea.run());
    }

    @Override
    public List<Record> getRecord(int userId, int groupId, int surveyId,
            int taxonGroupId, Date startDate, Date endDate,
            String speciesScientificNameSearch, int limit) {
        return getRecord(userId, groupId, surveyId,
                taxonGroupId, startDate, endDate,
                speciesScientificNameSearch, limit, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Record> getRecord(int userId, int groupId, int surveyId,
            int taxonGroupId, Date startDate, Date endDate,
            String speciesScientificNameSearch, int limit, boolean fetch) {

        Calendar cal = new GregorianCalendar();

        if(startDate == null) {
            startDate = new Date(1l);
        }

        cal.setTime(startDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        startDate = cal.getTime();

        if(endDate == null) {
            endDate = new Date(System.currentTimeMillis());
        }
        cal.clear();
        cal.setTime(endDate);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        endDate = cal.getTime();

        Map<String, Object> paramMap = new HashMap<String, Object>();
        StringBuilder builder = new StringBuilder("select r from Record r ");
        if (fetch) {
            builder.append("left join fetch r.attributes at left join fetch at.attribute left join fetch r.location left join fetch r.user left join fetch r.survey left join fetch r.species ");
        }
        builder.append("where r.id > 0");

        builder.append(" and r.time >= :startTime and r.time <= :endTime");
        paramMap.put("startTime", startDate.getTime());
        paramMap.put("endTime", endDate.getTime());

        if(userId > 0) {
            builder.append(" and r.user.id = :userId");
            paramMap.put("userId", userId);
        }
        if(surveyId > 0) {
            builder.append(" and r.survey.id = :surveyId");
            paramMap.put("surveyId", surveyId);
        }
        if(groupId > 0) {
            // Cascade classes and groups
            builder.append(" and r.user.id in (select u.id from Group c, Group g, User u where g.id in (select id from c.groups) and u.id in (select id from g.users) and (c.id = :groupId or g.id = :groupId))");
            paramMap.put("groupId", groupId);
        }
        if(taxonGroupId > 0) {
            builder.append(" and r.species.id in (select s1.id from IndicatorSpecies s1 where s1.taxonGroup.id = :taxonGroupId)");
            paramMap.put("taxonGroupId", taxonGroupId);
        }
        if(speciesScientificNameSearch != null && !speciesScientificNameSearch.isEmpty()) {
            builder.append(" and (UPPER(r.species.commonName) like UPPER('%" + StringEscapeUtils.escapeSql(speciesScientificNameSearch) +
                    "%') or UPPER(r.species.scientificName) like UPPER ('%" + StringEscapeUtils.escapeSql(speciesScientificNameSearch) + "%'))");
        }
        builder.append(" order by r.when desc");

        log.debug(builder.toString());
        Query q = getSession().createQuery(builder.toString());
        for(Map.Entry<String,Object> entry: paramMap.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }
        q.setMaxResults(limit);
        log.debug("Limiting to "+limit+" records");

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
    public AttributeValue updateAttribute(Integer id, BigDecimal numeric,
            String value, Date date) {
        RecordAttribute att = getByID(RecordAttribute.class, id);
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
    public RecordAttribute saveRecordAttribute(RecordAttribute recAttr) {
        return save(recAttr);
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
    public RecordAttribute getRecordAttribute(int recordAttributePk) {
        return getByID(RecordAttribute.class, recordAttributePk);
    }
    
    @Override
    public PersistentImpl getRecordForRecordAttributeId(
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
        Set<RecordAttribute> attributeList = new HashSet<RecordAttribute>(record.getAttributes());
        record.getAttributes().clear();
        record = save(record);
        
        DeleteCascadeHandler cascadeHandler = 
            delService.getDeleteCascadeHandlerFor(RecordAttribute.class);
        for(RecordAttribute recAttr : attributeList) {
            recAttr = saveRecordAttribute(recAttr);
            cascadeHandler.deleteCascade(recAttr);
        }
        deleteByQuery(record);
    }
    
    @Override
    public void delete(RecordAttribute recordAttribute) {
        deleteByQuery(recordAttribute);
    }
}




