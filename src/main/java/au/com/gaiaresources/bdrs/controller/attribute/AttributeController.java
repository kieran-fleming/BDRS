package au.com.gaiaresources.bdrs.controller.attribute;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.security.Role;

@Controller
public class AttributeController extends AbstractController {
    private Logger log = Logger.getLogger(getClass());
    
    private AttributeFormFieldFactory formFieldFactory = new AttributeFormFieldFactory();

    @RolesAllowed( {Role.USER,Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = "/bdrs/admin/attribute/ajaxAddAttribute.htm", method = RequestMethod.GET)
    public ModelAndView ajaxAddAttribute(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value="index", required=true) int index,
            @RequestParam(value="showScope", required=false, defaultValue="true") boolean showScope,
            @RequestParam(value="isTag", required=false, defaultValue="false") boolean isTag) {
       
        ModelAndView mv = new ModelAndView("attributeRow");
        mv.addObject("formField", formFieldFactory.createAttributeFormField(index));
        mv.addObject("showScope", showScope);
        mv.addObject("isTag", isTag);
        mv.addObject("index", index);
        return mv;
    }
}
