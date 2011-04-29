package au.com.gaiaresources.bdrs.service.lsid;

import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import au.com.gaiaresources.bdrs.db.Persistent;

@Service
public class LSIDService {
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());
    private Properties lsidProperties; 

    @PostConstruct
    public void init() throws Exception {
        lsidProperties = new Properties();
        lsidProperties.load(getClass().getResourceAsStream("lsid.properties"));
    }

    public Lsid toLSID(Persistent persistent) {
        if (persistent == null) {
            return null;
        }
        
        return new Lsid(lsidProperties,  persistent);
    }
    
    public Lsid fromLSID(String lsid) {
        return new Lsid(lsidProperties, lsid);
    }
}
