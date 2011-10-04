package au.com.gaiaresources.bdrs.spatial;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.ShapefileFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import au.com.gaiaresources.bdrs.attribute.AttributeDictionaryFactory;
import au.com.gaiaresources.bdrs.config.AppContext;
import au.com.gaiaresources.bdrs.deserialization.record.RecordKeyLookup;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.record.AccessControlledRecordAdapter;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.template.TemplateService;
import au.com.gaiaresources.bdrs.util.FileUtils;
import au.com.gaiaresources.bdrs.util.ZipUtils;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;


public class ShapeFileWriter {
        
    Logger log = Logger.getLogger(getClass());
    
    private static final String FIELD_DESCRIPTION_FILE = "fieldDescriptions.txt";
    private static final String FIELD_DESCRIPTION_FILE_DELIM = " : ";
    
    public static final String HELPER_FILE = "DONT_EDIT_ME.dat";
    public static final String SURVEY_ID_KEY = "survey_id";
    public static final String CENSUS_METHOD_ID_KEY = "census_method_id";
    public static final String HELPER_FILE_SEPARATOR = "=";
    public static final String ID_DELIM = ",";
    public static final String NEWLINE = "\r\n";
    private static final String METADATA_FILE = "metadata.xml";
    
    public static final String ATTR_ENTRY_PREFIX = "attr:";
    public static final String ATTR_ENTRY_DELIM = ",";
    
    private static final String METADATA_TEMPLATE_FILENAME = "shp_metadata_template.xml";
    
    public static final String KEY_RECORD_OWNER = "rec_owner";
    
    private static final SimpleDateFormat shpDateFormat = new SimpleDateFormat("dd MMM yyyy");
    
    private SimpleDateFormat metadataDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    
    private TemplateService templateService = AppContext.getBean(TemplateService.class);

    public File exportRecords(List<Record> recList, User accessor) throws Exception {
        
        Set<Survey> surveySet = new HashSet<Survey>();
        Set<CensusMethod> cmSet = new HashSet<CensusMethod>();
        Set<ShapefileType> shapefileTypeSet = new HashSet<ShapefileType>();
        
        for (Record r : recList) {
            if (r.getSurvey() != null) {
                surveySet.add(r.getSurvey());
            }
            if (r.getCensusMethod() != null) {
                cmSet.add(r.getCensusMethod());
            }
            try {
                shapefileTypeSet.add(getShapefileTypefromGeometry(r.getGeometry()));    
            } catch (IllegalStateException ise) {
                // ignore the error
                log.error("Error collecting shapefile types in record list", ise);
            }
        }
        
        List<Survey> surveyList = new ArrayList<Survey>(surveySet.size());
        surveyList.addAll(surveySet);
        List<CensusMethod> cmList = new ArrayList<CensusMethod>(cmSet.size());
        cmList.addAll(cmSet);
        return createZipShapefile(surveyList, cmList, shapefileTypeSet, recList, accessor);
    }
    
    /**
     * Create a zip file containing .shp, .dbf, .prj, .shx files. 
     * 
     * @param name
     * @param survey
     * @param cm
     * @throws Exception 
     */
    public File createZipShapefile(Survey survey, CensusMethod cm, ShapefileType shapefileType) throws Exception {
        if (survey.getId() == null) {
            throw new IllegalArgumentException("survey cannot have a null id");
        }
        if (cm != null && cm.getId() == null) {
            throw new IllegalArgumentException("cm cannot have a null id");
        }
        if (shapefileType == null) {
            throw new IllegalArgumentException("ShapefileType, shapefileType cannot have be null");
        }
        
        List<Survey> surveyList = new ArrayList<Survey>(1);
        surveyList.add(survey);
        List<CensusMethod> cmList = new ArrayList<CensusMethod>(1);
        if (cm != null) {
            cmList.add(cm);
        }
        Set<ShapefileType> shapefileTypeSet = new HashSet<ShapefileType>(1);
        shapefileTypeSet.add(shapefileType);
        
        return createZipShapefile(surveyList, cmList, shapefileTypeSet, Collections.EMPTY_LIST, null);
    }
    
