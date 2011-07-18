package au.com.gaiaresources.bdrs.model.taxa;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

public interface TypedAttributeValue {

	@ManyToOne
	@JoinColumn(name = "ATTRIBUTE_ID", nullable = false, updatable = false)
	/**
	 * Get the attribute definition that this value is for.
	 * @return {@link TaxonGroupAttribute}
	 */
	public Attribute getAttribute();

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

	public void setDateValue(Date dateValue);

	public String getFileURL();
}