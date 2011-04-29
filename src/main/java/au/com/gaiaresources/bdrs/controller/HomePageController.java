package au.com.gaiaresources.bdrs.controller;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.message.Message;
import au.com.gaiaresources.bdrs.model.group.Group;
import au.com.gaiaresources.bdrs.model.group.GroupDAO;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;

@Controller
public class HomePageController extends AbstractController {

    @Autowired
    private RecordDAO recordDAO;
    @Autowired
    private GroupDAO groupDAO;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private SurveyDAO surveyDAO;

    Logger log = Logger.getLogger(getClass());

    public static final String[] MOBILE_TAGS = { "android", "iphone", "ipad" };

    @RequestMapping(value = "/home.htm", method = RequestMethod.GET)
    public ModelAndView render(HttpServletRequest request,
            HttpServletResponse response) {
        log.debug("User Agent is : " + request.getHeader("User-Agent"));
        Integer id = null;
        ModelAndView view = new ModelAndView();

        // This method call will set session variables and therefore cannot
        // be invoked in the if statement
        boolean isMobileDevice = mobileHeaderCheck(request);
        log.debug(isMobileDevice);

        // Check to see if we are signed into a survey.
        if ((request.getParameter("surveyId") != null)
                && (!request.getParameter("surveyId").isEmpty())) {
            id = new Integer(request.getParameter("surveyId"));
            request.getSession().setAttribute("surveyId", id);
            view.setViewName("surveylogin");
        }

        if (!isMobileDevice) {
            if (request.getParameter("signin") != null) {
                view.setViewName("signin");
            } else {
                if (id == null) {
                    view.setViewName("home");
                    Record latestRecord = recordDAO.getLatestRecord();
                    view.addObject("recordCount", recordDAO.countAllRecords());
                    view.addObject("latestRecord", latestRecord);
                    view.addObject("uniqueSpeciesCount", recordDAO.countUniqueSpecies());
                    view.addObject("userCount", userDAO.countUsers());
                    view.addObject("publicSurveys", surveyDAO.getActivePublicSurveys(true));
                    if (latestRecord != null) {
                        User u = latestRecord.getUser();
                        String schoolName = u.getMetadataValue(Metadata.SCHOOL_NAME_KEY);
                        if (schoolName != null && !schoolName.isEmpty()) {
                            view.addObject("klassOrSchoolName", schoolName);
                        } else {
                            Group klass = groupDAO.getClassForUser(u);
                            String klassName = klass == null ? null
                                    : klass.getName();
                            view.addObject("klassOrSchoolName", klassName);
                        }
                    }
                } else {
                    view.setViewName("signin");
                }
            }
        } else {
            if (!getRequestContext().isAuthenticated()) {
                view.setViewName("loginmobile");
            } else {
                view.setView(new RedirectView("/bdrs/mobile/home.htm"));
            }
        }
        return view;
    }

    @RequestMapping(value = "/deviceDataStore.htm", method = RequestMethod.POST)
    public void storeDeviceData(HttpServletRequest request,
            HttpServletResponse response) {
        request.getSession().setAttribute("screenwidth", request.getParameter("screenwidth"));
        request.getSession().setAttribute("screenheight", request.getParameter("screenheight"));
        request.getSession().setAttribute("useragent", request.getHeader("User-Agent"));
        if (new Integer(request.getParameter("screenwidth")) > 480)
            request.getSession().setAttribute("device", "netbook");
    }

    /**
     * Returns true if the device is a mobile device, and they have not
     * requested a desktop session.
     * 
     * @param req
     * @return
     */
    public boolean mobileHeaderCheck(HttpServletRequest req) {
        req.getSession().setAttribute("device", "desktop");

        String userAgent = req.getHeader("user-agent").toLowerCase();
        if (req.getSession().getAttribute("sessionType") != null) {
            if (req.getSession().getAttribute("sessionType").equals("desktop")) {
                return false;
            } else if (req.getSession().getAttribute("sessionType").equals("mobile")) {
                return true;
            }
        }

        for (int i = 0; i < MOBILE_TAGS.length; i++) {
            if (userAgent.contains(MOBILE_TAGS[i].toLowerCase())) {
                req.getSession().setAttribute("device", MOBILE_TAGS[i]);
                return true;
            }
        }

        return false;
    }