    public File createZipShapefile(List<Survey> surveyList, List<CensusMethod> cmList, 
            Set<ShapefileType> shapefileTypeSet, List<Record> recList, User accessor) throws Exception {
        
        if (shapefileTypeSet.isEmpty()) {
            throw new IllegalArgumentException("Set<ShapefileType>, shapefileTypeSet, must have at least one item in it.");
        }
        
        boolean hasRecords = !recList.isEmpty();
        
        // we sort by database id's to achieve a deterministic order.
        Collections.sort(surveyList);
        Collections.sort(cmList);
       
        if (surveyList == null) {
            surveyList = Collections.EMPTY_LIST;
        }
        if (cmList == null) {
            cmList = Collections.EMPTY_LIST;
        }
        
        boolean taxonomic = false;
        if (cmList.isEmpty()) {
            // if there are no census methods, we default to taxonomic
            taxonomic = true;
        } else {
            // even if one of the census methods are taxonomic, we must display the taxonomic related fields.
            for (CensusMethod cm : cmList) {
                if (cm == null || cm.getTaxonomic() == Taxonomic.TAXONOMIC || cm.getTaxonomic() == Taxonomic.OPTIONALLYTAXONOMIC) {
                    taxonomic = true;
                    break;
                }
            }
            // yes we are spinning through this list yet again. I preferred it to
            // passing yet another parameter into this method
            for (Record rec : recList) {
                if (rec.getCensusMethod() == null) {
                    taxonomic = true;
                    break;
                }
            }
        }
        
        AttributeDictionaryFactory attrDictFact = new ShapefileAttributeDictionaryFactory();
        RecordKeyLookup klu = new ShapefileRecordKeyLookup();
        Map<Attribute, String> attrNameMap = attrDictFact.createNameKeyDictionary(surveyList, null, cmList);
        
        Map<ShapefileType, ShapeFileWriterContext> contextMap = new HashMap<ShapefileType, ShapeFileWriterContext>();
        Map<ShapefileType, ShapefileDataStore> datastoreMap = new HashMap<ShapefileType, ShapefileDataStore>();
        Map<ShapefileType, List<ShapefileFeature>> writeFeatureMap = new HashMap<ShapefileType, List<ShapefileFeature>>();
        
        for (ShapefileType shpType : shapefileTypeSet) {
            contextMap.put(shpType, new ShapeFileWriterContext(shpType, surveyList, cmList));
            writeFeatureMap.put(shpType, new LinkedList<ShapefileFeature>());
        }
        
        String baseFilename;
        if (recList.isEmpty()) {
            // is a record import template...
            baseFilename = "record_import_template";
        } else {
            // is a record export
            baseFilename = "record_export";
        }
        File tempdir = FileUtils.createTempDirectory("createShp");
        
        
        ShapeFileWriterContext contextForDescriptions = null;
        // 1 shape file for each output shapefile type
        // at least one geom type is guaranteed. see argument checks at top of method.
        for (Entry<ShapefileType, ShapeFileWriterContext> entry : contextMap.entrySet()) {
            
            // Not the greatest way to do it. I don't care which 
            contextForDescriptions = entry.getValue();
            
            ShapefileType shpType = entry.getKey();
            ShapeFileWriterContext context = entry.getValue();
            
            context.addInt(klu.getRecordIdKey(), null, "The record ID - leave blank if this is a new record");
            
            if (surveyList.size() > 1) {
                context.addInt(klu.getSurveyIdKey(), null, "The survey ID that the record is for. Can leave blank if there is only 1 survey associated with this shapefile as all records by default will be for this single survey.");
            }
            if (cmList.size() > 1) {
                context.addInt(klu.getCensusMethodIdKey(), null, "The census method ID that applies to this record. Can leave blank if there is only 1 census method associated with this shapefile as all records by default will apply this single census method.");
            }
            if (hasRecords) {
                context.addString(KEY_RECORD_OWNER, null, "The owner of the record. This field is non editable");
            }
            
            context.addDouble(klu.getAccuracyKey(), null, "The estimated accuracy in meters of the sighting");
            
            // DwC fields - position/location is unneeded of course
            if (taxonomic) {
                context.addString(klu.getSpeciesIdKey(), null, "The BDRS species ID key");
                context.addString(klu.getSpeciesNameKey(), null, "A scientific or common name describing the species to be recorded");
                context.addInt(klu.getIndividualCountKey(), null, "The number of individuals of the species sighted");
            }
            
            context.addDate(klu.getDateKey(), null, "The date of the recording");
            context.addString(klu.getTimeKey(), null, "The time of the recording");
            
            context.addString(klu.getNotesKey(), null, "Any additional notes about the recording");
            // end DwC Fields...
            
            // Write dynamic attributes
            writeAttributes(context, attrNameMap);
            
            SimpleFeatureType myFeatureType = context.getBuilder().buildFeatureType();
        
            String filename = getShpFilename(hasRecords, shpType);
            File shp = FileUtils.createFileInDir(tempdir, filename + ".shp");    
            
            ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

            Map<String, Serializable> params = new HashMap<String, Serializable>();
            params.put("url", shp.toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);
            
           
            ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
            
            newDataStore.createSchema(myFeatureType);
            
            datastoreMap.put(entry.getKey(), newDataStore);
            
            /*
             * You can comment out this line if you are using the createFeatureType
             * method (at end of class file) rather than DataUtilities.createType
             */
            newDataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);
        }
        
