package au.com.gaiaresources.bdrs.controller.map;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.activation.FileDataSource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataBuilder;
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataHelper;
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataRow;
import au.com.gaiaresources.bdrs.db.SessionFactory;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.geometry.GeometryBuilder;
import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapFeature;
import au.com.gaiaresources.bdrs.model.map.GeoMapFeatureDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayer;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayerDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayerSource;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.service.web.JsonService;
import au.com.gaiaresources.bdrs.spatial.ShapeFileReader;
import au.com.gaiaresources.bdrs.util.KMLUtils;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

@Controller
public class GeoMapLayerController extends AbstractController {

    public static final String BASE_ADMIN_URL = "/bdrs/admin/mapLayer/";
    public static final String LISTING_URL = BASE_ADMIN_URL + "listing.htm";
    public static final String EDIT_URL = BASE_ADMIN_URL + "edit.htm";
    
    public static final String LIST_SERVICE_URL = BASE_ADMIN_URL + "listService.htm";
    public static final String GET_LAYER_URL = "/bdrs/map/getLayer.htm";
    
    public static final String GET_FEATURE_SERVICE_URL = "/bdrs/map/getFeatureInfo.htm";
    public static final String CHECK_SHAPEFILE_SERVICE_URL = "/bdrs/map/checkShapefile.htm";
    
    public static final String GEO_MAP_LAYER_PK_VIEW = "geoMapLayerId";
    public static final String GEO_MAP_LAYER_PK_SAVE = "geoMapLayerPk";
    
    public static final String FILTER_NAME = "name";
    public static final String FILTER_DESCRIPTION = "description";
    
    public static final String PARAM_SURVEY_ID = "surveyPk";
    public static final String PARAM_PUBLISH = "publish";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_DESCRIPTION = "desc";
    public static final String PARAM_HIDE_PRIVATE_DETAILS = "hidePrivateDetails";
    public static final String PARAM_MANAGED_FILE_UUID = "mfuuid";
    
    public static final String PARAM_MAP_LAYER_SRC = "layerSrc";
    public static final String PARAM_SHAPE_TO_DB = "shpToDatabase";
    
    public static final String PARAM_LATITUDE_Y = "latitude";
    public static final String PARAM_LONGITUDE_X = "longitude";
    public static final String PARAM_BUFFER_KM = "buffer";
    public static final String PARAM_MAP_LAYER_ID = "mapLayerId";
    
    public static final String PARAM_STROKE_COLOR = "strokeColor";
    public static final String PARAM_FILL_COLOR = "fillColor";
    public static final String PARAM_SYMBOL_SIZE = "symbolSize";
    public static final String PARAM_STROKE_WIDTH = "strokeWidth";
    
    public static final String JSON_KEY_ITEMS = "items";
    
    public static final String KML_RECORD_FOLDER = "Record";
    public static final String KML_POINT_ICON_ID = "pointIcon";
    
    // var hexColorRegex = new RegExp('#[0-9A-F]{6}', 'i');
    Pattern colorPattern = Pattern.compile("#[0-9A-F]{6}", Pattern.CASE_INSENSITIVE);
    
    @Autowired
    private GeoMapLayerDAO layerDAO;
    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private ManagedFileDAO mfDAO;
    @Autowired
    private FileService fileService;
    @Autowired
    private RecordDAO recDAO;
    @Autowired
    private AttributeDAO attrDAO;
    @Autowired
    private GeoMapFeatureDAO featureDAO;
    @Autowired
    private JsonService jsonService;
    @Autowired
    private SessionFactory sessionFactory;
    
    GeometryBuilder geomBuilder = new GeometryBuilder();
    
    private Logger log = Logger.getLogger(getClass());
    
    @RequestMapping(value = LISTING_URL, method = RequestMethod.GET)
    public ModelAndView list(HttpServletRequest request, HttpServletResponse response) throws Exception { 
        ModelAndView mv = new ModelAndView("geoMapLayerListing");
        return mv;
    }
    
