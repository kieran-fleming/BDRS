package au.com.gaiaresources.bdrs.model.record;

import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.user.User;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Point;

/**
 * Service for dealing with records.
 * @author Tim Carpenter
 */
public interface RecordService {
    /**
     * Create a new record.
     * @param userLocation {@link Location}
     * @param speices {@link IndicatorSpecies}
     * @param when {@link Date}
     * @param time {@link Long}
     * @param notes {@link String}
     * @param attributes {@link Map} of {@link Attribute} to {@link Object}, it is expected that the
     * object value of the attribute is already in the correct data type for the attribute.
     * @return {@link Record}
     */
    Record createRecord(Location userLocation, IndicatorSpecies speices, Date when, Long time,
                        String notes, Boolean firstAppearance, Boolean lastAppearance,
                        String behaviour, String habitat, Integer number,
                        Map<Attribute, Object> attributes);

    void deleteRecord(Integer id);


    /**
     * Create a new record.
     * @param point {@link Point}
     * @param user {@link User}
     * @param speices {@link IndicatorSpecies}
     * @param when {@link Date}
     * @param time {@link Long}
     * @param notes {@link String}
     * @param attributes {@link Map} of {@link Attribute} to {@link Object}, it is expected that the
     * object value of the attribute is already in the correct data type for the attribute.
     * @return {@link Record}
     */
    Record createRecord(Point point, User user, IndicatorSpecies species, Date when, Long time,
                        String notes, Boolean firstAppearance, Boolean lastAppearance,
                        String behaviour, String habitat, Integer number,
                        Map<Attribute, Object> attributes);

    /**
     * Get all records that have been created by the given user.
     * @param user {@link User}
     * @return {@link List} of {@link Record}
     */
    List<Record> getRecords(User user);

    /**
     * Get all records that have been entered for the given location.
     * @param userLocation {@link Location}
     * @return {@link List} of {@link Record}
     */
    List<Record> getRecords(Location userLocation);

    /**
     * Get all records in a given radius around a location.
     * @param userLocation {@link Location}
     * @param extendKms The radius in km around the location to get records for.
     * @return {@link List} of {@link Record}
     */
    List<Record> getRecords(Location userLocation, Integer extendKms);

    /**
     * Get all records that have been entered for a species.
     * @param species {@link IndicatorSpecies}
     * @return {@link List} of {@link Record}
     */
    List<Record> getRecords(IndicatorSpecies species);

    /**
     * Count the number of records for locations entered by a user. If a location has not been
     * used yet, it is still returned in the map.
     * @param user {@link User}
     * @return {@link Map}
     */
    Map<Location, Integer> countLocationRecords(User user);

    Record getRecord(Integer id);

    void saveRecord(Record r);

    void updateRecord(Record r);

    TypedAttributeValue updateAttribute(Integer id, BigDecimal numeric, String value, Date date);
}