        // write records, build up write maps
        for (Record rec : recList) {
            // will throw an exception if we find a geometry we do not expect...
            try {
                ShapefileType recordShpType = getShapefileTypefromGeometry(rec.getGeometry());
                // only process the record if it is one of the requested geometry types.
                if (shapefileTypeSet.contains(recordShpType)) {
                    Map<String, Object> featureAttr = new LinkedHashMap<String, Object>();
                    
                    AccessControlledRecordAdapter recAdapter = new AccessControlledRecordAdapter(rec, accessor);
                    
                    // add record data here!
                    featureAttr.put(klu.getRecordIdKey(), recAdapter.getId());
                    
                    featureAttr.put(KEY_RECORD_OWNER, recAdapter.getUser().getFirstName() + " " + recAdapter.getUser().getLastName());
                    
                    if (recAdapter.getNumber() != null) {
                        featureAttr.put(klu.getIndividualCountKey(), recAdapter.getNumber());
                    }
                    
                    if (recAdapter.getWhen() != null) {
                        featureAttr.put(klu.getDateKey(), shpDateFormat.format(recAdapter.getWhen()));
                    }
                    
                    if (recAdapter.getNotes() != null) {
                        featureAttr.put(klu.getNotesKey(), recAdapter.getNotes());
                    }
                    
                    if (recAdapter.getAccuracyInMeters() != null) {
                        featureAttr.put(klu.getAccuracyKey(), recAdapter.getAccuracyInMeters());
                    }
                    
                    if (recAdapter.getSpecies() != null) {
                        featureAttr.put(klu.getSpeciesNameKey(), recAdapter.getSpecies().getScientificName());
                    }
                    if (recAdapter.getNumber() != null) {
                        featureAttr.put(klu.getIndividualCountKey(), recAdapter.getNumber());
                    }
                    
                    writeAttributeValues(recAdapter.getAttributes(), attrNameMap, featureAttr);
                    
                    ShapefileFeature feature = new ShapefileFeature(recAdapter.getGeometry(), featureAttr);
                    writeFeatureMap.get(recordShpType).add(feature);
                }
            } catch (IllegalStateException ise) {
                // ignore the record, log the error.
                log.error("Error processing a record for export to shape file.", ise);
            }
        }
        
        for (ShapefileType shpType : shapefileTypeSet) {
            writeFeatures(datastoreMap.get(shpType), writeFeatureMap.get(shpType));
        }
        
        File outfile = new File(tempdir, baseFilename + ".zip");

