package au.com.gaiaresources.bdrs.model.grid;

import java.math.BigDecimal;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;

@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Table(name = "GRID")
@AttributeOverride(name = "id", column = @Column(name = "GRID_ID"))
public class Grid extends PortalPersistentImpl {
    private BigDecimal precision;

    @Column(name = "GRID_PRECISION", nullable = false)
    public BigDecimal getPrecision() {
        return precision;
    }

    public void setPrecision(BigDecimal precision) {
        this.precision = precision;
    }

    /**
     * toString.
     *
     * @return <code>String</code>.
     */
    public String toString() {
        return "GridImpl [precision: " + precision.toPlainString() + "]";
    }
}
