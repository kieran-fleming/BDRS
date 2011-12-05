package au.com.gaiaresources.bdrs.model.record.impl;

import junit.framework.Assert;

import org.hibernate.Session;
import org.junit.Test;

import au.com.gaiaresources.bdrs.controller.AbstractGridControllerTest;
import au.com.gaiaresources.bdrs.model.record.ScrollableRecords;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

public class ScrollableRecordsImplTest extends AbstractGridControllerTest {

    @Test
    public void testScrollableRecords() {
        RecordFilter filter = new AdvancedRecordFilter();
        User accessor = userDAO.getUser("admin");
        filter.setAccessor(accessor);
        Integer recCount = recordDAO.countAllRecords();
        
        Session mySesh = RequestContextHolder.getContext().getHibernate();
        
        ScrollableRecords sr = recordDAO.getScrollableRecords(filter); 
        sr.rewind();  // test rewind functionality
        
        int scrollableCount = 0;
        while (sr.hasMoreElements()) {
            sr.nextElement();
            ++scrollableCount;
            mySesh.clear();
        }
        
        Assert.assertTrue("the number of recs in the database should be > 0", recCount > 0);
        
        Assert.assertEquals("scrollable count does not match record database count", recCount.intValue(), scrollableCount);
        
        // now test the rewind....
        sr.rewind();
        sr.rewind();  // double rewind ?
        int scrollableRewindCount = 0;
        while (sr.hasMoreElements()) {
            sr.nextElement();
            ++scrollableRewindCount;
            mySesh.clear();
        }
        
        Assert.assertEquals("scrollable count after rewind does not match record database count", recCount.intValue(), scrollableRewindCount);
    }
}
