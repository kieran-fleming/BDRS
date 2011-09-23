package au.com.gaiaresources.bdrs.model.method;


import org.hibernate.Session;

import au.com.gaiaresources.bdrs.db.TransactionDAO;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;


public interface CensusMethodDAO extends TransactionDAO {
    CensusMethod save(CensusMethod cm);
    CensusMethod update(CensusMethod cm);
    CensusMethod get(Integer pk);
    /**
     * Retrieves a <code>CensusMethod</code> with the specified primary
     * key using the provided session.
     * @param Session sesh the session to use when retrieving the <code>CensusMethod</code>.
     * @param pk the primary key of the <code>CensusMethod</code>.
     */
    CensusMethod get(Session sesh, Integer pk);
    PagedQueryResult<CensusMethod> search(PaginationFilter filter, String name, Integer surveyId);
        
}
