package au.com.gaiaresources.bdrs.model.record.impl;

import java.util.List;

import org.apache.log4j.Logger;

import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.ScrollableRecords;

@Deprecated
public class ScrollableRecordsList implements ScrollableRecords {
    private Logger log = Logger.getLogger(getClass());
    private List<Record> recordList;
    private int currentIndex = 0;

    public ScrollableRecordsList(List<Record> recordList) {
        log.warn("The use of the scrollable records list is not recommended exists primarily for legacy reasons.");
        this.recordList = recordList;
    }

    @Override
    public boolean hasMoreElements() {
        return currentIndex < recordList.size();
    }

    @Override
    public Record nextElement() {
        Record r = recordList.get(currentIndex);
        currentIndex++;
        return r;
    }
}
