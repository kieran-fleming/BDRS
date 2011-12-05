package au.com.gaiaresources.bdrs.service.bulkdata;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Represents a single row of an uploaded spreadsheet once it has been converted
 * to a Java representation.
 */
public class RecordUpload {

    private boolean error = false;
    private String errorMessage;

    private String id;

    private Date createdAt;
    private Date updatedAt;

    private String createdBy;
    private String updatedBy;

    private String className;
    private String groupName;

    private String surveyName;
    private String recordedBy;
    private String recordedByUsername;

    // Number seen may be null
    private Integer numberSeen;
    private boolean held;

    private boolean firstAppearance;
    private boolean lastAppearance;

    private Date when;
    private Date time;

    private String locationName;
    private double latitude;
    private double longitude;

    private String behaviour;
    private String habitat;
    private String notes;

    private String scientificName;
    private String commonName;
    
    private String parentId;
    private Integer censusMethodId = null;
    private Integer locationId = null;
    
    private Integer rowNumber;
    private Integer colNumber;
    
    private Map<String, Map<String, String>> named_attribute_map = new HashMap<String, Map<String, String>>(); 
    
    private Logger log = Logger.getLogger(getClass());
    

    public RecordUpload() {

    }

    public RecordUpload(String id, Date createdAt, Date updatedAt,
            String createdBy, String updatedBy, String className,
            String groupName, String surveyName, String recordedBy,
            String recordedByUsername, Integer numberSeen, boolean held,
            boolean firstAppearnce, boolean lastAppearance, Date when,
            Date time, String locationName, double latitude, double longitude,
            String behaviour, String habitat, String scientificName,
            String commonName) {

        super();

        this.id = id;
        this.createdAt = createdAt != null ? (Date)createdAt.clone() : null;
        this.updatedAt = updatedAt != null ? (Date)updatedAt.clone() : null;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.className = className;
        this.groupName = groupName;
        this.surveyName = surveyName;
        this.recordedBy = recordedBy;
        this.recordedByUsername = recordedByUsername;
        this.numberSeen = numberSeen;
        this.held = held;
        this.firstAppearance = firstAppearnce;
        this.lastAppearance = lastAppearance;
        this.when = when != null ? (Date)when.clone() : null;
        this.time = time != null ? (Date)time.clone() : null;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.behaviour = behaviour;
        this.habitat = habitat;
        this.scientificName = scientificName;
        this.commonName = commonName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt != null ? (Date)createdAt.clone() : null;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt != null ? (Date)createdAt.clone() : null;
    }

    public Date getUpdatedAt() {
        return updatedAt != null ? (Date)updatedAt.clone() : null;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt != null ? (Date)updatedAt.clone() : null;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getSurveyName() {
        return surveyName;
    }

    public void setSurveyName(String surveyName) {
        this.surveyName = surveyName;
    }

    public String getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }

    public String getRecordedByUsername() {
        return recordedByUsername;
    }

    public void setRecordedByUsername(String recordedByUsername) {
        this.recordedByUsername = recordedByUsername;
    }

    public Integer getNumberSeen() {
        return numberSeen;
    }

    public void setNumberSeen(Integer numberSeen) {
        this.numberSeen = numberSeen;
    }

    public boolean isHeld() {
        return held;
    }

    public void setHeld(boolean held) {
        this.held = held;
    }

    public boolean getFirstAppearance() {
        return firstAppearance;
    }

    public void setFirstAppearance(boolean firstAppearnce) {
        this.firstAppearance = firstAppearnce;
    }

    public boolean getLastAppearance() {
        return lastAppearance;
    }

    public void setLastAppearance(boolean lastAppearance) {
        this.lastAppearance = lastAppearance;
    }

    public Date getWhen() {
        Calendar calTime = Calendar.getInstance();
        Calendar calDate = Calendar.getInstance();
        if (time != null) {
        	calTime.setTime(time);
        }
        if (when != null) {
        	calDate.setTime(when);
        }
        calDate.add(Calendar.HOUR, calTime.get(Calendar.HOUR));
        calDate.add(Calendar.MINUTE, calTime.get(Calendar.MINUTE));
        calDate.add(Calendar.SECOND, calTime.get(Calendar.SECOND));
        calDate.set(Calendar.AM_PM, calTime.get(Calendar.AM_PM));
        
        if (time == null && when == null) {
        	return null;
        }
        
        return calDate.getTime();
    }

    public void setWhen(Date when) {
        this.when = when != null ? (Date)when.clone() : null;
    }

    public Date getTime() {
        return getWhen();
    }

    public void setTime(Date time) {
        this.time = time != null ? (Date)time.clone() : null;
    }

    /**
     * @return the lastDate
     */
    public Date getLastDate() {
        return getWhen();
    }

    /**
     * @return the lastTime
     */
    public Date getLastTime() {
        return getWhen();
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

    public String getBehaviour() {
        return behaviour;
    }

    public void setBehaviour(String behaviour) {
        this.behaviour = behaviour;
    }

    public String getHabitat() {
        return habitat;
    }

    public void setHabitat(String habitat) {
        this.habitat = habitat;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public void setNamedAttribute(String namespace, String name, String value) {
        if (named_attribute_map.get(namespace) == null) {
            named_attribute_map.put(namespace, new HashMap<String, String>());
        }
        named_attribute_map.get(namespace).put(name, value);
    }
    
    public String getNamedAttribute(String namespace, String name) {
        if (named_attribute_map.get(namespace) == null) {
            return null;
        }
        return named_attribute_map.get(namespace).get(name);
    }

    public boolean isError() {
        return error;
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

    public boolean isGPSLocationName() {
        return !((locationName != null) && !locationName.isEmpty()
                && !RecordRow.GPS_LOCATION.equals(locationName));
    }

    public boolean hasLatitudeLongitude() {
        return !Double.isNaN(latitude) && !Double.isNaN(longitude);
    }
    
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    
    public Integer getRowNumber() {
        return rowNumber;
    }
    
    public void setRowNumber(Integer val) {
        rowNumber = val;
    }
    
    public Integer getColNumber() {
        return colNumber;
    }
    
    public void setColNumber(Integer val) {
        colNumber = val;
    }

    public Integer getCensusMethodId() {
        return censusMethodId;
    }

    public void setCensusMethodId(Integer censusMethodId) {
        this.censusMethodId = censusMethodId;
    }
    
    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }
    
    public Integer getLocationId() {
        return this.locationId;
    }

    @Override
    public String toString() {
    	Date when = getWhen();
    	String whenString = "";
    	if (when != null){
    		whenString = when.toString();
    	}
        return "RecordUpload [behaviour=" + behaviour + ", className="
                + className + ", commonName=" + commonName + ", createdAt="
                + createdAt + ", createdBy=" + createdBy + ", error=" + error
                + ", errorMessage=" + errorMessage + ", firstAppearance="
                + firstAppearance + ", groupName=" + groupName + ", habitat="
                + habitat + ", held=" + held + ", id=" + id
                + ", lastAppearance=" + lastAppearance + ", lastDate="
                + whenString + ", lastTime=" + whenString + ", latitude="
                + latitude + ", locationName=" + locationName
                + ", longitude="
                + longitude + ", notes=" + notes + ", numberSeen=" + numberSeen
                + ", recordedBy=" + recordedBy + ", recordedByUsername="
                + recordedByUsername + ", scientificName=" + scientificName
                + ", surveyName=" + surveyName + ", time=" + whenString
                + ", updatedAt=" + updatedAt + ", updatedBy=" + updatedBy
                + ", when=" + whenString + "]";
    }
}