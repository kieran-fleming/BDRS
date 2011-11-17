package au.com.gaiaresources.bdrs.model.preference;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class PreferenceUtil {
    
    private PreferenceDAO prefDAO;
    
    private Logger log = Logger.getLogger(getClass());
    
    public PreferenceUtil(PreferenceDAO prefDAO) {
        if (prefDAO == null) {
            throw new IllegalArgumentException("PreferenceDAO, prefDAO, cannot be null");
        }
        this.prefDAO = prefDAO;
    }
    
    public boolean getBooleanPreference(String key) {
        Preference pref = prefDAO.getPreferenceByKey(key);
        if (pref == null) {
            throw new NullPointerException("Get preference by key returned null : " + key);
        }
        String value = pref.getValue();
        if (StringUtils.hasLength(value)) {
            return Boolean.parseBoolean(value);
        } else {
            return false;
        }
    }
}
