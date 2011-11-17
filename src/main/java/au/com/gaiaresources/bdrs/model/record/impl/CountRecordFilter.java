package au.com.gaiaresources.bdrs.model.record.impl;

import java.util.List;
import java.util.Map;

import au.com.gaiaresources.bdrs.db.impl.SortingCriteria;

/**
 * Creates a count query filtered by the {@link #RecordFilter} members.
 * This class will only query the Record table for optimized counting queries
 * @author stephanie
 */
public class CountRecordFilter extends AbstractRecordFilter {

    public CountRecordFilter() {
    }

    /* (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.AbstractRecordFilter#createTableJoin()
     */
    @Override
    public String createTableJoin() {
        // there are no joins for count queries
        return "";
    }

    /* (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.AbstractRecordFilter#getJoinedClauses(java.util.Map)
     */
    @Override
    public String getJoinedClauses(Map<String, Object> paramMap) {
        // there are no joined clauses because count only operates on the record table
        return "";
    }

    /* (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.AbstractRecordFilter#getOrderingClause(java.util.List)
     */
    @Override
    public String getOrderingClause(List<SortingCriteria> sortCriteria) {
        // the default ordering is handled in the super class, no more ordering is required
        return "";
    }

    /* (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.AbstractRecordFilter#getRecordQuery(org.hibernate.Session, java.util.List)
     */
    @Override
    public String getQueryPredicate() {
        String query = "select count(distinct record)";
        return query;
    }

}
