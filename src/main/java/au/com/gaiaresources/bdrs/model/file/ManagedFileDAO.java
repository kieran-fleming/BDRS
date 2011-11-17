package au.com.gaiaresources.bdrs.model.file;

import java.util.List;

import org.hibernate.Session;

import au.com.gaiaresources.bdrs.db.TransactionDAO;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.model.portal.Portal;

public interface ManagedFileDAO  extends TransactionDAO {

    ManagedFile getManagedFile(String uuid);
    
    ManagedFile getManagedFile(Session sesh, String uuid);

    ManagedFile getManagedFile(Integer id);

    List<ManagedFile> getManagedFiles();

    PagedQueryResult<ManagedFile> getManagedFiles(PaginationFilter filter);

    ManagedFile save(ManagedFile mf);
    
    ManagedFile saveOrUpdate(ManagedFile mf);

    void delete(ManagedFile mf);

    ManagedFile getManagedFile(String themeFileUUID, Portal portal);

    ManagedFile getManagedFileByDescription(String fileDescription);

    ManagedFile getManagedFileByName(String fileName);
}
