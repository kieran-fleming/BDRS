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
    
    private Date date;

    public MonthFacetOption(Date date, Long count, String[] selectedOpts) {
        super(new SimpleDateFormat("MMM yyyy").format(date), String.valueOf(date.getTime()), count,
              Arrays.binarySearch(selectedOpts, String.valueOf(date.getTime())) > -1);
        
        this.date = new Date(date.getTime());;      
    }

    public Predicate getPredicate() {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, cal.getActualMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getActualMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getActualMinimum(Calendar.SECOND));
        Date startDate = cal.getTime();
        
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, cal.getActualMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getActualMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getActualMaximum(Calendar.SECOND));
        Date endDate = cal.getTime();
        
        return new Predicate("(record.when >= ? and record.when <= ?)", startDate, endDate);
    }
}
