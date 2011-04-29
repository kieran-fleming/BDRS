package au.com.gaiaresources.bdrs.controller.attribute;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.security.Role;

@Controller
public class SurveyAttributeBaseController extends AbstractController {
    
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private TaxaDAO taxaDAO;

    @Autowired
    private MetadataDAO metadataDAO;

    @Autowired
    private SurveyDAO surveyDAO;
    
    @Autowired
    private AttributeDAO attributeDAO;
    
    private AttributeFormFieldFactory formFieldFactory = new AttributeFormFieldFactory();

    @RolesAllowed( {Role.USER,Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = "/bdrs/admin/survey/editAttributes.htm", method = RequestMethod.GET)
    public ModelAndView editSurveyAttributes(HttpServletRequest request, HttpServletResponse response) {
        Survey survey = getSurvey(request.getParameter("surveyId"));
        
        List<AttributeFormField> formFieldList = new ArrayList<AttributeFormField>();
        for(Attribute attr : survey.getAttributes()) {
            formFieldList.add(formFieldFactory.createAttributeFormField(attributeDAO, attr));
        }
        
        for(String propertyName : Record.RECORD_PROPERTY_NAMES) {
            formFieldList.add(formFieldFactory.createAttributeFormField(metadataDAO, survey, propertyName));
        }
        
        Collections.sort(formFieldList);
        
        ModelAndView mv = new ModelAndView("surveyEditAttributes");
        mv.addObject("survey", survey);
        mv.addObject("formFieldList", formFieldList);
        return mv;
    }

    @RolesAllowed( {Role.USER,Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = "/bdrs/admin/survey/editAttributes.htm", method = RequestMethod.POST)
    public ModelAndView submitSurveyAttributes(HttpServletRequest request, HttpServletResponse response) {
        
        Survey survey = getSurvey(request.getParameter("surveyId"));
        for (String propertyName : Record.RECORD_PROPERTY_NAMES) {
            AttributeFormField formField = formFieldFactory.createAttributeFormField(metadataDAO, survey, propertyName, request.getParameterMap());
            formField.save();
        }
        
        List<Attribute> attributeList = new ArrayList<Attribute>();
        // First look for any Attributes that may have been updated
        // All attributes have a hidden input called 'attribute'
        Attribute attr;
        if(request.getParameterValues("attribute") != null) {
            for(String rawAttrPk : request.getParameterValues("attribute")) {
                // Disallow blank names
                if(rawAttrPk != null && !rawAttrPk.isEmpty() &&
                        !request.getParameter("name_"+rawAttrPk).isEmpty()) {
                    
                    attr = taxaDAO.getAttribute(Integer.parseInt(rawAttrPk));
                    AttributeFormField formField = formFieldFactory.createAttributeFormField(attributeDAO, attr, request.getParameterMap());
                    attr = (Attribute) formField.save();
                    attributeList.add(attr);
                }
            }
        }

        // Create new Attributes
        if(request.getParameter("add_attribute") != null) {
            for(String rawIndex : request.getParameterValues("add_attribute")) {
                if(rawIndex != null && !rawIndex.isEmpty() &&
                        !request.getParameter("add_name_"+rawIndex).isEmpty()) {
                    
                    int index = Integer.parseInt(rawIndex);
                    AttributeFormField formField = formFieldFactory.createAttributeFormField(attributeDAO, index, request.getParameterMap());
                    attributeList.add((Attribute)formField.save());
                }
            }
        }

        survey.setAttributes(attributeList);

        // Update the form rendering type given the new criteria
        SurveyFormRendererType formRenderType = survey.getFormRendererType();
        if(formRenderType == null || (formRenderType != null && !formRenderType.isEligible(survey))) {
            Metadata md = survey.setFormRendererType(SurveyFormRendererType.DEFAULT);
            metadataDAO.save(md);
        }
        surveyDAO.save(survey);

        getRequestContext().addMessage("bdrs.survey.attributes.success", new Object[]{survey.getName()});

        ModelAndView mv;
        if(request.getParameter("saveAndContinue") != null) {
            mv = new ModelAndView(new RedirectView("/bdrs/admin/survey/editLocations.htm", true));
            mv.addObject("surveyId", survey.getId());
        }
        else if(request.getParameter("saveAndPreview") != null) {
            mv = new ModelAndView(new RedirectView("/bdrs/user/surveyRenderRedirect.htm", true));
            mv.addObject("surveyId", survey.getId());
            mv.addObject("preview", "preview");
        }
        else {
            mv = new ModelAndView(new RedirectView("/bdrs/admin/survey/listing.htm", true));
        }
        return mv;
    }

    private Survey getSurvey(String rawSurveyId) {
        if(rawSurveyId == null){
            // Do not know which survey to deal with. Bail out.
            throw new HTTPException(HttpServletResponse.SC_NOT_FOUND);
        }
        return surveyDAO.getSurvey(Integer.parseInt(rawSurveyId));
    }

    @InitBinder
    public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(
                dateFormat, true));
    }
}
