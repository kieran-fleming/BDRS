package au.com.gaiaresources.bdrs.model.theme.impl;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.FilterManager;
import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.theme.Theme;
import au.com.gaiaresources.bdrs.model.theme.ThemeDAO;
import au.com.gaiaresources.bdrs.model.theme.ThemeElement;
import au.com.gaiaresources.bdrs.model.theme.ThemePage;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

@Repository
public class ThemeDAOImpl extends AbstractDAOImpl implements ThemeDAO {
    private Logger log = Logger.getLogger(getClass());
    
    @Override
    public Theme getActiveTheme(Portal portal) {
        try {
            disablePortalFilter();
            
            Query q = getSession().createQuery("from Theme where portal = :portal and active = :active order by id asc");
            q.setParameter("portal", portal);
            q.setParameter("active", true);
            
            List<Theme> themeList = q.list();
            if(themeList.size() > 1){
                log.warn(String.format("More than one active theme returned for portal with ID: %d. Returning the first theme.",portal.getId()));
            } 
            Theme theme = themeList.isEmpty() ? null : themeList.get(0);
            return theme;
        } catch(Error e) {
            throw e;
        } finally {
            enablePortalFilter();
        }
    }

    @Override
    public Theme getTheme(int themeId) {
        return (Theme)getSession().get(Theme.class, themeId);
    }
    
    @Override
    public ThemeElement getThemeElement(int themeElementId) {
        return (ThemeElement)getSession().get(ThemeElement.class, themeElementId);
    }

    @Override
    public List<Theme> getThemes(Portal portal) {
        try {
            disablePortalFilter();
            return find("from Theme where portal = ?", portal);
        }
        catch(Error e) {
            throw e;
        } finally {
            enablePortalFilter();
        }
    }

    @Override
    public List<Theme> getThemes() {
        return find("from Theme");
    }

    @Override
    public Theme save(Theme theme) {
        return super.save(theme);
    }
    
    @Override
    public ThemeElement save(ThemeElement themeElement) {
        return super.save(themeElement);
    }

    @Override
    public void delete(ThemeElement themeElement) {
        super.deleteByQuery(themeElement);
    }
    
    @Override
    public List<ThemePage> getThemePages(int themeId) {
        List<ThemePage> result = Collections.EMPTY_LIST;
        try {
            disablePortalFilter();
            result = find("from ThemePage where theme.id = " + String.format("%d", themeId));
        } finally {
            enablePortalFilter();
        }
        return result;
    }

    @Override
    public ThemePage getThemePage(int themeId, String key) {
        if (key == null) {
            return null;
        }
        List<ThemePage> result = Collections.EMPTY_LIST;
        try {
            disablePortalFilter();
            Query q = getSession().createQuery("from ThemePage where theme.id = :themeId and key = :key");
            q.setParameter("themeId", themeId);
            q.setParameter("key", key);
            result = q.list();
            
        } finally {
            enablePortalFilter();
        }
        if (result.size() > 1) {
            log.warn("more than 1 item returned for ThemeDAO.getThemePage - duplicate theme page keys in the theme. Returning first result");
        }
        return result.isEmpty() ? null : result.get(0);
    }
    
    @Override
    public void delete(ThemePage page) {
        super.delete(page);
    }
    
    @Override
    public ThemePage save(ThemePage page) {
        return super.save(page);
    }
    
    private void enablePortalFilter() {
        Portal portal = RequestContextHolder.getContext().getPortal();
        FilterManager.setPortalFilter(getSession(), portal);
    }
    
    private void disablePortalFilter() {
        getSession().disableFilter(PortalPersistentImpl.PORTAL_FILTER_NAME);
    }
}
