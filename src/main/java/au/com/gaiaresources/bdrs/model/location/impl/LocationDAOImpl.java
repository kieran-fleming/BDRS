package au.com.gaiaresources.bdrs.model.location.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.classic.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.QueryOperation;
import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.region.Region;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.db.DeleteCascadeHandler;
import au.com.gaiaresources.bdrs.service.db.DeletionService;

import com.vividsolutions.jts.geom.Point;

/**
 * Implementation of {@link LocationDAO} for dealing with {@link Location}
 * objects.
 * 
 * @author Tim Carpenter
 */

@Repository
public class LocationDAOImpl extends AbstractDAOImpl implements LocationDAO {
    private Logger log = Logger.getLogger(getClass().getName());
    
    @Autowired
    private DeletionService delService;
    
    @PostConstruct
    public void init() throws Exception {
        delService.registerDeleteCascadeHandler(Location.class, new DeleteCascadeHandler() {
            @Override
            public void deleteCascade(PersistentImpl instance) {
                delete((Location)instance);
            }
        });
    }

    @Override
    public Location getLocation(int pk) {
        return getByID(Location.class, pk);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<Location> getLocations(List<Integer> ids) {
    	Query query = getSession().createQuery("FROM Location loc WHERE loc.id IN (:ids)");
    	query.setParameterList("ids", ids);
    	return query.list();
    }

    @Override
    public Location createUserLocation(User user, String locationName,
            Point location, Collection<? extends Region> regions) {
        Location l = new Location();
        l.setUser(user);
        l.setLocation(location);
        l.setName(locationName);
        Set<Region> regionSet = new HashSet<Region>();
        for (Region r : regions) {
            regionSet.add(r);
        }
        l.setRegions(regionSet);
        return save(l);
    }

    @Override
    public Location getUserLocation(User user, String locationName) {
        String query = "from Location loc where loc.user = :user and loc.name = :name";
        Query q = getSession().createQuery(query);
        q.setParameter("user", user);
        q.setParameter("name", locationName);
        List<Location> locList = q.list();
        if (locList.isEmpty()) {
            return null;
        } else {
            if (locList.size() > 1) {
                log.warn(String.format("More than one Location returned for the user \"%s\" with location name \"%s\" returning the first.", user.getName(), locationName));
            }
            return locList.get(0);
        }
    }

    @Override
    public Location getUserLocation(User user, Integer locationID) {
        Location l = getByID(Location.class, locationID);
        if (l.getUser().equals(user)) {
            return l;
        }
        return null;
    }

    @Override
    public List<Location> getUserLocations(User user) {
        return find("from Location l where l.user = ? order by l.name", user);
    }

    @Override
    public List<Location> getUserLocations(User user, Region region) {
        return newQueryCriteria(Location.class).add("user", QueryOperation.EQUAL, user).add("location", QueryOperation.WITHIN, region).addOrderBy("name", true).run();
    }

    @Override
    public Location updateUserLocation(Integer locationID,
            String newLocationName, Point newLocation) {
        Location location = getByID(Location.class, locationID);
        location.setName(newLocationName);
        return update(location);
    }

    @Override
    public Integer countUserLocations(User user) {
        Query q = getSession().createQuery("select count(*) from Location l where l.user = ?");
        q.setParameter(0, user);
        Integer count = Integer.parseInt(q.list().get(0).toString(), 10);
        return count;
    }

    @Override
    public Location createLocation(Location loc) {
        return super.save(loc);
    }

    @Override
    public Location updateLocation(Location loc) {
        Object o = merge(loc);
        update((Location) o);
        return (Location) o;
    }

    @Override
    public Location save(Location loc) {
        if (loc.getId() == null || (loc.getId() != null && loc.getId() <= 0)) {
            return this.createLocation(loc);
        } else {
            return this.updateLocation(loc);
        }
    }

    @Override
    public Location getLocationByName(String surveyName, String locationName) {
        return getLocationByName(null, surveyName, locationName);
    }

    @Override
    public Location getLocationByName(Session sesh, String surveyName,
            String locationName) {
        if(sesh == null) {
            sesh = getSessionFactory().getCurrentSession();
        }
        
        List<Location> locations = find(sesh, "select loc from Survey s, Location loc where loc.id in (select id from s.locations) and s.name = ? and loc.name = ?", new Object[] {
                surveyName, locationName });
        if (locations.isEmpty()) {
            return null;
        } else {
            if (locations.size() > 1) {
                log.warn("Multiple locations with the same name found. Returning the first");

            }
            return locations.get(0);
        }
    }

    @Override
    public void delete(Location delLoc) {
        super.deleteByQuery(delLoc);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Location> getLocation(Survey survey, User user) {
        StringBuilder builder = new StringBuilder();
        builder.append(" select loc ");
        builder.append(" from Survey s left join s.locations loc ");
        builder.append(" where ");
        builder.append("     s = :survey and ");
        builder.append("     loc.user = :user and ");
        builder.append("     (s.public = true or :user in (select u from s.users u))");
        builder.append(" order by loc.name");

        Query q = getSession().createQuery(builder.toString());
        q.setParameter("survey", survey);
        q.setParameter("user", user);

        return q.list();
    }
}
