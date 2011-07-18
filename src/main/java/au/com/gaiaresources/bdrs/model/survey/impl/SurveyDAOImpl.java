package au.com.gaiaresources.bdrs.model.survey.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.classic.Session;
import org.hibernate.type.CustomType;
import org.hibernatespatial.GeometryUserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.controller.file.AbstractDownloadFileController;
import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.db.impl.HqlQuery;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.db.impl.QueryPaginator;
import au.com.gaiaresources.bdrs.geometry.GeometryBuilder;
import au.com.gaiaresources.bdrs.model.group.Group;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.db.DeleteCascadeHandler;
import au.com.gaiaresources.bdrs.service.db.DeletionService;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

/**
 * @author aj
 */
@SuppressWarnings("unchecked")
@Repository
public class SurveyDAOImpl extends AbstractDAOImpl implements SurveyDAO {
     Logger log = Logger.getLogger(AbstractDownloadFileController.class);
     private static String CACHE_NAME = "survey.cache";
     
     @Autowired
     private DeletionService delService;
     
     @PostConstruct
     public void init() throws Exception {
         delService.registerDeleteCascadeHandler(Survey.class, new DeleteCascadeHandler() {
             @Override
             public void deleteCascade(PersistentImpl instance) {
                 delete((Survey)instance);
             }
         });
     }
     
     public void delete(Survey survey) {
         List<Location> locationsList = new ArrayList(survey.getLocations());
         List<Attribute> attributesList = new ArrayList(survey.getAttributes());
         Set<Metadata> metadataSet = new HashSet(survey.getMetadata());
         
         survey.getLocations().clear();
         survey.getAttributes().clear();
         survey.getMetadata().clear();
         
         survey = save(survey);
         
         DeleteCascadeHandler mdHandler = delService.getDeleteCascadeHandlerFor(Metadata.class);
         for(Metadata md : metadataSet) {
             super.save(md);
             mdHandler.deleteCascade(md);
         }
         
         DeleteCascadeHandler locationHandler = delService.getDeleteCascadeHandlerFor(Location.class);
         for(Location loc : locationsList) {
             super.save(loc);
             locationHandler.deleteCascade(loc);
         }
         
         DeleteCascadeHandler attributeHandler = delService.getDeleteCascadeHandlerFor(Attribute.class);
         for(Attribute attr : attributesList) {
             super.save(attr);
             attributeHandler.deleteCascade(attr);
         }
         
         super.deleteByQuery(survey);
     }
     
    public List<Survey> getSurveys(User user) {
        if(user == null) return new ArrayList<Survey>();
        if(user.isAdmin()) {
            return find("from Survey order by name asc");
        } else {
            List<Group> klasses = find("select c from Group c left join fetch c.groups g left join fetch g.users u where u=?", new Object[]{user});
            StringBuilder builder = new StringBuilder();
            // Find the surveys
            builder.append("select distinct s");
            builder.append(" from Survey s left outer join fetch s.users u");
            
            if(!klasses.isEmpty()) {
                builder.append(" left outer join fetch s.groups c");
            }
            builder.append(" where u=:user");
            if(!klasses.isEmpty()) {
                builder.append(" or c in (:classgroup)");
            }

            builder.append(" order by s.name asc");

            Query q = getSession().createQuery(builder.toString());
            q.setParameter("user", user);
            if(!klasses.isEmpty()) {
                // Because HQL is fracked and cannot handle 'extra' parameters
                q.setParameterList("classgroup", klasses);
            }

            List<Survey> surveys = q.list();
            return surveys;
        }
    }

    public List<Survey> getActiveSurveysForUser(User user) {
        StringBuilder builder = new StringBuilder();

        // Find all surveys that are active, and
        // public or
        // user is in the survey or group
        builder.append("select distinct s");
        builder.append(" from Survey s left join s.users u left join s.groups g");
        builder.append("  where s.active = true");
        builder.append("   and ((u = :user)");
        builder.append("         or ");
        builder.append("        (s.public = true)");
        builder.append("         or ");
        builder.append("        (u.id in (select id from g.users)");
        builder.append("                  and g.id in (select id from s.groups)");
        builder.append("        )");
        builder.append("       )");
        builder.append("   order by s.name");

        Query q = getSession().createQuery(builder.toString());
        q.setParameter("user", user);
        return q.list();
    }

    public Survey getLastSurveyForUser(User user) {
        List<Survey> surveys = getSurveys(user);
        if (surveys.size() > 0)
            return surveys.get(surveys.size() - 1);
        return null;
    }

    public Survey createSurvey(String name) {
        Survey survey = new Survey();
        survey.setName(name);
        save(survey);
        return survey;
    }

    public Survey updateSurvey(Survey survey) {
        //Object o = merge(survey);
        //update((Survey)o);
        update(survey);
        this.expireCache(survey);
        return survey;
        //return (Survey) o;
//    	return update(survey);
    }

