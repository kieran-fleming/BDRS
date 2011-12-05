package au.com.gaiaresources.bdrs.service.facet;

import edu.emory.mathcs.backport.java.util.Arrays;
import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.model.location.Location;

/**
 * Creates a {@link FacetOption} for showing records for a {@link Location}. 
 * @author stephanie
 */
public class LocationFacetOption extends FacetOption {

    /**
     * The location for which to retrieve records
     */
    private Location location;
    
    /**
     * Creates a Location Option
     * @param location the location
     * @param count the number of records found for this location
     * @param selectedOpts options for selecting the option by id
     */
    public LocationFacetOption(Location location, Long count, String[] selectedOpts) {
        super(location.getName(), String.valueOf(location.getId()), count, 
              Arrays.binarySearch(selectedOpts, String.valueOf(location.getId())) > -1);
        this.location = location;
    }

    /* (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.service.facet.FacetOption#getPredicate()
     */
    @Override
    public Predicate getPredicate() {
        return new Predicate("(record.location.id = ?)", location.getId());
    }

}
