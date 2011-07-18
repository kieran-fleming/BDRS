package au.com.gaiaresources.bdrs.controller.preference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
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
import au.com.gaiaresources.bdrs.model.preference.Preference;
import au.com.gaiaresources.bdrs.model.preference.PreferenceCategory;
import au.com.gaiaresources.bdrs.model.preference.PreferenceDAO;
import au.com.gaiaresources.bdrs.security.Role;

/**
 * The <code>PreferenceController</code> handles all view requests for preference
 * configuration.
 */
@Controller
public class PreferenceController extends AbstractController {

    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private PreferenceDAO prefDAO;

    @RolesAllowed( { Role.ADMIN })
    @RequestMapping(value = "/bdrs/admin/preference/preference.htm", method = RequestMethod.GET)
    public ModelAndView edit(HttpServletRequest request,
            HttpServletResponse response) {

        Map<String, Preference> prefMap = prefDAO.getPreferences();
        // { Category : [ Preference ] }
        Map<PreferenceCategory, List<Preference>> categoryMap = new HashMap<PreferenceCategory, List<Preference>>();
        // { Category : categoryDisplayName }
        
        for (PreferenceCategory pc : prefDAO.getPreferenceCategories()) {
            categoryMap.put(pc, new ArrayList<Preference>());
        }

        for (Preference pref : prefMap.values()) {
            PreferenceCategory cat = pref.getPreferenceCategory();
            List<Preference> prefList = categoryMap.get(cat);
            prefList.add(pref);
        }

        ModelAndView mv = new ModelAndView("preference");
        mv.addObject("categoryMap", categoryMap);
        return mv;
    }

    @RolesAllowed( { Role.ADMIN })
    @RequestMapping(value = "/bdrs/admin/preference/preference.htm", method = RequestMethod.POST)
    public ModelAndView editSubmit(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "preference_id", required = false) int[] prefIdArray,
            @RequestParam(value = "add_preference", required = false) int[] addIndexArray) {

        Map<Integer, Preference> prefMap = new HashMap<Integer, Preference>();
        for (Preference pref : prefDAO.getPreferences().values()) {
            prefMap.put(pref.getId(), pref);
        }

        // Edited Preferences
        Preference pref;
        String key;
        String value;
        String description;
        if(prefIdArray != null){
            for (int prefId : prefIdArray) {
                pref = prefMap.remove(prefId);
                if (pref == null) {
                    log.error(String.format("Invalid preference with ID \"%d\" encountered. Could be a pref for the wrong portal.", prefId));
                    throw new IllegalArgumentException();
                }
                
                key = request.getParameter(String.format("preference_key_%d", prefId));
                value = request.getParameter(String.format("preference_value_%d", prefId));
                description = request.getParameter(String.format("preference_description_%d", prefId));
                
                boolean isModified = !key.equals(pref.getKey()) || 
                    !value.equals(pref.getValue()) ||
                    !description.equals(pref.getDescription());
                
                if(isModified) {
                 // If this is a system preference, then we will 'clone' it
                    // and create an portal specific pref.
                    if(pref.getPortal() == null) {
                        
                        Preference clone = new Preference();
                        clone.setPortal(getRequestContext().getPortal());
                        clone.setPreferenceCategory(pref.getPreferenceCategory());
                        clone.setLocked(false);
                        pref = clone;
                    }
                    pref.setKey(key);
                    pref.setValue(value);
                    pref.setDescription(description);
                    prefDAO.save(pref);
                }
            }
        }

        // Any preferences left in the map at this stage have been deleted.
        for (Preference delPref : prefMap.values()) {
            prefDAO.delete(delPref);
        }
        
        // Added Preferences
        if (addIndexArray != null) {
            for (int index : addIndexArray) {
                
                int catId = Integer.parseInt(request.getParameter(String.format("add_preference_category_%d", index)));
                key = request.getParameter(String.format("add_preference_key_%d", index));
                value = request.getParameter(String.format("add_preference_value_%d", index));
                description = request.getParameter(String.format("add_preference_description_%d", index));

                pref = new Preference();
                pref.setPreferenceCategory(prefDAO.getPreferenceCategory(catId));
                pref.setKey(key);
                pref.setValue(value);
                pref.setDescription(description);
                pref.setPortal(getRequestContext().getPortal());
                pref.setLocked(false);
                prefDAO.save(pref);
            }
        }

        getRequestContext().addMessage("preference.save.success");
        return new ModelAndView(new RedirectView(
                "/bdrs/admin/preference/preference.htm", true));
    }

    @RolesAllowed( { Role.ADMIN })
    @RequestMapping(value = "/bdrs/admin/preference/ajaxAddPreferenceRow.htm", method = RequestMethod.GET)
    public ModelAndView ajaxAddPreferenceRow(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "categoryId", required = true) int categoryId,
            @RequestParam(value = "index", required = true) int index) {

        Preference pref = new Preference();
        pref.setPreferenceCategory(prefDAO.getPreferenceCategory(categoryId));
        pref.setPortal(getRequestContext().getPortal());

        ModelAndView mv = new ModelAndView("preferenceRow");
        mv.addObject("pref", pref);
        mv.addObject("index", index);

        return mv;
    }
}
