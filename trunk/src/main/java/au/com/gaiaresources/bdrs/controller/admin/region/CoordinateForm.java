package au.com.gaiaresources.bdrs.controller.admin.region;

import java.math.BigDecimal;

/**
 * Command object for entering a coordinate.
 * @author Tim Carpenter
 */
public class CoordinateForm {
    // CHECKSTYLE_OFF: MagicNumber
    private BigDecimal latitude;
    private BigDecimal longitude;
    // CHECKSTYLE_ON: MagicNumber
    
    /**
     * Get the latitude of this instance.
     * @return <code>BigDecimal</code>.
     */
    public BigDecimal getLatitude() {
        return latitude;
    }
    /**
     * Set the latitude.
     * @param latitude <code>BigDecimal</code>.
     */
    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }
    /**
     * Get the longitude of this instance.
     * @return <code>BigDecimal</code>.
     */
    public BigDecimal getLongitude() {
        return longitude;
    }
    /**
     * Set the longitude.
     * @param longitude <code>BigDecimal</code>.
     */
    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }
}
