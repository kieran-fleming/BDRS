package au.com.gaiaresources.bdrs.model.taxa;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringEscapeUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.ParamDef;

import au.com.gaiaresources.bdrs.annotation.CompactAttribute;
import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.file.FileService;

@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "TAXON_GROUP")
@AttributeOverride(name = "id", column = @Column(name = "TAXON_GROUP_ID"))
public class TaxonGroup extends PortalPersistentImpl {
    private String name;
    private String image;
    private String thumbNail;
    private boolean behaviourIncluded;
    private boolean firstAppearanceIncluded;
    private boolean lastAppearanceIncluded;
    private boolean habitatIncluded;
    private boolean weatherIncluded;
    private boolean numberIncluded;

    private List<Attribute> attributes = new ArrayList<Attribute>();

    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @Column(name = "NAME")
    /**
     * Get the name of this taxon group.
     * @return <code>String</code>.
     */
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @Column(name = "IMAGE")
    public String getImage() {
        return image;
    }
    public void setImage(String image){
        this.image = image;
    }

    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @Column(name = "THUMBNAIL")
    public String getThumbNail() {
        return thumbNail;
    }
    public void setThumbNail(String thumbNail){
        this.thumbNail = thumbNail;
    }

    /**
     * {@inheritDoc}
     */
    @Column(name = "BEHAVIOUR_INCLUDED")
    public boolean isBehaviourIncluded() {
        return behaviourIncluded;
    }
    public void setBehaviourIncluded(boolean behaviourIncluded) {
        this.behaviourIncluded = behaviourIncluded;
    }

    /**
     * {@inheritDoc}
     */
    @Column(name = "FIRST_APPEARANCE_INCLUDED")
    public boolean isFirstAppearanceIncluded() {
        return firstAppearanceIncluded;
    }
    public void setFirstAppearanceIncluded(boolean firstAppearanceIncluded) {
        this.firstAppearanceIncluded = firstAppearanceIncluded;
    }

    /**
     * {@inheritDoc}
     */
    @Column(name = "LAST_APPEARANCE_INCLUDED")
    public boolean isLastAppearanceIncluded() {
        return lastAppearanceIncluded;
    }
    public void setLastAppearanceIncluded(boolean lastAppearanceIncluded) {
        this.lastAppearanceIncluded = lastAppearanceIncluded;
    }

    /**
     * {@inheritDoc}
     */
    @Column(name = "HABITAT_INCLUDED")
    public boolean isHabitatIncluded() {
        return habitatIncluded;
    }
    public void setHabitatIncluded(boolean habitatIncluded) {
        this.habitatIncluded = habitatIncluded;
    }

    /**
     * {@inheritDoc}
     */
    @Column(name = "WEATHER_INCLUDED")
    public boolean isWeatherIncluded() {
        return weatherIncluded;
    }
    public void setWeatherIncluded(boolean weatherIncluded) {
        this.weatherIncluded = weatherIncluded;
    }

    /**
     * {@inheritDoc}
     */
    @Column(name = "NUMBER_INCLUDED")
    public boolean isNumberIncluded() {
        return numberIncluded;
    }
    public void setNumberIncluded(boolean numberIncluded) {
        this.numberIncluded = numberIncluded;
    }

    @CompactAttribute
    @OneToMany
    @IndexColumn(name = "pos")
    public List<Attribute> getAttributes() {
        return attributes;
    }
    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }
    
    @Transient
    public String getImageFileURL() {
        return getFileURL(getImage());
    }
    
    @Transient
    public String getThumbnailFileURL() {
        return getFileURL(getThumbNail());
    }
    
    @Transient
    private String getFileURL(String filename) {
    	if (filename == null) {
    		return null;
    	}
        try {
            return String.format(FileService.FILE_URL_TMPL, URLEncoder.encode(getClass()
                    .getCanonicalName(), "UTF-8"), getId(), URLEncoder.encode(
                    filename, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return String.format(FileService.FILE_URL_TMPL, StringEscapeUtils
                    .escapeHtml(getClass().getCanonicalName()), getId(),
                    StringEscapeUtils.escapeHtml(filename));
        }
    }
}
