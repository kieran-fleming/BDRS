package au.com.gaiaresources.bdrs.controller.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.group.Group;
import au.com.gaiaresources.bdrs.model.group.GroupDAO;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordAttribute;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.kml.KMLWriter;

@Controller
public class RecordMapController extends AbstractController {

    public static final String KML_RECORD_FOLDER = "Record";
    public static final String KML_POINT_ICON_ID = "pointIcon";

    public static final int DEFAULT_LIMIT = 300;

    public static final long MILLISECONDS_IN_DAY = 86400000;

    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private RecordDAO recordDAO;
    @Autowired
    private TaxaDAO taxaDAO;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private GroupDAO groupDAO;

    public RecordMapController() {
        super();
    }

    @RequestMapping(value = "/map/recordTracker.htm", method = RequestMethod.GET)
    public ModelAndView showrecordTracker(HttpServletRequest request,
            HttpServletResponse response) throws UnsupportedEncodingException {

        boolean showDate = false;
        String showDateStr = request.getParameter("show_date");
        if(showDateStr != null){
            showDate = Boolean.parseBoolean(showDateStr);
        }

        Set<Date> recordDates = new TreeSet<Date>();
        if(showDate) {
            List<Date> dateList = recordDAO.getRecordDatesByScientificNameSearch(request.getParameter("species"));
            Calendar cal = new GregorianCalendar();
            for(Date d : dateList) {

                cal.clear();
                cal.setTime(d);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.clear(Calendar.MINUTE);
                cal.clear(Calendar.SECOND);
                cal.clear(Calendar.MILLISECOND);
                recordDates.add(cal.getTime());
            }
        }

        ModelAndView mv = new ModelAndView("recordTracker");
        mv.addObject("recordDateList", new ArrayList<Date>(recordDates));
        return mv;
    }

    @RequestMapping(value = "/map/mySightings.htm", method = RequestMethod.GET)
    public ModelAndView showMySightings(HttpServletRequest request,
            HttpServletResponse response) throws UnsupportedEncodingException {
        
        User user = getRequestContext().getUser();

        ModelAndView mv = new ModelAndView("mySightings");
        String sDefaultSurveyId = request.getParameter("defaultSurveyId");
        if (StringUtils.hasLength(sDefaultSurveyId))
        {
            try
            {
            Integer defaultSurveyId = Integer.parseInt(sDefaultSurveyId);
            mv.addObject("defaultSurveyId", defaultSurveyId);
            }
            catch (NumberFormatException e)
            {
                log.error("Could not parse string '" + sDefaultSurveyId + "' to a number. Not setting default survey id");
            }
        }
        mv.addObject("surveyList", surveyDAO.getActiveSurveysForUser(user));
        return mv;
    }

    @RequestMapping(value = "/map/recordBaseMap.htm", method = RequestMethod.GET)
    public ModelAndView showRecordBaseMap(HttpServletRequest request, HttpServletResponse response) {

        ModelAndView mv = new ModelAndView("recordBaseMap");
        User user = getRequestContext().getUser();

        List<User> users;
        Set<Group> groups = new HashSet<Group>();
        if(user.isAdmin()) {
            users = userDAO.getUsers();

            groups.addAll(groupDAO.getAllGroups());
        }
        else {
            users = new ArrayList<User>(1);
            users.add(user);

            groups.addAll(groupDAO.getGroupsForAdmin(user));
            groups.addAll(groupDAO.getGroupsForUser(user));
        }

        mv.addObject("surveys", surveyDAO.getSurveys(user));
        mv.addObject("users", users);
        mv.addObject("taxonGroups", taxaDAO.getTaxonGroups());
        mv.addObject("groups", groups);

        return mv;

    }

