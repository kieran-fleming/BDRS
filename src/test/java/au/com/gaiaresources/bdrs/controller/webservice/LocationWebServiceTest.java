package au.com.gaiaresources.bdrs.controller.webservice;

import junit.framework.Assert;
import net.sf.json.JSONSerializer;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.controller.record.TrackerController;
import au.com.gaiaresources.bdrs.geometry.GeometryBuilder;

import com.vividsolutions.jts.geom.Geometry;

public class LocationWebServiceTest extends AbstractControllerTest {

    GeometryBuilder gb;
    
    private Logger log = Logger.getLogger(getClass());
    
    @Before
    public void setup() {
        gb = new GeometryBuilder();
    }
    
    @Test
    public void testPointWkt() throws Exception {
        request.setRequestURI(LocationWebService.IS_WKT_VALID_URL);
        request.setMethod("GET");
        
        Geometry geom = gb.createPoint(-10, -20);

        request.setParameter(TrackerController.PARAM_WKT, geom.toText());
        
        handle(request, response);

        JSONObject json = (JSONObject)JSONSerializer.toJSON(response.getContentAsString());
        Assert.assertTrue("wkt should be valid", json.getBoolean(LocationWebService.JSON_KEY_ISVALID));
    }
    
    @Test
    public void testPolygonWkt() throws Exception {
        request.setRequestURI(LocationWebService.IS_WKT_VALID_URL);
        request.setMethod("GET");
        
        Geometry geom = gb.createSquare(-10, -10, 10);

        request.setParameter(TrackerController.PARAM_WKT, geom.toText());
        
        handle(request, response);

        JSONObject json = (JSONObject)JSONSerializer.toJSON(response.getContentAsString());
        Assert.assertTrue("wkt should be valid", json.getBoolean(LocationWebService.JSON_KEY_ISVALID));
    }
    
    @Test
    public void testLineWkt() throws Exception {
        request.setRequestURI(LocationWebService.IS_WKT_VALID_URL);
        request.setMethod("GET");
                
        Geometry geom = gb.createLine(1,2,3,4);

        request.setParameter(TrackerController.PARAM_WKT, geom.toText());

        handle(request, response);

        JSONObject json = (JSONObject)JSONSerializer.toJSON(response.getContentAsString());
        Assert.assertTrue("wkt should be valid", json.getBoolean(LocationWebService.JSON_KEY_ISVALID));
    }
    
    @Test
    public void testInvalidPolyWkt() throws Exception {
        request.setRequestURI(LocationWebService.IS_WKT_VALID_URL);
        request.setMethod("GET");
        
        // a self intersecting wkt string
        String wktString = "POLYGON((135.44531250005 -35.427036840141,124.19531250049 -27.957942082073,113.12109375093 -41.477654408167,134.74218750007 -26.55160357215,135.44531250005 -35.427036840141))";
        
        request.setParameter(TrackerController.PARAM_WKT, wktString);

        handle(request, response);
        
        JSONObject json = (JSONObject)JSONSerializer.toJSON(response.getContentAsString());
        Assert.assertFalse("wkt should be invalid", json.getBoolean(LocationWebService.JSON_KEY_ISVALID));
    }
}
