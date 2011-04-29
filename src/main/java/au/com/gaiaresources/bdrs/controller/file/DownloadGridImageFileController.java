package au.com.gaiaresources.bdrs.controller.file;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.model.grid.Grid;
import au.com.gaiaresources.bdrs.model.grid.GridService;

@Controller
public class DownloadGridImageFileController extends AbstractDownloadFileController {
    @Autowired
    private GridService gridService;
    
    @RequestMapping(value = "/files/downloadGrid.htm", method = RequestMethod.GET)
    public ModelAndView download(@RequestParam("precision") BigDecimal precision, 
    							 @RequestParam(required = false, value = "kml") Integer kml,
    							 @RequestParam(required = false, value = "") String species) {
        for (Grid g : gridService.getGrids()) {
            if (g.getPrecision().compareTo(precision) == 0) {
                if (kml == null || kml == 0) {
                	if (species == null || species.isEmpty())
                		return downloadFile(Grid.class.getName(), g.getId(), "grid-sm.png");
                	else
                		return downloadFile(Grid.class.getName(), g.getId(), species + "-grid-sm.png");
                } else {
                    if (species == null || species.isEmpty())
                    	return downloadFile(Grid.class.getName(), g.getId(), "gridkml.kml");
                    else
                		return downloadFile(Grid.class.getName(), g.getId(), species + "-gridkml.kml");
                }
            }
        }
        
        throw new IllegalArgumentException("No grid with precision " + precision + " found.");
    }
}
