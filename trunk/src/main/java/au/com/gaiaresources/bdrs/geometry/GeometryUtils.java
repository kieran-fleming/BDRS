package au.com.gaiaresources.bdrs.geometry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class GeometryUtils {
    public static double findMinX(Geometry g) {
        Double minX = null;
        for (Coordinate c : g.getCoordinates()) {
            if (minX == null || c.x < minX.doubleValue()) {
                minX = c.x;
            }
        }
        return minX;
    }
    
    public static double findMaxX(Geometry g) {
        Double maxX = null;
        for (Coordinate c : g.getCoordinates()) {
            if (maxX == null || c.x > maxX.doubleValue()) {
                maxX = c.x;
            }
        }
        return maxX;
    }
    
    public static double findMinY(Geometry g) {
        Double minY = null;
        for (Coordinate c : g.getCoordinates()) {
            if (minY == null || c.y < minY.doubleValue()) {
                minY = c.y;
            }
        }
        return minY;
    }
    
    public static double findMaxY(Geometry g) {
        Double maxY = null;
        for (Coordinate c : g.getCoordinates()) {
            if (maxY == null || c.y > maxY.doubleValue()) {
                maxY = c.y;
            }
        }
        return maxY;
    }
}