    public Survey get(Integer id) {
        return getByID(Survey.class, id);
    }

    @Override
    public Survey getSurvey(int pk) {
        return getByID(Survey.class, pk);
    }
    
    @Override
    public Survey getSurveyData(int pk) {
    	List<Survey> surveys = find("select s from Survey s left outer join fetch s.attributes a left outer join fetch a.options o where s.id=" + pk);
    	return (surveys.size() > 0) ? surveys.get(0) : null;
    }

    @Override
    public List<IndicatorSpecies> getSpeciesForSurvey(Survey thisSurvey, List<Survey> notTheseSurveys) {
    	for (Survey s : notTheseSurveys) {
    		if (s.getSpecies().size() == 0) {
    			// Device already has all species, so let's get outta here.
    			return new ArrayList<IndicatorSpecies>();
    		}
    	}
    	if (thisSurvey.getSpecies().size() == 0) {
    		return find("from IndicatorSpecies");
    	}
    	
    	String query2 = "select a1 from Survey a join a.species a1 where a1 not in (select b1 from Survey b join b.species b1 where b in (:notIds)) and a.id = :id";    	 
 	    Query q = getSession().createQuery(query2);
 	    q.setParameter("id", thisSurvey.getId());
 	    q.setParameterList("notIds", notTheseSurveys);
 	    return q.list();
    }
    
    @Override
    public Survey getSurvey(Session sesh, int pk) {
        if(sesh == null) {
            return getByID(Survey.class, pk); 
        } else {
            return (Survey)sesh.get(Survey.class, pk);
        }
    }
    
    @Override 
    public Survey getSurvey(org.hibernate.Session sesh, int pk) {
        return super.getByID(sesh, Survey.class, pk);
    }

    @Override
    public Survey createSurvey(Survey survey) {
        survey.setActive(true);
        return super.save(survey);
    }

    @Override
    public Survey save(Survey survey) {

        // Save transient objects first

        if (survey.getId() == null
                || (survey.getId() != null && survey.getId() < 1)) {
            return this.createSurvey(survey);
        } else {
            this.expireCache(survey);
            return this.updateSurvey(survey);
        }
    }


    @Override
    public Survey getSurveyByName(Session sesh, String surveyName) {
        List<Survey> surveys;
        if(sesh == null) {
            surveys = find("select s from Survey s where s.name = ?", surveyName);
        }
        else {
            surveys = find(sesh, "select s from Survey s where s.name = ?", surveyName);
        }
        
        if(surveys.isEmpty()) {
            return null;
        } else {
            if(surveys.size() > 1) {
                log.warn("Multiple surveys with the same name found. Returning the first");

            }
            return surveys.get(0);
        }
    }

    @Override
    public Survey getSurveyByName(String surveyName) {
        return getSurveyByName(null, surveyName);
    }

    @Override
    public List<Record> getRecordsForSurvey(Survey survey) {
        return find("select r from Record r, Survey s where r.survey = s and s = ?", survey);
    }

    @Override
    public List<IndicatorSpecies> getSpeciesForSurveySearch(int surveyPk, String species) {

    	StringBuilder query = new StringBuilder("");
		String[] bits = species.split(" ");
		for (int i = 0; i < bits.length; i++) {
			query.append("%");
			query.append(bits[i]);
		}
		query.append("%");
    	
	    Query q;
	    String query2 = "select count(t) from Survey s join s.species t where s.id = :survey";
	    q = getSession().createQuery(query2);
	    q.setParameter("survey", surveyPk);
	    int count = Integer.parseInt(q.list().get(0).toString(), 10);
        StringBuilder builder = new StringBuilder();

	    if (count > 0) {
	        builder.append("select distinct t");
	        builder.append(" from Survey s left join s.species as t");
	        builder.append(" where s.id = :surveyPk");
	        builder.append("  and (");
	        builder.append("        UPPER(t.commonName) like UPPER('" + query.toString() + "') ");
	        builder.append("        or UPPER(t.scientificName) like UPPER ('" + query.toString() + "')");
	        builder.append("      )");
	        builder.append(" order by t.scientificName");
	        q = getSession().createQuery(builder.toString());
	        q.setParameter("surveyPk", surveyPk);
	        
	    } else {
	    	builder.append("select distinct t from IndicatorSpecies t where ");
	        builder.append("        UPPER(t.commonName) like UPPER('" + query.toString() + "') ");
	        builder.append("        or UPPER(t.scientificName) like UPPER ('" + query.toString() + "')");
	        builder.append("      )");
	        builder.append(" order by t.scientificName");
	        q = getSession().createQuery(builder.toString());    
	    }
	    
	    return q.list();
    }
    
