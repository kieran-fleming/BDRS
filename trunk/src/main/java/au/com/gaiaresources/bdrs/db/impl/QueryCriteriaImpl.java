package au.com.gaiaresources.bdrs.db.impl;

import java.util.Arrays;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernatespatial.criterion.SpatialRestrictions;

import au.com.gaiaresources.bdrs.db.QueryCriteria;
import au.com.gaiaresources.bdrs.db.QueryOperation;

import com.vividsolutions.jts.geom.Geometry;

public class QueryCriteriaImpl<T extends PersistentImpl> implements QueryCriteria<T> {
    private Criteria criteria;
    private Map<String, Criteria> pathCriteria;

    /**
     * Constructor.
     * @param criteria {@link Criteria}
     */
    public QueryCriteriaImpl(Criteria criteria) {
        this.criteria = criteria;
        this.pathCriteria = new HashMap<String, Criteria>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryCriteriaImpl<T> add(String propertyName, QueryOperation operation, Object ... parameter) {
        add(criteria, propertyName, operation, parameter);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryCriteriaImpl<T> add(String path, String propertyName, QueryOperation operation, Object ... parameter) {
        Criteria c = buildNAwayCriteria(path);
        add(c, propertyName, operation, parameter);
        return this;
    }

    private Criteria buildNAwayCriteria(String path) {
        return buildNAwayCriteria(path.split("\\."));
    }

    private Criteria buildNAwayCriteria(String[] pathElements) {
        Criteria priorCriteria = criteria;
        String tempPath = null;
        for (int i = 0; i < pathElements.length; i++) {
            if (tempPath == null) {
                tempPath = pathElements[i];
            } else {
                tempPath = tempPath + "." + pathElements[i];
            }
            if (!pathCriteria.containsKey(tempPath)) {
                pathCriteria.put(tempPath, priorCriteria.createCriteria(pathElements[i]));
            }
            priorCriteria = pathCriteria.get(tempPath);
        }
        return priorCriteria;
    }

    private void add(Criteria addToCriteria, String propertyName, QueryOperation operation, Object ... parameter) {
        switch (operation) {
        case EQUAL:
            addToCriteria.add(Restrictions.eq(propertyName, parameter[0]));
            break;
        case NOT_EQUAL:
            addToCriteria.add(Restrictions.ne(propertyName, parameter[0]));
            break;
        case LIKE:
            addToCriteria.add(Restrictions.like(propertyName, parameter[0]));
            break;
        case ILIKE:
            addToCriteria.add(Restrictions.ilike(propertyName, parameter[0]));
            break;
        case IS_NULL:
            addToCriteria.add(Restrictions.isNull(propertyName));
            break;
        case IS_NOT_NULL:
            addToCriteria.add(Restrictions.isNotNull(propertyName));
            break;
        case GREATER_THAN:
            addToCriteria.add(Restrictions.gt(propertyName, parameter[0]));
            break;
        case GREATER_THAN_OR_EQUAL:
            addToCriteria.add(Restrictions.ge(propertyName, parameter[0]));
            break;
        case LESS_THAN:
            addToCriteria.add(Restrictions.lt(propertyName, parameter[0]));
            break;
        case LESS_THAN_OR_EQUAL:
            addToCriteria.add(Restrictions.le(propertyName, parameter[0]));
            break;
        case BETWEEN:
            addToCriteria.add(Restrictions.between(propertyName, parameter[0], parameter[1]));
            break;
        case IN:
            addToCriteria.add(Restrictions.in(propertyName, parameter));
            break;
        case NOT_IN:
            addToCriteria.add(Restrictions.not(Restrictions.in(propertyName, parameter)));
            break;
        case WITHIN:
            addToCriteria.add(SpatialRestrictions.within(propertyName, (Geometry) parameter[0]));
            break;
        case CONTAINS:
            addToCriteria.add(SpatialRestrictions.contains(propertyName, (Geometry) parameter[0]));
            break;
        case INTERSECTS:
            addToCriteria.add(SpatialRestrictions.intersects(propertyName, (Geometry) parameter[0]));
            break;
        default:
            throw new IllegalArgumentException("Invalid QueryOperation: " + operation);
        }
    }

    @Override
    public QueryCriteriaImpl<T> addOrderBy(String property, boolean ascending) {
        String[] elements = property.split("\\.");
        if (elements.length > 1) {
            Criteria c = buildNAwayCriteria(Arrays.copyOfRange(elements, 0, elements.length - 1));
            c.addOrder(ascending ? Order.asc(elements[elements.length - 1])
                                 : Order.desc(elements[elements.length - 1]));
        } else {
            criteria.addOrder(ascending ? Order.asc(property) : Order.desc(property));
        }
        return this;
    }

    public QueryCriteriaImpl<T> groupBy(String property) {
        criteria.setProjection(Projections.groupProperty(property));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <U> Map<U, Integer> groupByAndCount(String property) {
        criteria.setProjection(Projections.projectionList().add(Projections.groupProperty(property))
                                                           .add(Projections.rowCount()));
        List<?> results = run();
        Map<U, Integer> groupedResults = new HashMap<U, Integer>();
        for (Iterator<?> i = results.iterator(); i.hasNext();) {
            Object[] r = (Object[]) i.next();
            groupedResults.put((U) r[0], (Integer) r[1]);
        }
        return groupedResults;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> run() {
        return (List<T>) criteria.list();
    }

    @Override
    public Integer count() {
        criteria.setProjection(Projections.rowCount());
        return (Integer) criteria.list().get(0);
    }

    @Override
    public Integer countDistinct(String propertyName) {
        criteria.setProjection(Projections.countDistinct(propertyName));
        return (Integer) criteria.list().get(0);
    }

    @Override
    public T runAndGetFirst() {
        List<T> results = run();
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }
}
