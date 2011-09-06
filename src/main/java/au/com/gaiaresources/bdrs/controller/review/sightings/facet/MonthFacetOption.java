package au.com.gaiaresources.bdrs.controller.review.sightings.facet;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import au.com.gaiaresources.bdrs.db.impl.Predicate;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Represents a single month of records.
 */
public class MonthFacetOption extends FacetOption {
    
	private static final String[] months = { "January", "February", "March", "April", "May", "June",
	                                         "July", "August", "September", "October", "November", "December" };
    
	private Long month; // starts at 0

    public MonthFacetOption(Long month, Long count, String[] selectedOpts) {
        super(months[month.intValue()-1], month.toString(), count,
        		Arrays.binarySearch(selectedOpts, month.toString()) > -1);
        this.month = month;    
    }

    public Predicate getPredicate() {
        return new Predicate("(month(record.when) = ?)", month.intValue());
    }
    
}
