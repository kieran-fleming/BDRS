package au.com.gaiaresources.bdrs.model.taxa;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.hibernate.annotations.Index;
import org.hibernate.annotations.ParamDef;

import au.com.gaiaresources.bdrs.annotation.CompactAttribute;
import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.file.FileService;

/**
 * The value of an attribute attached to a record.
 *
 * @author Tim Carpenter
 *
 */
@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Table(name = "ATTRIBUTE_VALUE")
@AttributeOverride(name = "id", column = @Column(name = "ATTRIBUTE_VALUE_ID"))
public class AttributeValue extends PortalPersistentImpl implements TypedAttributeValue {
    private Attribute attribute;
    private BigDecimal numericValue;
    private String stringValue = "Not recorded";
    private Date dateValue;

    /**
     * Populates the <code>numericValue</code> or <code>dateValue</code> from
     * the contents of the <code>stringValue</code>
     */
    @Transient
    public void populateFromStringValue() throws NumberFormatException,
            ParseException {
        if (attribute == null) {
            return;
        }

        // Nothing to be done for String, Text, String with Valid Values,
        // Image or File

        AttributeType type = attribute.getType();
        if (AttributeType.INTEGER.equals(type)
                || AttributeType.DECIMAL.equals(type)) {
            BigDecimal num = null;
            if (!stringValue.isEmpty()) {
                num = new BigDecimal(stringValue);
            }
            setNumericValue(num);
        } else if (AttributeType.DATE.equals(type)) {
            Date date = null;
            if (!stringValue.isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
                date = dateFormat.parse(stringValue);
            }
            setDateValue(date);
        }
    }


    /**
     * Get the attribute definition that this value is for.
     * @return {@link TaxonGroupAttribute}
     */
    @CompactAttribute
    @ManyToOne
    @JoinColumn(name = "ATTRIBUTE_ID", nullable = false)    
    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }


    /**
     * Get the value as an number, returns a value if and only if the type of
     * the {@link TaxonGroupAttribute} is integer or decimal.
     * @return {@link BigDecimal}
     */
    @CompactAttribute
    @Column(name = "NUMERIC_VALUE")
    public BigDecimal getNumericValue() {
        return numericValue;
    }

    public void setNumericValue(BigDecimal numericValue) {
        this.numericValue = numericValue;
    }

    
    /**
     * Get the value as a string, returns a value if and only if the type of
     * the {@link TaxonGroupAttribute} is string.
     * @return {@link String}
     */
    @CompactAttribute
    @Column(name = "STRING_VALUE")
    @Index(name="attribute_value_string_value_index")
    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    /**
     * Get the value as a date, returns a value if and only if the type of
     * the {@link TaxonGroupAttribute} is string.
     * @return {@link Date}
     * @return
     */
    @CompactAttribute
    @Column(name = "DATE_VALUE")
    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    @Transient
    public String getFileURL() {
        if(getStringValue() != null) {
            try {
                return String.format(FileService.FILE_URL_TMPL, URLEncoder.encode(getClass()
                        .getCanonicalName(), "UTF-8"), getId(), URLEncoder.encode(
                        getStringValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                return String.format(FileService.FILE_URL_TMPL, StringEscapeUtils
                        .escapeHtml(getClass().getCanonicalName()), getId(),
                        StringEscapeUtils.escapeHtml(getStringValue()));
            }
        } else {
            return "";
        }
    }
}
