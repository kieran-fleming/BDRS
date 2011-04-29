package au.com.gaiaresources.bdrs.service.bulkdata;

public class LocationUpload {

    private String surveyName;
    private String locationName;
    private double latitude;
    private double longitude;

    public LocationUpload(String surveyName, String locationName,
            double latitude, double longitude) {
        super();
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

    @Override
    public String toString() {
        return "LocationUpload [latitude=" + latitude + ", locationName="
                + locationName + ", longitude=" + longitude + ", surveyName="
                + surveyName + "]";
    }
}