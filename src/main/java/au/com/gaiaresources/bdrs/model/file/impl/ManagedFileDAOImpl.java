package au.com.gaiaresources.bdrs.model.file.impl;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.db.impl.HqlQuery;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.db.impl.QueryPaginator;
import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
import au.com.gaiaresources.bdrs.service.db.DeleteCascadeHandler;
import au.com.gaiaresources.bdrs.service.db.DeletionService;

@Repository
public class ManagedFileDAOImpl extends AbstractDAOImpl implements ManagedFileDAO {
    
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());
    
    @Autowired
    private DeletionService delService;
    
    @PostConstruct
    public void init() throws Exception {
        delService.registerDeleteCascadeHandler(ManagedFile.class, new DeleteCascadeHandler() {
            @Override
            public void deleteCascade(PersistentImpl instance) {
                delete((ManagedFile)instance);
            }
        });
    }
    
    @Override
    public ManagedFile getManagedFile(Integer id) {
        return getByID(ManagedFile.class, id);
    }
    
    @Override 
    public ManagedFile getManagedFile(String uuid) {
        return getManagedFile(getSession(), uuid);
    }
    
    @Override
    public ManagedFile getManagedFile(Session sesh, String uuid) {
        List<ManagedFile> files = find(sesh, "from ManagedFile f where f.uuid = ?", uuid);
        return files.isEmpty() ? null : files.get(0);
    }
    
    @Override
    public List<ManagedFile> getManagedFiles() {
        List<ManagedFile> files = find("from ManagedFile f order by f.createdAt");
        return files;
    }
    
    @Override
    public PagedQueryResult<ManagedFile> getManagedFiles(PaginationFilter filter) {
        HqlQuery q = new HqlQuery("from ManagedFile f");
        return new QueryPaginator<ManagedFile>().page(this.getSession(), q.getQueryString(), q.getParametersValue(), filter);
    }

    @Override
    public ManagedFile save(ManagedFile mf) {
        return super.save(mf);
    }
    
    @Override
    public void delete(ManagedFile mf) {
        super.deleteByQuery(mf);
    }
    
    @Override 
    public ManagedFile saveOrUpdate(ManagedFile mf) {
        return super.saveOrUpdate(super.getSession(), mf);
    }
}
