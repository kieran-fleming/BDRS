package au.com.gaiaresources.bdrs.model.record.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.gaiaresources.bdrs.event.EventPublisher;
import au.com.gaiaresources.bdrs.geometry.GeometryBuilder;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.record.NewRecordEvent;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.record.RecordService;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.user.User;

import com.vividsolutions.jts.geom.Point;

@Service
public class RecordServiceImpl implements RecordService {
    @Autowired
    private RecordDAO recordDAO;
    @Autowired
    private LocationDAO locationDAO;
    @Autowired
    private GeometryBuilder geometryBuilder;
    @Autowired
    private EventPublisher eventPublisher;

    @Override
    public Record createRecord(Location userLocation, IndicatorSpecies species, Date when, Long time,
                               String notes, Boolean firstAppearance, Boolean lastAppearance,
                               String behaviour, String habitat, Integer number,
                               Map<Attribute, Object> attributes)
    {
        Record r = recordDAO.createRecord(userLocation, species, when, time, notes, firstAppearance, lastAppearance,
                                          behaviour, habitat, number, attributes);
        eventPublisher.publish(new NewRecordEvent(r));
        return r;
    }

    @Override
    public void deleteRecord(Integer id) {
    	recordDAO.deleteById(id);
    }

    @Override
    public List<Record> getRecords(User user) {
        return recordDAO.getRecords(user);
    }

    @Override
    public List<Record> getRecords(Location userLocation) {
        return recordDAO.getRecords(userLocation);
    }

    @Override
    public List<Record> getRecords(Location userLocation, Integer extendKms) {
        return recordDAO.getRecords(geometryBuilder.bufferInKm(userLocation.getLocation(), extendKms.doubleValue()));
    }

    @Override
    public List<Record> getRecords(IndicatorSpecies species) {
        return recordDAO.getRecords(species);
    }

    @Override
    public Map<Location, Integer> countLocationRecords(User user) {
        Map<Location, Integer> locations = recordDAO.countLocationRecords(user);
        for (Location l : locationDAO.getUserLocations(user)) {
            if (!locations.keySet().contains(l)) {
                locations.put(l, 0);
            }
        }

        return locations;
    }

    @Override
    public Record getRecord(Integer id) {
        return recordDAO.getRecord(id);
    }

	@Override
	public Record createRecord(Point point, User user,IndicatorSpecies species,
			Date when, Long time, String notes, Boolean firstAppearance,
			Boolean lastAppearance, String behaviour, String habitat,
			Integer number, Map<Attribute, Object> attributes) {
		Record r = recordDAO.createRecord(null, point, user, species, when, time, notes, firstAppearance, lastAppearance,
        behaviour, habitat, number, attributes);
		eventPublisher.publish(new NewRecordEvent(r));
		return r;
	}

	@Override
	public void saveRecord(Record r)
	{
		recordDAO.saveRecord(r);
	}

	@Override
	public void updateRecord(Record r)
	{
		recordDAO.updateRecord(r);
	}

	@Override
	public AttributeValue updateAttribute(Integer id, BigDecimal numeric, String value, Date date) {
		return recordDAO.updateAttribute(id, numeric, value, date);
	}

}
