package au.com.gaiaresources.bdrs.model.grid.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.model.grid.Grid;
import au.com.gaiaresources.bdrs.model.grid.GridDAO;
import au.com.gaiaresources.bdrs.model.grid.GridEntry;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

@Repository
public class GridDAOImpl extends AbstractDAOImpl implements GridDAO {
    @Override
    public GridEntry createGridEntry(Grid grid, Polygon p, IndicatorSpecies s) {
        GridEntry entry = new GridEntry();
        entry.setGrid(grid);
        entry.setBoundary(p);
        entry.setNumberOfRecords(0);
        entry.setSpecies(s);
        return save(entry);
    }

    @Override
    public GridEntry getGridEntry(Grid grid, Point p, IndicatorSpecies s) {
    	return null;
//        return newQueryCriteria(GridEntry.class).add("grid",
//                                                         QueryOperation.EQUAL,
//                                                         grid)
//                .add("boundary", QueryOperation.CONTAINS, p)
//                .add("species", QueryOperation.EQUAL, s).runAndGetFirst();
    }

    @Override
    public List<Grid> getGrids() {
        //return newQueryCriteria(Grid.class).run();
        return new ArrayList<Grid>();
    }

    @Override
    public GridEntry updateGridEntry(GridEntry grid, int numberOfRecords) {
        grid.setNumberOfRecords(numberOfRecords);
        return update(grid);
    }

    @Override
    public List<GridEntry> getGridEntries(Grid g, IndicatorSpecies s) {
    	return new ArrayList<GridEntry>();
//        if (s == null) {
//            return newQueryCriteria(GridEntry.class)
//                    .add("grid", QueryOperation.EQUAL, g).run();
//        } else {
//            return newQueryCriteria(GridEntry.class)
//                    .add("grid", QueryOperation.EQUAL, g)
//         	           .add("species", QueryOperation.EQUAL, s).run();
//        }
    }

    @Override
    public Grid createGrid(BigDecimal precision) {
    	// Please note, this method is never called from a HttpServletContext, therefore it must do it's own transaction management.
        Grid g = new Grid();
        g.setPrecision(precision);
        getSession().beginTransaction();
        Grid r = save(g);
        getSession().getTransaction().commit();
        return r;
    }
}
