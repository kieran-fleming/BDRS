package au.com.gaiaresources.bdrs.model.taxa.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StaleStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.QueryOperation;
import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.db.impl.HqlQuery;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.db.impl.QueryPaginator;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.region.Region;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeOption;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpeciesAttribute;
import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfile;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.taxa.TaxonRank;
import au.com.gaiaresources.bdrs.service.db.DeleteCascadeHandler;
import au.com.gaiaresources.bdrs.service.db.DeletionService;
import au.com.gaiaresources.bdrs.util.Pair;

/**
 * 
 * @author Tim Carpenter
 * 
 */
@SuppressWarnings("unchecked")
@Repository
public class TaxaDAOImpl extends AbstractDAOImpl implements TaxaDAO {

    private Logger log = Logger.getLogger(getClass());
    
    @Autowired
    private DeletionService delService;
    
    @PostConstruct
    public void init() throws Exception {
        delService.registerDeleteCascadeHandler(TaxonGroup.class, new DeleteCascadeHandler() {
            @Override
            public void deleteCascade(PersistentImpl instance) {
                delete((TaxonGroup)instance);
            }
        });
        delService.registerDeleteCascadeHandler(IndicatorSpecies.class, new DeleteCascadeHandler() {
            @Override
            public void deleteCascade(PersistentImpl instance) {
                delete((IndicatorSpecies)instance);
            }
        });
        delService.registerDeleteCascadeHandler(IndicatorSpeciesAttribute.class, new DeleteCascadeHandler() {
            @Override
            public void deleteCascade(PersistentImpl instance) {
                delete((IndicatorSpeciesAttribute)instance);
            }
        });
    }
	
    @Override
    public TaxonGroup createTaxonGroup(String name, boolean includeBehaviour,
            boolean includeFirstAppearance, boolean includeLastAppearance,
            boolean includeHabitat, boolean includeWeather,
            boolean includeNumber) {
        TaxonGroup tg = new TaxonGroup();
        tg.setName(name);
        tg.setBehaviourIncluded(includeBehaviour);
        tg.setFirstAppearanceIncluded(includeFirstAppearance);
        tg.setLastAppearanceIncluded(includeLastAppearance);
        tg.setHabitatIncluded(includeHabitat);
        tg.setWeatherIncluded(includeWeather);
        tg.setNumberIncluded(includeNumber);
        return save(tg);
    }

	@Override
	public TaxonGroup createTaxonGroup(String name, boolean includeBehaviour,
			boolean includeFirstAppearance, boolean includeLastAppearance,
			boolean includeHabitat, boolean includeWeather,
			boolean includeNumber, String image, String thumbNail) {
		TaxonGroup tg = new TaxonGroup();
		tg.setName(name);
		tg.setBehaviourIncluded(includeBehaviour);
		tg.setFirstAppearanceIncluded(includeFirstAppearance);
		tg.setLastAppearanceIncluded(includeLastAppearance);
		tg.setHabitatIncluded(includeHabitat);
		tg.setWeatherIncluded(includeWeather);
		tg.setNumberIncluded(includeNumber);
		tg.setImage(image);
		tg.setThumbNail(thumbNail);
		return save(tg);
	}

	@Override
	public TaxonGroup updateTaxonGroup(Integer id, String name,
			boolean includeBehaviour, boolean includeFirstAppearance,
			boolean includeLastAppearance, boolean includeHabitat,
			boolean includeWeather, boolean includeNumber) {
	    
		TaxonGroup tg = getTaxonGroup(id);
		tg.setName(name);
		tg.setBehaviourIncluded(includeBehaviour);
		tg.setFirstAppearanceIncluded(includeFirstAppearance);
		tg.setLastAppearanceIncluded(includeLastAppearance);
		tg.setHabitatIncluded(includeHabitat);
		tg.setWeatherIncluded(includeWeather);
		tg.setNumberIncluded(includeNumber);
		return update(tg);
	}

