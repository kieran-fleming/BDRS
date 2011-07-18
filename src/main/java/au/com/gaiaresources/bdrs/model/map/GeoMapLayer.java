package au.com.gaiaresources.bdrs.model.map;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.ParamDef;

import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;


@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Table(name = "GEO_MAP_LAYER")
@AttributeOverride(name = "id", column = @Column(name = "GEO_MAP_LAYER_ID"))
public class GeoMapLayer extends PortalPersistentImpl {

    Survey survey = null;
    String name = "";
    String description = "";
    boolean hidePrivateDetails = false;
    String managedFileUUID = "";
    // Will probably do this using hierarchical roles.
    String roleRequired = ""; // can be null / empty string
    boolean publish = true;
    GeoMapLayerSource layerSrc = null;
    
    public static final String DEFAULT_STROKE_COLOR = "#000000";
    public static final String DEFAULT_FILL_COLOR = "#EE9900";
    
    // Styling
    String strokeColor = DEFAULT_STROKE_COLOR;
    String fillColor = DEFAULT_FILL_COLOR;
    int symbolSize = 5;
    int strokeWidth = 1;
    
    List<Attribute> attributes = new LinkedList<Attribute>();
    
    @ManyToOne
    @JoinColumn(name = "SURVEY_ID", nullable = true)
    @ForeignKey(name = "GEO_MAP_LAYER_TO_SURVEY_FK")
    public Survey getSurvey() {
        return survey;
    }
    public void setSurvey(Survey survey) {
        this.survey = survey;
    }
    
    @Column(name = "MANAGED_FILE_UUID", nullable = true)
    public String getManagedFileUUID() {
        return this.managedFileUUID;
    }
    public void setManagedFileUUID(String uuid) {
        this.managedFileUUID = uuid;
    }
    
    @Column(name = "NAME", nullable = false)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    @Enumerated(EnumType.STRING)
    @Column(name = "LAYER_SOURCE", nullable=true)
    public GeoMapLayerSource getLayerSource() {
        return this.layerSrc;
    }
    public void setLayerSource(GeoMapLayerSource value) {
        this.layerSrc = value;
    }
    
    @Column(name = "DESCRIPTION", length=1023, nullable = false)
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Column(name = "HIDE_PRIVATE_DETAILS", nullable = false)
    public boolean isHidePrivateDetails() {
        return hidePrivateDetails;
    }
    public void setHidePrivateDetails(boolean hidePrivateDetails) {
        this.hidePrivateDetails = hidePrivateDetails;
    }
    
    @Column(name = "ROLE_REQUIRED", nullable = true)
    public String getRoleRequired() {
        return roleRequired;
    }
    public void setRoleRequired(String roleRequired) {
        this.roleRequired = roleRequired;
    }
    
    @Column(name = "PUBLISH", nullable = false)
    public boolean isPublish() {
        return publish;
    }
    public void setPublish(boolean publish) {
        this.publish = publish;
    }
    
    @OneToMany
    @JoinTable(name="GEO_MAP_LAYER_ATTRIBUTES")
    public List<Attribute> getAttributes() {
        return this.attributes;
    }
    public void setAttributes(List<Attribute> value) {
        this.attributes = value;
    }
    
    @Column(name = "STROKE_COLOR", length=15, nullable = false)
    public String getStrokeColor() {
        return this.strokeColor;
    }
    public void setStrokeColor(String value) {
        this.strokeColor = value;
    }
    
    @Column(name = "FILL_COLOR", length=15, nullable = false)
    public String getFillColor() {
        return fillColor;
    }
    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }
    
    @Column(name = "SYMBOL_SIZE", nullable = false)
    public int getSymbolSize() {
        return symbolSize;
    }
    public void setSymbolSize(int size) {
        this.symbolSize = size;
    }
    
    @Column(name = "STROKE_WIDTH", nullable = false)
    public int getStrokeWidth() {
        return strokeWidth;
    }
    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }
}
