package au.com.gaiaresources.bdrs.spatial;

import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Currently this is used to help in writing shapefiles. we could also use it for reading
 * shapefiles so the process is symmetrical. In such a case I recommend ShapeFileReader
 * returns an array of these objects. We might want to add a member detailing
 * the shapefile this ShapefileFeature came from as well.
 * 
 * @author aaron
 *
 */
public class ShapefileFeature {
    
    private Geometry geom;
    private Map<String, Object> attributes;
    
    public ShapefileFeature(Geometry geom, Map<String, Object> attributes) {
        this.geom = geom;
        this.attributes = attributes;
    }
    
    public Geometry getGeometry() {
        return geom;
    }
    
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
