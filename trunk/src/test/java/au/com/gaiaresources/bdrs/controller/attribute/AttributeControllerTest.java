package au.com.gaiaresources.bdrs.controller.attribute;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.security.Role;

public class AttributeControllerTest extends AbstractControllerTest {

    @Test
    public void testAjaxAddAttribute() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        // Attempt 3 requests
        for (int i = 0; i < 3; i++) {
            for(Boolean showScope : new Boolean[]{ true, false }) {
                for(Boolean isTag : new Boolean[]{ true, false}) {
                    request.setMethod("GET");
                    request.setRequestURI("/bdrs/admin/attribute/ajaxAddAttribute.htm");
                    request.setParameter("index", String.valueOf(i));
                    request.setParameter("showScope", showScope.toString());
                    request.setParameter("isTag", isTag.toString());
        
                    ModelAndView mv = handle(request, response);
                    ModelAndViewAssert.assertViewName(mv, "attributeRow");
                    ModelAndViewAssert.assertModelAttributeAvailable(mv, "formField");
                    ModelAndViewAssert.assertModelAttributeAvailable(mv, "showScope");
                    ModelAndViewAssert.assertModelAttributeAvailable(mv, "index");
        
                    Assert.assertEquals(i, Integer.parseInt(mv.getModelMap().get("index").toString()));
                    AttributeFormField formField = (AttributeFormField) mv.getModelMap().get("formField");
                    Assert.assertTrue(formField.isAttributeField());
                    
                    Assert.assertTrue(showScope.equals(Boolean.parseBoolean(mv.getModelMap().get("showScope").toString())));
                    Assert.assertTrue(isTag.equals(Boolean.parseBoolean(mv.getModelMap().get("isTag").toString())));
        
                    AttributeInstanceFormField attrFormField = (AttributeInstanceFormField) formField;
                    Assert.assertNull(attrFormField.getAttribute());
                    Assert.assertEquals(String.format("add_weight_%d", i), attrFormField.getWeightName());
                    Assert.assertEquals(0, attrFormField.getWeight());
                }
            }
        }
    }
}
