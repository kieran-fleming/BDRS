package au.com.gaiaresources.bdrs.model.file;

import java.util.List;

import au.com.gaiaresources.bdrs.db.TransactionDAO;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;

public interface ManagedFileDAO  extends TransactionDAO {

    ManagedFile getManagedFile(String uuid);

    ManagedFile getManagedFile(Integer id);

    List<ManagedFile> getManagedFiles();

    PagedQueryResult<ManagedFile> getManagedFiles(PaginationFilter filter);

    ManagedFile save(ManagedFile mf);

    void delete(ManagedFile mf);

}
