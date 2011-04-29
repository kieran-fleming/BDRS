package au.com.gaiaresources.bdrs.controller.insecure.taxa;

import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;

public class ComparePersistentImplByWeight implements java.util.Comparator<PersistentImpl>{
    
    @Override
    /**
     * @author kehan
     * @param TaxonGroup o1 the first taxon group to compare
     * @param TaxonGroup o2 the second taxon group to compare
     * @return int the difference between their respective weights
     */
    public int compare(PersistentImpl o1, PersistentImpl o2) {
        // Cast the objects as taxon groups
       return o1.getWeight() - o2.getWeight();
    }


}
