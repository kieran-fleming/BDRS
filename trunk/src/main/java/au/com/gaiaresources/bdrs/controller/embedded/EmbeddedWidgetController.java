package au.com.gaiaresources.bdrs.controller.embedded;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.user.UserDAO;

@Controller
public class EmbeddedWidgetController extends AbstractController {

    public static final int DEFAULT_WIDTH = 250;
    public static final int DEFAULT_HEIGHT = 300;

    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private RecordDAO recordDAO;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private SurveyDAO surveyDAO;

    @RequestMapping(value = "/bdrs/public/embedded/widgetBuilder.htm", method = RequestMethod.GET)
    public ModelAndView widgetBuilder(HttpServletRequest request,
            HttpServletResponse response) {

        ModelAndView mv = new ModelAndView("widgetBuilder");
        mv.addObject("domain", request.getServerName());
        mv.addObject("port", request.getServerPort());
        return mv;
    }

    @RequestMapping(value = "/bdrs/public/embedded/bdrs-embed.js", method = RequestMethod.GET)
    public ModelAndView generateEmbeddedJS(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "domain", required = false) String domain,
            @RequestParam(value = "port", required = false) String port,
            @RequestParam(value = "contextPath", required = false) String contextPath,
            @RequestParam(value = "targetId", required = false) String targetId,
            @RequestParam(value = "width", required = false) String widthStr,
            @RequestParam(value = "height", required = false) String heightStr,
            @RequestParam(value = "feature", required = false) String featureStr) {

        domain = domain == null ? request.getServerName() : domain;
        port = port == null ? String.valueOf(request.getServerPort()) : port;

        int width;
        try {
            width = Integer.parseInt(widthStr);
        } catch (NumberFormatException nfe) {
            width = DEFAULT_WIDTH;
        }

        int height;
        try {
            height = Integer.parseInt(heightStr);
        } catch (NumberFormatException nfe) {
            height = DEFAULT_HEIGHT;
        }
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.putAll(toSimpleParameterMap(request.getParameterMap()));
        params.put("domain", domain);
        params.put("port", port);
        params.put("contextPath", contextPath);
        params.put("targetId", targetId);
        params.put("height", height);
        params.put("width", width);
        params.put("feature", featureStr);
        params.put("showFooter", request.getParameterMap().containsKey("showFooter"));

        ModelAndView mv = new ModelAndView("bdrs_embed_js");
        mv.addAllObjects(params);
        mv.addObject("paramMap", params);
        
        response.setContentType("text/javascript");

        return mv;
    }

    @RequestMapping(value = "/bdrs/public/embedded/bdrs-embed.css", method = RequestMethod.GET)
    public ModelAndView generateEmbeddedJS(HttpServletRequest request,
            HttpServletResponse response) {

        ModelAndView mv = new ModelAndView("bdrs_embed_css");
        mv.addAllObjects(toSimpleParameterMap(request.getParameterMap()));
        response.setContentType("text/css");

        return mv;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/bdrs/public/embedded/redirect.htm", method = RequestMethod.GET)
    public ModelAndView edit(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "feature", required = false) String featureStr) {

        EmbeddedFeature feature = featureStr == null ? EmbeddedFeature.LATEST_STATISTICS
                : EmbeddedFeature.valueOf(featureStr);

        ModelAndView mv = new ModelAndView(
                new RedirectView(
                        String.format("/bdrs/public/embedded/%s.htm", feature.toString().toLowerCase()),
                        true));
        mv.addAllObjects(request.getParameterMap());
        return mv;
    }

    @RequestMapping(value = "/bdrs/public/embedded/latest_statistics.htm", method = RequestMethod.GET)
    public ModelAndView latest_statistics(HttpServletRequest request,
            HttpServletResponse response) {

        Record latestRecord = recordDAO.getLatestRecord();

        Map<String, Object> params = new HashMap<String, Object>();
        params.putAll(toSimpleParameterMap(request.getParameterMap()));
        params.put("recordCount", recordDAO.countAllRecords());
        params.put("latestRecord", latestRecord);
        params.put("uniqueSpeciesCount", recordDAO.countUniqueSpecies());
        params.put("userCount", userDAO.countUsers());
        params.put("publicSurveys", surveyDAO.getActivePublicSurveys(true));

        ModelAndView mv = new ModelAndView("latest_statistics");
        mv.addAllObjects(params);
        mv.addObject("paramMap", params);
        
        response.setContentType("text/javascript");
        

        return mv;
    }

    private Map<String, String> toSimpleParameterMap(Map requestParameterMap) {
        Map<String, String> simple = new HashMap<String, String>(
                requestParameterMap.size());
        for (Map.Entry<String, String[]> entry : ((Map<String, String[]>) requestParameterMap).entrySet()) {
            if (entry.getValue().length > 0) {
                simple.put(entry.getKey(), entry.getValue()[0]);
            }
        }
        return simple;
    }
}
