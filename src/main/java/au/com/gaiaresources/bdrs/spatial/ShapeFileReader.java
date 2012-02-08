package au.com.gaiaresources.bdrs.spatial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.location.impl.LocationServiceImpl;
import au.com.gaiaresources.bdrs.model.map.GeoMapFeature;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.util.FileUtils;
import au.com.gaiaresources.bdrs.util.ZipUtils;

import com.vividsolutions.jts.geom.Geometry;

public class ShapeFileReader {
    
    private static final String WGS84 = "GCS_WGS_1984";
    private static final int WGS84_EPSG = 4326;
    public static final String UNKNOWN_CRS_CODE = "Unknown CRS code";
    
    private Logger log = Logger.getLogger(getClass());
    
    private List<ShapefileDataStore> dsList = new ArrayList<ShapefileDataStore>();
    
    private String crsCode = UNKNOWN_CRS_CODE;
    
    private boolean geometryValid = false;
    private LocationService locationService = new LocationServiceImpl();
    
    private File unzipDir = null;
    
    private List<Integer> surveyIdList = new LinkedList<Integer>();
    private List<Integer> censusMethodIdList = new LinkedList<Integer>();
    
    private Map<Integer, String> attrIdNameMap = new HashMap<Integer, String>();
    
    private static final Pattern attrPattern = Pattern.compile("attr:([\\d]+),(.*)");
    private static final Pattern surveyPattern = Pattern.compile("survey_id=([0-9]+)(,[0-9]+)*");
    private static final Pattern censusMethodPattern = Pattern.compile("census_method_id=([0-9]+)(,[0-9]+)*");
    
    /**
     * if there are multiple shapefiles in the zip file, should we try and read from
     * all of them. If this is enabled certain methods in ShapeFileReader will throw
     * exceptions. Would be better to refactor but I don't have the time at the moment.
     */
    private boolean allowMultipleShapefiles = false;
    
    public ShapeFileReader(File zippedShapefile) throws IOException {
        this(zippedShapefile, false);
    }
    
