package au.com.gaiaresources.bdrs.db.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

// I like this implementation. CP from http://ahlawat.net/wordpress/?p=227

// the text representations 1 and 2 are the values passed back by displaytag
// to indicate ascending and descending sort
public class PaginationFilter {
    public enum SortOrder {
        ASCENDING("1"), DESCENDING("2");

        private String text;

        SortOrder(String text) {
            this.text = text;
        }

        public static SortOrder fromString(String text) throws ParseException,
                NullPointerException {
            if (text != null) {
                for (SortOrder e : SortOrder.values()) {
                    if (text.equalsIgnoreCase(e.text)) {
                        return e;
                    }
                }
                // some more definitions for asc / desc sort orders...
                if (text.equalsIgnoreCase("desc")) {
                    return SortOrder.DESCENDING;
                }
                if (text.equalsIgnoreCase("asc")) {
                    return SortOrder.ASCENDING;
                }
                throw new ParseException(
                        "Cannot create enum PaginationFilter.SortOrder from text: "
                                + text, 0);
            }
            throw new NullPointerException(
                    "Cannot create enum PaginationFilter.SortOrder from null string");
        }
    }

    public class SortingCriteria {
        private String column;
        private SortOrder order;

        public SortingCriteria(String column, SortOrder order) {
            this.column = column;
            this.order = order;
        }

        public String getColumn() {
            return column;
        }

        public SortOrder getOrder() {
            return order;
        }
    }

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