        // File for field descriptions
        FileWriter descFileWriter = null;
        try {
            String newline = NEWLINE;
            File fieldDescFile = new File(tempdir, FIELD_DESCRIPTION_FILE);
            descFileWriter = new FileWriter(fieldDescFile);

            descFileWriter.write("Field descriptions for " + baseFilename + ".shp");
            descFileWriter.write(newline);
            
            descFileWriter.write("Survey ID" + FIELD_DESCRIPTION_FILE_DELIM + "Survey Name");
            descFileWriter.write(newline);
            descFileWriter.write(newline);
            
            for (Survey survey : surveyList) {
                if (survey != null && survey.getId() != null) {
                    descFileWriter.write(survey.getId().toString() + FIELD_DESCRIPTION_FILE_DELIM + survey.getName());
                    descFileWriter.write(newline);
                }
            }
            
            if (!cmList.isEmpty()) {
                descFileWriter.write(newline);
                descFileWriter.write("Census Method ID" + FIELD_DESCRIPTION_FILE_DELIM + "Census Method Name");
                descFileWriter.write(newline);
                for (CensusMethod cm : cmList) {
                    if (cm != null && cm.getId() != null) {
                        descFileWriter.write(cm.getId().toString() + FIELD_DESCRIPTION_FILE_DELIM + cm.getName());
                        descFileWriter.write(newline);
                    }
                }
            }
            
            
            descFileWriter.write(newline);
            descFileWriter.write(newline);
            
            descFileWriter.write("Shapefile attribute name" + FIELD_DESCRIPTION_FILE_DELIM + 
                                 "Database name" + FIELD_DESCRIPTION_FILE_DELIM + 
                                 "Survey" + FIELD_DESCRIPTION_FILE_DELIM + 
                                 "Census Method" + FIELD_DESCRIPTION_FILE_DELIM + 
                                 "Description");
            
            descFileWriter.write(newline);
            descFileWriter.write(newline);
            

            if (contextForDescriptions != null) {
                for (AttributeDescriptorItem adi : contextForDescriptions.getFieldDescriptions()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(adi.getKey());
                    sb.append(FIELD_DESCRIPTION_FILE_DELIM);
                    sb.append(adi.getDatabaseName());
                    sb.append(FIELD_DESCRIPTION_FILE_DELIM);
                    sb.append(adi.getSurveyDescription());
                    sb.append(FIELD_DESCRIPTION_FILE_DELIM);
                    sb.append(adi.getCensusMethodDescription());
                    sb.append(FIELD_DESCRIPTION_FILE_DELIM);
                    sb.append(adi.getDescription());
                    sb.append(newline);
                    descFileWriter.write(sb.toString());
                }
            }
        } finally {
            if (descFileWriter != null) {
                descFileWriter.close();    
            }
        }
        
        // contains the survey id and the census method id
        FileWriter helperFileWriter = null;
        try {
            String newline = NEWLINE;
            File fieldDescFile = new File(tempdir, HELPER_FILE);
            helperFileWriter = new FileWriter(fieldDescFile);
            
            // for building the ID arrays in the helper file....
            List<String> surveyIds = new ArrayList<String>();
            for (Survey survey : surveyList) {
                if (survey != null && survey.getId() != null) {
                    surveyIds.add(survey.getId().toString());
                }
            }
            List<String> cmIds = new ArrayList<String>();
            for (CensusMethod cm : cmList ) {
                if (cm != null && cm.getId() != null) {
                    cmIds.add(cm.getId().toString());
                }
            }
            // add census method id = 0...
            if (cmIds.isEmpty()) {
                cmIds.add("0");
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append(SURVEY_ID_KEY);
            sb.append(HELPER_FILE_SEPARATOR);
            sb.append(org.apache.commons.lang.StringUtils.join(surveyIds.toArray(),  ID_DELIM));
            sb.append(newline);
            sb.append(CENSUS_METHOD_ID_KEY);
            sb.append(HELPER_FILE_SEPARATOR);
            sb.append(org.apache.commons.lang.StringUtils.join(cmIds.toArray(),  ID_DELIM));
            sb.append(newline);
            
            helperFileWriter.write(sb.toString());
            
            // write attribute id map
            if (contextForDescriptions != null) {
                for (AttributeDescriptorItem adi : contextForDescriptions.getFieldDescriptions()) {
                    if (adi.getAttribute() != null && adi.getAttribute().getId() != null) {
                        StringBuilder adiBuilder = new StringBuilder();
                        adiBuilder.append(ATTR_ENTRY_PREFIX);
                        adiBuilder.append(adi.getAttribute().getId().toString());
                        adiBuilder.append(ATTR_ENTRY_DELIM);
                        adiBuilder.append(adi.getKey());
                        helperFileWriter.write(adiBuilder.toString());
                        helperFileWriter.write(newline);
                    }
                }
            }

        } finally {
            if (helperFileWriter != null) {
                helperFileWriter.close();    
            }
        }
        
        // metadata file
        FileWriter metadataFileWriter = null;
        try {
            Map<String, Object> mdParam = new HashMap<String, Object>();
            
            if (surveyList.size() == 1) {
                Survey survey = surveyList.get(0);
                mdParam.put("shpPurpose", "Shape file template for making new recordings for the Biological Data Recording System (BDRS)");
                mdParam.put("surveyDescription", survey.getDescription());
                mdParam.put("westBoundLongitude", "-180");
                mdParam.put("eastBoundLongitude", "180");
                mdParam.put("northBoundLatitude", "90");
                mdParam.put("southBoundLatitude", "-90");
                mdParam.put("responsiblePerson", "");
                mdParam.put("responsibleOrg", "");
                mdParam.put("shpTitle",survey.getName());
                mdParam.put("dateProduced", metadataDateFormat.format(new Date()));
                mdParam.put("surveyStartDate", metadataDateFormat.format(survey.getStartDate()));
            } else {
                mdParam.put("shpPurpose", "Shape file template for making new recordings for the Biological Data Recording System (BDRS)");
                mdParam.put("surveyDescription", "Log into your BDRS site to see detailed information about the surveys in this shapefile");;
                mdParam.put("westBoundLongitude", "-180");
                mdParam.put("eastBoundLongitude", "180");
                mdParam.put("northBoundLatitude", "90");
                mdParam.put("southBoundLatitude", "-90");
                mdParam.put("responsiblePerson", "");
                mdParam.put("responsibleOrg", "");
                mdParam.put("shpTitle", "Shape file for BDRS record import/export");
                mdParam.put("dateProduced", metadataDateFormat.format(new Date()));
                mdParam.put("surveyStartDate", metadataDateFormat.format(new Date()));
            }
            
            String output = templateService.transformToString(METADATA_TEMPLATE_FILENAME, ShapeFileWriter.class, mdParam);
            File metadataFile = new File(tempdir, METADATA_FILE);
            metadataFileWriter = new FileWriter(metadataFile);
            metadataFileWriter.write(output);
        } finally {
            if (metadataFileWriter != null) {
                metadataFileWriter.close();
            }
        }
        
        List<File> filesToCompress = new ArrayList<File>();
        
        for (ShapefileType shpType : shapefileTypeSet) {
            String filename = getShpFilename(hasRecords, shpType);
            filesToCompress.add(FileUtils.getFileFromDir(tempdir, filename + ".shp"));
            filesToCompress.add(FileUtils.getFileFromDir(tempdir, filename + ".prj"));
            filesToCompress.add(FileUtils.getFileFromDir(tempdir, filename + ".shx"));
            filesToCompress.add(FileUtils.getFileFromDir(tempdir, filename + ".dbf"));            
        }        
        filesToCompress.add(FileUtils.getFileFromDir(tempdir, FIELD_DESCRIPTION_FILE));
        filesToCompress.add(FileUtils.getFileFromDir(tempdir, HELPER_FILE));
        filesToCompress.add(FileUtils.getFileFromDir(tempdir, METADATA_FILE));
        
        ZipUtils.compress(filesToCompress, outfile);
        
        return outfile; 
    }
    
