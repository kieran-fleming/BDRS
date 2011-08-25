package au.com.gaiaresources.bdrs.model.record;

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
 * All the 'get' properties for a record. Is up to the implementation whether
 * things like returned sets are immutable or not.
 * 
 * @author aaron
 *
 */
public interface ReadOnlyRecord {
    
    public Integer getId();

    public IndicatorSpecies getSpecies();

    public Record getParentRecord();
    
    public Set<Record> getChildRecords();

    public Survey getSurvey();

    public CensusMethod getCensusMethod();

    public User getUser();

    public Location getLocation();

    public Point getPoint();

    public Geometry getGeometry();

    public Double getAccuracyInMeters();

    public Boolean isHeld();

    public Boolean getHeld();

    public Date getWhen();

    public Long getTime();

    public Date getLastDate();

    public Long getLastTime();

    public Date getTimeAsDate();

    public Date getLastTimeAsDate();

    public Set<AttributeValue> getAttributes();

    public String getNotes();

    public Boolean isFirstAppearance();

    public Boolean isLastAppearance();

    public String getBehaviour();

    public String getHabitat();

    public Integer getNumber();

    public Set<ReviewRequest> getReviewRequests();

    public RecordVisibility getRecordVisibility();
    
    public Set<Metadata> getMetadata();
    
    public String getMetadataValue(String key);

    public Double getLatitude();

    public Double getLongitude();

    public boolean isNotDuplicate();

    public List<AttributeValue> getOrderedAttributes();
}
