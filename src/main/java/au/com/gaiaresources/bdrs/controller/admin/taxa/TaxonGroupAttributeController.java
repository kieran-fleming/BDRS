package au.com.gaiaresources.bdrs.controller.admin.taxa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.db.TransactionCallback;
import au.com.gaiaresources.bdrs.model.taxa.TaxaService;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeOption;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.util.CollectionUtils;

@Controller
public class TaxonGroupAttributeController extends AbstractController {
    @Autowired
    private TaxaService taxaService;

    @RequestMapping(value = "/admin/taxonGroups/attributes.htm", method = RequestMethod.GET)
    public View render() {
        return new RedirectView("/admin/taxonGroups.htm", true);
    }
    
    @RequestMapping(value = "/admin/taxonGroups/attributes.htm", method = RequestMethod.GET, params = {"taxonGroupID"})
    public ModelAndView render(@RequestParam("taxonGroupID") Integer taxonGroupID) {
        Map<String, Object> model = new HashMap<String, Object>();
        TaxonGroup group = taxaService.getTaxonGroup(taxonGroupID);
        if (CollectionUtils.size(group.getAttributes()) == 0) {
            return new ModelAndView("redirect:/admin/taxonGroups/addAttributes.htm", "taxonGroupID", taxonGroupID);
        }
        model.put("group", group);
        model.put("attributes", new ArrayList<Attribute>(group.getAttributes()));
        return new ModelAndView("adminTaxonGroupAttributes", model);
    }
    
    @RequestMapping(value = "/admin/taxonGroups/addAttributes.htm", method = RequestMethod.GET)
    public ModelAndView addAttribute(@RequestParam("taxonGroupID") Integer taxonGroupID) {
        Map<String, Object> model = new HashMap<String, Object>();
        
        TaxonGroupAttributeForm form = new TaxonGroupAttributeForm();
        form.setTaxonGroupId(taxonGroupID);
        model.put("attribute", form);
        model.put("types", AttributeType.values());
        return new ModelAndView("addTaxonGroupAttribute", model);
    }
    
    @RequestMapping(value = "/admin/taxonGroups/addAttributes.htm", method = RequestMethod.POST)
    public ModelAndView saveAttribute(@ModelAttribute("attribute") final TaxonGroupAttributeForm attributeForm,  
                                      BindingResult result) 
    {
        doInTransaction(new TransactionCallback<Attribute>() {
            @Override
            public Attribute doInTransaction(TransactionStatus status) {
                TaxonGroup group = taxaService.getTaxonGroup(attributeForm.getTaxonGroupId());
                AttributeType type = AttributeType.valueOf(attributeForm.getDataTypeCode());
                return taxaService.createAttribute(group, attributeForm.getName(), type,
                                                   attributeForm.isRequired());
            }
        });
        return new ModelAndView("redirect:/admin/taxonGroups/attributes.htm", "taxonGroupID", attributeForm.getTaxonGroupId());
    }
    
    @RequestMapping(value = "/admin/taxonGroups/editAttribute.htm", method = RequestMethod.GET)
    public ModelAndView editAttribute(@RequestParam("attributeID") Integer attributeID) {
        Map<String, Object> model = new HashMap<String, Object>();
        
        Attribute attribute = taxaService.getAttribute(attributeID);
        
        TaxonGroupAttributeForm form = new TaxonGroupAttributeForm();
        form.setAttributeId(attributeID);
        form.setDataTypeCode(AttributeType.find(attribute.getTypeCode(), AttributeType.values()).name());
        form.setRequired(attribute.isRequired());
        model.put("attribute", form);
        model.put("types", AttributeType.values());
        return new ModelAndView("addTaxonGroupAttribute", model);
    }
    
    @RequestMapping(value = "/admin/taxonGroups/editAttribute.htm", method = RequestMethod.POST)
    public ModelAndView editAttribute(@ModelAttribute("attribute") final TaxonGroupAttributeForm attributeForm,  
            BindingResult result) {
        doInTransaction(new TransactionCallback<Attribute>() {
            @Override
            public Attribute doInTransaction(TransactionStatus status) {
                   	return taxaService.updateAttribute(attributeForm.getAttributeId(), 
            			attributeForm.getName(), 
            			AttributeType.valueOf(attributeForm.getDataTypeCode()),
            			attributeForm.isRequired());
            }
        });
        return new ModelAndView("redirect:/admin/taxonGroups/attributes.htm", "taxonGroupID", attributeForm.getTaxonGroupId());
    }
    
    
    @RequestMapping(value = "/admin/taxonGroups/editAttributeValueOptions", method = RequestMethod.GET, params = {"attributeID"})
    public ModelAndView renderOptions(@RequestParam("attributeID") Integer attributeID) {
    	Attribute attr = taxaService.getAttribute(attributeID);
    	
        if (CollectionUtils.size(attr.getOptions()) == 0) {
            return new ModelAndView("redirect:/admin/taxonGroups/addAttributeOptions.htm", "attributeID", attributeID);
        }
        
    	Map<String, Object> model = new HashMap<String, Object>();
        model.put("attribute", attr);
        model.put("options", new ArrayList<AttributeOption>(attr.getOptions()));
        return new ModelAndView("adminTaxonGroupAttributeOptions", model);
    }
    
    
    
    @RequestMapping(value = "/admin/taxonGroups/addAttributeOptions.htm", method = RequestMethod.GET)
    public ModelAndView addAttributeOptions(@RequestParam("attributeID") Integer attributeID) {
        Map<String, Object> model = new HashMap<String, Object>();
        
        TaxonGroupAttributeOptionForm form = new TaxonGroupAttributeOptionForm();
        form.setAttributeId(attributeID);
        model.put("option", form);
        return new ModelAndView("addTaxonGroupAttributeOption", model);
    }
    
    @RequestMapping(value = "/admin/taxonGroups/addAttributeOptions.htm", method = RequestMethod.POST)
    public ModelAndView addAttributeOptions(@ModelAttribute("option") final TaxonGroupAttributeOptionForm optionForm,  
                                      BindingResult result) 
    {
    	final Attribute attr = taxaService.getAttribute(optionForm.getAttributeId());
    	
    	doInTransaction(new TransactionCallback<AttributeOption>() {
            @Override
            public AttributeOption doInTransaction(TransactionStatus status) {
            	return taxaService.createAttributeOption(attr, optionForm.getValue());
            }
        });
    	
        return new ModelAndView("redirect:/admin/taxonGroups/editAttributeValueOptions.htm", "attributeID", optionForm.getAttributeId());
    }
}
