package au.com.gaiaresources.bdrs.spatial;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.geotools.data.shapefile.shp.ShapefileException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.model.map.GeoMapFeature;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.test.AbstractSpringContextTest;

public class ShapeFileReaderTest extends AbstractSpringContextTest {
    @Autowired
    UserDAO userDAO;
    
    User admin;
    
    @Before
    public void setup() {
        admin = userDAO.getUser("admin");
    }
    
    @Test
    public void testShapefiletoGeoMapLayer() throws ShapefileException, IOException {
        String filename = getClass().getResource("Simple4.zip").getFile();
        File file = new File(filename);
        
        ShapeFileReader reader = new ShapeFileReader(file);
        
        Assert.assertFalse(reader.isCrsSupported());
        
        List<Attribute> attributeList = reader.readAttributes();
        List<Record> recordList = reader.readAsRecords(attributeList, new Date(), admin);
        
        Assert.assertEquals(25, recordList.size());
        Assert.assertEquals(4, attributeList.size());
        
        {
            Attribute a1 = findAttribute(attributeList, "FID_1");
            Assert.assertNotNull(a1);
            Assert.assertEquals(AttributeType.INTEGER, a1.getType());
        }
        
        {
            Attribute a1 = findAttribute(attributeList, "Id");
            Assert.assertNotNull(a1);
            Assert.assertEquals(AttributeType.INTEGER, a1.getType());
        }
        
        {
            Attribute a1 = findAttribute(attributeList, "Descriptio");
            Assert.assertNotNull(a1);
            Assert.assertEquals(AttributeType.STRING, a1.getType());
        }
        
        {
            Attribute a1 = findAttribute(attributeList, "Name");
            Assert.assertNotNull(a1);
            Assert.assertEquals(AttributeType.STRING, a1.getType());
        }
    }
    
    @Test
    public void testShapefiletoGeoMapLayerNumber2() throws ShapefileException, IOException {
        String filename = getClass().getResource("Small_GDA.zip").getFile();
        File file = new File(filename);
        
        ShapeFileReader reader = new ShapeFileReader(file);
        
        Assert.assertFalse(reader.isCrsSupported());
        
        List<Attribute> attributeList = reader.readAttributes();
        List<Record> recordList = reader.readAsRecords(attributeList, new Date(), admin);
        
        Assert.assertEquals(3, recordList.size());
        Assert.assertEquals(4, attributeList.size());
        
        {
            Attribute a1 = findAttribute(attributeList, "Id");
            Assert.assertNotNull(a1);
            Assert.assertEquals(AttributeType.INTEGER, a1.getType());
        }
        
        {
            Attribute a1 = findAttribute(attributeList, "String");
            Assert.assertNotNull(a1);
            Assert.assertEquals(AttributeType.STRING, a1.getType());
        }
        
        {
            Attribute a1 = findAttribute(attributeList, "DateType");
            Assert.assertNotNull(a1);
            Assert.assertEquals(AttributeType.DATE, a1.getType());
        }
        {
            Attribute a1 = findAttribute(attributeList, "Float");
            Assert.assertNotNull(a1);
            Assert.assertEquals(AttributeType.DECIMAL, a1.getType());
        }
    }
    
    @Test
    public void testShapefileToGeoMapFeatures() throws ShapefileException, IOException {
        String filename = getClass().getResource("Small_GDA.zip").getFile();
        File file = new File(filename);
        
        ShapeFileReader reader = new ShapeFileReader(file);
        
        Assert.assertFalse(reader.isCrsSupported());
        
        List<Attribute> attributeList = reader.readAttributes();
        List<GeoMapFeature> featureList = reader.readAsMapFeatures(attributeList);
        
        Assert.assertEquals(3, featureList.size());
        Assert.assertEquals(4, attributeList.size());
        
        {
            Attribute a1 = findAttribute(attributeList, "Id");
            Assert.assertNotNull(a1);
            Assert.assertEquals(AttributeType.INTEGER, a1.getType());
        }
        
        {
            Attribute a1 = findAttribute(attributeList, "String");
            Assert.assertNotNull(a1);
            Assert.assertEquals(AttributeType.STRING, a1.getType());
        }
        
        {
            Attribute a1 = findAttribute(attributeList, "DateType");
            Assert.assertNotNull(a1);
            Assert.assertEquals(AttributeType.DATE, a1.getType());
        }
        {
            Attribute a1 = findAttribute(attributeList, "Float");
            Assert.assertNotNull(a1);
            Assert.assertEquals(AttributeType.DECIMAL, a1.getType());
        }
    }
    
    @Test
    public void testShapefileToGeoMapFeaturesReadSRID() throws ShapefileException, IOException {
        String filename = getClass().getResource("Small_MGAz50_Project.zip").getFile();
        File file = new File(filename);
        
        ShapeFileReader reader = new ShapeFileReader(file);
        
        List<Attribute> attributeList = reader.readAttributes();
        List<GeoMapFeature> featureList = reader.readAsMapFeatures(attributeList);

        Assert.assertTrue(reader.isCrsSupported());
        
        for (GeoMapFeature f : featureList) {
            Assert.assertEquals(4326, f.getGeometry().getSRID());
        }
    }
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void testZipWithNoShp() throws IOException {
        thrown.expect(IOException.class);
        
        String filename = getClass().getResource("noShp.zip").getFile();
        File file = new File(filename);
        new ShapeFileReader(file);
    }
    
    private Attribute findAttribute(List<Attribute> attributes, String name) {
        for (Attribute a : attributes) {
            if (name.equals(a.getName())) {
                return a;
            }
        }
        return null;
    }
}
