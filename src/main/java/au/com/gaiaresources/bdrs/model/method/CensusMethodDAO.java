package au.com.gaiaresources.bdrs.model.method;

import au.com.gaiaresources.bdrs.db.TransactionDAO;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;


public interface CensusMethodDAO extends TransactionDAO {
    CensusMethod save(CensusMethod cm);
    CensusMethod update(CensusMethod cm);
    CensusMethod get(Integer pk);
    PagedQueryResult<CensusMethod> search(PaginationFilter filter, String name, Integer surveyId);    
}
