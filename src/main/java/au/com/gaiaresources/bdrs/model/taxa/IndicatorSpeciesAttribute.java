package au.com.gaiaresources.bdrs.model.taxa;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringEscapeUtils;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import au.com.gaiaresources.bdrs.annotation.CompactAttribute;
import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.file.FileService;

/**
 * The value of an attribute attached to a record.
 * @author Tim Carpenter
 *
 */
@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Table(name = "INDICATOR_SPECIES_ATTRIBUTE")
@AttributeOverride(name = "id", column = @Column(name = "INDICATOR_SPECIES_ATTRIBUTE_ID"))
public class IndicatorSpeciesAttribute extends PortalPersistentImpl implements TypedAttributeValue {
    private Attribute attribute;
    private BigDecimal numericValue;
    private String stringValue = "Not recorded";
    private Date dateValue;
    private String description = "";

    /* (non-Javadoc)
	 * @see au.com.gaiaresources.bdrs.model.taxa.AttributeValue#getAttribute()
	 */
    @CompactAttribute
    @ManyToOne
    @JoinColumn(name = "ATTRIBUTE_ID", nullable = false)
    /**
     * Get the attribute definition that this value is for.
     * @return {@link TaxonGroupAttribute}
     */
    public Attribute getAttribute() {
        return attribute;
    }
    /* (non-Javadoc)
	 * @see au.com.gaiaresources.bdrs.model.taxa.AttributeValue#setAttribute(au.com.gaiaresources.bdrs.model.taxa.Attribute)
	 */
    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    /* (non-Javadoc)
	 * @see au.com.gaiaresources.bdrs.model.taxa.AttributeValue#getNumericValue()
	 */
    @CompactAttribute
    @Column(name = "NUMERIC_VALUE")
    /**
     * Get the value as an number, returns a value if and only if the type of
     * the {@link TaxonGroupAttribute} is integer or decimal.
     * @return {@link BigDecimal}
     */
    public BigDecimal getNumericValue() {
        return numericValue;
    }
    /* (non-Javadoc)
	 * @see au.com.gaiaresources.bdrs.model.taxa.AttributeValue#setNumericValue(java.math.BigDecimal)
	 */
    public void setNumericValue(BigDecimal numericValue) {
        this.numericValue = numericValue;
    }

    /* (non-Javadoc)
	 * @see au.com.gaiaresources.bdrs.model.taxa.AttributeValue#getStringValue()
	 */
    @CompactAttribute
    @Column(name = "STRING_VALUE")
    /**
     * Get the value as a string, returns a value if and only if the type of
     * the {@link TaxonGroupAttribute} is string.
     * @return {@link String}
     */
    public String getStringValue() {
        return stringValue;
    }
    /* (non-Javadoc)
	 * @see au.com.gaiaresources.bdrs.model.taxa.AttributeValue#setStringValue(java.lang.String)
	 */
    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    @CompactAttribute
    @Column(name = "DESCRIPTION")
    /**
     * An optional attribute which is the description of the attribute for text images..
     * @return {@link String}
     */
    public String getDescription() {
        return description;
    }
    public void setDescription(String desc) {
        this.description = desc;
    }
    
    /* (non-Javadoc)
	 * @see au.com.gaiaresources.bdrs.model.taxa.AttributeValue#getDateValue()
	 */
    @CompactAttribute
    @Column(name = "DATE_VALUE")
    /**
     * Get the value as a date, returns a value if and only if the type of
     * the {@link TaxonGroupAttribute} is string.
     * @return {@link Date}
     * @return
     */
    public Date getDateValue() {
        return dateValue;
    }
    /* (non-Javadoc)
	 * @see au.com.gaiaresources.bdrs.model.taxa.AttributeValue#setDateValue(java.util.Date)
	 */
    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }
    
    @Transient
    public String getFileURL() {
        try {
            return String.format(FileService.FILE_URL_TMPL, URLEncoder.encode(getClass()
                    .getCanonicalName(), "UTF-8"), getId(), URLEncoder.encode(
                    getStringValue(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return String.format(FileService.FILE_URL_TMPL, StringEscapeUtils
                    .escapeHtml(getClass().getCanonicalName()), getId(),
                    StringEscapeUtils.escapeHtml(getStringValue()));
        }
    }
}
