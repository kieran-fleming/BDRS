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
public class YearFacetOption extends FacetOption {
    
	private Long year; // starts at 0

    public YearFacetOption(Long year, Long count, String[] selectedOpts) {
        super(year.toString(), year.toString(), count, Arrays.binarySearch(selectedOpts, year.toString()) > -1);
        this.year = year;    
    }

    public Predicate getPredicate() {
        return new Predicate("year(record.when) = ?)", year.intValue());
    }
    
}