    @RequestMapping(value = "/map/addRecordBaseMapLayer.htm", method = RequestMethod.GET)
    public void addRecordBaseMapLayer(HttpServletRequest request,
                                      HttpServletResponse response,
                                      @RequestParam(value="ident", defaultValue="") String ident,
                                      @RequestParam(value="species", defaultValue="") String speciesScientificNameSearch,
                                      @RequestParam(value="user", defaultValue="0") int userPk,
                                      @RequestParam(value="group", defaultValue="0") int groupPk,
                                      @RequestParam(value="survey", defaultValue="0") int surveyPk,
                                      @RequestParam(value="taxon_group", defaultValue="0") int taxonGroupPk,
                                      @RequestParam(value="date_start", defaultValue="01 Jan 1970") Date startDate,
                                      @RequestParam(value="date_end", defaultValue="01 Jan 9999") Date endDate,
                                      @RequestParam(value="limit", defaultValue="300") int limit)
        throws JAXBException, IOException, ParseException {

        response.setContentType("application/vnd.google-earth.kml+xml");
        response.setHeader("Content-Disposition", "attachment;filename=layer_"+System.currentTimeMillis()+".kml");

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            dateFormat.setLenient(false);

            List<Record> recordList = recordDAO.getRecord(userPk, groupPk,
                    surveyPk, taxonGroupPk, startDate, endDate,
                    speciesScientificNameSearch, limit);

            KMLWriter writer = new KMLWriter();
            String placemark = request.getContextPath()+"/map/icon/record_placemark.png?color=";
            if(request.getParameter("placemark_color") == null) {
                placemark = placemark + "EE9900";
            } else {
                placemark = placemark + request.getParameter("placemark_color");
            }
            writer.createStyleIcon(KML_POINT_ICON_ID, placemark, 16, 16);
            writer.createFolder(KML_RECORD_FOLDER);
            String label;
            String description;
            Map<String, Object> descMap = new HashMap<String, Object>(7);
            Map<Attribute, RecordAttribute> recAttrMap = new HashMap<Attribute, RecordAttribute>();

            IndicatorSpecies species;
            for(Record record : recordList) {
                species = record.getSpecies();
                label = String.format("%d x %s @ %s", record.getNumber(), species.getScientificName(), dateFormat.format(record.getWhen()));
                descMap.clear();

                // Create a map that will be converted to a JSON object
                // { record_attribute_name: record_attribute_value,
                //   attributes: [ { attribute_description: record_attribute_value,
                //                 ... }]
                // }
                descMap.put("species", species.getScientificName());
                descMap.put("common_name", species.getCommonName());
                descMap.put("number", record.getNumber());
                descMap.put("notes", record.getNotes());
                descMap.put("habitat", record.getHabitat());
                descMap.put("when", record.getWhen().getTime());
                descMap.put("behaviour", record.getBehaviour());

                List<Map<String, Object>> orderedAttr = new ArrayList<Map<String,Object>>();
                for(RecordAttribute recAttr: record.getAttributes()) {
                    String value = recAttr.getStringValue();
                    Map<String, Object> tuple = new HashMap<String, Object>(1);
                    tuple.put(recAttr.getAttribute().getDescription(), value);
                    orderedAttr.add(tuple);
                }
                descMap.put("attributes", orderedAttr);

                description = JSONObject.fromObject(descMap).toString();

                if(record.getPoint() != null) {
                    writer.createPlacemark(KML_RECORD_FOLDER, label, description, record.getPoint(), KML_POINT_ICON_ID);
                } else if(record.getLocation() != null && record.getLocation().getLocation() != null) {
                    writer.createPlacemark(KML_RECORD_FOLDER, label, description, record.getLocation().getLocation(), KML_POINT_ICON_ID);
                } else {
                    log.info("Cannot find coordinate for record");
                }
            }

            writer.write(false, response.getOutputStream());

        } catch (JAXBException e) {
            log.error(e);
            throw e;
        } catch (IOException e) {
            log.error(e);
            throw e;
        }
    }

    @RequestMapping(value = "/map/icon/record_placemark.png", method = RequestMethod.GET)
    public void renderRecordPlacemark(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D)img.getGraphics();
        Color borderColor = new Color(238,153,0);

        if(request.getParameter("color") != null){
            int color = Integer.parseInt(request.getParameter("color"), 16);
            borderColor = new Color(color);
        }
        Color fillColor  = new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), 160);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fillColor);
        g2.fillOval(12,12,8,8);

        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawOval(12,12,8,8);

        response.setContentType("image/png");
        ImageIO.write(img, "png", response.getOutputStream());
    }

    @InitBinder
    public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(
                dateFormat, true));
    }
}