    public ShapeFileReader(File zippedShapefile, boolean allowMultipleShapefiles) throws IOException {
        
        this.allowMultipleShapefiles = allowMultipleShapefiles;
        
        unzipDir = unzipShapeFileToTempDir(zippedShapefile);
        
        List<String> shapefileNames = detectShapefileNames(unzipDir);
        
        if (shapefileNames.isEmpty()) {
            throw new IOException("No .shp file contained in zip file: " + zippedShapefile.getAbsolutePath());
        }
        if (!this.allowMultipleShapefiles && shapefileNames.size() > 1) {
            throw new IOException("More than one shapefile detected in zip file. Only 1 shapefile is allowed");
        }
        
        String shapefileDir = unzipDir.getAbsolutePath() + File.separatorChar;
        
        for (String shpFilename : shapefileNames) {
            dsList.add(new ShapefileDataStore(new URL("file://" + shapefileDir + shpFilename)));
        }

        checkShapefile();
        
        File helperFile = new File(shapefileDir + ShapeFileWriter.HELPER_FILE);
        if (helperFile.exists()) {
            // if this file exists this is a previously exported BDRS shapefile.
            
            BufferedReader bufferedReader = null;
            FileReader reader = null;
            try {
                reader = new FileReader(helperFile);
                bufferedReader = new BufferedReader(reader);
                
                // read survey id line
                String surveyLine = bufferedReader.readLine();
                
                surveyIdList = readIdList("Survey", surveyLine, surveyPattern);
                
                String censusMethodLine = bufferedReader.readLine();
                censusMethodIdList = readIdList("Census Method", censusMethodLine, censusMethodPattern);
                
                // read the attributes
                String attrLine = bufferedReader.readLine();
                while (attrLine != null) {
                    Matcher m = attrPattern.matcher(attrLine);
                    if (m.matches()) {
                        // group 0 is the whole string
                        // group 1 is the id - it is already validated as a number
                        // group 2 is the name
                        attrIdNameMap.put(Integer.parseInt(m.group(1)), m.group(2));
                    }
                    // get the next line for the next iteration
                    attrLine = bufferedReader.readLine();
                }
                
            } catch (IOException ioe) {
                log.error("error reading shapefile helper file which should contain survey id and census method id", ioe);
                throw ioe;
            } finally {
                if (reader != null) {
                    reader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        }
    }
    
    /**
     * indicates whether this zipped shapefile package is an upload of records to the BDRS
     * @return
     */
    public boolean isRecordUpload() {
        return !surveyIdList.isEmpty();
    }
    
    /**
     * indicates which survey the records uploaded in this zipped shapefile package are for
     * @return
     */
    public List<Integer> getSurveyIdList() {
        return Collections.unmodifiableList(surveyIdList);
    }
    
    /**
     * indicates which census method the records uploaded in this zipped shapefile package are instances of
     * @return
     */
    public List<Integer> getCensusMethodIdList() {
        return Collections.unmodifiableList(censusMethodIdList);
    }
    
    /**
     * get the File object that the zipped up shape file has been extracted to
     * @return
     */
    public File getUnzipDir() {
        return unzipDir;
    }
    
    /**
     * Returns the map that has been parsed from the helper file. Used to
     * keep the mapping between attribute and the name used in the shape
     * file consistent. 
     * 
     * @return
     */
    public Map<Integer, String> getAttributeIdNameMap() {
        return Collections.unmodifiableMap(attrIdNameMap);
    }
    
    /*
     * Use this text for an error message
     * 
     * "The shapefile is in a different coordinate system than required for (BDRS/PostGIS). 
     * You may still be able to display the data as a map layer; 
     * however, some data may be misaligned or contain accuracy problems"
     *   Modified version of the warning ArcMap gives when you try to use wrong projections
     *   
     *   Shapefile PRJ to PostGIS SRID lookup table?
     *   http://gis.stackexchange.com/questions/7608/shapefile-prj-to-postgis-srid-lookup-table
     *   
     *   Put the PRJ contents into a URL and it gives back the SRID. Example below...
     *   http://prj2epsg.org/search.json?terms=GEOGCS[%22GCS_WGS_1984%22,DATUM[%22D_WGS_1984%22,SPHEROID[%22WGS_1984%22,6378137.0,298.257223563]],PRIMEM[%22Greenwich%22,0.0],UNIT[%22Degree%22,0.0174532925199433]]
     *   
     */
    

    /**
     * Reads the id list out of a string
     */
    private List<Integer> readIdList(String listName, String lineToParse, Pattern pattern) throws IOException {
        List<Integer> result = new LinkedList<Integer>();
        {
            Matcher m = pattern.matcher(lineToParse);
            if (m.matches()) {
                // group 0 is the whole string
                // group 1 is the first integer all the way to N
               
                // hence we start at 1
                for (int i=1; i<= m.groupCount(); ++i) {
                    String idString = m.group(i);
                    
                    if (idString != null) {
                        if (idString.startsWith(ShapeFileWriter.ID_DELIM)) {
                            idString = idString.substring(1);
                        }
                        result.add(Integer.parseInt(idString));
                    }
                }
            } else {
                throw new IOException("Could not read " + listName + " IDs out of helper file");
            }
        }
        return result;
    }
    
    /**
     * get all of the file names ending with .shp
     * 
     * @param unzippedDir the directory that the shape file has been unzipped to
     * @return
     */
    private List<String> detectShapefileNames(File unzippedDir) {
        List<String> result = new ArrayList<String>();
        for (String filename : unzippedDir.list()) {
            if (filename.endsWith(".shp")) {
                result.add(filename);
            }
        }
        return result;
    }
    
    /**
     * Unzips the shape file to a temporary directory. This unzipped dir is then used
     * as a parameter for further shape file operations
     * 
     * @param zippedShapefile
     * @return File object representing the directory we unzipped to
     * @throws IOException
     */
    private File unzipShapeFileToTempDir(File zippedShapefile) throws IOException {
        File targetDir = FileUtils.createTempDirectory("unzipShapeFile");
        ZipUtils.decompressToDir(zippedShapefile, targetDir);
        return targetDir;
    }
    
    private void checkShapefile() throws IOException {
        this.crsCode = UNKNOWN_CRS_CODE;
        this.geometryValid = true;
        
        Iterator<SimpleFeature> iter = getFeatureIterator();
       
        // populate the layer attributes...
        if (iter.hasNext()) {
            SimpleFeature feature = iter.next();
            CoordinateReferenceSystem crs = feature.getFeatureType().getCoordinateReferenceSystem();
            this.crsCode = crs.getName().getCode();
            
            this.geometryValid = this.geometryValid && ((Geometry)feature.getDefaultGeometry()).isValid();
        }
        // get to the end of the iterator so the file lock is released
        // and check the geoms on the way...
        while (iter.hasNext()) {
            SimpleFeature feature = iter.next();
            this.geometryValid = this.geometryValid && ((Geometry)feature.getDefaultGeometry()).isValid();
        }
    }
    
    /**
     * Parses the first map feature in the shape file to determine the attributes.
     * Assumption: Every feature in the shape file has the same list of attributes.
     * 
     * The client of ShapefileService needs to handle persisting.
     * 
     * This method is only available when multiple shapefile reading is disabled.
     * 
     * @param unzippedFolder
     * @param shapefileName
     * @return
     * @throws IOException 
     */
    public List<Attribute> readAttributes() throws IOException {
        
        if (allowMultipleShapefiles) {
            throw new IllegalStateException("cannot call readAttributes() when allowMultipleShapeFiles is set to true");
        }
        // we are guaranteed to have one datastore in the ctor
        ShapefileDataStore ds = dsList.get(0);

        List<Attribute> result = new LinkedList<Attribute>();
        SimpleFeatureType sft = ds.getSchema();
        for (int i=0; i<sft.getAttributeCount(); ++i) {
            Attribute a = new Attribute();
            
            if (sft.getType(i).getBinding() == String.class) {
                a.setTypeCode(AttributeType.STRING.getCode());
            } else if (sft.getType(i).getBinding() == Integer.class) {
                a.setTypeCode(AttributeType.INTEGER.getCode());
            } else if (sft.getType(i).getBinding() == Date.class) {
                a.setTypeCode(AttributeType.DATE.getCode());
            } else if (sft.getType(i).getBinding() == Double.class) {
                a.setTypeCode(AttributeType.DECIMAL.getCode());
            } else {
                log.error("unrecognized type, ignoring");
                continue;
            }
            a.setName(sft.getType(i).getName().toString());
            a.setRequired(false);
            //probably doesn't matter ??
            a.setScope(AttributeScope.RECORD);
            a.setTag(false);
            result.add(a);
        }
        return result;
    }
    
    /**
     * Returns the coordinate reference system code. Currently the only supported return code
     * is "GCS_WGS_1984".
     * 
     * @return
     */
    public String getCrsCode() {
        return this.crsCode;
    }
    
    /**
     * Compares the CRS code against the list of supported codes. Allows the client class
     * to warn the user if a non compliant CRS is being used in the uploaded shapefile.
     * 
     * Currently the only supported CRS code is GCS_WGS_1984
     * 
     * @return
     */
    public boolean isCrsSupported() {
        return WGS84.equals(getCrsCode());
    }
    
    /**
     * Gets the list of CRS (coordinate reference systems) supported by our implementation.
     * Currently we can only support WSG84 however we will allow the option of the user
     * to upload whatever they like with the caveat that it may not display properly.
     * 
     * Often the CRS are so similar, especially at non extreme longitudes (i.e. not near the
     * poles) that the user can get away with uploading their non supported shapefile anyway.
     * @return
     */
    public List<String> getSupportedCrsCodes() {
        List<String> result = new ArrayList<String>();
        result.add(WGS84);
        return result;
    }
    
    /**
     * If this returns true all of the geometries in the shapefile are valid.
     * If this returns false one or more of the geometries in the shapefile are invalid. This may cause
     * problems with spatial queries.
     * @return boolean, result
     */
    public boolean isGeometryValid() {
        return this.geometryValid;
    }
    
    /**
     * Sometimes we may want to manipulate the datastore directly. 
     * 
     * method can only be called when allowMultipleShapefiles is false!
     * 
     * @return
     */
    public ShapefileDataStore getDataStore() {
        
        if (allowMultipleShapefiles) {
            throw new IllegalStateException("cannot call getDataStore() when allowMultipleShapeFiles is set to true");
        }
        
        // we are guaranteed to have one datastore in the ctor
        return dsList.get(0);
    }
    
    /**
     * Returns an iterator that will iterate over all of the features. Will iterate
     * across shapefiles in the case allowMultipleShapefiles is true (see ctor)
     * 
     * @return
     * @throws IOException
     */
    public Iterator<SimpleFeature> getFeatureIterator() throws IOException {
        
        return new MultiShapefileDataStoreFeatureIterator(dsList);
    }
    
    /**
     * Given a list of attributes, turn each map feature in a shapefile into a GeoMapFeature. 
     * Need to use ShapeFileReader.getAttributes previously in order to have
     * a valid list of attributes to create GeoMapFeature from.
     * 
     * The client of ShapefileService needs to handle persisting.
     * 
     * @param attrList
     * @return
     * @throws IOException
     */
    public List<GeoMapFeature> readAsMapFeatures(List<Attribute> attrList) throws IOException {
        
        if (!isCrsSupported()) {
            log.warn("User is attempting to load an incompatible shapefile format: " + getCrsCode());
        }
        
        if (allowMultipleShapefiles) {
            throw new IllegalStateException("cannot call getDataStore() when allowMultipleShapeFiles is set to true");
        }
        // we are guaranteed to have one datastore in the ctor
        ShapefileDataStore ds = dsList.get(0);
        
        Iterator<SimpleFeature> iter = ds.getFeatureSource().getFeatures().iterator();
        List<GeoMapFeature> result = new LinkedList<GeoMapFeature>();
        
        // hash it for performance...
        Map<String, Attribute> attributeMap = createAttributeMap(attrList); 

        // create and populate records..
        while (iter.hasNext()) {
            SimpleFeature feature = iter.next();
            Geometry shape = extractGeom(feature);

            GeoMapFeature gmf = new GeoMapFeature();
            gmf.setGeometry(shape);
            
            Set<AttributeValue> attrValues = getAttributeValues(attributeMap, feature.getProperties());
            gmf.setAttributes(attrValues);
            
            result.add(gmf);
        }
        return result;
    }
    
    private Geometry extractGeom(SimpleFeature feature) {
        Geometry geom = (Geometry)feature.getDefaultGeometry();
        
        geom = locationService.convertToMultiGeom(geom);
        
        geom.setSRID(WGS84_EPSG);
        return geom;
    }
    
    private Map<String, Attribute> createAttributeMap(List<Attribute> attrList) {
        // hash it for performance...
        Map<String, Attribute> attributeMap = new HashMap<String, Attribute>();
        for (Attribute a : attrList) {
            attributeMap.put(a.getName(), a);
        }
        return attributeMap;
    }
    
    private Set<AttributeValue> getAttributeValues(Map<String, Attribute> attributeMap, Collection<Property> properties) {
        Set<AttributeValue> result = new LinkedHashSet<AttributeValue>();
        for (Property prop : properties) {
            Attribute attr = attributeMap.get(prop.getName().toString());
            if (attr != null) {
                AttributeValue attrValue = new AttributeValue();
                attrValue.setAttribute(attr);
                switch (attr.getType()) {
                case DATE:
                    attrValue.setDateValue((Date)prop.getValue());
                    break;
                case INTEGER:
                    if (prop.getValue() instanceof Integer) {
                        attrValue.setNumericValue(new BigDecimal(((Integer)prop.getValue()).doubleValue()));
                    } else {
                        attrValue.setNumericValue(new BigDecimal(((Double)prop.getValue()).doubleValue()));
                    }
                    break;
                case STRING:
                    // there is a terminating null character (\000) in the string, trim it off!
                    attrValue.setStringValue(((String)prop.getValue()).trim());
                    break;
                case DECIMAL:
                    attrValue.setNumericValue(new BigDecimal(((Double)prop.getValue()).doubleValue()));
                    break;
                case HTML:
                case HTML_NO_VALIDATION:
                case HTML_COMMENT:
                case HTML_HORIZONTAL_RULE:
                    // do nothing for view only types
                    break;
                default:
                    throw new IllegalStateException("Shouldn't be possible to reach here...??");
                }
                result.add(attrValue);
            }
        }
        return result;
    }
}

