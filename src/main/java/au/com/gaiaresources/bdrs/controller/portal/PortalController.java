package au.com.gaiaresources.bdrs.controller.portal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.PortalDAO;
import au.com.gaiaresources.bdrs.model.portal.PortalEntryPoint;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.servlet.filter.PortalMatches;
import au.com.gaiaresources.bdrs.servlet.filter.PortalSelectionFilter;
import au.com.gaiaresources.bdrs.servlet.filter.PortalSelectionFilterMatcher;

/**
 * The <code>PortalController</code> handles all view requests for portal wide
 * configuration.
 */
@Controller
public class PortalController extends AbstractController {
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());
    
    public static final String PORTAL_ENTRY_POINT_EDIT_PATTERN_TMPL = "entryPoint_pattern_%d";
    public static final String PORTAL_ENTRY_POINT_EDIT_REDIRECT_TMPL = "entryPoint_redirect_%d";
    
    public static final String PORTAL_ENTRY_POINT_ADD_PATTERN_TMPL = "add_entryPoint_pattern_%d";
    public static final String PORTAL_ENTRY_POINT_ADD_REDIRECT_TMPL = "add_entryPoint_redirect_%d";
    
    private PortalSelectionFilterMatcher portalFilterMatcher;
    
    @Autowired
    private PortalDAO portalDAO;
    @Autowired
    private SessionFactory sessionFactory;
    
    /**
     * Initialises this controller after dependency injection has been completed.
     * @throws Exception
     */
    @PostConstruct
    public void init() throws Exception {
        portalFilterMatcher = new PortalSelectionFilterMatcher(portalDAO);
    }

    @RolesAllowed( { Role.ROOT })
    @RequestMapping(value = "/bdrs/root/portal/listing.htm", method = RequestMethod.GET)
    public ModelAndView listing(HttpServletRequest request,
            HttpServletResponse response) {
        
        ModelAndView mv = new ModelAndView("portalSetup");
        mv.addObject("portalList", portalDAO.getPortals());
        return mv;
    }
    
    @RolesAllowed( { Role.ROOT })
    @RequestMapping(value = "/bdrs/root/portal/edit.htm", method = RequestMethod.GET)
    public ModelAndView edit(   HttpServletRequest request, 
                                HttpServletResponse response,
                                @RequestParam(value="id", required=false, defaultValue="0") int pk) {
        
        Portal portal = portalDAO.getPortal(pk);
        List<PortalEntryPoint> portalEntryPointList;
        if(portal == null) {
            portal = new Portal();
            portalEntryPointList = new ArrayList<PortalEntryPoint>();
        } else {
            portalEntryPointList = portalDAO.getPortalEntryPoints(portal);
        }
        
        ModelAndView mv = new ModelAndView("portalEdit");
        mv.addObject("portal", portal);
        mv.addObject("portalEntryPointList", portalEntryPointList);
        return mv;
    }
    
    @RolesAllowed( { Role.ROOT })
    @RequestMapping(value = "/bdrs/root/portal/edit.htm", method = RequestMethod.POST)
    public ModelAndView editSubmit( HttpServletRequest request, 
                                    HttpServletResponse response,
        @RequestParam(value="portalId", required=false, defaultValue="0") int pk,
        @RequestParam(value="name", required=true) String name,
        @RequestParam(value="default", required=false, defaultValue="false") boolean isDefault,
        @RequestParam(value="add_portalEntryPoint", required=false) int[] add_portalEntryPointIndexes,
        @RequestParam(value="portalEntryPoint_id", required=false) int[] portalEntryPointPks) throws Exception {
        
        Portal portal = parsePortalEditForm(getRequestContext().getHibernate(),
                                            request, pk, name, isDefault, add_portalEntryPointIndexes, portalEntryPointPks);
        
        getRequestContext().addMessage("bdrs.portal.save.success", new Object[]{portal.getName()});
        return new ModelAndView(new RedirectView("/bdrs/root/portal/listing.htm", true));
    }

    @RolesAllowed( { Role.ROOT })
    @RequestMapping(value = "/bdrs/root/portal/ajaxAddPortalEntryPoint.htm", method = RequestMethod.GET)
    public ModelAndView ajaxAddPortalEntryPoint(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value="index", required=true) int index) {
       
        ModelAndView mv = new ModelAndView("portalEntryPointRow");
        mv.addObject("portalEntryPoint", new PortalEntryPoint());
        mv.addObject("index", index);
        
        return mv;
    }
    
    /**
     * Tests the specified test URL against all current portals and 
     * the portals and entry points currently on the editing form but not yet 
     * saved. 
     */
    @RolesAllowed( { Role.ROOT })
    @RequestMapping(value = "/bdrs/root/portal/ajaxTestPortalEntryPointPattern.htm", method = RequestMethod.GET)
    public void ajaxTestPortalEntryPointPattern(HttpServletRequest request, HttpServletResponse response,
                                                @RequestParam(value="portalId", required=false, defaultValue="0") int pk,
                                                @RequestParam(value="name", required=true) String name,
                                                @RequestParam(value="default", required=false, defaultValue="false") boolean isDefault,
                                                @RequestParam(value="add_portalEntryPoint", required=false) int[] add_portalEntryPointIndexes,
                                                @RequestParam(value="portalEntryPoint_id", required=false) int[] portalEntryPointPks,
                                                @RequestParam(value="testUrl", required=true) String testUrl) throws Exception {

        // A new session is started, and later rollbacked because we will 
        // first perform a 'fake' save of all form data to create the necessary
        // Portal and PortalEntryPoints. Next, this data will be fed into the
        // PortalSelectionFilterMatcher using the session below. When complete
        // the json data will be encoded and the session roll backed to remove
        // the data that was temporarily 'saved' for the purposes of 
        // portal matching.
        Session sesh = sessionFactory.openSession();
        sesh.disableFilter(PortalPersistentImpl.PORTAL_FILTER_PORTALID_PARAMETER_NAME);
        sesh.beginTransaction();
        
        parsePortalEditForm(sesh, request, pk, name, isDefault, add_portalEntryPointIndexes, portalEntryPointPks);
        
        PortalMatches matches = portalFilterMatcher.match(sesh, testUrl);
        
        Map<String, Object> content = new HashMap<String, Object>();
        content.put("defaultPortal", matches.getDefaultPortal().flatten());
        content.put("matchedPortal", matches.getMatchedPortal() == null ? null : matches.getMatchedPortal().flatten());
        content.put("matchedEntryPoint", matches.getMatchedEntryPoint() == null ? null : matches.getMatchedEntryPoint().flatten());
        content.put("testURL", testUrl);
        
        JSONArray invalidEntries = new JSONArray();
        for(PortalEntryPoint invalid : matches.getInvalidPatternList()) {
            invalidEntries.add(invalid.flatten());
        }
        
        content.put("invalidPatternList", invalidEntries);
        
        
        response.setContentType("application/json");
        response.getWriter().write(JSONObject.fromObject(content).toString());
        
        sesh.getTransaction().rollback();
        sesh.close();
    }
    
    private Portal parsePortalEditForm(Session sesh, HttpServletRequest request, int pk,
            String name, boolean isDefault, int[] add_portalEntryPointIndexes,
            int[] portalEntryPointPks) throws Exception {
        
        Portal portal = portalDAO.getPortal(sesh, pk);
        if(portal == null) {
            portal = new Portal();
        }
        portal.setName(name);
        portal.setDefault(isDefault);
        if(isDefault) {
            // Only one default portal is allowed.
            Portal defaultPortal = portalDAO.getPortal(true);
            if(portal.getId() != null && !portal.equals(defaultPortal)) {
                defaultPortal.setDefault(false);
                defaultPortal = portalDAO.save(sesh, defaultPortal);
            }
        }
        portal = portalDAO.save(sesh, portal);
        
        Map<Integer, PortalEntryPoint> portalEntryPointMap = new HashMap<Integer, PortalEntryPoint>(); 
        for(PortalEntryPoint ep : portalDAO.getPortalEntryPoints(sesh, portal)) {
            portalEntryPointMap.put(ep.getId(), ep);
        }
        
        PortalEntryPoint entryPoint;
        String pattern;
        String redirect;
        
        // Edited Portal Entries First.
        if(portalEntryPointPks != null) {
            for(int entryPk : portalEntryPointPks) {
                entryPoint = portalEntryPointMap.remove(entryPk);
                
                pattern = request.getParameter(String.format(PORTAL_ENTRY_POINT_EDIT_PATTERN_TMPL, entryPk));
                redirect = request.getParameter(String.format(PORTAL_ENTRY_POINT_EDIT_REDIRECT_TMPL, entryPk));
                entryPoint.setPortal(portal);
                entryPoint.setPattern(pattern);
                entryPoint.setRedirect(redirect);
                entryPoint = portalDAO.save(sesh, entryPoint);
            }
        }
        
        if(add_portalEntryPointIndexes != null) {
            for(int index : add_portalEntryPointIndexes) {
                entryPoint = new PortalEntryPoint();
                
                pattern = request.getParameter(String.format(PORTAL_ENTRY_POINT_ADD_PATTERN_TMPL, index));
                redirect = request.getParameter(String.format(PORTAL_ENTRY_POINT_ADD_REDIRECT_TMPL, index));
                
                entryPoint.setPortal(portal);
                entryPoint.setPattern(pattern);
                entryPoint.setRedirect(redirect);
                entryPoint = portalDAO.save(sesh, entryPoint);
            }
        }
        
        // Delete the remaining entry points.
        for(Map.Entry<Integer, PortalEntryPoint> mapEntry : portalEntryPointMap.entrySet()) {
            portalDAO.delete(sesh, mapEntry.getValue());
        }
        return portal;
    }
    
    @RequestMapping(value = "/portal/**")
    public void restfulPortalRequestForward(HttpServletRequest request, 
                                                    HttpServletResponse response) throws ServletException, IOException {
        Pattern pattern = Pattern.compile(PortalSelectionFilter.RESTFUL_PORTAL_PATTERN_STR);
        Matcher matcher = pattern.matcher(request.getServletPath());
        
        String subServletPath = matcher.replaceFirst("");
        RequestDispatcher dispatcher = request.getRequestDispatcher("/"+subServletPath);
        dispatcher.forward(request, response);
    }
}
