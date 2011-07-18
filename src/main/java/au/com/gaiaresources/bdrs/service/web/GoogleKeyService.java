package au.com.gaiaresources.bdrs.service.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.gaiaresources.bdrs.model.preference.Preference;
import au.com.gaiaresources.bdrs.model.preference.PreferenceDAO;


@Service
public class GoogleKeyService {

    @Autowired
    PreferenceDAO prefDAO;
    
    Logger log = Logger.getLogger(this.getClass());
    
    public String getGoogleMapApiKey(String hostname) throws IOException {
        // Try to load from database. If not found in DB, load from the GoogleMapKey.properties file
        List<Preference> googlePrefs = prefDAO.getPreferenceByKeyPrefix(Preference.GOOGLE_MAP_KEY_PREFIX);
        if (googlePrefs.size() > 0) {
            for (Preference pref : googlePrefs) {
                String[] split = pref.getValue().split(",");
                if (split.length != 2) {
                    log.error("Misformed google map key preference. Must be a comma delimited string of form: hostname,key");
                    continue;
                }
                // index 0 is the hostname, index 1 is google map key
                if (hostname.equals(split[0])) {
                    return split[1];
                }
            }
        } else {
            Properties properties = new Properties();
            InputStream stream = null;
            String result = null;
            try {
                stream = getClass().getResourceAsStream("GoogleMapKey.properties");
                properties.load(stream);
                result = properties.getProperty("google.map.key");
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            return result;
        }
        return null;
    }
}
