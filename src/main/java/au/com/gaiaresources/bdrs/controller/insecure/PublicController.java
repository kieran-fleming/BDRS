package au.com.gaiaresources.bdrs.controller.insecure;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.portal.PortalDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.theme.Theme;
import au.com.gaiaresources.bdrs.service.template.TemplateService;

@Controller
public class PublicController extends AbstractController {

    @Autowired
    private TaxaDAO taxaDAO;
    @Autowired
    private PortalDAO portalDAO;
    
    @Autowired
    private TemplateService templateService;
    
    private Logger log = Logger.getLogger(this.getClass());

    @RequestMapping(value = "/about.htm", method = RequestMethod.GET)
    public ModelAndView render() {
        ModelAndView view = new ModelAndView("about");
        
        Map<String, Object> subParams = new HashMap<String, Object>();
        String coreRevisionInfo = templateService.transformToString("coreRevision.vm", PublicController.class, subParams);
        String coreRevisionNumber = templateService.transformToString("coreRevisionNumber.vm", PublicController.class, subParams); 
        String implRevisionInfo;
        String implRevisionNumber;
        try {
            implRevisionInfo = templateService.transformToString("implRevision.vm", PublicController.class, subParams);
            implRevisionNumber = templateService.transformToString("implRevisionNumber.vm", PublicController.class, subParams); 
        } catch (Exception e) {
            implRevisionInfo = null;
            implRevisionNumber = null;
            log.error("Could not retrieve implementation revision info. If this is a core build with no implementation this is expected.", e);
        }
        
        view.addObject("coreRevisionNumber", coreRevisionNumber);
        view.addObject("coreRevisionInfo", coreRevisionInfo);
        view.addObject("implRevisionInfo", implRevisionInfo);
        view.addObject("implRevisionNumber", implRevisionNumber);
        
        return view;
    }

    @RequestMapping(value = "/help.htm", method = RequestMethod.GET)
    public ModelAndView help() {
        ModelAndView view = new ModelAndView("helpme");
        return view;
    }

    @RequestMapping(value = "/speciesCount.htm", method = RequestMethod.GET)
     public void speciesCount(HttpServletRequest request,
             HttpServletResponse response) throws IOException {
        response.getWriter().write(taxaDAO.countAllSpecies().toString());
    }
    
    @RequestMapping(value = "/termsAndConditions.htm", method = RequestMethod.GET)
    public ModelAndView renderTermsAndConditions() {
        ModelAndView view = new ModelAndView("termsAndConditions");
        return view;
    }
    
    @RequestMapping(value = "/privacyStatement.htm", method = RequestMethod.GET)
    public ModelAndView renderPrivacyStatement() {
        ModelAndView view = new ModelAndView("privacyStatement");
        return view;
    }
    
    @RequestMapping(value="/index.html", method = RequestMethod.GET)
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView view = new ModelAndView("index");
        view.addObject("portalList", portalDAO.getPortals());
        return view;
    }
}