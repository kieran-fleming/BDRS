package au.com.gaiaresources.bdrs.model.location;

import java.util.Comparator;

/**
 * Compares two <code>Locations</code> by their names. If both locations have
 * the same name, then the locations will be ordered using their primary keys.
 */
public class LocationNameComparator implements Comparator<Location> {

    @Override
    public int compare(Location loc, Location other) {
        
        int compare = loc.getName().compareTo(other.getName());
        if(compare == 0 && !loc.equals(other)) {
            compare = loc.getId().compareTo(other.getId());
        }
        return compare;
    }
}
