package au.com.gaiaresources.bdrs.controller.map;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataBuilder;
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataHelper;
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataRow;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.model.map.AssignedGeoMapLayer;
import au.com.gaiaresources.bdrs.model.map.GeoMap;
import au.com.gaiaresources.bdrs.model.map.GeoMapDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayer;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayerDAO;

@Controller
public class GeoMapController extends AbstractController {
    
    @Autowired
    GeoMapDAO geoMapDAO;
    @Autowired
    GeoMapLayerDAO mapLayerDAO;
    
    private Logger log = Logger.getLogger(getClass());
    
    // map admin
    public static final String BASE_URL = "/bdrs/admin/map/";
    public static final String LISTING_URL = BASE_URL + "listing.htm";
    public static final String EDIT_URL = BASE_URL + "edit.htm";
    public static final String DELETE_URL = BASE_URL + "delete.htm";
    
    // viewing the map
    public static final String VIEW_MAP_URL = "/bdrs/map/view.htm";
    
    public static final String LIST_SERVICE_URL = BASE_URL + "listService.htm";
    public static final String GET_AVAILABLE_MAP_SERVICE_URL = "/bdrs/public/webservice/getAvailableMaps.htm";
    public static final String LIST_MAP_LAYER_SERVICE_URL = "/bdrs/public/webservice/getMapLayersForMap.htm";
    
    public static final String GEO_MAP_PK_VIEW = "geoMapId";
    public static final String GEO_MAP_PK_SAVE = "geoMapPk";
    
    public static final String FILTER_NAME = "name";
    public static final String FILTER_DESCRIPTION = "description";
    public static final String FILTER_ANONYMOUS_ACCESS = "anonymousAccess";
    public static final String FILTER_PUBLISH = "publish";
    public static final String FILTER_MAP_PK = "mapPk";
    
    public static final String MAV_GEO_MAP = "geoMap";
    public static final String MAV_ASSIGNED_LAYERS = "assignedLayers";
    
    public static final String PARAM_MAP_LAYER_VISIBLE = "mapLayerVisible";

    @RequestMapping(value = LISTING_URL, method = RequestMethod.GET)
    public ModelAndView listing(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("geoMapListing");
        return mv;
    }
    
