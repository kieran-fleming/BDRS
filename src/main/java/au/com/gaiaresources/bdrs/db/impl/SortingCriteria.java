package au.com.gaiaresources.bdrs.db.impl;

/**
 * Captures the parameters used for sorting results namely, the column to be 
 * sorted and if it is to be sorted in ascending or descending order.
 */
public class SortingCriteria {
    private String column;
    private SortOrder order;

    /**
     * Creates a new <code>SortingCriteria</code>.
     * @param column the HQL column name to be sorted.
     * @param order indicates if the sorting should be in ascending or descending order.
     */
    public SortingCriteria(String column, SortOrder order) {
        this.column = column;
        this.order = order;
    }

    /**
     * @return the HQL name of the column to be sorted.
     */
    public String getColumn() {
        return column;
    }

    /**
     * @return the order to sort the results, ascending or descending.
     */
    public SortOrder getOrder() {
        return order;
    }
}