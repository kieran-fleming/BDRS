package au.com.gaiaresources.bdrs.model.record;

import java.util.Enumeration;

/**
 * Represents a lazy loaded enumeration of Records. The purpose of the
 * <code>ScrollableRecords</code> interface is the provide a single api
 * for retrieving records from a result set. 
 */
public interface ScrollableRecords extends Enumeration<Record> {
    public static final int RECORD_BATCH_SIZE = 500;
}
