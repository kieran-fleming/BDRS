/**
 * 
 */
package au.com.gaiaresources.bdrs.model.attribute;

import java.util.Set;

import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;


/**
 * @author stephanie
 *
 */
public interface Attributable<T extends TypedAttributeValue> {
    public Set<T> getAttributes();
    public T createAttribute();
    public void setAttributes(Set<T> attributes);
}
