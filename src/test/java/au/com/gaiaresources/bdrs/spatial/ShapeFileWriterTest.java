package au.com.gaiaresources.bdrs.spatial;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;

import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.test.AbstractSpringContextTest;

public class ShapeFileWriterTest extends AbstractSpringContextTest {
    
    Logger log = Logger.getLogger(getClass());

    /**
     * creates a file using a survey which contains one of every attribute type.
     * The test won't fail if an attribute is not tested but the ShapeFileWriter WILL 
     * throw an exception which will protect us from unknowingly not handling an AttributeType
     * @throws Exception 
     */
    @Test
    public void testCreateFile_Point() throws Exception {
        testWriter(ShapefileType.POINT);
    }
    
    @Test
    public void testCreateFile_MultiLine() throws Exception {
        testWriter(ShapefileType.MULTI_LINE);
    }
    
    @Test
    public void testCreateFile_MultiPolygon() throws Exception {
        testWriter(ShapefileType.MULTI_POLYGON);
    }
    
    private void testWriter(ShapefileType shpType) throws Exception {
        ShapeFileWriter writer = new ShapeFileWriter();
        
        Survey survey = new Survey();
        survey.setId(99);
        survey.setStartDate(new Date());
        survey.setName("my survey");
        survey.setDescription("my survey description woooo");
               
        List<Attribute> surveyAttrList = new ArrayList<Attribute>();
        
        Map<String, AttributeType> attrNameTypeMap = new HashMap<String, AttributeType>(); 
        
        AttributeType[] attrTypeArray = AttributeType.values();
        for (int i=0; i<attrTypeArray.length; ++i) {
            AttributeType attrType = attrTypeArray[i];
            String name = String.format("sattr_%d", i);
            attrNameTypeMap.put(name, attrType);
            
            Attribute a = createAttribute(name);
            a.setTypeCode(attrType.getCode());
            surveyAttrList.add(a);
        }
        
        survey.setAttributes(surveyAttrList);
        
        File zippedShapefile = writer.createZipShapefile(survey, null, shpType);
        
        ShapeFileReader reader = new ShapeFileReader(zippedShapefile);
        
        // Not all attributes will be written, hence not all of them will be read.
        List<Attribute> readList = reader.readAttributes();
        
        assertAttributesExist(readList, attrNameTypeMap, AttributeType.INTEGER, AttributeType.INTEGER);
        assertAttributesExist(readList, attrNameTypeMap, AttributeType.INTEGER_WITH_RANGE, AttributeType.INTEGER);
        assertAttributesExist(readList, attrNameTypeMap, AttributeType.DECIMAL, AttributeType.DECIMAL);
        assertAttributesExist(readList, attrNameTypeMap, AttributeType.BARCODE, AttributeType.STRING);
        // we are writing out dates as strings now. this won't have any real consequence on our system as
        // normally when we use the writer we are writing out surveys where the attribute name is mapped
        // to an actual system attribute, thus the type will normally be preserved.
        // This test uses the reader to 'blind read' the shape file thus, the returned type will be a string
        assertAttributesExist(readList, attrNameTypeMap, AttributeType.DATE, AttributeType.STRING);
        assertAttributesExist(readList, attrNameTypeMap, AttributeType.TIME, AttributeType.STRING);
        assertAttributesExist(readList, attrNameTypeMap, AttributeType.STRING, AttributeType.STRING);
        assertAttributesExist(readList, attrNameTypeMap, AttributeType.STRING_AUTOCOMPLETE, AttributeType.STRING);
        assertAttributesExist(readList, attrNameTypeMap, AttributeType.TEXT, AttributeType.STRING);
        assertAttributesExist(readList, attrNameTypeMap, AttributeType.STRING_WITH_VALID_VALUES, AttributeType.STRING);
        assertAttributesExist(readList, attrNameTypeMap, AttributeType.MULTI_CHECKBOX, AttributeType.STRING);
        assertAttributesExist(readList, attrNameTypeMap, AttributeType.MULTI_SELECT, AttributeType.STRING);
        assertAttributesExist(readList, attrNameTypeMap, AttributeType.SINGLE_CHECKBOX, AttributeType.STRING);
    }
    
    // unfortunately we can't tell certain validation types on upload at the moment so compare the read attribute types
    // to the 'basic' value type - i.e. integer with range becomes plain integer
    private void assertAttributesExist(List<Attribute> list, Map<String, AttributeType> attrNameTypeMap, AttributeType type, AttributeType basicType) {
        List<String> names = getNamesForAttributeType(attrNameTypeMap, type);
        for (String n : names) {
            boolean foundName = false;
            for (Attribute a : list) {
                if (a.getName().equals(n)) {
                    // dates are always written out as strings so they should be read back in as a string.
                    if (type == AttributeType.DATE) {
                        Assert.assertEquals("dates are expected to be read back in as type string", AttributeType.STRING.getCode(), a.getTypeCode());
                    } else {
                        Assert.assertEquals("name is the same but type code also expected to be the same", basicType.getCode(), a.getTypeCode());   
                    }
                    foundName = true;
                    break;
                }
            }
            if (!foundName) {
                Assert.fail("Expecting to find name: " + n + ", of type: " + type.toString() + " but could not find it in the attribute list");
            }
        }
    }
    
    private List<String> getNamesForAttributeType(Map<String, AttributeType> attrNameTypeMap, AttributeType type) {
        List<String> result = new ArrayList<String>();
        for (Entry<String, AttributeType> entry : attrNameTypeMap.entrySet()) {
            if (entry.getValue() == type) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
    
    private Attribute createAttribute(String name) {
        Attribute a = new Attribute();
        a.setName(name);
        a.setDescription(name + " desc");
        a.setRequired(true);
        a.setTag(false);
        return a;
    }
}
