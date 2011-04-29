package au.com.gaiaresources.bdrs.kml.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;

public class KMLWriter extends au.com.gaiaresources.bdrs.kml.KMLWriter {
    private HttpServletRequest request;
    
    public KMLWriter(HttpServletRequest request) throws JAXBException {
        super();
        this.request = request;
    }
    
    public void createStyleIcon(String id, String iconUrl, boolean relativePath) {
        if (relativePath) {
            String url = request.getContextPath() + iconUrl;
            super.createStyleIcon(id, url);
        } else {
            super.createStyleIcon(id, iconUrl);
        }
    }
    
    public void createStyleIcon(String id, String iconUrl, boolean relativePath, int hotSpotX, int hotSpotY) {
        if (relativePath) {
            String url = request.getContextPath() + iconUrl;
            super.createStyleIcon(id, url, hotSpotX, hotSpotY);
        } else {
            super.createStyleIcon(id, iconUrl, hotSpotX, hotSpotY);
        }
    }
}
