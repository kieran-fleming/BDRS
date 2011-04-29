package au.com.gaiaresources.bdrs.controller.form;

import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.util.DateFormatter;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

/**
 * Class that can display the form for entering a record and is also used to render record details.
 * @see RecordFormEnhancer
 * @author Tim Carpenter
 */
public class RecordForm {
    // ID
    private Integer id;
    
    // Who
    private Integer userID;
    
    // Where
    private Integer locationId;
    private String locationName;
    private BigDecimal locationLatitude;
    private BigDecimal locationLongitude;
    
    // What
    private Integer indicatorSpeciesID;
    private String speciesCommonName;
    private String speciesScientificName;
    
    // When
    private Date date;
    
    private Integer hour;
    private Integer minute;
    
    // Notes
    private String notes;
    
    // File
    private MultipartFile file;
    
    // Optional information
    private String behaviour;
    private Boolean firstAppearance;
    private Boolean lastAppearance;
    private String habitat;
    private Integer number;
    
    // CHECKSTYLE_OFF: JavaDoc
    
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getUserID() {
        return userID;
    }
    public void setUserID(Integer userID) {
        this.userID = userID;
    }
    public Integer getLocationId() {
        return locationId;
    }
    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }
    public String getLocationName() {
        return locationName;
    }
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
    public BigDecimal getLocationLongitude() {
        return locationLongitude;
    }
    public void setLocationLongitude(BigDecimal locationLongitude) {
        this.locationLongitude = locationLongitude;
    }
    public BigDecimal getLocationLatitude() {
        return locationLatitude;
    }
    public void setLocationLatitude(BigDecimal locationLatitude) {
        this.locationLatitude = locationLatitude;
    }
    public Integer getIndicatorSpeciesID() {
        return indicatorSpeciesID;
    }
    public void setIndicatorSpeciesID(Integer indicatorSpeciesID) {
        this.indicatorSpeciesID = indicatorSpeciesID;
    }
    public String getSpeciesCommonName() {
        return speciesCommonName;
    }
    public void setSpeciesCommonName(String speciesCommonName) {
        this.speciesCommonName = speciesCommonName;
    }
    public String getSpeciesScientificName() {
        return speciesScientificName;
    }
    public void setSpeciesScientificName(String speciesScientificName) {
        this.speciesScientificName = speciesScientificName;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public Date getDate() {
        return date;
    }
    public String getDisplayDate() {
        return DateFormatter.format(getDate(), DateFormatter.DAY_MONTH_YEAR);
    }
    public Integer getHour() {
        return hour;
    }
    public void setHour(Integer hour) {
        this.hour = hour;
    }
    public Integer getMinute() {
        return minute;
    }
    public void setMinute(Integer minute) {
        this.minute = minute;
    }
    public String getDisplayTime() {
        String time = "";
        if (hour != null) {
            time += hour;
            if (minute != null) {
                if (minute < 10) {
                    time += ":0" + minute;
                } else {
                    time += ":" + minute;
                }
            } else {
                time += ":00";
            }
        }
        return time;
    }
    public MultipartFile getFile() {
        return file;
    }
    public void setFile(MultipartFile file) {
        this.file = file;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public void copyCoreElements(RecordForm f) {
        this.id = f.getId();
        
        this.userID = f.getUserID();
        
        this.locationId = f.getLocationId();
        this.locationName = f.getLocationName();
        this.locationLatitude = f.getLocationLatitude();
        this.locationLongitude = f.getLocationLongitude();
        
        this.indicatorSpeciesID = f.getIndicatorSpeciesID();
        this.speciesCommonName = f.getSpeciesCommonName();
        this.speciesScientificName = f.getSpeciesScientificName();
        
        this.date = f.getDate();
        this.hour = f.getHour();
        this.minute = f.getMinute();
    }
    
    
    public void load(Record record) {
        this.id = record.getId();
        
        this.userID = record.getUser().getId();
        this.locationId = record.getLocation().getId();
        this.locationName = record.getLocation().getName();
        this.locationLatitude = new BigDecimal(record.getLocation().getLocation().getY());
        this.locationLongitude = new BigDecimal(record.getLocation().getLocation().getX());
        
        this.indicatorSpeciesID = record.getSpecies().getId();
        this.speciesCommonName = record.getSpecies().getCommonName();
        this.speciesScientificName = record.getSpecies().getScientificName();
        
        this.date = record.getWhen();
        if (record.getTimeAsDate() != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(record.getTimeAsDate());
            this.hour = c.get(Calendar.HOUR_OF_DAY);
            this.minute = c.get(Calendar.MINUTE);
        }
        
        this.notes = record.getNotes();
        
        this.behaviour = record.getBehaviour();
        this.firstAppearance = record.isFirstAppearance();
        this.lastAppearance = record.isLastAppearance();
        this.habitat = record.getHabitat();
        this.number = record.getNumber();
    }
    
    public String getBehaviour() {
        return behaviour;
    }
    public void setBehaviour(String behaviour) {
        this.behaviour = behaviour;
    }
    public Boolean isFirstAppearance() {
        return firstAppearance;
    }
    public void setFirstAppearance(Boolean firstAppearance) {
        this.firstAppearance = firstAppearance;
    }
    public Boolean isLastAppearance() {
        return lastAppearance;
    }
    public void setLastAppearance(Boolean lastAppearance) {
        this.lastAppearance = lastAppearance;
    }
    public String getHabitat() {
        return habitat;
    }
    public void setHabitat(String habitat) {
        this.habitat = habitat;
    }
    public Integer getNumber() {
        return number;
    }
    public void setNumber(Integer number) {
        this.number = number;
    }
}
