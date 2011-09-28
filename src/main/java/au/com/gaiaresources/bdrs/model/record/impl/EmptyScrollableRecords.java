package au.com.gaiaresources.bdrs.model.record.impl;

import java.util.NoSuchElementException;

import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.ScrollableRecords;

/**
 * Provides an empty <code>ScrollableRecords</code> implementation without
 * a database query. 
 */
public class EmptyScrollableRecords implements ScrollableRecords {

    @Override
    public boolean hasMoreElements() {
        return false;
    }

    @Override
    public Record nextElement() {
        throw new NoSuchElementException();
    }
}
