package au.com.gaiaresources.bdrs.spatial;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;

import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

public class ShapeFileWriterContext {

    private static final int DEFAULT_STRING_LENGTH = 255;
    private static final int DEFAULT_DATE_STRING_LENGTH = 20;
    
    private List<AttributeDescriptorItem> descList;
    private SimpleFeatureTypeBuilder builder;
    private List<Survey> surveyList;
    private List<CensusMethod> cmList;
    
    private static final int KEY_LENGTH_LIMIT = 10;
    
    public ShapeFileWriterContext(ShapefileType shapefileType) {
        this(shapefileType, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }
    
    public ShapeFileWriterContext(ShapefileType shapefileType, List<Survey> surveyList, List<CensusMethod> cmList) {
        this(DefaultGeographicCRS.WGS84, shapefileType, surveyList, cmList);
    }
    
    public ShapeFileWriterContext(CoordinateReferenceSystem crs, ShapefileType shapefileType, List<Survey> surveyList, List<CensusMethod> cmList) {
        if (crs == null) {
            throw new IllegalArgumentException("CoordinateReferenceSystem, crs, cannot be null");
        }
        if (shapefileType == null) {
            throw new IllegalArgumentException("ShapefileType, shapefileType, cannot be null");
        }
        if (surveyList == null) {
            throw new IllegalArgumentException("List<Survey>, surveyList, cannot be null");
        }
        if (cmList == null) {
            throw new IllegalArgumentException("List<CensusMethod>,  cmList, cannot be null");
        }
        
        this.surveyList = surveyList;
        this.cmList = cmList;
        descList = new LinkedList<AttributeDescriptorItem>();
        builder = new SimpleFeatureTypeBuilder();
        builder.setName("Record");
        builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference system
        
        switch (shapefileType) {
        case POINT:
            builder.add(ShapefileFields.THE_GEOM, Point.class);
            break;
        case MULTI_POLYGON:
            builder.add(ShapefileFields.THE_GEOM, MultiPolygon.class);
            break;
        case MULTI_LINE:
            builder.add(ShapefileFields.THE_GEOM, MultiLineString.class);
            break;
        default:
           throw new IllegalArgumentException("Shape file type not yet supported");
        }
    }
    
    public void addString(String key, Attribute attr, String desc) throws SchemaException {
        key = limitKeyLength(key);
        addToDescList(key, attr, desc);
        builder.length(DEFAULT_STRING_LENGTH).add(key, String.class);
    }
    
    public void addInt(String key, Attribute attr, String desc) throws SchemaException {
        key = limitKeyLength(key);
        addToDescList(key, attr, desc);
        builder.add(key, Integer.class);
    }
    
    public void addDouble(String key, Attribute attr, String desc) throws SchemaException {
        key = limitKeyLength(key);
        addToDescList(key, attr, desc);
        builder.add(key, Double.class);
    }
    
    public void addDate(String key, Attribute attr, String desc) throws SchemaException {
        key = limitKeyLength(key);
        addToDescList(key, attr, desc);
        // write dates out as strings - in QGIS if you don't do this they get written as
        // strings anyway but with a length limit of 10 characters which doesn't
        // work with the date format we are using - dd MMM yyyy <- 11 chars long!
        builder.length(DEFAULT_DATE_STRING_LENGTH).add(key, String.class);
    }
    
    public SimpleFeatureTypeBuilder getBuilder() {
        return this.builder;
    }
    
    public List<AttributeDescriptorItem> getFieldDescriptions() {
        return this.descList;
    }
    
    private void addToDescList(String key, Attribute a, String desc) throws SchemaException {
        descList.add(new AttributeDescriptorItem(key, a, desc, getOwningSurvey(a), getOwningCensusMethod(a)));
    }
    
    // find the survey that owns the attribute. returns null if 
    // the attribute is not owned by a survey
    private Survey getOwningSurvey(Attribute a) {
        for (Survey survey : surveyList) {
            if (survey.getAttributes().contains(a)) {
                return survey;
            }
        }
        return null;
    }
    
    private CensusMethod getOwningCensusMethod(Attribute a) {
        for (CensusMethod cm : cmList) {
            if (cm != null) {
                if (cm.getAttributes().contains(a)) {
                    return cm;
                }
            }
        }
        return null;
    }
    
    private String limitKeyLength(String key) {
        if (key.length() < KEY_LENGTH_LIMIT) {
            return key;
        }
        return key.substring(0, KEY_LENGTH_LIMIT);
    }
}