    /**
     * Sends you back to the login page, passing on any URL parameters that were
     * in the original request.
     * 
     * @param request
     * @return
     */
    @RequestMapping(value = "/loginfailed.htm", method = RequestMethod.GET)
    public ModelAndView renderLoginFailed(HttpServletRequest request) {
        getRequestContext().addMessage(new Message("login.failed"));

        String referer = request.getHeader("Referer");

        String urlparams = "";
        if (referer != null) {
            urlparams = referer.substring(referer.indexOf('?'));
        }

        return new ModelAndView(new RedirectView("/home.htm" + urlparams, true));
    }

    /**
     * Redirects logged in user to the appropriate page.
     * 
     * @param req
     * @param res
     * @return
     */
    @RequestMapping(value = "/authenticated/redirect.htm", method = RequestMethod.GET)
    public String redirectForRole(HttpServletRequest req,
            HttpServletResponse res) {
        // Catch a login redirect.
        if (req.getSession().getAttribute("login-redirect") != null) {
            String url = "redirect:"
                    + req.getSession().getAttribute("login-redirect").toString();
            req.getSession().removeAttribute("login-redirect");
            return url;
        }

        boolean isMobile = mobileHeaderCheck(req);
        if (isMobile || req.getSession().getAttribute("surveyId") != null) {
            return getRedirectSecureMobileHome();
        } else if (getRequestContext().getRoles() != null) {
            List<String> rolesList = Arrays.asList(getRequestContext().getRoles());
            if (rolesList.contains(Role.ADMIN)) {
                return getRedirectAdminHome();
            } else if (rolesList.contains(Role.SUPERVISOR)) {
                return "redirect:/teacher/home.htm";
            } else if (rolesList.contains(Role.POWERUSER)) {
                return "redirect:/power/home.htm";
            } else if (rolesList.contains(Role.USER)) {
                return "redirect:/user/home.htm";
            } 
        }

        return getRedirectHome();
    }

    /**
     * Sets a session variable "sessionType" to "desktop" and redirects the user
     * to the home page
     * 
     * @param req
     * @param res
     * @return
     */
    @RequestMapping(value = "/desktopSession.htm", method = RequestMethod.GET)
    public String setDesktopSession(HttpServletRequest req,
            HttpServletResponse res) {
        req.getSession().setAttribute("sessionType", "desktop");
        return "redirect:/home.htm";
    }

    /**
     * Sets a session variable "sessionType" to "mobile" and redirects the user
     * to the home page.
     * 
     * @param req
     * @param res
     * @return
     */
    @RequestMapping(value = "/mobileSession.htm", method = RequestMethod.GET)
    public String setMobileSession(HttpServletRequest req,
            HttpServletResponse res) {
        req.getSession().setAttribute("sessionType", "mobile");
        return "redirect:/home.htm";
    }
    
	/**
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/bdrs/mobile/home.htm", method = RequestMethod.GET)
	public ModelAndView getHome(HttpServletRequest request,
			HttpServletResponse response) {
		request.getSession().setAttribute("sessionType", "mobile");
		ModelAndView mv = new ModelAndView("mobilehome");
		String ident = getRequestContext().getUser().getRegistrationKey();

		mv.addObject("surveys", surveyDAO.getSurveys(getRequestContext().getUser()));
		mv.addObject("hometype", "basic");

		if ((request.getParameter("surveyId") != null)
				&& (!request.getParameter("surveyId").isEmpty())) {
			int id = new Integer(request.getParameter("surveyId"));
			request.getSession().setAttribute("surveyId", id);
		}

		mv.addObject("manifest", "mobile.manifest?ident=" + ident);

		Cookie cookie = new Cookie("regkey", getRequestContext().getUser().getRegistrationKey());
		response.addCookie(cookie);
		
		return mv;
	}
	
/*	@RequestMapping(value = "/bdrs/mobile/index.htm", method = RequestMethod.GET)
	public ModelAndView getIndex(HttpServletRequest request,
			HttpServletResponse response) {
		log.debug("THIS MOBILE INDEX");
		ModelAndView mv = new ModelAndView("mobileTemplate");
		String ident = getRequestContext().getUser().getRegistrationKey();
		mv.addObject("manifest", "mobile.manifest?ident=" + ident);
		Cookie cookie = new Cookie("regkey", getRequestContext().getUser().getRegistrationKey());
		response.addCookie(cookie);
		
		return mv;
	}*/
}
