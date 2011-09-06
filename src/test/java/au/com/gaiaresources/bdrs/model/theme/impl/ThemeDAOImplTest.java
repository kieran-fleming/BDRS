package au.com.gaiaresources.bdrs.model.theme.impl;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.model.theme.Theme;
import au.com.gaiaresources.bdrs.model.theme.ThemeDAO;
import au.com.gaiaresources.bdrs.model.theme.ThemePage;
import au.com.gaiaresources.bdrs.test.AbstractTransactionalTest;

public class ThemeDAOImplTest extends AbstractTransactionalTest {
    @Autowired
    ThemeDAO themeDAO;
    
    Theme theme;
    
    @Before
    public void setup() {
        theme = new Theme();
        theme.setName("my theme");
        theme.setActive(false);
        theme.setThemeFileUUID("dummy uuid");
        theme = themeDAO.save(theme);
    }
    
    @Test
    public void testSaveGetDelete() {
        ThemePage page = new ThemePage();
        page.setKey("key");
        page.setTitle("page title");
        page.setDescription("page description");
        page.setTheme(theme);
        page = themeDAO.save(page);
        
        List<ThemePage> pageList = themeDAO.getThemePages(theme.getId().intValue());
        Assert.assertEquals("expect 1 item", 1, pageList.size());
        Assert.assertEquals("items the same", page, pageList.get(0));
        
        themeDAO.delete(page);
        
        List<ThemePage> pageListAfterDelete = themeDAO.getThemePages(theme.getId().intValue());
        Assert.assertEquals("expect 0 items after delete", 0, pageListAfterDelete.size());
    }
    
    @Test
    public void testGetPageByKey() {
        ThemePage page = new ThemePage();
        page.setKey("key");
        page.setTitle("page title");
        page.setDescription("page description");
        page.setTheme(theme);
        page = themeDAO.save(page);
        
        ThemePage pBadName = themeDAO.getThemePage(theme.getId().intValue(), "blah");
        Assert.assertNull("expect null, bad page key used", pBadName);
        
        ThemePage pNullName = themeDAO.getThemePage(theme.getId().intValue(), null);
        Assert.assertNull("expect null, nul key used", pNullName);
        
        ThemePage pGoodName = themeDAO.getThemePage(theme.getId().intValue(), page.getKey());
        Assert.assertNotNull("expect page returned", pGoodName);
        Assert.assertEquals("expect equal id", page.getId(), pGoodName.getId());
    }

}