	@Override
	public TaxonGroup updateTaxonGroup(Integer id, String name,
			boolean includeBehaviour, boolean includeFirstAppearance,
			boolean includeLastAppearance, boolean includeHabitat,
			boolean includeWeather, boolean includeNumber, String image,
			String thumbNail) {
		TaxonGroup tg = getTaxonGroup(id);
		tg.setName(name);
		tg.setBehaviourIncluded(includeBehaviour);
		tg.setFirstAppearanceIncluded(includeFirstAppearance);
		tg.setLastAppearanceIncluded(includeLastAppearance);
		tg.setHabitatIncluded(includeHabitat);
		tg.setWeatherIncluded(includeWeather);
		tg.setNumberIncluded(includeNumber);
		tg.setImage(image);
		tg.setThumbNail(thumbNail);
		return update(tg);
	}

	public List<TaxonGroup> getTaxonGroup(Survey survey) {
		if (survey.getSpecies().size() == 0) {
			return getTaxonGroups();
		} else {
			
			return find("select distinct g from IndicatorSpecies i join i.taxonGroup g where i in (select elements(b.species) from Survey b where b = ?)", survey);
//			StringBuilder builder = new StringBuilder();
//			builder.append("select distinct g");
//			builder.append(" from TaxonGroup g, IndicatorSpecies i, Survey s");
//			builder.append(" where i.taxonGroup=g");
//			builder.append(" and i.id in (select id from s.species) and s=?");
//			return find(builder.toString(), survey);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaxonGroup getTaxonGroup(String name) {
		return newQueryCriteria(TaxonGroup.class).add("name",
				QueryOperation.EQUAL, name).runAndGetFirst();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaxonGroup getTaxonGroup(Session sesh, String name) {
		List<TaxonGroup> groups = this.find(sesh,
				"from TaxonGroup g where name = ?", name);
		if (groups.isEmpty()) {
			return null;
		} else {
			if (groups.size() > 1) {
				log.warn("Multiple TaxonGroups matched. Returning the first.");
			}
			return (TaxonGroup) groups.get(0);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<TaxonGroup> getTaxonGroupSearch(String nameFragment) {
		return this.find("from TaxonGroup g where UPPER(name) like UPPER('%"
				+ nameFragment + "%')");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaxonGroup getTaxonGroup(Integer id) {
		return getByID(TaxonGroup.class, id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<TaxonGroup> getTaxonGroups() {
		return find("from TaxonGroup");
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<TaxonGroup> getTaxonGroupsSortedByName() {
	    return find("from TaxonGroup g order by g.name"); 
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Attribute createAttribute(TaxonGroup group, String name,
			AttributeType type, boolean required) {
		Attribute attribute = new Attribute();

		attribute.setName(name);
		attribute.setTypeCode(type.getCode());
		attribute.setRequired(required);
		Attribute att = save(attribute);
		group.getAttributes().add(att);
		save(group);
		return att;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Attribute createAttribute(TaxonGroup group, String name,
			AttributeType type, boolean required, boolean isTag) {
		return createAttribute(group, name, null, type, required, isTag);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Attribute createAttribute(TaxonGroup group, String name,
			String description, AttributeType type, boolean required,
			boolean isTag) {
		Attribute attribute = new Attribute();

		attribute.setName(name);
		attribute.setDescription(description);
		attribute.setTypeCode(type.getCode());
		attribute.setRequired(required);
		attribute.setTag(isTag);
		Attribute att = save(attribute);
		group.getAttributes().add(att);
		save(group);
		return att;
	}

	@Override
	public Attribute save(Attribute attribute) {
		return super.save(attribute);
	}

	@Override
	public TaxonGroup save(TaxonGroup taxongroup) {
		return super.save(taxongroup);
	}

	@Override
	public TaxonGroup save(Session sesh, TaxonGroup taxongroup) {
		return super.save(sesh, taxongroup);
	}

	@Override
	public IndicatorSpecies save(IndicatorSpecies taxon) {
		return super.save(taxon);
	}

	@Override
	public IndicatorSpecies save(Session sesh, IndicatorSpecies taxon) {
		return super.save(sesh, taxon);
	}

	@Override
	public AttributeOption save(AttributeOption opt) {
		return super.save(opt);
	}

	@Override
	public IndicatorSpeciesAttribute save(
			IndicatorSpeciesAttribute taxonAttribute) {
		return super.save(taxonAttribute);
	}

	@Override
	public IndicatorSpeciesAttribute save(Session sesh,
			IndicatorSpeciesAttribute taxonAttribute) {
		return super.save(sesh, taxonAttribute);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Attribute updateAttribute(Integer id, String name,
			AttributeType type, boolean required) {
		Attribute att = getByID(Attribute.class, id);
		att.setName(name);
		att.setTypeCode(type.getCode());
		att.setRequired(required);
		return update(att);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Attribute updateAttribute(Integer id, String name,
			String description, AttributeType type, boolean required) {
		Attribute att = getByID(Attribute.class, id);
		att.setName(name);
		att.setDescription(description);
		att.setTypeCode(type.getCode());
		att.setRequired(required);
		return update(att);
	}

	/**
	 * {@inheritDoc}
	 */
	public AttributeOption createAttributeOption(Attribute attribute,
			String option) {
		AttributeOption optionImpl = new AttributeOption();
		optionImpl.setValue(option);
		AttributeOption opt = save(optionImpl);
		attribute.getOptions().add(opt);
		save(attribute);
		return opt;
	}

	public TypedAttributeValue createIndicatorSpeciesAttribute(
			IndicatorSpecies species, Attribute attr, String value) {
		return createIndicatorSpeciesAttribute(species, attr, value, null);
	}

	public TypedAttributeValue createIndicatorSpeciesAttribute(
			IndicatorSpecies species, Attribute attr, String value, String desc) {
		IndicatorSpeciesAttribute impl = new IndicatorSpeciesAttribute();
		impl.setDescription(desc);
		impl.setAttribute(attr);
		impl.setStringValue(value);
		if (attr == null) {
			log.debug("Indicator species: " + species.getScientificName()
					+ "attribute " + value + " null.");
			return impl;
		}
		return save(impl);
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteTaxonGroupAttributeOption(Integer id) {
		delete(getOption(id));
	}

	public AttributeOption getOption(Integer id) {
		return getByID(AttributeOption.class, id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Attribute getAttribute(Integer id) {
		return getByID(Attribute.class, id);
	}

	@Override
	public Attribute getAttribute(TaxonGroup taxonGroup, String name,
			boolean isTag) {
		Object[] args = { taxonGroup, name, (Boolean) isTag };
		String hql = "select attribute from TaxonGroup tg join tg.attributes attribute where tg = ? and attribute.name = ? and attribute.tag = ?";
		List attributes = find(hql, args);
		if (attributes.size() > 0) {
			return (Attribute) attributes.get(0);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IndicatorSpecies createIndicatorSpecies(String scientificName,
			String commonName, TaxonGroup taxonGroup,
			Collection<Region> regions, List<SpeciesProfile> infoItems) {
		IndicatorSpecies species = new IndicatorSpecies();
		species.setCommonName(commonName);
		species.setScientificName(scientificName);
		species.setTaxonGroup(taxonGroup);
		Set<Region> regionSet = new HashSet<Region>();
		for (Region r : regions) {
			regionSet.add(r);
		}
		species.setRegions(regionSet);
		if (infoItems != null)
			species.setInfoItems((List<SpeciesProfile>) infoItems);
		return save(species);
	}

	/**
	 * {@inheritDoc}
	 */
	public IndicatorSpecies updateIndicatorSpecies(Integer id,
			String scientificName, String commonName, TaxonGroup taxonGroup,
			Collection<Region> regions, List<SpeciesProfile> infoItems) {
		IndicatorSpecies species = getIndicatorSpecies(id);
		species.setCommonName(commonName);
		species.setScientificName(scientificName);
		species.setTaxonGroup(taxonGroup);
		Set<Region> regionSet = new HashSet<Region>();
		for (Region r : regions) {
			regionSet.add(r);
		}
		species.setRegions(regionSet);

		if (infoItems != null) {
			for (SpeciesProfile oldProfile : species.getInfoItems()) {
				delete(oldProfile);
			}
			species.setInfoItems((List<SpeciesProfile>) infoItems);
		}

		return update(species);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndicatorSpecies updateIndicatorSpecies(Integer id,
			String scientificName, String commonName, TaxonGroup taxonGroup,
			Collection<Region> regions, List<SpeciesProfile> infoItems,
			Set<IndicatorSpeciesAttribute> attributes) {
		IndicatorSpecies species = getIndicatorSpecies(id);
		species.setCommonName(commonName);
		species.setScientificName(scientificName);
		species.setTaxonGroup(taxonGroup);
		Set<Region> regionSet = new HashSet<Region>();
		for (Region r : regions) {
			regionSet.add(r);
		}
		species.setRegions(regionSet);

		if (infoItems != null) {
			for (SpeciesProfile oldProfile : species.getInfoItems()) {
				delete(oldProfile);
			}
			species.setInfoItems((List<SpeciesProfile>) infoItems);
		}
		species.setAttributes(attributes);

		return update(species);
	}

	@Override
	public IndicatorSpecies updateIndicatorSpecies(IndicatorSpecies species) {
		return save(species);
	}

	@Override
	public List<IndicatorSpecies> getIndicatorSpecies() {
		return find("from IndicatorSpecies");
	}

	@Override
	public List<IndicatorSpecies> getIndicatorSpeciesById(Integer[] pks) {
		Query q = getSession().createQuery(
				"from IndicatorSpecies s where s.id in (:pks)");
		q.setParameterList("pks", pks);
		return q.list();
	}

	@Override
	public IndicatorSpecies getIndicatorSpeciesBySourceDataID(Session sesh,
			String sourceDataId) {
		String query = "select s from IndicatorSpecies s join s.metadata md where md.key = :source_data_id_key and md.value = :source_data_id";
		Query q;
		if (sesh == null) {
			q = getSession().createQuery(query);
		} else {
			q = sesh.createQuery(query);
		}
		q.setParameter("source_data_id_key", Metadata.TAXON_SOURCE_DATA_ID);
		q.setParameter("source_data_id", sourceDataId);
		List<IndicatorSpecies> taxonList = q.list();
		if (taxonList.isEmpty()) {
			return null;
		} else {
			if (taxonList.size() > 1) {
				log
						.warn("More than one IndicatorSpecies returned for the provided Source Data ID: "
								+ sourceDataId + " Returning the first");
			}
			return taxonList.get(0);
		}
	}

	@Override
	public List<IndicatorSpecies> getIndicatorSpeciesBySpeciesProfileItem(
			String type, String content) {
		String[] args = { type, content };
		String hql = "select s from IndicatorSpecies s join s.infoItems p where p.type = ? and p.content = ?";
		List<IndicatorSpecies> result = this.find(hql, args);
		return result;
	}
	
	@Override
	public List<IndicatorSpecies> getIndicatorSpeciesBySurvey(Session sesh, Survey survey, int start, int maxResults) {
	    Query q;
	    String query = "select count(t) from Survey s join s.species t where s = :survey";
	    if(sesh == null) {
	        q = getSession().createQuery(query);
	    } else {
	        q = sesh.createQuery(query);
	    }
	    q.setParameter("survey", survey);
	    int count = Integer.parseInt(q.list().get(0).toString(), 10);
	    
	    if(count == 0) {
	        query = "select t from IndicatorSpecies t left join fetch t.taxonGroup";
	        if(sesh == null) {
	            q = getSession().createQuery(query);
	        } else {
	            q = sesh.createQuery(query);
	        }
	    } else {
	        query = "select t from Survey s join s.species t left join fetch t.taxonGroup where s = :survey";
	        if(sesh == null) {
	            q = getSession().createQuery(query);
	        } else {
	            q = sesh.createQuery(query);
	        }
	        q.setParameter("survey", survey);
	    }
	    q.setFirstResult(start);
	    q.setMaxResults(maxResults);
	    return q.list();
	}

	@Override
	public List<IndicatorSpecies> getIndicatorSpecies(Region region) {
		return this.find(
				"from IndicatorSpecies i where ? in elements(i.regions)",
				region.getId());
	}

	@Override
	public List<IndicatorSpecies> getIndicatorSpecies(TaxonGroup group) {
		return this.find("from IndicatorSpecies i where i.taxonGroup.id = ?",
				group.getId());
	}

	@Override
    public List<IndicatorSpecies> getIndicatorSpeciesByNameSearch(String name) {
    	StringBuilder query = new StringBuilder("");
		String[] bits = name.split(" ");
		for (int i = 0; i < bits.length; i++) {
			query.append("%");
			query.append(bits[i]);
		}
		query.append("%");
    	
        return find("from IndicatorSpecies i where UPPER(commonName) like UPPER('"
                + query.toString()
                + "') or UPPER(scientificName) like UPPER ('"
                + query.toString() + "')", new Object[0], 30);
    }
	
    @Override
    public SpeciesProfile getSpeciesProfileById(Integer id) {
        return getByID(SpeciesProfile.class, id);
    }
    
    @Override
	public IndicatorSpecies getIndicatorSpecies(Integer id) {
		return getByID(IndicatorSpecies.class, id);
	}

	@Override
	public IndicatorSpecies getIndicatorSpeciesByGuid(String guid) {
		Object[] args = new Object[2];
		args[0] = Metadata.SCIENTIFIC_NAME_SOURCE_DATA_ID;
		args[1] = guid;
		List<IndicatorSpecies> list = find("select i from IndicatorSpecies i join i.metadata m where m.key = ? and m.value = ?", args);
		if (list.size() == 0) {
			log.warn("No species found for guid : " + guid);
			return null;
		} else if (list.size() > 1) {
			log.warn("Multiple species found for guid, return first : " + guid);
		}
		return list.get(0);
	}

    @Override
    public List<IndicatorSpecies> getIndicatorSpeciesByCommonName(
            String commonName) {
        return this.find("from IndicatorSpecies i where UPPER(commonName) like UPPER(?)", commonName);
    }

	@Override
	public IndicatorSpecies getIndicatorSpeciesByCommonName(Session sesh,
			String commonName) {
		List<IndicatorSpecies> species = find(sesh,
				"select i from IndicatorSpecies i where i.commonName = ?",
				commonName);
		if (species.isEmpty()) {
			return null;
		} else {
			if (species.size() > 1) {
				log
						.warn("Multiple IndicatorSpecies with the same common name found. Returning the first");
			}
			return species.get(0);
		}
	}
	
	@Override
	public IndicatorSpecies getIndicatorSpeciesByScientificNameAndRank(String scientificName, TaxonRank rank) {
	    return this.getIndicatorSpeciesByScientificNameAndRank(null, scientificName, rank);
	}
	
	@Override
	public IndicatorSpecies getIndicatorSpeciesByScientificNameAndRank(Session sesh,
	            String scientificName, TaxonRank rank) { 
	    Query q;
	    String query = "select t from IndicatorSpecies t where t.scientificName = :name and t.taxonRank = :rank";
            if(sesh == null) {
                q = getSession().createQuery(query);
            } else {
                q = sesh.createQuery(query);
            }
            q.setParameter("name", scientificName);
            q.setParameter("rank", rank);
            
            List<IndicatorSpecies> list = q.list();
            if (list.size() == 0) {
                    log.warn(String.format("No taxon found for scientific name %s with rank %s: ", scientificName, rank));
                    return null;
            } else if (list.size() > 1) {
                    log.warn(String.format("Multiple taxa found for scientific name %s with rank %s. Returning the first ", scientificName, rank));
            }
            return list.get(0);
	}

	@Override
	public IndicatorSpecies getIndicatorSpeciesByScientificName(Session sesh,
			String scientificName) {
	    if(sesh == null) {
	        sesh = getSession();
	    }
		List<IndicatorSpecies> species = find(sesh,
				"select i from IndicatorSpecies i where i.scientificName = ?",
				scientificName);
		if (species.isEmpty()) {
			return null;
		} else {
			if (species.size() > 1) {
				log
						.warn("Multiple IndicatorSpecies with the same scientific name found. Returning the first");
			}
			return species.get(0);
		}
	}
	
	@Override
        public List<IndicatorSpecies> getIndicatorSpeciesListByScientificName(String scientificName) {
            return find("select i from IndicatorSpecies i where i.scientificName = ?", scientificName);
        }

	@Override
	public IndicatorSpecies getIndicatorSpeciesByScientificName(
			String scientificName) {
	    return getIndicatorSpeciesByScientificName(null, scientificName);
	}

	@Override
	public List<IndicatorSpecies> getIndicatorSpecies(Integer[] taxonGroupIds) {
		if (taxonGroupIds.length == 0) {
			return Collections.emptyList();
		}

		StringBuilder builder = new StringBuilder();
		builder.append("select i");
		builder.append(" from IndicatorSpecies i where i.taxonGroup in (select g from TaxonGroup g where g.id in (:ids))");

		Query q = getSession().createQuery(builder.toString());
		q.setParameterList("ids", taxonGroupIds, Hibernate.INTEGER);
		return q.list();
	}

	@Override
	public Integer countIndicatorSpecies(Integer[] taxonGroupIds) {
		if (taxonGroupIds.length == 0) {
			return 0;
		}

		StringBuilder builder = new StringBuilder();
		builder.append("select count(*)");
		builder.append(" from IndicatorSpecies i where i.taxonGroup in (select g from TaxonGroup g where g.id in (:ids))");
		
		Query q = getSession().createQuery(builder.toString());
		q.setParameterList("ids", taxonGroupIds, Hibernate.INTEGER);
		return Integer.parseInt(q.list().get(0).toString(), 10);
	}
	
	@Override
	public IndicatorSpecies refresh(IndicatorSpecies s) {
		return s;
	}

	@Override
	public Integer countAllSpecies() {
		Query q = getSession().createQuery(
				"select count(*) from IndicatorSpecies");
		Integer count = Integer.parseInt(q.list().get(0).toString(), 10);
		return count;
	}

	@Override
	public int countSpeciesForSurvey(Survey survey) {
		Query q = getSession()
				.createQuery(
						"select count(t) from Survey s left join s.species t where s = :survey");
		q.setParameter("survey", survey);
		Integer count = Integer.parseInt(q.list().get(0).toString(), 10);
		return count;
	}
	
    public List<Pair<IndicatorSpecies, Integer>> getTopSpecies(int userPk, int limit) {
    	List<Pair<IndicatorSpecies, Integer>> list = new ArrayList<Pair<IndicatorSpecies, Integer>>();

    	StringBuilder queryString = new StringBuilder(
    			"select i.id, count (r) from Record as r join r.species as i ");
    	if (userPk != 0) {
    		queryString.append("where r.user.id = " + userPk);
    	}
    	queryString.append(" group by i.id order by count(r) desc");
    	Query q = getSession().createQuery(queryString.toString());
    	q.setMaxResults(limit);
		List<Object[]> results = q.list();
		for (int i = 0; i < results.size(); i++) {
			Integer id = Integer.parseInt(results.get(i)[0].toString());
			Integer count = Integer.parseInt(results.get(i)[1].toString());
			list.add(new Pair<IndicatorSpecies, Integer>(getIndicatorSpecies(id), count));
		}
    	return list;
    }
    
    @Override
    public PagedQueryResult<IndicatorSpecies> getIndicatorSpecies(TaxonGroup taxonGroup, PaginationFilter filter) {

        HqlQuery q = new HqlQuery("from IndicatorSpecies s");
        if (taxonGroup != null) {
            q.and(Predicate.eq("s.taxonGroup.id", taxonGroup.getId()));
        }
        
        return new QueryPaginator<IndicatorSpecies>().page(this.getSession(), q.getQueryString(), q.getParametersValue(), filter);
    }
    
    @Override
    public List<IndicatorSpecies> getChildTaxa(IndicatorSpecies taxon) {
        return find("from IndicatorSpecies s where s.parent = ?", taxon);
    }

    @Override
    public void delete(IndicatorSpeciesAttribute attr) {
        deleteByQuery(attr);
    }
    
    @Override
    public void delete(IndicatorSpecies taxon) throws StaleStateException {
        
        DeleteCascadeHandler taxonCascadeHandler = 
            delService.getDeleteCascadeHandlerFor(IndicatorSpecies.class);
        for(IndicatorSpecies child : this.getChildTaxa(taxon)) {
            taxonCascadeHandler.deleteCascade(child);
        }
        
        delService.deleteRecords(taxon);
        delService.unlinkFromSurvey(taxon);
        
        Set<IndicatorSpeciesAttribute> taxonAttributes = new HashSet(taxon.getAttributes());
        taxon.getAttributes().clear();
        
        Set<SpeciesProfile> taxonProfiles = new HashSet(taxon.getInfoItems());
        taxon.getInfoItems().clear();
        
        taxon = save(taxon);
        
        DeleteCascadeHandler taxonAttributeCascadeHandler = 
            delService.getDeleteCascadeHandlerFor(IndicatorSpeciesAttribute.class);
        for(IndicatorSpeciesAttribute attr : taxonAttributes) {
            attr = save(attr);
            taxonAttributeCascadeHandler.deleteCascade(attr);
        }
        
        DeleteCascadeHandler taxonProfileCascadeHandler = 
            delService.getDeleteCascadeHandlerFor(SpeciesProfile.class);
        for(SpeciesProfile profile : taxonProfiles) {
            profile = save(profile);
            taxonProfileCascadeHandler.deleteCascade(profile);
        }
        
        deleteByQuery(taxon);
    }
    
    @Override
    public void delete(TaxonGroup taxonGroup) {
        
        DeleteCascadeHandler taxonCascadeHandler = 
            delService.getDeleteCascadeHandlerFor(IndicatorSpecies.class);
        for(IndicatorSpecies taxon : this.getIndicatorSpecies(taxonGroup)) {
            try {
                taxonCascadeHandler.deleteCascade(taxon);
            } catch(StaleStateException sse) {
                // State State Exception can becaused by deleting a taxon
                // that has already been deleted. This can be caused by
                // deleting a parent taxon where the parent and child taxon
                // are in the same group. Deletion of the parent taxon
                // automatically deletes the child taxon. The child is still
                // in the group list and gets deleted twice.
                log.error("The exception that you see occurs because we are deleting a taxon twice. This is expected behaviour.");
            }
        }
        
        List<Attribute> attributeList = new ArrayList(taxonGroup.getAttributes());
        taxonGroup.getAttributes().clear();
        taxonGroup = save(taxonGroup);
        
        DeleteCascadeHandler attributeCascadeHandler = 
            delService.getDeleteCascadeHandlerFor(Attribute.class); 
        for(Attribute attr : attributeList) {
            attr = save(attr);
            attributeCascadeHandler.deleteCascade(attr);
        }
        
        deleteByQuery(taxonGroup);
    }
}