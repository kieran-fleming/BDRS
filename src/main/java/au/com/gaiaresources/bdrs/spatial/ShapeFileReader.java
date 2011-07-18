package au.com.gaiaresources.bdrs.spatial;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import au.com.gaiaresources.bdrs.model.map.GeoMapFeature;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.util.ZipUtils;

import com.vividsolutions.jts.geom.Geometry;

public class ShapeFileReader {
    
    private static final String WGS84 = "GCS_WGS_1984";
    private static final int WGS84_EPSG = 4326;
    public static final String UNKNOWN_CRS_CODE = "Unknown CRS code";
    
    private Logger log = Logger.getLogger(getClass());
    
    ShapefileDataStore ds;
    
    String crsCode;
    
    public ShapeFileReader(File zippedShapefile) throws IOException {
        File unzipDir = unzipShapeFileToTempDir(zippedShapefile);
        String shapefilename = detectShapefileName(unzipDir);
        
        if (shapefilename == null) {
            throw new IOException("No .shp file contained in zip file: " + zippedShapefile.getAbsolutePath());
        }
        
        String shapefileDir = unzipDir.getAbsolutePath() + File.separatorChar;
        ds = new ShapefileDataStore(new URL("file://" + shapefileDir + shapefilename));
       
        crsCode = readCrsCode();
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
     * Look through the files in the immediate directory for .shp extension and return
     * the filename
     * 
     * @param unzippedDir the directory that the shape file has been unzipped to
     * @return
     */
    private String detectShapefileName(File unzippedDir) {
        for (String filename : unzippedDir.list()) {
            if (filename.endsWith(".shp")) {
                return filename;
            }
        }
        return null;
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
        File targetDir = createTempDirectory();
        ZipFile zipFile = new ZipFile(zippedShapefile);
        ZipUtils.decompressToDir(zipFile, targetDir);
        return targetDir;
    }
    
    private String readCrsCode() throws IOException {
        String result = UNKNOWN_CRS_CODE;
        
        Iterator<SimpleFeature> iter = ds.getFeatureSource().getFeatures().iterator();
       
        // populate the layer attributes...
        if (iter.hasNext()) {
            SimpleFeature feature = iter.next();
            CoordinateReferenceSystem crs = feature.getFeatureType().getCoordinateReferenceSystem();
            result = crs.getName().getCode();
        }
        // get to the end of the iterator so the file lock is released
        while (iter.hasNext()) {
            iter.next();
        }
        return result;
    }
    
    /**
     * Parses the first map feature in the shape file to determine the attributes.
     * Assumption: Every feature in the shape file has the same list of attributes.
     * 
     * The client of ShapefileService needs to handle persisting.
     * 
     * @param unzippedFolder
     * @param shapefileName
     * @return
     * @throws IOException 
     */
    public List<Attribute> readAttributes() throws IOException {
        Iterator<SimpleFeature> iter = ds.getFeatureSource().getFeatures().iterator();
        List<Attribute> result = new LinkedList<Attribute>();
        // populate the layer attributes...
        if (iter.hasNext()) {
            SimpleFeature feature = iter.next();
            for (Property prop : feature.getProperties()) {
                if (!(prop.getValue() instanceof Geometry)) {
                    Attribute a = new Attribute();
                    if (prop.getValue() instanceof String) {
                        a.setTypeCode(AttributeType.STRING.getCode());
                    } else if (prop.getValue() instanceof Integer) {
                        a.setTypeCode(AttributeType.INTEGER.getCode());
                    } else if (prop.getValue() instanceof Date) {
                        a.setTypeCode(AttributeType.DATE.getCode());
                    } else if (prop.getValue() instanceof Double) {
                        a.setTypeCode(AttributeType.DECIMAL.getCode());
                    } else {
                        log.error("unrecognized type, ignoring");
                        continue;
                    }
                    a.setName(prop.getName().toString());
                    a.setRequired(false);
                    // probably doesn't matter ??
                    a.setScope(AttributeScope.RECORD);
                    a.setTag(false);

                    result.add(a);
                }
            }
        }
        // get to the end of the iterator so the file lock is released
        while (iter.hasNext()) {
            iter.next();
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
     * Given a list of attributes, turn each map feature in a shapefile into a record. 
     * Need to use ShapeFileReader.getAttributes previously in order to have
     * a valid list of attributes to create records from.
     * 
     * The client of ShapefileService needs to handle persisting.
     * 
     * @param unzippedFolder
     * @param shapefileName
     * @param attrList
     * @return
     * @throws IOException
     */
    public List<Record> readAsRecords(List<Attribute> attrList, Date now, User currentUser) throws IOException {
        
        if (!isCrsSupported()) {
            log.warn("User is attempting to load an incompatible shapefile format: " + getCrsCode());
        }
        
        Iterator<SimpleFeature> iter = ds.getFeatureSource().getFeatures().iterator();
        List<Record> result = new LinkedList<Record>();
        
        // hash it for performance...
        Map<String, Attribute> attributeMap = createAttributeMap(attrList); 
            
        // create and populate records..
        while (iter.hasNext()) {
            SimpleFeature feature = iter.next();
            Geometry shape = extractGeom(feature);
            
            Record rec = new Record();
            rec.setGeometry(shape);
            
            Set<AttributeValue> attrValues = getAttributeValues(attributeMap, feature.getProperties());
            rec.setAttributes(attrValues);
            
            // handle compulsory record fields:
            rec.setLastDate(now);
            rec.setWhen(now);
            rec.setUser(currentUser);
            
            result.add(rec);
        }
        return result;
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
        Geometry shape = (Geometry)feature.getDefaultGeometry();
        shape.setSRID(WGS84_EPSG);
        return shape;
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
                    // ok, so we can't handle floats right now
                    break;
                case STRING:
                    // there is a terminating null character (\000) in the string, trim it off!
                    attrValue.setStringValue(((String)prop.getValue()).trim());
                    break;
                case DECIMAL:
                    attrValue.setNumericValue(new BigDecimal(((Double)prop.getValue()).doubleValue()));
                    break;
                default:
                    throw new IllegalStateException("Shouldn't be possible to reach here...??");
                }
                result.add(attrValue);
            }
        }
        return result;
    }
    
    /**
     * Used to create a temp directory for us to unzip our shape file
     * 
     * @return
     * @throws IOException
     */
    private File createTempDirectory() throws IOException {
        final File temp;
        temp = File.createTempFile("unzipShapeFile", Long.toString(System.nanoTime()));
        if(!(temp.delete())) {
            throw new IOException("Could not delete temp file to create temp directory: " + temp.getAbsolutePath());
        }
        if(!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }
        return (temp);
    }
}
