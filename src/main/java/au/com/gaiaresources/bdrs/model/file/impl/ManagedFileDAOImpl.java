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
import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.db.impl.QueryPaginator;
import au.com.gaiaresources.bdrs.db.impl.SortOrder;
import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.service.db.DeleteCascadeHandler;
import au.com.gaiaresources.bdrs.service.db.DeletionService;
import au.com.gaiaresources.bdrs.util.StringUtils;

@Repository
public class ManagedFileDAOImpl extends AbstractDAOImpl implements ManagedFileDAO {
    
	/** The number of rows returned from a search if no pagination filter is supplied */
	private static final int DEFAULT_SEARCH_PAGE_SIZE = 1000;
	
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
        // Trim the white space from the uuid to make the system more robust
        uuid = uuid.trim();
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
        HqlQuery q = new HqlQuery("from ManagedFile f order by f.createdAt");
        return new QueryPaginator<ManagedFile>().page(this.getSession(), q.getQueryString(), q.getParametersValue(), filter);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public PagedQueryResult<Object[]> search(
    		PaginationFilter filter, String fileSearchText, String contentTypeFilter, String userSearchText) {
    	
    	// This query is constructed using an implicit join on the User table as the updatedBy property in 
    	// PersistentImpl is of type Integer for performance reasons, but we need the User information.
    	HqlQuery q = new HqlQuery("from ManagedFile file, User createdBy, User updatedBy");
    	Predicate joinClause = new Predicate("file.createdBy = createdBy.id and file.updatedBy = updatedBy.id");
    	q.and(joinClause);
    	
    	if(StringUtils.notEmpty(fileSearchText)) {
            Predicate searchPredicate = Predicate.ilike("file.filename", String.format("%%%s%%", fileSearchText));
            searchPredicate.or(Predicate.ilike("file.description", String.format("%%%s%%", fileSearchText)));
            
            q.and(searchPredicate);
        }
    	
    	if (StringUtils.notEmpty(contentTypeFilter)) {
    		Predicate typePredicate = Predicate.ilike("file.contentType", String.format("%s%%", contentTypeFilter));
    		q.and(typePredicate);
    	}
    	
    	if (StringUtils.notEmpty(userSearchText)) {
    		Predicate userPredicate = Predicate.ilike("updatedBy.firstName", String.format("%s%%", userSearchText));
    		userPredicate.or(Predicate.ilike("updatedBy.lastName",  String.format("%s%%", userSearchText)));
    		userPredicate.or(Predicate.ilike("createdBy.firstName", String.format("%s%%", userSearchText)));
    		userPredicate.or(Predicate.ilike("createdBy.lastName",  String.format("%s%%", userSearchText)));
    		q.and(userPredicate);
    	}
    	
    	if (filter == null) {
    		filter = new PaginationFilter(0, DEFAULT_SEARCH_PAGE_SIZE);
    	}
    	if (filter.getSortingCriterias().isEmpty()) {
    		filter.addSortingCriteria("file.updatedAt", SortOrder.DESCENDING);
    	}
    	
        return new QueryPaginator<Object[]>().page(this.getSession(), q.getQueryString(), q.getParametersValue(), filter);

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

    @Override
    public ManagedFile getManagedFile(String themeFileUUID, Portal portal) {
        try {
            disablePortalFilter();
            return getManagedFile(themeFileUUID);
        } finally {
            enablePortalFilter();
        }
    }

    @Override
    public ManagedFile getManagedFileByDescription(String fileDescription) {
        List<ManagedFile> files = find("from ManagedFile f where f.description = ?", fileDescription);
        return files.isEmpty() ? null : files.get(0);
    }

    @Override
    public ManagedFile getManagedFileByName(String fileName) {
        List<ManagedFile> files = find("from ManagedFile f where f.filename = ?", fileName);
        return files.isEmpty() ? null : files.get(0);
    }
}
