package au.com.gaiaresources.bdrs.controller.insecure.taxa;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;

@Controller("insecureIndicatorSpeciesController")
public class IndicatorSpeciesController {
    @Autowired
    private FileService fileService;
    
    @RequestMapping(value = "/insecure/taxa/getFiles.htm", method = RequestMethod.GET)
    public ModelAndView getFiles(@RequestParam("speciesID") Integer speciesID) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("files", fileService.getFiles(IndicatorSpecies.class, speciesID));
        model.put("className", IndicatorSpecies.class.getName());
        model.put("instanceID", speciesID);
        return new ModelAndView("fileList", model);
    }
}
