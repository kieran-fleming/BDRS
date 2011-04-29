package au.com.gaiaresources.bdrs.service.bulkdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.gaiaresources.bdrs.model.group.Group;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.user.User;

/**
 * Represents a link between uploaded spreadsheet data and existing persisted
 * objects.
 */
public class BulkUpload {

    // Group Name : Group
    private Map<String, Group> groupMap;
    private Set<String> groupNameSet;

    // Username : User
    private Map<String, User> userMap;
    private Set<String> usernameSet;

    // Location Name : Location
    private Map<LocationUpload, Location> locationMap;
    private Set<LocationUpload> locationUploadSet;

    // Survey Name : Survey
    private Map<String, Survey> surveyMap;
    private Set<String> surveyNameSet;

    // Indicator Species
    private Map<String, IndicatorSpecies> indicatorSpeciesScientificNameMap;
    private Set<String> indicatorSpeciesScientificNameSet;

    private Map<String, IndicatorSpecies> indicatorSpeciesCommonNameMap;
    private Set<String> indicatorSpeciesCommonNameSet;

    private List<RecordUpload> recordUploadList;
    private List<RecordUpload> errorRecordUploadList;

    private List<String> missingGroups = Collections.emptyList();
    private List<String> missingUsers = Collections.emptyList();
    private List<String> missingSurveys = Collections.emptyList();
    private List<LocationUpload> missingLocations = Collections.emptyList();
    private List<String> missingIndicatorSpecies = Collections.emptyList();
    
    private Map<IndicatorSpecies, Survey> invalidSurveySpecies = new HashMap<IndicatorSpecies, Survey>(); 

    public BulkUpload() {
        // Group Name
        groupMap = new HashMap<String, Group>();
        groupNameSet = new HashSet<String>();

        // Username
        userMap = new HashMap<String, User>();
        usernameSet = new HashSet<String>();

        // Location Name
        locationMap = new HashMap<LocationUpload, Location>();
        locationUploadSet = new HashSet<LocationUpload>();

        // Survey Name
        surveyMap = new HashMap<String, Survey>();
        surveyNameSet = new HashSet<String>();

        // Indicator Species
        indicatorSpeciesScientificNameMap = new HashMap<String, IndicatorSpecies>();
        indicatorSpeciesScientificNameSet = new HashSet<String>();

        indicatorSpeciesCommonNameMap = new HashMap<String, IndicatorSpecies>();
        indicatorSpeciesCommonNameSet = new HashSet<String>();

        recordUploadList = new ArrayList<RecordUpload>();
        errorRecordUploadList = new ArrayList<RecordUpload>();
    }

    // ----- Group

    public void addGroup(Group group) {
        groupMap.put(group.getName(), group);
    }

    public Group getGroupByName(String groupName) {
        return groupMap.get(groupName);
    }

    public void addGroupName(String groupName) {
        groupNameSet.add(groupName);
    }

    public Set<String> getGroupNames() {
        return Collections.unmodifiableSet(groupNameSet);
    }

    // ----- User

    public void addUsername(String username) {
        usernameSet.add(username);
    }

    public User getUserByUsername(String username) {
        return userMap.get(username);
    }

    public void addUser(User user) {
        userMap.put(user.getName(), user);
    }

    public Set<String> getUsernames() {
        return Collections.unmodifiableSet(usernameSet);
    }

    // ----- Location

    public void addLocationUpload(LocationUpload loc) {
        locationUploadSet.add(loc);
    }

    public Location getLocationByLocationUpload(LocationUpload locationUpload) {
        return locationMap.get(locationUpload);
    }

    public void addLocation(LocationUpload key, Location loc) {
        locationMap.put(key, loc);
    }

    public Set<LocationUpload> getLocationUploads() {
        return Collections.unmodifiableSet(locationUploadSet);
    }

    // ----- Survey

    public void addSurveyName(String surveyName) {
        surveyNameSet.add(surveyName);
    }

    public Survey getSurveyByName(String surveyName) {
        return surveyMap.get(surveyName);
    }

    public void addSurvey(Survey survey) {
        surveyMap.put(survey.getName(), survey);
    }

    public Set<String> getSurveyNames() {
        return Collections.unmodifiableSet(surveyNameSet);
    }

    // ----- Indicator Species

    public void addIndicatorSpecies(IndicatorSpecies indicatorSpecies) {
        indicatorSpeciesScientificNameMap.put(indicatorSpecies.getScientificName(), indicatorSpecies);
        indicatorSpeciesCommonNameMap.put(indicatorSpecies.getCommonName(), indicatorSpecies);
    }

