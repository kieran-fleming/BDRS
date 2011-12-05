package au.com.gaiaresources.bdrs.model.taxa;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import au.com.gaiaresources.bdrs.db.Persistent;

public interface TypedAttributeValue extends Persistent {

	@ManyToOne
	@JoinColumn(name = "ATTRIBUTE_ID", nullable = false, updatable = false)
	/**
	 * Get the attribute definition that this value is for.
	 * @return {@link TaxonGroupAttribute}
	 */
	public Attribute getAttribute();

	/**
	 * Set the attribute
	 * @param attribute to set
	 */
	public void setAttribute(Attribute attribute);

	@Column(name = "NUMERIC_VALUE")
	/**
	 * Get the value as an number, returns a value if and only if the type of
	 * the {@link TaxonGroupAttribute} is integer or decimal.
	 * @return {@link BigDecimal}
	 */
	public BigDecimal getNumericValue();

	public void setNumericValue(BigDecimal numericValue);

	@Column(name = "STRING_VALUE")
	/**
	 * Get the value as a string, returns a value if and only if the type of
	 * the {@link TaxonGroupAttribute} is string.
	 * @return {@link String}
	 */
	public String getStringValue();

	public void setStringValue(String stringValue);

	@Column(name = "DATE_VALUE")
	/**
	 * Get the value as a date, returns a value if and only if the type of
	 * the {@link TaxonGroupAttribute} is string.
	 * @return {@link Date}
	 * @return
	 */
	public Date getDateValue();

	/**
	 * Set the date value
	 * @param dateValue - the new date value
	 * 
	 */
	public void setDateValue(Date dateValue);

	/**
	 * Get the file url
	 * @return a file url String
	 */
	public String getFileURL();
	
	/**
	 * Gets the multi checkbox value(s)
	 * 
	 * @return String[] with the multi checkbox values
	 */
	public String[] getMultiCheckboxValue();
	
	/**
	 * Gets the multi select value(s)
	 * 
	 * @return String[] with the multi select values
	 */
        public String[] getMultiSelectValue();
   
        /**
         * Set the multi checkbox values
         * 
         * @param String[] containing the multi checkbox values
         */
        public void setMultiCheckboxValue(String[] values);
        
        /**
         * Set the multi select values
         * 
         * @param String[] containing the multi select values
         */
        public void setMultiSelectValue(String[] values);
    
        /**
         * Check whether a value is contained in the stored multi checkbox value(s) 
         * 
         * @param val - value to search for
         * @return boolean whether the value exists
         */
        public boolean hasMultiCheckboxValue(String val);
        
        /**
         * Check whether a value is contained in the stored multi select value(s) 
         * 
         * @param val - value to search for
         * @return boolean whether the value exists
         */
        public boolean hasMultiSelectValue(String val);
    
        /**
         * get the boolean typed value
         * @return the stored boolean value
         */
	public Boolean getBooleanValue();
	
	/**
	 * set the boolean value
	 * @param boolean value to store
	 */
	public void setBooleanValue(String value);
	
	/**
	 * Returns true if the attribute value has a valid value to return to the user
	 * @return
	 */
	@Transient
	public boolean isPopulated();
}