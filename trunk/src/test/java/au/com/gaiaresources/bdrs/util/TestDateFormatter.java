package au.com.gaiaresources.bdrs.util;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

public class TestDateFormatter {
    @Test
    public void testTimeOnly() {
        // 11:20am in milliseconds
        long time = ((11 * 60) + 20) * 60 * 1000;
        Date d = new Date(time);
        
        Assert.assertEquals("Time should be the same", "11:20", DateFormatter.format(d, DateFormatter.TIME, true));
    }
}
