package au.com.gaiaresources.bdrs.model.region.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.gaiaresources.bdrs.model.region.Region;
import au.com.gaiaresources.bdrs.model.region.RegionDAO;
import au.com.gaiaresources.bdrs.model.region.RegionService;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

@Service
public class RegionServiceImpl implements RegionService {
    @Autowired
    private RegionDAO regionDAO;
    
    private GeometryFactory geometryFactory;
    
    public RegionServiceImpl() {
        geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    }
    
    @Override
    public Region createRegion(String name, List<BigDecimal[]> points) {
        return regionDAO.createRegion(name, constructPolygon(points));
    }
    
    public Region updateRegion(Integer id, String newName, List<BigDecimal[]> points) {
        return regionDAO.updateRegion(id, newName, constructPolygon(points));
    }
    
    private MultiPolygon constructPolygon(List<BigDecimal[]> points) {
        List<Coordinate> coords = new ArrayList<Coordinate>();
        for (BigDecimal[] point : points) {
            coords.add(new Coordinate(point[0].doubleValue(), point[1].doubleValue()));
        }
        // Check if we received a closed ring
        if (!coords.get(0).equals2D(coords.get(coords.size() - 1))) {
            coords.add(coords.get(0));
        }
        LinearRing ring = geometryFactory.createLinearRing(coords.toArray(new Coordinate[coords.size()]));
        
        Polygon[] polys = new Polygon[1];
        polys[0] = geometryFactory.createPolygon(ring, null);
        
        return geometryFactory.createMultiPolygon(polys);
    }

    @Override
    public List<Region> getRegions() {
        return regionDAO.getRegions();
    }

    @Override
    public Region getRegion(Integer id) {
        return regionDAO.getRegion(id);
    }

    @Override
    public Region getRegion(String name) {
        return regionDAO.getRegion(name);
    }
    
    @Override
    public List<Region> getRegions(String ... names) {
        return regionDAO.getRegions(names);
    }
}
