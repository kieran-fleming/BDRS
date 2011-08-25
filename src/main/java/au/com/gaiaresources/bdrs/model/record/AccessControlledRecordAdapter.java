package au.com.gaiaresources.bdrs.model.record;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import au.com.gaiaresources.bdrs.model.expert.ReviewRequest;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.user.User;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * Single point for the logic to control access to certain record fields.
 * Will return 'null' if the accessor does not have access to the fields.
 * 
 * @author aaron
 *
 */
public class AccessControlledRecordAdapter implements ReadOnlyRecord {

    private Record record;
    private boolean hideDetails;
    
    public AccessControlledRecordAdapter(Record record, User accessor) {
        if (record == null) {
            throw new IllegalArgumentException("Record, record cannot be null");
        }
        // accessor can be null
        
        this.record = record;
        hideDetails = record.hideDetails(accessor);
    }
    
    @Override
    public Integer getId() {
        return record.getId();
    }
    
    @Override
    public Double getAccuracyInMeters() {
        if (hideDetails) {
            return null;
        }
        return record.getAccuracyInMeters();
    }

    @Override
    public Set<AttributeValue> getAttributes() {
        if (hideDetails) {
            return Collections.EMPTY_SET;
        }
        return record.getAttributes();
    }

    @Override
    public String getBehaviour() {
        if (hideDetails) {
            return null;
        }
        return record.getBehaviour();
    }

    @Override
    public CensusMethod getCensusMethod() {
        return record.getCensusMethod();
    }

    @Override
    public Set<Record> getChildRecords() {
        return record.getChildRecords();
    }

    @Override
    public Geometry getGeometry() {
        return record.getGeometry();
    }

    @Override
    public String getHabitat() {
        if (hideDetails) {
            return null;
        }
        return record.getHabitat();
    }

    @Override
    public Boolean getHeld() {
        return record.getHeld();
    }

    @Override
    public Date getLastDate() {
        return record.getLastDate();
    }

    @Override
    public Long getLastTime() {
        return record.getLastTime();
    }

    @Override
    public Date getLastTimeAsDate() {
        return record.getLastTimeAsDate();
    }

    @Override
    public Double getLatitude() {
        return record.getLatitude();
    }

    @Override
    public Location getLocation() {
        return record.getLocation();
    }

    @Override
    public Double getLongitude() {
        return record.getLongitude();
    }

    @Override
    public Set<Metadata> getMetadata() {
        return record.getMetadata();
    }

    @Override
    public String getMetadataValue(String key) {
        return record.getMetadataValue(key);
    }

    @Override
    public String getNotes() {
        if (hideDetails) {
            return null;
        }
        return record.getNotes();
    }

    @Override
    public Integer getNumber() {
        if (hideDetails) {
            return null;
        }
        return record.getNumber();
    }

    @Override
    public List<AttributeValue> getOrderedAttributes() {
        if (hideDetails) {
            return Collections.EMPTY_LIST;
        }
        return record.getOrderedAttributes();
    }

    @Override
    public Record getParentRecord() {
        return record.getParentRecord();
    }

    @Override
    public Point getPoint() {
        return record.getPoint();
    }

    @Override
    public RecordVisibility getRecordVisibility() {
        return record.getRecordVisibility();
    }

    @Override
    public Set<ReviewRequest> getReviewRequests() {
        return record.getReviewRequests();
    }

    @Override
    public IndicatorSpecies getSpecies() {
        if (hideDetails) {
            return null;
        }
        return record.getSpecies();
    }

    @Override
    public Survey getSurvey() {
        return record.getSurvey();
    }

    @Override
    public Long getTime() {
        return record.getTime();
    }

    @Override
    public Date getTimeAsDate() {
        return record.getTimeAsDate();
    }

    @Override
    public User getUser() {
        return record.getUser();
    }

    @Override
    public Date getWhen() {
        return record.getWhen();
    }

    @Override
    public Boolean isFirstAppearance() {
        return record.isFirstAppearance();
    }

    @Override
    public Boolean isHeld() {
        return record.isHeld();
    }

    @Override
    public Boolean isLastAppearance() {
        return record.isLastAppearance();
    }

    @Override
    public boolean isNotDuplicate() {
        return record.isNotDuplicate();
    }

}
