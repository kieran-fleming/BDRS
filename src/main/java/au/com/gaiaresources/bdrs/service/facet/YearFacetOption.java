package au.com.gaiaresources.bdrs.service.facet;

import au.com.gaiaresources.bdrs.db.impl.Predicate;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Represents a single year of records.
 */
public class YearFacetOption extends FacetOption {
    
    private Long year; // starts at 0

    /**
     * Creates a new instance of this class.
     * 
     * @param year the human readable name of this option.
     * @param count the number of records that match this option.
     * @param selectedOpts true if this option is applied, false otherwise.
     */
    public YearFacetOption(Long year, Long count, String[] selectedOpts) {
        super(year.toString(), year.toString(), count, Arrays.binarySearch(selectedOpts, year.toString()) > -1);
        this.year = year;    
    }

    /**
     * Returns the predicate represented by this option that may be applied to a
     * query.
     */
    public Predicate getPredicate() {
        return new Predicate("(year(record.when) = ?)", year.intValue());
    }
    
}
