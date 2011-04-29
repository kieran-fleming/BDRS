package au.com.gaiaresources.bdrs.config;

import au.com.gaiaresources.bdrs.config.AppContext;

public class ProfileConfig {

    private String xmlConfig;

    public static ProfileConfig getProfileConfig() {
        return AppContext.getBean(ProfileConfig.class);
    }

    public ProfileConfig() {
    }

    public String getXmlConfigFilename() {
        return xmlConfig;
    }

    public void setXmlConfigFilename(String value) {
        xmlConfig = value;
    }
}
