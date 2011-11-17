package au.com.gaiaresources.bdrs.db.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernatespatial.GeometryUserType;
import org.springframework.util.StringUtils;

import com.vividsolutions.jts.geom.Geometry;

// I don't particularly like this since we need to instantiate
// QueryPaginator every time we want to use it with a different
// type
public class QueryPaginator<T extends PersistentImpl> {

    private Logger log = Logger.getLogger(getClass());
    
    private void applyArgToQuery(Query query, Object[] args) {
        if (args == null) {
            return;
        }
        for (int i = 0; i < args.length; i++) {
            Object obj = args[i];
            if (obj instanceof Geometry) {
                query.setParameter(i, obj, GeometryUserType.TYPE);
            } else {
                query.setParameter(i, obj);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void applyArgToQuery(Query query, Map<String, Object> args) {
        if (args == null) {
            return;
        }
        for (Entry<String, Object> entry : args.entrySet()) {
            Object obj = entry.getValue();
            if (obj instanceof Geometry) {
                query.setParameter(entry.getKey(), obj, GeometryUserType.TYPE);
            } else if (obj instanceof Collection) {
                query.setParameterList(entry.getKey(), (Collection)obj);
            } else {
                query.setParameter(entry.getKey(), obj);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<T> find(Session sesh, String hql, Object[] args) {
        Query query = sesh.createQuery(hql);
        applyArgToQuery(query, args);
        return query.list();
    }
    
    @SuppressWarnings("unchecked")
    private List<T> find(Session sesh, String hql, Map<String, Object> args) {
        Query query = sesh.createQuery(hql);
        applyArgToQuery(query, args);
        return query.list();
    }
    
    public PagedQueryResult<T> page(Session session, String hql, Map<String, Object> args,
                                    PaginationFilter filter, String sortTargetAlias) {
        return page(session, hql, null, args, filter, sortTargetAlias);
    }
    
    public PagedQueryResult<T> page(Session session, String hql, Object[] args,
            PaginationFilter filter, String sortTargetAlias) {
        return page(session, hql, args, null, filter, sortTargetAlias);
    }
    
    @SuppressWarnings("unchecked")
    private PagedQueryResult<T> page(Session session, String hql, Object[] argArray, Map<String, Object> argMap,
                                    PaginationFilter filter, String sortTargetAlias) {
     // [1] total size
        String countHql = "select count(*) "
                + HqlUtil.removeSelect(HqlUtil.removeOrders(hql));

        List countlist = argArray != null ? find(session, countHql, argArray) : find(session, countHql, argMap); 

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
        } else {
            // else sort by the weight
            paginationHql.append(" order by ");
            if (StringUtils.hasLength(sortTargetAlias) ) {
                paginationHql.append(sortTargetAlias);
                paginationHql.append(".");
            }
            paginationHql.append("weight");
        }
        Query query = session.createQuery(paginationHql.toString());
        
        if (argArray != null) {
            applyArgToQuery(query, argArray);    
        } else {
            applyArgToQuery(query, argMap);
        }
        
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