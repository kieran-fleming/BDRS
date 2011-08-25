package au.com.gaiaresources.bdrs.spatial;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.geotools.data.Query;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.opengis.feature.simple.SimpleFeature;

/**
 * Intended to iterate over the iterators of several ShapefileDataStores. Makes client
 * code that deals with multiple shape files simpler as long as the client does not 
 * need to know which shapefile SimpleFeature objects come from.
 * 
 * @author aaron
 *
 */
public class MultiShapefileDataStoreFeatureIterator implements Iterator<SimpleFeature> {

    private List<ShapefileDataStore> stores;
    private Iterator<SimpleFeature> currentInternalIterator = null;
    
    private Logger log = Logger.getLogger(getClass());
    
    public MultiShapefileDataStoreFeatureIterator(final List<ShapefileDataStore> stores) {
        if (stores == null) {
            throw new IllegalArgumentException("List<ShapefileDataStore>, stores cannot be null");
        }
        
     // make a copy of the list since we are going to be manipulating it...
        this.stores = new ArrayList<ShapefileDataStore>(stores.size());
        this.stores.addAll(stores);
    }

    @Override
    public boolean hasNext() {

        return currentIteratorStillGoing() || queuedItemsRemaining();
    }

    @Override
    public SimpleFeature next() {
        
        if (!hasNext()) {
            return null;
        }
        
        if (currentIteratorStillGoing()) {
            return currentInternalIterator.next();
        } 
        
        try {
            // switch to the new internal iterator...
            currentInternalIterator = stores.remove(0).getFeatureSource().getFeatures().iterator();
            return currentInternalIterator.next();
        } catch (Exception e) {
            // hasNext() has been called earlier. we are guaranteed to have another
            // iterator in the queue. The try/catch is to appease eclipse exception
            // thrown declaration checking
            log.error("Should never reach here!");
            return null;
        }
    }
    
    /**
     * returns true if any of the queued datastores have items in them.
     * @return
     */
    private boolean queuedItemsRemaining() {
        for (ShapefileDataStore s : this.stores) {
            try {
                if (s.getCount(Query.ALL) > 0) {
                    return true;
                }
            } catch (IOException ioe) {
                log.error("Error executing shapefile datastore query", ioe);
            }
        }
        return false;
    }
    
    private boolean currentIteratorStillGoing() {
        return currentInternalIterator != null && currentInternalIterator.hasNext(); 
    }

    /**
     * Not supported
     */
    @Override
    public void remove() {
        // do nothing
    }
}
