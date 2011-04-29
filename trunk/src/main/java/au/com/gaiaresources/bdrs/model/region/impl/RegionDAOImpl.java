package au.com.gaiaresources.bdrs.model.region.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.QueryOperation;
import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.model.region.Region;
import au.com.gaiaresources.bdrs.model.region.RegionDAO;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

@Repository
public class RegionDAOImpl extends AbstractDAOImpl implements RegionDAO {
    /**
     * {@inheritDoc}
     */
    @Override
    public Region createRegion(String name, MultiPolygon polygon) {
        Region region = new Region();
        region.setRegionName(name);
        region.setBoundary(polygon);
        return save(region);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Region> getRegions() {
        return newQueryCriteria(Region.class)
                        .addOrderBy("regionName", true)
                        .run();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Region updateRegion(Integer id, String newName, MultiPolygon newShape) {
        Region r = getRegion(id);
        r.setRegionName(newName);
        r.setBoundary(newShape);
        return update(r);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Region getRegion(Integer id) {
        return getByID(Region.class, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Region getRegion(String name) {
        return newQueryCriteria(Region.class)
                        .add("regionName", QueryOperation.EQUAL, name)
                        .runAndGetFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Region> getRegions(String ... names) {
        return newQueryCriteria(Region.class)
                        .add("regionName", QueryOperation.IN, (Object[]) names)
                        .run();
    }

    /**
     * {@inheritDoc}
     */
    public List<Region> findRegions(Point point) {
        return newQueryCriteria(Region.class)
                        .add("boundary", QueryOperation.CONTAINS, point)
                        .run();
    }
}
