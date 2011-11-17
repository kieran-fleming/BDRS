package au.com.gaiaresources.bdrs.model.record.impl;

import java.util.List;

import au.com.gaiaresources.bdrs.db.impl.SortingCriteria;

/**
 * Creates a count query filtered by the {@link RecordFilter} members.
 * This class will join tables as in the {@link AdvancedRecordFilter} to make a 
 * more detailed query.
 * @author stephanie
 */
public class AdvancedCountRecordFilter extends AdvancedRecordFilter {

    public AdvancedCountRecordFilter() {
    }

    /* (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.AbstractRecordFilter#getRecordQuery(org.hibernate.Session, java.util.List)
     */
    @Override
    public String getQueryPredicate() {
        String query = "select count(distinct record)";
        return query;
    }
    
    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.AdvancedRecordFilter#getOrderingClause(java.util.List)
     */
    public String getOrderingClause(List<SortingCriteria> sortCriteria) {
        return "";
    }
}
