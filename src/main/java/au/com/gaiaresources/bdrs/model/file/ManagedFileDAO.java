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

    
    /**
     * Allows the ManagedFiles in the system to be searched in a number of different ways.  If one of the search parameters
     * are not supplied it will not be used to restrict the search results.  Note that all text matches are case insensitive.
     * <em>For the purposes of controlling the order of the returned results, the aliases used by the search query 
     * table are: MangedFile: file, Created by User: createdBy, Updated By User: updatedBy</em>
     * 
     * @param filter controls the subset of results that are returned.
     * @param fileSearchText Restricts the results to ManagedFiles with filename or description properties containing this String.
     * @param contentTypeFilter Restricts the results to ManagedFiles with a context type <em>starting with</em> this String.  It
     * was designed to do image search for example (eg contextType starts with "image" will return image/jpg, image/png etc.)
     * @param userSearchText Restricts the results to ManagedFiles created by or last updated by a user with a firstname or lastname containing this String.
     * @return a PagedQueryResult of type <Object[]> containing the matched results.  The actual types in the returned array 
     * are: (index 0) ManagedFile, (index 1) User (created by), (index 2) User (last modified by).
     */
    PagedQueryResult<Object[]> search(PaginationFilter filter, String fileSearchText, String contentTypeFilter, String userSearchText);
        
}
