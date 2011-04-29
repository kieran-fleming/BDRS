package au.com.gaiaresources.bdrs.model.metadata.impl;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.service.db.DeleteCascadeHandler;
import au.com.gaiaresources.bdrs.service.db.DeletionService;

@Repository
public class MetadataDAOImpl extends AbstractDAOImpl implements MetadataDAO {
     Logger log = Logger.getLogger(MetadataDAOImpl.class);
     
     @Autowired
     private DeletionService delService;
     
     @PostConstruct
     public void init() throws Exception {
         delService.registerDeleteCascadeHandler(Metadata.class, new DeleteCascadeHandler() {
             @Override
             public void deleteCascade(PersistentImpl instance) {
                 delete((Metadata)instance);
             }
         });
     }

    @Override
    public Metadata get(Integer id) {
        return getByID(Metadata.class, id);
//        List<Metadata> mds = super.find("from Metadata where id = ?", id);  
//        return mds.isEmpty() ? null : mds.get(0);
    }

    @Override
    public Metadata save(Metadata md) {
        return super.save(md);
    }
    
    @Override
    public Metadata save(Session sesh, Metadata md) {
        return super.save(sesh, md);
    }
    
    @Override
    public void delete(Metadata md) {
        if(md != null) {
            super.deleteByQuery(md);
        }
    }
    
    @Override
    public Metadata update(Metadata md) {
        return super.update(md);
    }

    @Override
    public List<Metadata> getMetadata() {
        return super.find("from Metadata");
    }
}