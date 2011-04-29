package au.com.gaiaresources.bdrs.model.theme.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.theme.Theme;
import au.com.gaiaresources.bdrs.model.theme.ThemeDAO;
import au.com.gaiaresources.bdrs.model.theme.ThemeElement;

@Repository
public class ThemeDAOImpl extends AbstractDAOImpl implements ThemeDAO {
    private Logger log = Logger.getLogger(getClass());
    
    @Override
    public Theme getActiveTheme(Portal portal) {
        getSession().disableFilter(PortalPersistentImpl.PORTAL_FILTER_NAME);
        Query q = getSession().createQuery("from Theme where portal = :portal and active = :active order by id asc");
        q.setParameter("portal", portal);
        q.setParameter("active", true);
        
        List<Theme> themeList = q.list();
        if(themeList.size() > 1){
            log.warn(String.format("More than one active theme returned for portal with ID: %d. Returning the first theme.",portal.getId()));
        } 
        return themeList.isEmpty() ? null : themeList.get(0);    
    }

    @Override
    public Theme getTheme(int themeId) {
        return (Theme)getSession().get(Theme.class, themeId);
        //return getByID(Theme.class, themeId);
    }
    
    @Override
    public ThemeElement getThemeElement(int themeElementId) {
        return (ThemeElement)getSession().get(ThemeElement.class, themeElementId);
    }

    @Override
    public List<Theme> getThemes(Portal portal) {
        getSession().disableFilter(PortalPersistentImpl.PORTAL_FILTER_NAME);
        return find("from Theme where portal = ?", portal);
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
}
