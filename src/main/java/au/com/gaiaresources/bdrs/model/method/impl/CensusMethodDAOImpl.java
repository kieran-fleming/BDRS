package au.com.gaiaresources.bdrs.model.method.impl;

import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.db.impl.HqlQuery;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.db.impl.QueryPaginator;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;

@Repository
public class CensusMethodDAOImpl extends AbstractDAOImpl implements CensusMethodDAO {

    @Override
    public CensusMethod save(CensusMethod cm) {
        return super.save(cm);
    }
    
    @Override
    public CensusMethod get(Integer pk) {
        return super.getByID(CensusMethod.class, pk);
    }

    @Override
    public PagedQueryResult<CensusMethod> search(PaginationFilter filter, String name, Integer surveyId) {
        
        HqlQuery q;
        String sortTargetAlias = "cm";
        if (surveyId != null) {
            q = new HqlQuery("select distinct cm from Survey s ");
            q.join("s.censusMethods", "cm");
            q.and(Predicate.eq("s.id", surveyId));
        } else {
            q = new HqlQuery("from CensusMethod cm ");
        }
        if (name != null) {
            q.and(Predicate.ilike("cm.name", name + "%"));
        }
        return new QueryPaginator<CensusMethod>().page(this.getSession(), q.getQueryString(), q.getParametersValue(), filter, sortTargetAlias);
    }
    
    @Override
    public CensusMethod update(CensusMethod cm) {
        return super.update(cm);
    }
}
