package au.com.gaiaresources.bdrs.controller.admin.region;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.db.TransactionCallback;
import au.com.gaiaresources.bdrs.model.region.Region;
import au.com.gaiaresources.bdrs.model.region.RegionService;
import au.com.gaiaresources.bdrs.kml.servlet.KMLWriter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

@Controller
public class RegionController extends AbstractController {
    @Autowired
    private RegionService regionService;
    @Autowired
    private RegionFormValidator validator;
    
    /**
     * Render the region admin screen. Added to the model are:
     * <ul>
     *   <li><code>regions</code> - The list of currently defined regions.</li>
     * </ul>
     * @return <code>ModelAndView</code>.
     */
    @RequestMapping(value = "/admin/regions.htm", method = RequestMethod.GET)
    public ModelAndView render() {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("regions", regionService.getRegions());
        return new ModelAndView("adminRegions", model);
    }
    
    /**
     * Render the draw region form.
     * @return <code>ModelAndView</code>.
     */
    @RequestMapping(value = "/admin/drawRegion.htm", method = RequestMethod.GET)
    public ModelAndView drawRegion() {
        return new ModelAndView("drawRegions", "region", new RegionForm());
    }
    
    @RequestMapping(value = {"/admin/drawRegion.htm", "/admin/editRegion.htm"}, method = RequestMethod.POST)
    public String saveRegion(@ModelAttribute("region") final RegionForm r, BindingResult result) {
        validator.validate(r, result);
        if (result.hasErrors()) {
            return "drawRegions";
        } else {
            //Region region = 
            	doInTransaction(new TransactionCallback<Region>() {
                public Region doInTransaction(TransactionStatus status) {
                    List<BigDecimal[]> coordinates = new ArrayList<BigDecimal[]>();
                    for (CoordinateForm c : r.getCoordinates()) {
                        coordinates.add(new BigDecimal[] {c.getLongitude(), c.getLatitude()});
                    }
                    if (r.getId() == null) {
                        return regionService.createRegion(r.getRegionName(), coordinates);
                    } else {
                        return regionService.updateRegion(r.getId(), r.getRegionName(), coordinates);
                    }
                }
            });
            return "redirect:/admin/regions.htm";
        }
    }
    
    @RequestMapping(value = "/admin/editRegion.htm", method = RequestMethod.GET)
    public ModelAndView renderEdit(@RequestParam(value = "regionID", required = true) Integer regionID) {
        Region region = regionService.getRegion(regionID);
        RegionForm regionForm = new RegionForm();
        regionForm.setId(region.getId());
        regionForm.setRegionName(region.getRegionName());
        
        for (Coordinate c : ((Polygon)region.getBoundary().convexHull()).getExteriorRing().getCoordinates()) {
            CoordinateForm cf = new CoordinateForm();
            cf.setLongitude(new BigDecimal(c.x));
            cf.setLatitude(new BigDecimal(c.y));
            regionForm.getCoordinates().add(cf);
        }
        return new ModelAndView("drawRegions", "region", regionForm);
    }
    
    @RequestMapping(value = "/authenticated/getRegion.htm", method = RequestMethod.GET)
    public ModelAndView getRegion(HttpServletRequest request, @RequestParam("regionID") Integer regionId) throws JAXBException {
        KMLWriter writer = new KMLWriter(request);
        Region r = regionService.getRegion(regionId);
        writer.createFolder("Region");
        writer.createPlacemark("Region", r.getRegionName(), r.getBoundary(), "regionpoly");
        writer.createStylePoly("regionpoly", "aa0000ff".toCharArray());
        return new ModelAndView("kml", "kml", writer);
    }
}
