package au.com.gaiaresources.bdrs.controller.user;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;

@Controller
public class UserHomePageController extends AbstractController {
    Logger log = Logger.getLogger(UserHomePageController.class);
    
    @Autowired
	private RecordDAO recordDAO;

    @Autowired
	private LocationDAO locationDAO;
    
    @RequestMapping(value = "/user/home.htm", method = RequestMethod.GET)
    public ModelAndView render(HttpServletRequest request) {
        ModelAndView view = new ModelAndView("userHome");
        view.addObject("recordCount", recordDAO.countRecords(getRequestContext().getUser()));
        System.err.println(getRequestContext().getUser());
        view.addObject("locationCount", locationDAO.countUserLocations(getRequestContext().getUser()));
        return view;
    }
}
