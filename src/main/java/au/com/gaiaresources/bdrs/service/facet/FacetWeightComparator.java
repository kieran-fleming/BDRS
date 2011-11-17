package au.com.gaiaresources.bdrs.service.facet;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares {@link Facet}s based on their weight attribute.
 */
public class FacetWeightComparator implements Comparator<Facet>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(Facet f1, Facet f2) {
        return Integer.valueOf(f1.getWeight()).compareTo(f2.getWeight());
    }
}
