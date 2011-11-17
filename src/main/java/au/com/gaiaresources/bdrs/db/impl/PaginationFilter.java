package au.com.gaiaresources.bdrs.db.impl;

import java.util.ArrayList;
import java.util.List;

// I like this implementation. CP from http://ahlawat.net/wordpress/?p=227

// the text representations 1 and 2 are the values passed back by displaytag
// to indicate ascending and descending sort
public class PaginationFilter {
    private int firstResult;
    private int maxResult;
    private List<SortingCriteria> sortingCriterias;

    /**
     * Argument holding class for pagination operations. Ultimately used by
     * QueryPaginator
     * 
     * @param firstResult
     *            The index of the first desired result. starts at 0
     * @param maxResult
     *            The number of items to return on the page
     */
    public PaginationFilter(int firstResult, int maxResult) {
        this.firstResult = firstResult;
        this.maxResult = maxResult;

        this.sortingCriterias = new ArrayList<SortingCriteria>();
    }

    public PaginationFilter addSortingCriteria(String field, SortOrder order) {
        this.sortingCriterias.add(new SortingCriteria(field, order));
        return this;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public int getMaxResult() {
        return maxResult;
    }

    public List<SortingCriteria> getSortingCriterias() {
        return sortingCriterias;
    }
}