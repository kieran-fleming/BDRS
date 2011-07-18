package au.com.gaiaresources.bdrs.controller.attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataBuilder;
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataHelper;
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataRow;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;

@Controller
public class CensusMethodController extends AbstractController {
    
    @Autowired
    CensusMethodDAO cmDAO;
    @Autowired
    AttributeDAO attributeDAO;
    
    private AttributeFormFieldFactory formFieldFactory = new AttributeFormFieldFactory();
    
    @RequestMapping(value = "/bdrs/admin/censusMethod/listing.htm", method = RequestMethod.GET)
    public ModelAndView listing(HttpServletRequest request, HttpServletResponse response) {
        
        ModelAndView mv = new ModelAndView("censusMethodList");
        //mv.addObject("censusMethodList", cmDAO.get
        return mv;
    }
    
    @RequestMapping(value = "/bdrs/admin/censusMethod/edit.htm", method = RequestMethod.GET)
    public ModelAndView openEdit(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value="censusMethodId", defaultValue="0", required=false) Integer pk) {
        CensusMethod cm;
        if(pk == 0) {
            cm = new CensusMethod();
        } else {
            cm = cmDAO.get(pk);
        }
        
        List<AttributeFormField> attributeFormFieldList = new ArrayList<AttributeFormField>();
        for(Attribute attr : cm.getAttributes()) {
            AttributeFormField formField = formFieldFactory.createAttributeFormField(attributeDAO, attr);
            attributeFormFieldList.add(formField);
        }
            
        Collections.sort(attributeFormFieldList);
        
        ModelAndView mv = new ModelAndView("censusMethodEdit");
        mv.addObject("censusMethod", cm);
        mv.addObject("attributeFormFieldList", attributeFormFieldList);
        return mv;
    }
    
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/bdrs/admin/censusMethod/edit.htm", method = RequestMethod.POST)
    public ModelAndView save(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value="censusMethodName", required=true) String name,
            @RequestParam(value="taxonomic", required=false, defaultValue="NONTAXONOMIC") String taxonomic,
            @RequestParam(value="type", required=true, defaultValue="") String type,
            @RequestParam(value="description", required=true, defaultValue="") String description,
            @RequestParam(value="censusMethodId", defaultValue="0", required=false) Integer pk,
            @RequestParam(value="attribute", required=false) int[] attributePkArray,
            @RequestParam(value="add_attribute", required=false) int[] attributeIndexArray,
            @RequestParam(value="childCensusMethod", required=false) int[] childCensusMethodList) {
        
        CensusMethod cm;
        if(pk == 0) {
            cm = new CensusMethod();
        } else {
            cm = cmDAO.get(pk);
        }
        
        cm.setName(name);
        cm.setType(type);
        cm.setDescription(description);
        cm.setTaxonomic(Taxonomic.valueOf(taxonomic));
        
        // -- Attributes --
        List<Attribute> attributeList = new ArrayList<Attribute>();

        // Attribute Updates
        // All attributes have a hidden input called 'attribute'
        Attribute attr;
        if(attributePkArray != null) {
            for(int attributePk : attributePkArray) {
                String attrName = request.getParameter(String.format("name_"+attributePk));
                if(attrName != null && !attrName.isEmpty()) {
                    attr = attributeDAO.get(attributePk);
                    AttributeFormField formField = formFieldFactory.createAttributeFormField(attributeDAO, attr, request.getParameterMap());
                    attr = (Attribute) formField.save();
                    attributeList.add(attr);
                }
            }
        }

        // Create new Attributes
        if(attributeIndexArray != null) {
            for(int index : attributeIndexArray) {
                String attrName = request.getParameter(String.format("add_name_"+index));
                if(attrName != null && !attrName.isEmpty()) {
                    AttributeFormField formField = formFieldFactory.createAttributeFormField(attributeDAO, index, request.getParameterMap());
                    attributeList.add((Attribute)formField.save());
                }
            }
        }
        cm.setAttributes(attributeList);
        
        // no child protection!
        List<CensusMethod> childList = new ArrayList<CensusMethod>();
        if (childCensusMethodList != null) {
            for (int cmId : childCensusMethodList) {
                CensusMethod child = cmDAO.get(cmId);
                childList.add(child);
            }
        }
        cm.setCensusMethods(childList);
        
        cmDAO.save(cm);
        
        return new ModelAndView(new RedirectView("/bdrs/admin/censusMethod/listing.htm", true));
    }
    
    @RequestMapping(value="/bdrs/admin/censusMethod/search.htm", method = RequestMethod.GET)
    public void searchService(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value="name", required=false) String name,
            @RequestParam(value="taxonomic", required=false) Boolean taxonomic,
            @RequestParam(value="surveyId", required=false) Integer surveyId) throws Exception {
        JqGridDataHelper jqGridHelper = new JqGridDataHelper(request);
        PaginationFilter filter = jqGridHelper.createFilter(request);
        
        PagedQueryResult<CensusMethod> queryResult = cmDAO.search(filter, name, surveyId);
        
        JqGridDataBuilder builder = new JqGridDataBuilder(jqGridHelper.getMaxPerPage(), queryResult.getCount(), jqGridHelper.getRequestedPage());

        if (queryResult.getCount() > 0) {
            for (CensusMethod cm : queryResult.getList()) {
                JqGridDataRow row = new JqGridDataRow(cm.getId());
                row
                .addValue("name", cm.getName())
                .addValue("taxonomic", cm.getTaxonomic().getName());
                builder.addRow(row);
            }
        }
        response.setContentType("application/json");
        response.getWriter().write(builder.toJson());
    }
    
    @RequestMapping(value="/bdrs/user/censusMethod/getSurveyCensusMethods.htm", method = RequestMethod.GET)
    public void getSurveyCensusMethods(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value="surveyId", required=true) Integer surveyId) throws Exception {
        PagedQueryResult<CensusMethod> queryResult = cmDAO.search(null, null, surveyId);
        
        List<CensusMethod> censusMethodList = queryResult.getList();
        JSONArray array = new JSONArray();
        
        // cover the default case...
        CensusMethod defaultCensusMethod = new CensusMethod();
        defaultCensusMethod.setId(0);
        defaultCensusMethod.setName("Standard Taxonomic (Default)");
        array.add(defaultCensusMethod.flatten());
        
        if (censusMethodList != null) {
            for(CensusMethod cm : censusMethodList) {
                array.add(cm.flatten());
            }
        }
        
        // support for JSONP
        if (request.getParameter("callback") != null) {
                response.setContentType("application/javascript");              
                response.getWriter().write(request.getParameter("callback") + "(");
        } else {
                response.setContentType("application/json");
        }
        response.getWriter().write(array.toString());
        if (request.getParameter("callback") != null) {
                response.getWriter().write(");");
        }
    }
    
    // AL - I'm doing it this way to use the tile as a template since we have no javascript templating at the moment and 
    // it keeps it consistent with the rest of the form. Yes I know it's inefficient.
    @RequestMapping(value="/bdrs/admin/censusMethod/ajaxAddSubCensusMethod.htm", method = RequestMethod.GET)
    public ModelAndView addSubCensusMethod(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value="id", required=true) Integer id) {
        CensusMethod cm = cmDAO.get(id);
        ModelAndView mv = new ModelAndView("censusMethodEditRow");
        mv.addObject("id", cm.getId());
        mv.addObject("name", cm.getName());
        mv.addObject("taxonomic", cm.getTaxonomic());
        return mv;
    }
}
