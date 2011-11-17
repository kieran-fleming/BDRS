package au.com.gaiaresources.bdrs.service.facet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import au.com.gaiaresources.bdrs.db.impl.Predicate;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Represents a single month of records.
 */
public class MonthFacetOption extends FacetOption {

    /**
     * An unmodifiable collection of Months where the index of the month corresponds
     * with the name of the month. That is, 1 - January, 2 - Febuary and so on.
     */
    public static final List<String> MONTHS;
    static {
        List<String> temp = new ArrayList<String>();
        for(String m : new String[]{ "January", "February", "March",
            "April", "May", "June", "July", "August", "September", "October",
            "November", "December" }) {
            temp.add(m);
        }
        MONTHS = Collections.unmodifiableList(temp);
    }
    
    private Long month; // starts at 0

    /**
     * Creates a new instance of this class.
     * 
     * @param month the human readable name of this option.
     * @param count the number of records that match this option.
     * @param selectedOpts true if this option is applied, false otherwise.
     */
    public MonthFacetOption(Long month, Long count, String[] selectedOpts) {
        super(MONTHS.get(month.intValue() - 1), month.toString(), count, Arrays.binarySearch(selectedOpts, month.toString()) > -1);
        this.month = month;
    }

    /**
     * Returns the predicate represented by this option that may be applied to a
     * query.
     */
    public Predicate getPredicate() {
        return new Predicate("(month(record.when) = ?)", month.intValue());
    }

}
