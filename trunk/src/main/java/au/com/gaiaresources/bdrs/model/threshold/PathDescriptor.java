package au.com.gaiaresources.bdrs.model.threshold;

import java.beans.PropertyDescriptor;

/**
 * Describes a navigable path between two objects. The <code>propertyPath</code>
 * is a '.' delimited series of property names between the two objects. The 
 * property descriptor describes the property at the end of the property path.
 */
public class PathDescriptor {
    private String propertyPath;
    private PropertyDescriptor propertyDescriptor;

    /**
     * Creates a new <code>PathDescriptor</code>.
     * @param propertyPath the path to the property.
     * @param propertyDescriptor describes the property at the end of the path.
     */
    public PathDescriptor(String propertyPath,
            PropertyDescriptor propertyDescriptor) {
        super();
        this.propertyPath = propertyPath;
        this.propertyDescriptor = propertyDescriptor;
    }

    public String getPropertyPath() {
        return propertyPath;
    }

    public PropertyDescriptor getPropertyDescriptor() {
        return propertyDescriptor;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((propertyDescriptor == null) ? 0
                        : propertyDescriptor.hashCode());
        result = prime * result
                + ((propertyPath == null) ? 0 : propertyPath.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PathDescriptor other = (PathDescriptor) obj;
        if (propertyDescriptor == null) {
            if (other.propertyDescriptor != null)
                return false;
        } else if (!propertyDescriptor.equals(other.propertyDescriptor))
            return false;
        if (propertyPath == null) {
            if (other.propertyPath != null)
                return false;
        } else if (!propertyPath.equals(other.propertyPath))
            return false;
        return true;
    }
}