    @Override
    public List<IndicatorSpecies> getSurveySpeciesForTaxon(int surveyId, int taxonGroupId){
    	Integer[] args = new Integer[] {surveyId, taxonGroupId};
    	return find("select sp from Survey s left join s.species sp  left join sp.taxonGroup tg where s.id = ? and tg.id = ?", args);
    }
    @Override
    public Set<IndicatorSpecies> getSpeciesWithinSurveyLocations(Survey s){
        if (s == null) {
            return new HashSet<IndicatorSpecies>();
        }
 
        if(s.getLocations().size()<1){
            // There are no locations associated with this survey, return all species
            return s.getSpecies();
        }
        /*
         * First we convert the survey's location list into a Geometry, so we can query on 
         * it
         */
        CacheManager cacheManager = CacheManager.getInstance();
        Cache cache = null;
        Set<Integer> speciesIds;
        Set<IndicatorSpecies> result = new HashSet<IndicatorSpecies>();
        
        boolean cacheExists = cacheManager.cacheExists(CACHE_NAME); 
        if (cacheExists) {
            cache = cacheManager.getCache(CACHE_NAME);
        }
        if(!cacheExists || cache == null) {
            cache = new Cache(CACHE_NAME, 120, false, false, 600, 600);
            cacheManager.addCache(cache);
        } else {
            if(cache.isKeyInCache(s) && cache.get(s) != null && cache.get(s).getObjectValue() != null){
                speciesIds = (Set<Integer>) cache.get(s).getObjectValue();
                if (speciesIds == null || speciesIds.size() < 1){
                    return new HashSet<IndicatorSpecies>();
                }
                String hql = "select sp from IndicatorSpecies sp where sp.id in (:speciesIds)";
                Query q = getSession().createQuery(hql);
                q.setParameterList("speciesIds", speciesIds);
                result.addAll(q.list());
                return result;
            }
        }

        GeometryBuilder geometryBuilder = new GeometryBuilder();
        GeometryFactory geometryFactory = geometryBuilder.getFactory();
        Set<Point> pointSet = new HashSet<Point>();
        String hql = "select sp from Survey s join s.species sp  join sp.regions r WHERE  s = :survey AND intersects(r.boundary, :locations) = TRUE";
        Query q = getSession().createQuery(hql);
        q.setParameter("survey", s);
        CustomType geometryType = new CustomType(GeometryUserType.class, null);
        for(Location l: s.getLocations()){
            pointSet.add(l.getLocation());
            
        }
        MultiPoint locationPoints = geometryFactory.createMultiPoint((Point[]) pointSet.toArray(new Point[pointSet.size()]));
        q.setParameter("locations", locationPoints, geometryType);
        result.addAll(q.list());
        speciesIds = new HashSet<Integer>(result.size());
        for (IndicatorSpecies species: result){
            speciesIds.add(species.getId());
        }
        
        cache.put(new Element(s, speciesIds));
        return result;
    }
    
        @Override
        public void expireCache(Survey s){
            CacheManager cacheManager = CacheManager.getInstance();
            if(cacheManager.cacheExists(CACHE_NAME)){
                cacheManager.getCache(CACHE_NAME).remove(s);
            }
            
        }
    
        @Override
    public List<Survey> getActivePublicSurveys(boolean fetchMetadata) {
        //left join fetch
        StringBuilder builder = new StringBuilder();
        builder.append(" select distinct s");
        builder.append(" from Survey s");
        if(fetchMetadata) {
            builder.append(" left join fetch s.metadata");
        }
        builder.append(" where s.active = true and");
        builder.append(" s.public = true");
        
        return getSession().createQuery(builder.toString()).list();
    }
    
	@Override
	public Integer countSpeciesForSurvey(int surveyPk) {
		Query q = getSession().createQuery(
				"select count(*) from Survey s join s.species as i where s.id = :surveyPk");
		q.setParameter("surveyPk", surveyPk);
		Integer count = Integer.parseInt(q.list().get(0).toString(), 10);
		return count;
	}

    @Override
    public void delete(int id) {
        // This bit works. It's frakked up, but it works.
        Survey s = get(id);
        s = (Survey)merge(s);

        List<Record> recordList = getRecordsForSurvey(s);
        for(Record r : recordList) {
            r.setSurvey(null);
            update(r);
        }

        s.setLocations(new ArrayList<Location>());
        s.setUsers(new HashSet<User>());
        s.setGroups(new HashSet<Group>());
        s.setSpecies(new HashSet<IndicatorSpecies>());

        update(s);
        delete(s);
    }

    @Override
    public List<Survey> getSurveys(IndicatorSpecies taxon) {
        return super.find("select distinct s from Survey s left join s.species t where t = ?", taxon);
    }
    
    @Override
    public PagedQueryResult<Survey> search(PaginationFilter filter) {
        HqlQuery q = new HqlQuery("from Survey s");
        return new QueryPaginator<Survey>().page(this.getSession(), q.getQueryString(), q.getParametersValue(), filter);
    }
}
