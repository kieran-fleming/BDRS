package au.com.gaiaresources.bdrs.model.content;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;

import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;

@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "CONTENT")
@AttributeOverride(name = "id", column = @Column(name = "CONTENT_ID"))
public class Content extends PortalPersistentImpl {
	public static final String HELP_ITEM_MAP = "helpItemMap";
	public static final String HELP_ADD_CLASS = "addClass";
	public static final String HELP_ADD_GROUP = "addGroup";
	public static final String HELP_FIELD_GUIDE = "fieldGuide";
	public static final String HELP_SURVEY_NAME = "addSurveyName";
	public static final String HELP_SURVEY_DATE = "addSurveyDate";
	public static final String HELP_SURVEY_DESCRIPT = "addSurveyDescript";
	public static final String HELP_SURVEY_MAP = "addSurveyMap";
	public static final String HELP_SURVEY_ANIMALS = "addSurveyAnimals";
	public static final String HELP_SURVEY_CLASS = "addSurveyClass";
	public static final String PUBLIC_HOME = "public/home";
	public static final String USER_HOME = "user/home";
	public static final String PUBLIC_ABOUT = "public/about";
	public static final String PUBLIC_HELP = "public/help";
	
    private String key;
    private String value;

    /**
     * {@inheritDoc}
     */
    @Column(name = "KEY")
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * {@inheritDoc}
     */
    @Column(name = "VALUE", columnDefinition="TEXT")
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}
