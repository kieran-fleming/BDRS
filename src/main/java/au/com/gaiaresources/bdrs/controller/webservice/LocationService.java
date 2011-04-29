package au.com.gaiaresources.bdrs.controller.webservice;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;

@Controller
public class LocationService {

    @Autowired
    private LocationDAO locationDAO;

    @RequestMapping(value="/webservice/location/getLocationById.htm", method=RequestMethod.GET)
    public void getLocationById(HttpServletRequest request,
                                HttpServletResponse response,
                                @RequestParam(value="id", required=true) int pk)
        throws IOException {

        Location location = locationDAO.getLocation(pk);
        response.setContentType("application/json");
        response.getWriter().write(JSONObject.fromObject(location.flatten()).toString());
    }

}
