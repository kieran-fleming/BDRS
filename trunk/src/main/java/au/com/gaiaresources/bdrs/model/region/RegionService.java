package au.com.gaiaresources.bdrs.model.region;

import java.math.BigDecimal;
import java.util.List;

public interface RegionService {
    /**
     * Create a region.
     * @param name The name of the region.
     * @param points A <code>List</code> of two dimensional arrays.
     * @return <code>Region</code>.
     */
    Region createRegion(String name, List<BigDecimal[]> points);
    
    Region updateRegion(Integer id, String name, List<BigDecimal[]> points);
    
    /**
     * Get all regions.
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
}
