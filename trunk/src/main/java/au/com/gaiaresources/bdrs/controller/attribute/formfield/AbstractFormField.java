package au.com.gaiaresources.bdrs.controller.attribute.formfield;

public abstract class AbstractFormField  implements FormField  {

	private String prefix;
	
	public AbstractFormField(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAttributeFormField() {
	    return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPropertyFormField() {
	    return false;
	}

	public String getPrefix() {
	    return prefix;
	}

	public void setPrefix(String prefix) {
	    this.prefix = prefix;
	}
}