    // Scientific Name
    public void addIndicatorSpeciesScientificName(String scientificName) {
        indicatorSpeciesScientificNameSet.add(scientificName);
    }

    public IndicatorSpecies getIndicatorSpeciesByScientificName(
            String scientificName) {
        return indicatorSpeciesScientificNameMap.get(scientificName);
    }

    public Set<String> getIndicatorSpeciesScientificName() {
        return Collections.unmodifiableSet(indicatorSpeciesScientificNameSet);
    }

    // Common Name
    public void addIndicatorSpeciesCommonName(String commonName) {
        indicatorSpeciesCommonNameSet.add(commonName);
    }

    public IndicatorSpecies getIndicatorSpeciesByCommonName(String commonName) {
        return indicatorSpeciesCommonNameMap.get(commonName);
    }

    public Set<String> getIndicatorSpeciesCommonName() {
        return Collections.unmodifiableSet(indicatorSpeciesCommonNameSet);
    }

    // ----- RecordUpload

    public boolean hasError() {
        return !errorRecordUploadList.isEmpty();
    }

    public int getErrorCount() {
        return errorRecordUploadList.size();
    }

    public void addRecordUpload(RecordUpload recordUpload) {
        if (recordUpload.isError()) {
            errorRecordUploadList.add(recordUpload);
        } else {
            recordUploadList.add(recordUpload);

            if (recordUpload.getClassName() != null) {
                groupNameSet.add(recordUpload.getClassName());
            }
            if (recordUpload.getGroupName() != null) {
                groupNameSet.add(recordUpload.getGroupName());
            }
            if (recordUpload.getRecordedByUsername() != null) {
                usernameSet.add(recordUpload.getRecordedByUsername());
            }
            if (recordUpload.getLocationUpload() != null) {
                locationUploadSet.add(recordUpload.getLocationUpload());
            }
            if (recordUpload.getSurveyName() != null) {
                surveyNameSet.add(recordUpload.getSurveyName());
            }
            if (recordUpload.getScientificName() != null
                    && !recordUpload.getScientificName().isEmpty()) {
                indicatorSpeciesScientificNameSet.add(recordUpload.getScientificName());
            } else if (recordUpload.getCommonName() != null
                    && !recordUpload.getCommonName().isEmpty()) {
                indicatorSpeciesCommonNameSet.add(recordUpload.getCommonName());
            }
        }
    }

    public List<RecordUpload> getRecordUploadList() {
        return recordUploadList;
    }

    public void setRecordUploadList(List<RecordUpload> recordUploadList) {
        throw new UnsupportedOperationException();
    }

    public List<RecordUpload> getErrorRecordUploadList() {
        return errorRecordUploadList;
    }

    public void setErrorRecordUploadList(
            List<RecordUpload> errorRecordUploadList) {
        throw new UnsupportedOperationException();
    }

    // ----- Missing Data

    public List<String> getMissingGroups() {
        return missingGroups;
    }

    public void setMissingGroups(List<String> missingGroups) {
        this.missingGroups = missingGroups;
    }

    public List<String> getMissingUsers() {
        return missingUsers;
    }

    public void setMissingUsers(List<String> missingUsers) {
        this.missingUsers = missingUsers;
    }

    public List<String> getMissingSurveys() {
        return missingSurveys;
    }

    public void setMissingSurveys(List<String> missingSurveys) {
        this.missingSurveys = missingSurveys;
    }

    public List<LocationUpload> getMissingLocations() {
        return missingLocations;
    }

    public void setMissingLocations(List<LocationUpload> missingLocations) {
        this.missingLocations = missingLocations;
    }

    public List<String> getMissingIndicatorSpecies() {
        return missingIndicatorSpecies;
    }

    public void setMissingIndicatorSpecies(List<String> missingIndicatorSpecies) {
        this.missingIndicatorSpecies = missingIndicatorSpecies;
    }
    
    public boolean hasInvalidSurveySpecies() {
        return !this.invalidSurveySpecies.isEmpty();
    }
    
    public Map<IndicatorSpecies, Survey> getInvalidSurveySpecies() {
        return invalidSurveySpecies;
    }

    public void setInvalidSurveySpecies(
            Map<IndicatorSpecies, Survey> invalidSurveySpecies) {
        this.invalidSurveySpecies = invalidSurveySpecies;
    }

    public boolean isMissingData() {
        return !missingGroups.isEmpty() || !missingUsers.isEmpty()
                || !missingSurveys.isEmpty() || !missingLocations.isEmpty()
                || !missingIndicatorSpecies.isEmpty();
    }  
}