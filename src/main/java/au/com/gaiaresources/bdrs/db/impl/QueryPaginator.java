package au.com.gaiaresources.bdrs.db.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.springframework.util.StringUtils;

import au.com.gaiaresources.bdrs.db.impl.PaginationFilter.SortOrder;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter.SortingCriteria;

// I don't particularly like this since we need to instantiate
// QueryPaginator every time we want to use it with a different
// type
public class QueryPaginator<T extends PersistentImpl> {

    private Logger log = Logger.getLogger(getClass());
    
    private void applyArgToQuery(Query query, Object[] args) {
        for (int i = 0; i < args.length; i++) {
            query.setParameter(i, args[i]);
        }
    }

    @SuppressWarnings("unchecked")
    private List<T> find(Session sesh, String hql, Object[] args) {
        Query query = sesh.createQuery(hql);
        applyArgToQuery(query, args);
        return query.list();
    }
    
    @SuppressWarnings("unchecked")
    public PagedQueryResult<T> page(Session session, String hql, Object[] args,
            PaginationFilter filter, String sortTargetAlias) {

        // [1] total size
        String countHql = "select count(*) "
                + HqlUtil.removeSelect(HqlUtil.removeOrders(hql));

        List countlist = find(session, countHql, args);

        int totalCount = ((Long) countlist.get(0)).intValue();
        if (totalCount < 1) {
            PagedQueryResult<T> result = new PagedQueryResult<T>();
            result.setCount(0);
            return result;
        }

        PagedQueryResult<T> result = new PagedQueryResult<T>();
        result.setCount(totalCount);

        // to make life easy we are going to strip ordering now....so don't expect
        // any original ordering specified in the HQL to work!

        //String noOrderHql = HqlUtil.removeOrders(hql);
        StringBuilder paginationHql = new StringBuilder(
                HqlUtil.removeOrders(hql));

        if (filter != null) {
            // build up our ordering from the filter params...
            if (filter.getSortingCriterias().size() > 0) {
                paginationHql.append(" order by");
                for (SortingCriteria sc : filter.getSortingCriterias()) {
                    paginationHql.append(" ");
                    if (StringUtils.hasLength(sortTargetAlias) ) {
                        paginationHql.append(sortTargetAlias);
                        paginationHql.append(".");
                    }
                    paginationHql.append(sc.getColumn());
                    paginationHql.append(" ");
                    if (sc.getOrder() == SortOrder.ASCENDING) {
                        paginationHql.append("asc");
                    } else {
                        paginationHql.append("desc");
                    }
                }
            }
        }
        Query query = session.createQuery(paginationHql.toString());
        
        applyArgToQuery(query, args);
        if (filter != null) {
            query.setFirstResult(filter.getFirstResult());
            query.setMaxResults(filter.getMaxResult());
        }
        List<T> resultList = query.list();
        result.setList(resultList);
        return result;
    }

    /**
     * to make life easy we are going to strip ordering now....so don't expect
     * any original ordering specified in the HQL to work!
     * 
     * could change this to use HqlQuery object but I don't want to force
     * everyone to use it just yet...
     * 
     * @param session
     * @param hql
     * @param args
     * @param filter
     * @return
     */
    @SuppressWarnings("unchecked")
    public PagedQueryResult<T> page(Session session, String hql, Object[] args,
            PaginationFilter filter) {
        return page(session, hql, args, filter, null);
    }

    @SuppressWarnings("unchecked")
    public PagedQueryResult<T> page(Criteria crit, PaginationFilter filter) {
        PagedQueryResult<T> result = new PagedQueryResult<T>();

        // do the count first as it is easy to remove the projection
        crit.setProjection(Projections.rowCount());
        result.setCount((Integer) crit.uniqueResult());

        // remove the projection so that we may reuse the criteria
        crit.setProjection(null);
        crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

        if (filter != null) {
            crit.setMaxResults(filter.getMaxResult());
            crit.setFirstResult(filter.getFirstResult());
            for (SortingCriteria sc : filter.getSortingCriterias()) {
                if (sc.getOrder() == SortOrder.ASCENDING) {
                    crit.addOrder(Order.asc(sc.getColumn()));
                } else {
                    crit.addOrder(Order.desc(sc.getColumn()));
                }
            }
        }
        // assumes that you are going to be casting correctly.....
        result.setList(crit.list());
        return result;
    }
}