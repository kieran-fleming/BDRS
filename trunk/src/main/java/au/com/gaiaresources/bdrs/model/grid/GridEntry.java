package au.com.gaiaresources.bdrs.model.grid;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Type;

import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;

import com.vividsolutions.jts.geom.Polygon;

@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Table(name = "GRID_ENTRY")
@AttributeOverride(name = "id", column = @Column(name = "GRID_ENTRY_ID"))
public class GridEntry extends PortalPersistentImpl {
    private Polygon boundary;
    private int numberOfRecords;
    private Grid grid;
    private IndicatorSpecies species;

    @Column(name = "BOUNDARY")
    @Type(type = "org.hibernatespatial.GeometryUserType")
    public Polygon getBoundary() {
        return boundary;
    }

    public void setBoundary(Polygon boundary) {
        this.boundary = boundary;
    }

    @Column(name = "NUMBER_OF_RECORDS")
    public int getNumberOfRecords() {
        return numberOfRecords;
    }

    public void setNumberOfRecords(int numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
    }

    @ManyToOne
    @JoinColumn(name = "GRID_ID", nullable = false)
    public Grid getGrid() {
        return grid;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    @ManyToOne
    @JoinColumn(name = "INDICATOR_SPECIES_ID", nullable = false)
    public IndicatorSpecies getSpecies() {
        return species;
    }

    public void setSpecies(IndicatorSpecies species) {
        this.species = species;
    }

}
