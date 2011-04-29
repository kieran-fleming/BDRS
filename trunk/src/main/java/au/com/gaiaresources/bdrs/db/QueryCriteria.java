package au.com.gaiaresources.bdrs.db;

import java.util.List;
import java.util.Map;

public interface QueryCriteria<T extends Persistent> {
    /**
     * Add a restriction to the query.
     * @param propertyName The name of the property in the persistent class to restrict on.
     * @param operation <code>QueryOperation</code>.
     * @param parameter The paramameters for the operation.
     * @return <code>this</code> for method chaining.
     */
    QueryCriteria<T> add(String propertyName, QueryOperation operation, Object ... parameter);
    
    /**
     * Add a restriction to an n-away path.
     * @param path The path to the object that contains the property.
     * @param propertyName The name of the property to restrict.
     * @param operation <code>QueryOperation</code>.
     * @param parameter The paramameters for the operation.
     * @return <code>this</code> for method chaining.
     */
    QueryCriteria<T> add(String path, String propertyName, QueryOperation operation, Object ... parameter);
    
    /**
     * Add an order by clause.
     * @param property The property to order by.
     * @param ascending Should the order be ascending.
     * @return <code>this</code> for method chaining.
     */
    QueryCriteria<T> addOrderBy(String property, boolean ascending);
    
    QueryCriteria<T> groupBy(String property);
    
    <U> Map<U, Integer> groupByAndCount(String property);
    
    List<T> run();
    
    Integer count();
    
    Integer countDistinct(String propertyName);
    
    T runAndGetFirst();
}