    private ShapefileType getShapefileTypefromGeometry(Geometry geom) {
        if (geom instanceof Point) {
            return ShapefileType.POINT;
        } else if (geom instanceof MultiLineString) {
            return ShapefileType.MULTI_LINE;
        } else if (geom instanceof MultiPolygon) {
            return ShapefileType.MULTI_POLYGON;
        } else {
            throw new IllegalStateException("Cannot handle geometry of type : " + geom.getClass().getName());
        }   
    }
    
    private void writeFeatures(ShapefileDataStore ds, List<ShapefileFeature> featureList) throws IOException {
        
        org.geotools.data.Transaction shapefileTransaction = new DefaultTransaction("create");
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(ds.getSchema());
        ShapefileFeatureStore featureStore = new ShapefileFeatureStore(ds, Collections.EMPTY_SET, ds.getSchema());

        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = FeatureCollections.newCollection();
        for (ShapefileFeature f : featureList) {
            Map<String, Object> attributes = f.getAttributes();
            featureBuilder.reset();
            for (Entry<String, Object> entry : attributes.entrySet()) {
                featureBuilder.set(entry.getKey(), entry.getValue());
            }
            featureBuilder.set(ShapefileFields.THE_GEOM, f.getGeometry());
            collection.add(featureBuilder.buildFeature(null));
        }

        featureStore.setTransaction(shapefileTransaction);
        try {
            featureStore.addFeatures(collection);
            shapefileTransaction.commit();
        } catch (Exception ex) {
            log.debug("error while writing shapefile:", ex);
            shapefileTransaction.rollback();
        } finally {
            shapefileTransaction.close();
        }
    }
    
