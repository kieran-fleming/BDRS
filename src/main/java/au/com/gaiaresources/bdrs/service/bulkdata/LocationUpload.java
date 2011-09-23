package au.com.gaiaresources.bdrs.service.bulkdata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import au.com.gaiaresources.bdrs.model.taxa.Attribute;

/**
 * Represents an uploaded <code>Location</code>. The Location may or may not 
 * have been saved. If the Location is unsaved, the primary key of this object
 * will be {@link LocationUpload#DEFAULT_PK}.
 */
public class LocationUpload {
    
    public static final int DEFAULT_PK = 0;
    
    private boolean error = false;
    private String errorMessage;

    private int pk = DEFAULT_PK;
    private String surveyName;
    private String locationName;
    private double latitude;
    private double longitude;
    
    private Map<Attribute, String> attrMap = new HashMap<Attribute, String>();

    public LocationUpload() {
    }

    public LocationUpload(String surveyName, String locationName, double latitude, double longitude) {
        this(DEFAULT_PK, surveyName, locationName, latitude, longitude);
    }
    
    public LocationUpload(int pk, String surveyName, String locationName, double latitude, double longitude) {
        this.pk = pk;
        this.surveyName = surveyName;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getSurveyName() {
        return surveyName;
    }

    public void setSurveyName(String surveyName) {
        this.surveyName = surveyName;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public void setAttributeValue(Attribute attr, String value) {
        attrMap.put(attr, value);
    }
    
    public String getAttributeValue(Attribute attr) {
        return attrMap.get(attr);
    }
    
    public int getPk() {
        return pk;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public boolean isError() {
        return error;
    }

    @Override
    public String toString() {
        final int maxLen = 100;
        return "LocationUpload [attrMap="
                + (attrMap != null ? toString(attrMap.entrySet(), maxLen)
                        : null) + ", error=" + error + ", errorMessage="
                + errorMessage + ", latitude=" + latitude + ", locationName="
                + locationName + ", longitude=" + longitude + ", pk=" + pk
                + ", surveyName=" + surveyName + "]";
    }

    private String toString(Collection<?> collection, int maxLen) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int i = 0;
        for (Iterator<?> iterator = collection.iterator(); iterator.hasNext()
                && i < maxLen; i++) {
            if (i > 0)
                builder.append(", ");
            builder.append(iterator.next());
        }
        builder.append("]");
        return builder.toString();
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
