package au.com.gaiaresources.bdrs.model.record.impl;

import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;

import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.ScrollableRecords;

/**
 * Base implmentation of the {@link ScrollableRecords}. This implementation
 * converts a {@link Query} into a {@link ScrollableResults} and provides the
 * necessary casting to fulfill the {@link ScrollableRecords} implementation. 
 */
public class ScrollableRecordsImpl implements ScrollableRecords {
    private Logger log = Logger.getLogger(getClass());
    private ScrollableResults results;
    
    private boolean hasMoreElements = false;
    private int entriesPerPage = -1;
    private int currentPageEntryIndex = 0;
    private Record record = null;
    
    /**
     * Creates a new instance.
     * @param query the query for a set of Records.
     */
    public ScrollableRecordsImpl(Query query) {
        results = query.setCacheMode(CacheMode.IGNORE).setFetchSize(ScrollableRecords.RECORD_BATCH_SIZE).scroll(ScrollMode.FORWARD_ONLY);
        nextRecord();
    }
    
    /**
     * Creates a new instance.   
     * @param query the query for a set of Records.
     * @param pageNumber the 1 indexed paging of results
     * @param entriesPerPage the number of records per page of data.  
     */
    public ScrollableRecordsImpl(Query query, int pageNumber, int entriesPerPage) {
        results = query.setCacheMode(CacheMode.IGNORE).setFetchSize(ScrollableRecords.RECORD_BATCH_SIZE).scroll();
        this.entriesPerPage = entriesPerPage;
        
        // To set the row number, the scoll mode cannot be forward only.
        recordAt((pageNumber-1) * entriesPerPage);
    }

    @Override
    public boolean hasMoreElements() {
        boolean nextEntryAllowed = (entriesPerPage < 0) || (entriesPerPage > -1 && currentPageEntryIndex < entriesPerPage);
        if(record == null) {
            nextRecord();
        }
        return nextEntryAllowed && hasMoreElements;
    }

    @Override
    public Record nextElement() {
        if(record == null) {
            throw new NoSuchElementException();
        } else {
            Record r = record;
            record = null;
            return r;
        }
    }
    
    private void nextRecord() {
        hasMoreElements = results.next();
        record = hasMoreElements ? (Record)results.get(0) : null;
        currentPageEntryIndex++;
    }
    
    private void recordAt(int rowNum) {
        hasMoreElements = results.setRowNumber(rowNum);
        record = hasMoreElements ? (Record)results.get(0) : null;
        currentPageEntryIndex++;
    }
}
