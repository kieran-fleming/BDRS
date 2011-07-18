package au.com.gaiaresources.bdrs.controller.map;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapFeatureDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayerDAO;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

// You may note we do not test the 'shape file is too big' case here, I didn't want to commit
// a large binary file to the database.
public class GeoMapController_ShpCheckService_Test extends
        AbstractControllerTest {

    @Autowired
    GeoMapLayerDAO layerDAO;
    @Autowired
    SurveyDAO surveyDAO;
    @Autowired
    ManagedFileDAO fileDAO;
    @Autowired
    FileService fileService;
    @Autowired 
    AttributeDAO attrDAO;
    @Autowired
    RecordDAO recDAO;
    @Autowired 
    UserDAO userDAO;
    @Autowired
    GeoMapFeatureDAO featureDAO;

    Logger log = Logger.getLogger(getClass());
    
    @Test
    public void checkShp_emptyUuidError() throws Exception {
        request.setRequestURI(GeoMapLayerController.CHECK_SHAPEFILE_SERVICE_URL);
        request.setMethod("GET");
        request.setParameter(GeoMapLayerController.PARAM_MANAGED_FILE_UUID, "");
        
        this.handle(request, response);
        
        JSONObject obj = (JSONObject)JSONSerializer.toJSON(response.getContentAsString());
        
        Assert.assertEquals(GeoMapLayerController.JSON_STATUS_ERROR, obj.getString(GeoMapLayerController.JSON_KEY_STATUS));
    }
    
    @Test
    public void checkShp_noManagedFileError() throws Exception {
        request.setRequestURI(GeoMapLayerController.CHECK_SHAPEFILE_SERVICE_URL);
        request.setMethod("GET");
        request.setParameter(GeoMapLayerController.PARAM_MANAGED_FILE_UUID, "uuidthatpointstonothing");
        
        this.handle(request, response);
        
        JSONObject obj = (JSONObject)JSONSerializer.toJSON(response.getContentAsString());
        
        Assert.assertEquals(GeoMapLayerController.JSON_STATUS_ERROR, obj.getString(GeoMapLayerController.JSON_KEY_STATUS));
    }
    
    @Test
    public void checkShp_noFileError() throws Exception {
        ManagedFile shapefilemf = new ManagedFile();
        shapefilemf.setFilename("nonexistentfile.zip");
        shapefilemf.setContentType("image/jpeg");
        shapefilemf.setWeight(0);
        shapefilemf.setDescription("This is a test image");
        shapefilemf.setCredit("Creative Commons");
        shapefilemf.setLicense("Nobody");
        shapefilemf.setPortal(RequestContextHolder.getContext().getPortal());
        fileDAO.save(shapefilemf);
        
        request.setRequestURI(GeoMapLayerController.CHECK_SHAPEFILE_SERVICE_URL);
        request.setMethod("GET");
        request.setParameter(GeoMapLayerController.PARAM_MANAGED_FILE_UUID, shapefilemf.getUuid());
        
        this.handle(request, response);
        
        JSONObject obj = (JSONObject)JSONSerializer.toJSON(response.getContentAsString());
        
        Assert.assertEquals(GeoMapLayerController.JSON_STATUS_ERROR, obj.getString(GeoMapLayerController.JSON_KEY_STATUS));
    }
    
    @Test
    public void checkShp_wrongFormatWarn() throws Exception {
        ManagedFile mf = prepShapefileZip("Small_GDA.zip");
        
        request.setRequestURI(GeoMapLayerController.CHECK_SHAPEFILE_SERVICE_URL);
        request.setMethod("GET");
        request.setParameter(GeoMapLayerController.PARAM_MANAGED_FILE_UUID, mf.getUuid());
        
        this.handle(request, response);
        
        JSONObject obj = (JSONObject)JSONSerializer.toJSON(response.getContentAsString());
        
        Assert.assertEquals(GeoMapLayerController.JSON_STATUS_WARN, obj.getString(GeoMapLayerController.JSON_KEY_STATUS));
    }
    
    @Test
    public void checkShp_Ok() throws Exception {
        ManagedFile mf = prepShapefileZip("Small_MGAz50_Project.zip");
        
        request.setRequestURI(GeoMapLayerController.CHECK_SHAPEFILE_SERVICE_URL);
        request.setMethod("GET");
        request.setParameter(GeoMapLayerController.PARAM_MANAGED_FILE_UUID, mf.getUuid());
        
        this.handle(request, response);
        
        JSONObject obj = (JSONObject)JSONSerializer.toJSON(response.getContentAsString());
        
        Assert.assertEquals(GeoMapLayerController.JSON_STATUS_OK, obj.getString(GeoMapLayerController.JSON_KEY_STATUS));
    }
    
    
    private ManagedFile prepShapefileZip(String filename) throws IOException {
        File file = new File(getClass().getResource(filename).getFile());

        ManagedFile shapefilemf = new ManagedFile();
        shapefilemf.setFilename(filename);
        shapefilemf.setContentType("image/jpeg");
        shapefilemf.setWeight(0);
        shapefilemf.setDescription("This is a test image");
        shapefilemf.setCredit("Creative Commons");
        shapefilemf.setLicense("Nobody");
        shapefilemf.setPortal(RequestContextHolder.getContext().getPortal());
        fileDAO.save(shapefilemf);
        
        fileService.createFile(shapefilemf, file, filename);
        return shapefilemf;
    }
}