    private String getShpFilename(boolean hasRecords, ShapefileType shpType) {
        StringBuilder filenameBuilder = new StringBuilder();
        if (!hasRecords) {
            // is a record import template...
            filenameBuilder.append("record_import_template");
        } else {
            // is a record export
            filenameBuilder.append("record_export");
        }
        switch (shpType) {
        case POINT:
            filenameBuilder.append("_POINT");
            break;
        case MULTI_LINE:
            filenameBuilder.append("_MULTI_LINE");
            break;
        case MULTI_POLYGON:
            filenameBuilder.append("_MULTI_POLYGON");
            break;
            default:
                throw new IllegalArgumentException("shpType not supported : " + shpType.toString());
        }
        return filenameBuilder.toString();
    }
    
    private void writeAttributeValues(Set<AttributeValue> avSet, Map<Attribute, String> attrNameMap, Map<String, Object> targetMap) {
        
        for (AttributeValue av : avSet) {
            
            Attribute a = av.getAttribute();
            String name = attrNameMap.get(a);
            if (name == null) {
                log.error("cannot find name in attrNameMap for attribute id : " + a.getId());
                // this can be caused by removing an attribute from a survey/census method but the record
                // still may have an AttributeValue associated with the Attribute. Log the error but continue
                // processing the record.
                continue;
            }
            if (targetMap.containsKey(name)) {
                log.error("key already exists in target map, we should have no collisions : " + name);
                throw new IllegalStateException("Could not write attribute values, key for value already in target map");
            }
            
            switch (a.getType()) {
            // integer
            case INTEGER:
            case INTEGER_WITH_RANGE:
                if (av.getNumericValue() != null) {
                    targetMap.put(name, av.getNumericValue().toBigInteger().intValue());
                }
                break;
                
            // float
            case DECIMAL:
                if (av.getNumericValue() != null) {
                    targetMap.put(name, av.getNumericValue().doubleValue());
                }
                break;
                
            case DATE: // dates are written as strings...
                if (av.getDateValue() != null) {
                    targetMap.put(name, shpDateFormat.format(av.getDateValue()));    
                }
                break;
                
            // string
            case TIME:
            case BARCODE:
            case STRING:
            case STRING_AUTOCOMPLETE:
            case TEXT:
            case STRING_WITH_VALID_VALUES:
            case MULTI_CHECKBOX:
            case SINGLE_CHECKBOX:
            case MULTI_SELECT:
                if (av.getStringValue() != null) {
                    targetMap.put(name, av.getStringValue());
                }
                break;
            
            // not supported
            case IMAGE:
            case FILE:
                // don't add
                break;
            case HTML:
            case HTML_COMMENT:
            case HTML_HORIZONTAL_RULE:
                // display only fields, don't add
                break;
            // error, not expected
            default:
                // intended to cause regression failure when a new attribute type is not handled
                throw new IllegalStateException("An attribute type is not handled properly: " + a.getType());
            }
        }
    }
       
    private void writeAttributes(ShapeFileWriterContext context, Map<Attribute, String> attrNameMap) throws SchemaException {
        for (Entry<Attribute, String> entry : attrNameMap.entrySet()) {
            
            Attribute a = entry.getKey();
            String name = entry.getValue();
            
            switch (a.getType()) {
            // integer
            case INTEGER:
            case INTEGER_WITH_RANGE:
                context.addInt(name, a, a.getDescription());
                break;
                
            // float
            case DECIMAL:
                context.addDouble(name, a, a.getDescription());
                break;
                
            // date
            case DATE:
                context.addDate(name, a, a.getDescription());
                break;
                
            // string
            case TIME:
            case BARCODE:
            case STRING:
            case STRING_AUTOCOMPLETE:
            case TEXT:
            case STRING_WITH_VALID_VALUES:
            case MULTI_CHECKBOX:
            case SINGLE_CHECKBOX:
            case MULTI_SELECT:
                context.addString(name, a, a.getDescription());
                break;
            
            // not supported
            case IMAGE:
            case FILE:
                // don't add
                break;
            case HTML:
            case HTML_COMMENT:
            case HTML_HORIZONTAL_RULE:
                // display only fields, don't add
                break;
            // error, not expected
            default:
                // intended to cause regression failure when a new attribute type is not handled
                throw new IllegalStateException("An attribute type is not handled properly: " + a.getType());
            }
        }
    }
    
    private static String sanitizeString(String str) {
        // Make the survey name a safe filename.
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
