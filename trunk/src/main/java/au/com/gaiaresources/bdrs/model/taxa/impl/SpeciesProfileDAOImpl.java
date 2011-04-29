package au.com.gaiaresources.bdrs.model.taxa.impl;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.classic.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfile;
import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfileDAO;
import au.com.gaiaresources.bdrs.service.db.DeleteCascadeHandler;
import au.com.gaiaresources.bdrs.service.db.DeletionService;

@Repository
public class SpeciesProfileDAOImpl extends AbstractDAOImpl implements
        SpeciesProfileDAO {
    
    private Logger log = Logger.getLogger(getClass());
    
    @Autowired
    private DeletionService delService;
    
    @PostConstruct
    public void init() throws Exception {
        delService.registerDeleteCascadeHandler(SpeciesProfile.class, new DeleteCascadeHandler() {
            @Override
            public void deleteCascade(PersistentImpl instance) {
                delete((SpeciesProfile)instance);
            }
        });
    }

    @Override
    public SpeciesProfile createSpeciesProfile(String header, String content,
            String type) {
        return createSpeciesProfile(header, null, content, type);
    }

    @Override
    public SpeciesProfile createSpeciesProfile(String header, String desc,
            String content, String type) {
        SpeciesProfile s = new SpeciesProfile();
        s.setHeader(header);
        s.setDescription(desc);
        s.setContent(content);
        s.setType(type);
        s = save(s);
        return s;
    }

    @Override
    public List<SpeciesProfile> getSpeciesProfileForSpecies(int id) {
        return find("select i.infoItems from IndicatorSpecies i where i.id = ?", id);
    }

    @Override
    public SpeciesProfile save(Session sesh, SpeciesProfile profile) {
        return super.save(sesh, profile);
    }
    
    @Override
    public SpeciesProfile save(SpeciesProfile profile) {
        return this.save(getSession(), profile);
    }
    
    @Override
    public void delete(SpeciesProfile delProf) {
        super.deleteByQuery(delProf);
    }
    
    @Override 
    public List<SpeciesProfile> getSpeciesProfileByType(String[] types) {
        
        String queryString = "from SpeciesProfile p where p.type in (:types)";
        Query q = getSession().createQuery(queryString);
        q.setParameterList("types", types);
        return q.list();
    }

    @Override
    public SpeciesProfile getSpeciesProfileBySourceDataId(Session sesh, 
            IndicatorSpecies species, String sourceDataIdKey, String sourceDataId) {
        
        // If the species is null, or unsaved
        if(species == null || (species != null && species.getId() == null)) {
            return null;
        }
        
        //String query = "select p from SpeciesProfile p join p.metadata md where md.key = :source_data_id_key and md.value = :source_data_id";
        String query = "select p from IndicatorSpecies s join s.infoItems as p join p.metadata md where s=:species and md.key = :source_data_id_key and md.value = :source_data_id";
        Query q;
        if(sesh == null) {
            q = getSession().createQuery(query);
        }
        else {
            q = sesh.createQuery(query);
        }
        q.setParameter("species", species);
        q.setParameter("source_data_id_key", sourceDataIdKey);
        q.setParameter("source_data_id", sourceDataId);
        List<SpeciesProfile> profileList = q.list();
        if(profileList.isEmpty()) {
            return null;
        } else {
            if(profileList.size() > 1){
                log.warn("More than one Species Profile returned for the provided Source Data key "+sourceDataIdKey+" with ID: "+sourceDataId+" Returning the first");
            }
            return profileList.get(0);
        }
    }
	
}
