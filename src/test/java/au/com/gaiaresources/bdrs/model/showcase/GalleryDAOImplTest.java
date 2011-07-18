package au.com.gaiaresources.bdrs.model.showcase;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;

public class GalleryDAOImplTest extends AbstractControllerTest {
    
    @Autowired
    GalleryDAO galleryDAO;
    
    Gallery g1;
    Gallery g2;
    
    @Before
    public void setup() {
        g1 = new Gallery();
        g1.setName("one");
        g1.setDescription("gallery 1 description");
        
        g2 = new Gallery();
        g2.setName("two");
        g2.setDescription("description of gallery 2");
        
        galleryDAO.save(g1);
        galleryDAO.save(g2);
    }
    
    @Test
    public void testSearch1() {
        PagedQueryResult<Gallery> result = galleryDAO.search(null, "one", null);
        Assert.assertEquals(1, result.getCount());
        Assert.assertEquals(g1.getId(), result.getList().get(0).getId());
    }
    
    @Test
    public void testSearch2() {
        PagedQueryResult<Gallery> result = galleryDAO.search(null, null, null);
        Assert.assertEquals(2, result.getCount());
        Assert.assertTrue(result.getList().contains(g1));
        Assert.assertTrue(result.getList().contains(g2));
    }
}