    @RequestMapping(value = EDIT_URL, method = RequestMethod.GET)
    public ModelAndView view(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value=GEO_MAP_PK_VIEW, defaultValue="0") Integer pk) {
        GeoMap gm = (pk == null || pk == 0) ? new GeoMap() : geoMapDAO.get(pk);
        List<AssignedGeoMapLayer> layers = mapLayerDAO.getForMap(pk);
        ModelAndView mv = new ModelAndView("geoMapEdit");
        mv.addObject(MAV_GEO_MAP, gm);
        mv.addObject(MAV_ASSIGNED_LAYERS, layers);
        return mv; 
    }
    
    @RequestMapping(value = EDIT_URL, method = RequestMethod.POST)
    public ModelAndView save(HttpServletRequest request, HttpServletResponse response, 
            @RequestParam(value=GEO_MAP_PK_SAVE, defaultValue="0") Integer pk,
            @RequestParam(value="name", required=true) String name,
            @RequestParam(value="description", required=true) String desc, 
            @RequestParam(value="anonymousAccess", required=false) boolean anonAccess,
            @RequestParam(value="publish", required=false) boolean publish,
            @RequestParam(value="mapLayerPk", required=false) Integer[] mapLayerPkList,
            @RequestParam(value=PARAM_MAP_LAYER_VISIBLE, required=false) boolean[] mapLayerVisible,
            @RequestParam(value="hidePrivateDetails", required=false) boolean hidePrivateDetails,
            @RequestParam(value="weight", required=true) int weight) {
        
        boolean newMap = (pk == null || pk == 0);
        GeoMap gm = newMap ? new GeoMap() : geoMapDAO.get(pk);
        
        if (gm == null) {
            throw new IllegalArgumentException("Invalid id to edit existing geomap. id = " + pk.toString());
        }
        gm.setName(name);
        gm.setDescription(desc);
        gm.setAnonymousAccess(anonAccess);
        gm.setPublish(publish);
        gm.setHidePrivateDetails(hidePrivateDetails);
        gm.setWeight(weight);
        
        if (newMap) {
            geoMapDAO.save(gm);
        } else {
            geoMapDAO.update(gm);
        }
        
        // remove all assigned layers
        List<AssignedGeoMapLayer> removeLayerList = mapLayerDAO.getForMap(pk);
        mapLayerDAO.delete(removeLayerList);
        
        if (mapLayerPkList == null && mapLayerVisible == null) {
            // save successfully with no map layers assigned!
            ModelAndView mv = new ModelAndView(new RedirectView(LISTING_URL, true));
            return mv;
        }
        
        if (mapLayerPkList == null || mapLayerVisible == null) {
            throw new IllegalArgumentException("argument arrays must be of equal length (one of them is null)");
        }
        
        if (mapLayerPkList.length != mapLayerVisible.length) {
            throw new IllegalArgumentException("argument arrays must be of equal length");
        }
        
        // assign new layers
        List<AssignedGeoMapLayer> mapLayerList = new ArrayList<AssignedGeoMapLayer>();
        
        for (int i=0; i<mapLayerPkList.length; ++i) {
            Integer mlpk = mapLayerPkList[i];
            boolean visible = mapLayerVisible[i];
            
            GeoMapLayer layerToAdd = mapLayerDAO.get(mlpk);
            if (layerToAdd == null) {
                throw new IllegalArgumentException("Invalid of map layer to add to geomap. id = " + mlpk.toString());
            }
            AssignedGeoMapLayer assignedLayerToAdd = new AssignedGeoMapLayer();
            assignedLayerToAdd.setLayer(layerToAdd);
            assignedLayerToAdd.setMap(gm);
            assignedLayerToAdd.setVisible(visible);
            mapLayerList.add(assignedLayerToAdd);
        }
           
        mapLayerDAO.save(mapLayerList);
        
        ModelAndView mv = new ModelAndView(new RedirectView(LISTING_URL, true));
        return mv; 
    }
    
    @RequestMapping(value = LIST_SERVICE_URL, method = RequestMethod.GET)
    public void listService(
            @RequestParam(value = FILTER_NAME, defaultValue = "") String name,
            @RequestParam(value = FILTER_DESCRIPTION, defaultValue = "") String description,
            @RequestParam(value = FILTER_MAP_PK, required=false) Integer mapPk,
            @RequestParam(value = FILTER_ANONYMOUS_ACCESS, required=false) Boolean anonAccess,
            @RequestParam(value = FILTER_PUBLISH, required=false) Boolean publish,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        JqGridDataHelper jqGridHelper = new JqGridDataHelper(request);       
        PaginationFilter filter = jqGridHelper.createFilter(request);
        
        PagedQueryResult<GeoMap> queryResult = geoMapDAO.search(filter, name, description, mapPk, anonAccess, publish);
        
        JqGridDataBuilder builder = new JqGridDataBuilder(jqGridHelper.getMaxPerPage(), queryResult.getCount(), jqGridHelper.getRequestedPage());
        
        if (queryResult.getCount() > 0) {
            for (GeoMap gm : queryResult.getList()) {
                JqGridDataRow row = new JqGridDataRow(gm.getId());
                row
                .addValue("name", gm.getName())
                .addValue("description", gm.getDescription())
                .addValue("anonymousAccess", gm.isAnonymousAccess() ? "Yes" : "No")
                .addValue("publish", gm.isPublish() ? "Yes" : "No")
                .addValue("hidePrivateDetails", gm.isHidePrivateDetails() ? "Yes" : "No")
                .addValue("weight", String.format("%d", gm.getWeight()));
                builder.addRow(row);
            }
        }
        response.setContentType("application/json");
        response.getWriter().write(builder.toJson());
    }
    
    @RequestMapping(value = GET_AVAILABLE_MAP_SERVICE_URL, method = RequestMethod.GET)
    public void getAvailableService(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Boolean anonAccess = !this.getRequestContext().isAuthenticated() ? true : null;
        
        PagedQueryResult<GeoMap> queryResult = geoMapDAO.search(null, null, null, null, anonAccess, true);
        
        List<GeoMap> mapList = queryResult.getList();
        JSONArray array = new JSONArray();
        
        if (mapList != null) {
            for(GeoMap gm : mapList) {
                array.add(gm.flatten());
            }
        }
        // support for JSONP
        if (request.getParameter("callback") != null) {
                response.setContentType("application/javascript");              
                response.getWriter().write(request.getParameter("callback") + "(");
        } else {
                response.setContentType("application/json");
        }
        response.getWriter().write(array.toString());
        if (request.getParameter("callback") != null) {
                response.getWriter().write(");");
        }
    }
    
    @RequestMapping(value = LIST_MAP_LAYER_SERVICE_URL, method = RequestMethod.GET) 
    public void getMapLayers(HttpServletRequest request, HttpServletResponse response,
        @RequestParam(value = GEO_MAP_PK_VIEW, required=false, defaultValue="0") int mapPk) throws Exception {
        
        List<AssignedGeoMapLayer> layerList = mapLayerDAO.getForMap(mapPk);
        
        JSONArray array = new JSONArray();
        
        if (layerList != null) {
            for(AssignedGeoMapLayer gm : layerList) {
                array.add(gm.flatten());
            }
        }
        // support for JSONP
        if (request.getParameter("callback") != null) {
                response.setContentType("application/javascript");              
                response.getWriter().write(request.getParameter("callback") + "(");
        } else {
                response.setContentType("application/json");
        }
        response.getWriter().write(array.toString());
        if (request.getParameter("callback") != null) {
                response.getWriter().write(");");
        }
    }
    
    @RequestMapping(value = VIEW_MAP_URL, method = RequestMethod.GET) 
    public ModelAndView viewMap(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = GEO_MAP_PK_VIEW, required=true) int mapPk) {
        boolean anonAccessRequired = !getRequestContext().isAuthenticated();
        GeoMap gm = geoMapDAO.get(mapPk);
        if (anonAccessRequired && !gm.isAnonymousAccess()) {
            response.setStatus(401);
        }
        List<AssignedGeoMapLayer> assignedLayers = mapLayerDAO.getForMap(mapPk);
        
        ModelAndView mv = new ModelAndView("viewMap");
        mv.addObject(MAV_GEO_MAP, gm);
        mv.addObject(MAV_ASSIGNED_LAYERS, assignedLayers);
        return mv;
    }
    
    @RequestMapping(value = DELETE_URL, method = RequestMethod.POST)
    public ModelAndView deleteMap(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = GEO_MAP_PK_SAVE, required=true) int mapPk) {

        GeoMap gm = geoMapDAO.get(mapPk); 
        
        List<AssignedGeoMapLayer> removeLayerList = mapLayerDAO.getForMap(mapPk);
        mapLayerDAO.delete(removeLayerList);
        
        geoMapDAO.delete(gm);
        ModelAndView mv = new ModelAndView("geoMapListing");
        return mv;
    }
}
