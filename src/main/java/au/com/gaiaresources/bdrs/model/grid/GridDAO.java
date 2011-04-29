package au.com.gaiaresources.bdrs.model.grid;

import java.math.BigDecimal;
import java.util.List;

import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public interface GridDAO {
    List<? extends Grid> getGrids();
    
    GridEntry getGridEntry(Grid grid, Point p, IndicatorSpecies s);
    
    GridEntry createGridEntry(Grid grid, Polygon p, IndicatorSpecies s);
    
    GridEntry updateGridEntry(GridEntry grid, int numberOfRecords);
    
    List<? extends GridEntry> getGridEntries(Grid grid, IndicatorSpecies s);
    
    Grid createGrid(BigDecimal precision);
}