    @RequestMapping(value = EDIT_URL, method = RequestMethod.GET)
    public ModelAndView view(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = GEO_MAP_LAYER_PK_VIEW, defaultValue="0", required=false) int mapLayerPk) {
        GeoMapLayer gml = mapLayerPk == 0 ? new GeoMapLayer() : layerDAO.get(mapLayerPk);
        if (gml == null) {
            throw new IllegalArgumentException("Invalid pk for geo map layer. pk = " + mapLayerPk);
        }
        ModelAndView mv = new ModelAndView("geoMapLayerEdit");
        mv.addObject("geoMapLayer", gml);
        mv.addObject("surveyList", surveyDAO.search(null).getList());
        return mv;
    }
    
    private static final int BATCH_SIZE = 20;
    
    @RequestMapping(value = EDIT_URL, method = RequestMethod.POST)
    public ModelAndView save(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = GEO_MAP_LAYER_PK_SAVE, defaultValue="0", required=false) int mapLayerPk,
            @RequestParam(value = PARAM_SURVEY_ID, defaultValue="0", required=false) int surveyPk,
            @RequestParam(value = PARAM_NAME, required=true) String name,
            @RequestParam(value = PARAM_DESCRIPTION, required=true) String desc,
            @RequestParam(value = PARAM_PUBLISH, defaultValue="false") boolean publish,
            @RequestParam(value = PARAM_HIDE_PRIVATE_DETAILS, defaultValue="false") boolean hidePrivateDetails,
            @RequestParam(value = PARAM_MANAGED_FILE_UUID, defaultValue="") String mfuuid,
            @RequestParam(value = PARAM_MAP_LAYER_SRC, required=true) String mapLayerSrc,
            @RequestParam(value = PARAM_SHAPE_TO_DB, defaultValue="false") boolean shapeToDatabase,
            @RequestParam(value = PARAM_STROKE_COLOR, required=true) String strokeColor,
            @RequestParam(value = PARAM_FILL_COLOR, required=true) String fillColor,
            @RequestParam(value = PARAM_SYMBOL_SIZE, required=true) int symbolSize,
            @RequestParam(value = PARAM_STROKE_WIDTH, required=true) int strokeWidth) throws IOException {
        
        Session sesh = null;
        try {
            sesh = sessionFactory.openSession();

            sesh.beginTransaction();
            
            GeoMapLayer gml = mapLayerPk == 0 ? new GeoMapLayer() : layerDAO.get(sesh, mapLayerPk);
            if (gml == null) {
                throw new IllegalArgumentException("Invalid pk for geo map layer. pk = " + mapLayerPk);
            }
            
            strokeColor = strokeColor.trim();
            fillColor = fillColor.trim();
            boolean validStrokeColor = colorPattern.matcher(strokeColor).matches();
            boolean validFillColor = colorPattern.matcher(fillColor).matches();
            
            gml.setName(name.trim());
            gml.setDescription(desc.trim());
            gml.setSurvey(surveyDAO.getSurvey(sesh, surveyPk));
            gml.setPublish(publish);
            gml.setHidePrivateDetails(hidePrivateDetails);
            gml.setManagedFileUUID(mfuuid.trim());
            gml.setLayerSource(GeoMapLayerSource.fromString(mapLayerSrc));
            gml.setStrokeColor(validStrokeColor ? strokeColor : GeoMapLayer.DEFAULT_STROKE_COLOR);
            gml.setStrokeWidth(strokeWidth > 0 ? strokeWidth : 0);
            gml.setSymbolSize(symbolSize > 0 ? symbolSize : 0);
            gml.setFillColor(validFillColor ? fillColor : GeoMapLayer.DEFAULT_FILL_COLOR);
            
            if (mapLayerPk == 0) {
                layerDAO.save(sesh, gml);
            } else {
                layerDAO.update(sesh, gml);
            }
            
            if (gml.getLayerSource() == GeoMapLayerSource.SHAPEFILE && shapeToDatabase) {
                // delete all existing records
                List<GeoMapFeature> featuresToDelete = featureDAO.find(sesh, gml.getId());
                
                List<Attribute> attrToDelete = gml.getAttributes();
                gml.setAttributes(Collections.EMPTY_LIST);
                layerDAO.update(sesh, gml);
                
                for (GeoMapFeature f : featuresToDelete) {
                    featureDAO.deleteCascade(sesh, f);
                }
                
                for (Attribute a : attrToDelete) {
                 // sometimes because of errors during writes of large shapefiles to the
                    // database we can get AttributeValues that aren't assigned to geo map features.
                    // so....
                    for (AttributeValue av : attrDAO.getAttributeValueObjects(sesh, a)) {
                        attrDAO.delete(sesh, av);
                    }
                    attrDAO.delete(sesh, a);
                }
                sesh.flush();
                
                List<GeoMapFeature> featuresToAdd = null;
               
                sesh.setFlushMode(FlushMode.MANUAL);
                
                
                // now insert the new stuff...
                ManagedFile mf = mfDAO.getManagedFile(sesh, gml.getManagedFileUUID());
                File file = fileService.getFile(mf, mf.getFilename()).getFile();
                
                ShapeFileReader reader = new ShapeFileReader(file);
    
                List<Attribute> attributeList = reader.readAttributes();
                featuresToAdd = reader.readAsMapFeatures(attributeList);
                
                int weight = 1;
                gml.setAttributes(attributeList);
                for (Attribute a : gml.getAttributes()) {
                    a.setWeight(++weight);
                    attrDAO.save(sesh, a);
                }
                sesh.flush();
                
                int insertCount = 0;
                int featureCount = 0;
                while (!featuresToAdd.isEmpty()) {
                    // so garbage collection runs on big lists...
                    GeoMapFeature f = featuresToAdd.remove(0);

                    for (AttributeValue av : f.getAttributes()) {
                        attrDAO.save(sesh, av);
                        insertCount = checkBatch(sesh, insertCount);
                    }
                    f.setLayer(gml);
                    featureDAO.save(sesh, f);
                    insertCount = checkBatch(sesh, insertCount);
                    
                    if (++featureCount % 100 == 0) {
                        log.debug("feature # " + featureCount);
                    }
                }   
                layerDAO.update(sesh, gml);
                
                getRequestContext().addMessage("bdrs.geoMapLayer.save.successWithDatabaseWrite", new Object[] { gml.getName() });
            } else if (gml.getLayerSource() == GeoMapLayerSource.SHAPEFILE && !shapeToDatabase) {
                getRequestContext().addMessage("bdrs.geoMapLayer.save.successNoDatabaseWrite", new Object[] { gml.getName() });
            } else {
                getRequestContext().addMessage("bdrs.geoMapLayer.save.success", new Object[] { gml.getName() });
            }
            
            if (!validStrokeColor || !validFillColor) {
                getRequestContext().addMessage("bdrs.geoMapLayer.save.invalidColor", new Object[] { gml.getName() });
            }
        } finally {
            if (sesh != null) {
                if (sesh.isOpen()) {
                    sesh.flush();
                    sesh.clear();
                    sesh.getTransaction().commit();
                    
                    sesh.close();
                }
            }
        }
        ModelAndView mv = new ModelAndView(new RedirectView(LISTING_URL, true));
        
        return mv;
    }
    
    private int checkBatch(Session sesh, int count) {
        if (count % BATCH_SIZE == 0) {
            sesh.flush();
            sesh.clear();
            
            sesh.getTransaction().commit();
            sesh.beginTransaction();
        }
        return count+1;
    }
    
    @RequestMapping(value = LIST_SERVICE_URL, method = RequestMethod.GET)
    public void listService(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "description", defaultValue = "") String description,
            @RequestParam(value = "mapPk", required=false) Integer mapPk,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        JqGridDataHelper jqGridHelper = new JqGridDataHelper(request);       
        PaginationFilter filter = jqGridHelper.createFilter(request);
        
        PagedQueryResult<GeoMapLayer> queryResult = layerDAO.search(filter, name, description);
        
        JqGridDataBuilder builder = new JqGridDataBuilder(jqGridHelper.getMaxPerPage(), queryResult.getCount(), jqGridHelper.getRequestedPage());
        
        if (queryResult.getCount() > 0) {
            for (GeoMapLayer layer : queryResult.getList()) {
                JqGridDataRow row = new JqGridDataRow(layer.getId());
                row
                .addValue("name", layer.getName())
                .addValue("description", layer.getDescription());
                builder.addRow(row);
            }
        }
        response.setContentType("application/json");
        response.getWriter().write(builder.toJson());
    }
    
    @RequestMapping(value = GET_LAYER_URL, method = RequestMethod.GET)
    public void getLayer(
            @RequestParam(value = "layerPk", required=true) int layerPk,
            HttpServletRequest request, HttpServletResponse response) throws Exception  {
        GeoMapLayer gml = layerDAO.get(layerPk);
        
        response.setContentType("application/vnd.google-earth.kml+xml");
        response.setHeader("Content-Disposition", "attachment;filename=layer_"+System.currentTimeMillis()+".kml");
        
        if (gml.getLayerSource() == GeoMapLayerSource.KML) {
            if (!StringUtils.hasLength(gml.getManagedFileUUID())) {
                response.setStatus(400);
                log.error("Layer configured to read a KML managed file but no file UUID is assigned to the layer");
                return;
            }
            ManagedFile mf = mfDAO.getManagedFile(gml.getManagedFileUUID());
            if (mf == null) {
                log.error("Can't find managed file with uuid : " + gml.getManagedFileUUID());
                response.setStatus(404);
                return;
            }
            FileDataSource fsrc = fileService.getFile(mf, mf.getFilename());
            InputStream fileIn = fsrc.getInputStream();
            try {
                IOUtils.copy(fileIn, response.getOutputStream());
            } finally {
                fileIn.close();
            }
        } else if (gml.getLayerSource() == GeoMapLayerSource.SURVEY_KML) {
            if (gml.getSurvey() == null) {
                log.error("Layer configured to use a survey to produce KML but there is no survey assigned to the layer");
                response.setStatus(400);
                return;
            }
            
            try {
                PagedQueryResult<Record> pagedRecordQuery = recDAO.search(null, gml.getSurvey().getId(), null);
                List<Record> recordList = pagedRecordQuery.getCount() > 0 ? pagedRecordQuery.getList() : Collections.EMPTY_LIST;
                KMLUtils.writeRecordsToKML(request.getContextPath(), 
                                           request.getParameter("placemark_color"), 
                                           recordList, 
                                           response.getOutputStream());
            } catch (JAXBException e) {
                log.error(e);
                throw e;
            } catch (IOException e) {
                log.error(e);
                throw e;
            }

        } else {
            // We are displaying the records using MapServer
        }
    }
    
    @RequestMapping(value=GET_FEATURE_SERVICE_URL, method=RequestMethod.GET) 
    public void getFeatureInfo(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value=PARAM_LATITUDE_Y, required = true) double latitude_y,
            @RequestParam(value=PARAM_LONGITUDE_X, required = true) double longitude_x,
            @RequestParam(value=PARAM_BUFFER_KM, required=true) double bufferKm,
            @RequestParam(value=PARAM_MAP_LAYER_ID, required=false) Integer[] mapLayedIds) throws IOException {
        
        if (mapLayedIds == null) {
            mapLayedIds = new Integer[]{};
        }
        
        Point point = geomBuilder.createPoint(longitude_x, latitude_y);
        Geometry spatialFilter = bufferKm > 0d ? geomBuilder.bufferInKm(point, bufferKm) : point;
        List<GeoMapFeature> gmfList = featureDAO.find(mapLayedIds, spatialFilter);
        List<Record> recList = recDAO.find(mapLayedIds, spatialFilter);
        
        JSONArray itemArray = new JSONArray();
        for (Record record : recList) {
            itemArray.add(jsonService.toJson(record));
        }
        for (GeoMapFeature f : gmfList) {
            itemArray.add(jsonService.toJson(f));
        }
	
        JSONObject parentObj = new JSONObject();
        parentObj.put(JSON_KEY_ITEMS, itemArray);
        
        writeJson(request, response, parentObj.toString());
    }
    
    // time limit of 300 secs / 5 minutes
    public static final int TIME_LIMIT_SECS = 300;
    
    // rough estimate from profiling
    private static final double SEC_PER_SHAPEFILE_ITEM = 0.007d;  
    private static final double SEC_PER_MIN = 60d;
    
    public static final String JSON_KEY_MESSAGE = "message";
    public static final String JSON_KEY_STATUS = "status";
    public static final String JSON_STATUS_ERROR = "error";
    public static final String JSON_STATUS_WARN = "warn";
    public static final String JSON_STATUS_OK = "ok";
    
    @RequestMapping(value=CHECK_SHAPEFILE_SERVICE_URL, method=RequestMethod.GET) 
    public void checkShapefile(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value=PARAM_MANAGED_FILE_UUID, required=true) String uuid) throws IOException {
        
        JSONObject parentObj = new JSONObject();
        JSONArray messageArray = new JSONArray();
        
        if (!StringUtils.hasLength(uuid)) {
            messageArray.add("Cannot have a blank uuid");
            parentObj.put(JSON_KEY_MESSAGE, messageArray);
            parentObj.put(JSON_KEY_STATUS, JSON_STATUS_ERROR);
            writeJson(request, response, parentObj.toString());
            return;
        }
        ManagedFile mf = mfDAO.getManagedFile(uuid);
        
        if (mf == null) {
            messageArray.add("This file UUID does not exist");
            parentObj.put(JSON_KEY_MESSAGE, messageArray);
            parentObj.put(JSON_KEY_STATUS, JSON_STATUS_ERROR);
            writeJson(request, response, parentObj.toString());
            return;
        }
        
        File file = null;
        try {
            file = fileService.getFile(mf, mf.getFilename()).getFile();
        } catch (IllegalArgumentException e) {
            log.warn("Could not find file", e);
        }
        
        if (file == null) {
            messageArray.add("Could not retrieve the requested file for this UUID");
            parentObj.put(JSON_KEY_MESSAGE, messageArray);
            parentObj.put(JSON_KEY_STATUS, JSON_STATUS_ERROR);
            writeJson(request, response, parentObj.toString());
            return;
        }
        
        ShapeFileReader reader = new ShapeFileReader(file);
        List<Attribute> attributeList = reader.readAttributes();
        List<GeoMapFeature> featuresToAdd = reader.readAsMapFeatures(attributeList);
        
        boolean warn = false;
        
        int numAttr = attributeList.size();
        int numFeatures = featuresToAdd.size();
        double estimatedSec = (numAttr*numFeatures*SEC_PER_SHAPEFILE_ITEM);
        
        if (estimatedSec > TIME_LIMIT_SECS) {
            warn = true;
            Integer estimatedMinutes = ((Double)Math.ceil(estimatedSec / SEC_PER_MIN)).intValue();
            messageArray.add("The file you have selected has been detected to be large, we estimate it will take more than " 
                             + estimatedMinutes.toString() + 
                             " minutes to save.\nAttempting to store this shapefile to the database will take a long time, " 
                             + "and there is no guarantee it will work.\n"
                             + "You may want to reduce the area of interest or detail of your file to make it more manageable.");
        }
        
        if (!reader.isCrsSupported()) {
            warn = true;
            messageArray.add("The file you have selected has an unsupported coordinate reference system (CRS): " 
                             + reader.getCrsCode() 
                             + "\n\nThe supported CRS are " + org.apache.commons.lang.StringUtils.join(reader.getSupportedCrsCodes().toArray(), ", "));
        }
        
        if (warn) {
            parentObj.put(JSON_KEY_MESSAGE, messageArray);
            parentObj.put(JSON_KEY_STATUS, JSON_STATUS_WARN);
            writeJson(request, response, parentObj.toString());
            return;
        }
        
        // we've got through all of that so...
        parentObj.put(JSON_KEY_STATUS, JSON_STATUS_OK);
        writeJson(request, response, parentObj.toString());
    }
}
