package au.com.gaiaresources.bdrs.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;

@Controller
public class AdminHomePageController extends AbstractController {
    @Autowired
    private RecordDAO recordDAO;
    
    public static final String ADMIN_HOME_URL = "/admin/home.htm";

    @RequestMapping(value = ADMIN_HOME_URL, method = RequestMethod.GET)
    public ModelAndView render() {
        ModelAndView view = new ModelAndView("adminHome");
        Record latestRecord = recordDAO.getLatestRecord();
        view.addObject("latestRecord", latestRecord);
        view.addObject("recordCount", recordDAO.countRecords(null));
        return view;
    }
}
