package au.com.gaiaresources.bdrs.service.content;

import org.junit.Assert;
import org.junit.Test;

import au.com.gaiaresources.bdrs.test.AbstractSpringContextTest;

public class ContentServiceTest {
    
    @Test
    public void testRequestURLRegex() {
        Assert.assertEquals("http://localhost:8080/BDRS", ContentService.getRequestURL("http://localhost:8080/BDRS/WEB-INF/jsp/template/theme/foundation"));
    }
    
    @Test
    public void testContextServiceRegex() {
        Assert.assertEquals("/BDRS", ContentService.getContextPath("http://localhost:8080/BDRS/WEB-INF/jsp/template/theme/foundation"));
    }
}
