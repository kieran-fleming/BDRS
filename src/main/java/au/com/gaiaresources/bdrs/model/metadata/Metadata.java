package au.com.gaiaresources.bdrs.model.metadata;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringEscapeUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.ParamDef;

import au.com.gaiaresources.bdrs.controller.attribute.RecordPropertyAttributeFormField;
import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;

@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "METADATA")
@AttributeOverride(name = "id", column = @Column(name = "ID"))
public class Metadata extends PortalPersistentImpl {
    public static final String FILE_URL_TMPL = "className=%s&id=%d&fileName=%s";

    // User Keys
    public static final String SCHOOL_NAME_KEY = "schoolname";
    public static final String SCHOOL_SUBURB_KEY = "school suburb";
    
    public static final String AGE = "age";
    public static final String HEAR_ABOUT = "hear about";
    public static final String TELEPHONE = "telephone";
    public static final String CLIMATEWATCH_USERNAME = "climatewatch username";
    
    public static final String TITLE = "title";
    public static final String GROUP_NAME = "group_name";
    public static final String ADDRESS_LINE_1 = "address_line_1";
    public static final String ADDRESS_LINE_2 = "address_line_2";
    public static final String ADDRESS_LINE_3 = "address_line_3";
    public static final String SUBURB = "suburb";
    public static final String STATE = "state";
    public static final String COUNTRY = "country";
    public static final String POSTCODE = "postcode";
    public static final String HOME_PHONE = "home_phone";
    public static final String MOBILE_PHONE = "mobile_phone";
    public static final String WORK_PHONE = "work_phone";
    public static final String PROMO_CODE = "promo_code";
    public static final String BA_MEMBER_NO = "ba_member_no";
    public static final String BIRTH_YEAR = "birth_year";
    public static final String SUBSCRIBE_NEWS = "subscribe_news";
    public static final String REQUEST_INFO = "request_info";
    
    // Record Keys
    // The original id when the record was uploaded from a spreadsheet.
    public static final String RECORD_UPLOAD_ORIG_ID = "record_upload_orig_id";
    public static final String RECORD_IS_MASTER = "record_is_master";
    public static final String RECORD_NOT_DUPLICATE = "record_is_not_duplicate";
    // Survey Keys
    public static final String FORM_RENDERER_TYPE = "FormRendererType";
    public static final String SURVEY_LOGO = "SurveyLogo";
    public static final String PREDEFINED_LOCATIONS_ONLY = "PredefinedLocationsOnly";
    
    // SpeciesProfile Keys
    public static final String SCIENTIFIC_NAME_SOURCE_DATA_ID = "ScientificNameSourceDataId";
    public static final String COMMON_NAME_SOURCE_DATA_ID = "CommonNameSourceDataId";
    public static final String PUBLICATION_SOURCE_DATA_ID = "PublicationSourceDataId";
    
    // IndicatorSpecies Keys
    public static final String TAXON_SOURCE_DATA_ID = "TaxonSourceDataID";
    public static final String TAXON_SOURCE = "TaxonSource";
    
    // Record Property
    /**
     * Template that is used to generate Metadata keys. Metadata are keyed
     * against the property name which results in keys such as
     * <i>Record.species</i> or <i>Record.location</i>.
     * 
     * @see RecordPropertyAttributeFormField#RECORD_PROPERTY_NAMES
     */
    public static final String RECORD_PROPERTY_FIELD_METADATA_KEY_TEMPLATE = "Record.%s";

    private String key;
    private String value;

    public Metadata() {
    	super();
    }
    
    public Metadata(String key, String value) {
    	super();
    	this.key = key;
    	this.value = value;
    }
    
    @Column(name="KEY", nullable=false)
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Column(name="VALUE", nullable=false)
    @Index(name="metadata_value_index")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    @Transient
    public String getFileURL() {
        try {
            return String.format(FILE_URL_TMPL, URLEncoder.encode(getClass()
                    .getCanonicalName(), "UTF-8"), getId(), URLEncoder.encode(
                    getValue(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return String.format(FILE_URL_TMPL, StringEscapeUtils
                    .escapeHtml(getClass().getCanonicalName()), getId(),
                    StringEscapeUtils.escapeHtml(getValue()));
        }
    }
}
