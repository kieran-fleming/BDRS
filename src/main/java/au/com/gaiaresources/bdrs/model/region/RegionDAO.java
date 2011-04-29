package au.com.gaiaresources.bdrs.model.region;

import java.util.List;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

public interface RegionDAO {
    /**
     * Create a region.
     * @param name The name of the region.
     * @param polygon The shape of the region, JTS <code>Polygon</code>.
     * @return <code>Region</code>.
     */
    Region createRegion(String name, MultiPolygon polygon);
    
    /**
     * Update a region.
     * @param id The id of the region.
     * @param newName The new name of the region.
     * @param newShape The new shape of the region.
     * @return <code>Region</code>.
     */
    Region updateRegion(Integer id, String newName, MultiPolygon newShape);
    
    /**
     * Get all regions that have been defined.
     * @return <code>List</code> of <code>Region</code>s.
     */
    List<Region> getRegions();
    
    /**
     * Get a region by id.
     * @param id <code>Integer</code>.
     * @return <code>Region</code>.
     */
    Region getRegion(Integer id);
    
    /**
     * Get a region by name.
     * @param name <code>String</code>.
     * @return <code>Region</code>.
     */
    Region getRegion(String name);
    
    /**
     * Get the regions with the given names.
     * @param names <code>String[]</code>.
     * @return <code>List</code>.
     */
    List<Region> getRegions(String ... names);
    
    /**
     * Find the region(s) that covers the given coordinate.
     * @param coordinate JTS <code>Point</code>.
     * @return <code>List</code> of <code>Region</code>.
     */
    List<Region> findRegions(Point point);
}
