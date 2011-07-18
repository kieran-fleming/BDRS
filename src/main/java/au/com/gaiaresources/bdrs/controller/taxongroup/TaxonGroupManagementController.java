package au.com.gaiaresources.bdrs.controller.taxongroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.controller.attribute.AttributeFormField;
import au.com.gaiaresources.bdrs.controller.attribute.AttributeFormFieldFactory;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.util.BeanUtils;

/**
 * The <code>TaxonGroupController</code> handles all view requests 
 * pertaining to the creating and updating of taxon groups.
 */
@Controller
public class TaxonGroupManagementController extends AbstractController {
    
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());
    
    @Autowired
    private TaxaDAO taxaDAO;
    @Autowired
    private FileService fileService;
    @Autowired
    private AttributeDAO attributeDAO;
    
    private AttributeFormFieldFactory formFieldFactory = new AttributeFormFieldFactory();
    
    @RolesAllowed( { Role.ADMIN })
    @RequestMapping(value = "/bdrs/admin/taxongroup/listing.htm", method = RequestMethod.GET)
    public ModelAndView listing(HttpServletRequest request,
            HttpServletResponse response) {
        
        ModelAndView mv = new ModelAndView("taxonGroupList");
        mv.addObject("taxonGroupList", taxaDAO.getTaxonGroupsSortedByName());
        return mv;
    }
    
    @RolesAllowed( { Role.ADMIN })
    @RequestMapping(value = "/bdrs/admin/taxongroup/edit.htm", method = RequestMethod.GET)
    public ModelAndView edit(HttpServletRequest request,
                            HttpServletResponse response,
                            @RequestParam(defaultValue="0", required=false, value="pk") int pk) {
        
        TaxonGroup taxonGroup;
        if(pk == 0) {
            taxonGroup = new TaxonGroup();
        } else {
            taxonGroup = taxaDAO.getTaxonGroup(pk);
        }
        
        AttributeFormField formField;
        List<AttributeFormField> attributeFormFieldList = new ArrayList<AttributeFormField>();
        List<AttributeFormField> identificationFormFieldList = new ArrayList<AttributeFormField>();
        for(Attribute attr : taxonGroup.getAttributes()) {
            formField = formFieldFactory.createAttributeFormField(attributeDAO, attr);
            if(attr.isTag()) {
                identificationFormFieldList.add(formField);
            } else {
                attributeFormFieldList.add(formField);
            }
        }
            
        Collections.sort(attributeFormFieldList);
        Collections.sort(identificationFormFieldList);
        
        ModelAndView mv = new ModelAndView("taxonGroupEdit");
        mv.addObject("taxonGroup", taxonGroup);
        mv.addObject("attributeFormFieldList", attributeFormFieldList);
        mv.addObject("identificationFormFieldList", identificationFormFieldList);
        return mv;
    }
    
    @RolesAllowed( { Role.ADMIN })
    @RequestMapping(value = "/bdrs/admin/taxongroup/edit.htm", method = RequestMethod.POST)
    public ModelAndView save(MultipartHttpServletRequest request,
                            HttpServletResponse response,
                            @RequestParam(value="taxonGroupPk", required=false, defaultValue="0") int pk,
                            @RequestParam(value="name", required=true) String name,
                            @RequestParam(value="image", required=false) String image,
                            @RequestParam(value="behaviourIncluded", required=false, defaultValue="false") boolean behaviourIncluded,
                            @RequestParam(value="firstAppearanceIncluded", required=false, defaultValue="false") boolean firstAppearanceIncluded, 
                            @RequestParam(value="lastAppearanceIncluded", required=false, defaultValue="false") boolean lastAppearanceIncluded, 
                            @RequestParam(value="habitatIncluded", required=false, defaultValue="false") boolean habitatIncluded, 
                            @RequestParam(value="weatherIncluded", required=false, defaultValue="false") boolean weatherIncluded, 
                            @RequestParam(value="numberIncluded", required=false, defaultValue="false") boolean numberIncluded,
                            @RequestParam(value="attribute", required=false) int[] attributePkArray,
                            @RequestParam(value="add_attribute", required=false) int[] attributeIndexArray) throws Exception {
        
        TaxonGroup taxonGroup;
        if(pk == 0) {
            taxonGroup = new TaxonGroup();
        } else {
            taxonGroup = taxaDAO.getTaxonGroup(pk);
        }
        
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

        taxonGroup.setAttributes(attributeList);
        
        // -- The basic stuff --
        taxonGroup.setName(name);
        taxonGroup.setBehaviourIncluded(behaviourIncluded);
        taxonGroup.setFirstAppearanceIncluded(firstAppearanceIncluded);
        taxonGroup.setLastAppearanceIncluded(lastAppearanceIncluded);
        taxonGroup.setHabitatIncluded(habitatIncluded);
        taxonGroup.setWeatherIncluded(weatherIncluded);
        taxonGroup.setNumberIncluded(numberIncluded);
        
        // Save the group here otherwise the file service cannot store the file.
        taxaDAO.save(taxonGroup);
        
        // -- Now for the images --
        // inputValue is empty when a file is cleared or the client 
        // does not have javascript enabled when uploading a file.
        // Without javascript, it is not possible to clear a file.
        
        // inputFile will always have size zero unless a file
        // is uploaded. 
        
        // If there is already a file, but the
        // group is updated, without changing the file input,
        // addOrUpdateProperty will be true but inputFile will
        // have size zero.
        for(String propertyName : new String[] { "image", "thumbNail" }) {
            String inputValue = request.getParameter(propertyName);
            MultipartFile inputFile = request.getFile(String.format("%s_file", propertyName));
            boolean addOrUpdateProperty = !inputValue.isEmpty() || (inputFile != null && inputFile.getSize() > 0);
            if(addOrUpdateProperty) {
                if (inputFile != null && inputFile.getSize() > 0) {
                    BeanUtils.injectProperty(taxonGroup, propertyName, inputFile.getOriginalFilename());
                    fileService.createFile(taxonGroup, inputFile);
                }
            }
            else {
                BeanUtils.injectProperty(taxonGroup, propertyName, null);
            }
        }
        taxaDAO.save(taxonGroup);
        
        getRequestContext().addMessage("taxongroup.save.success", new Object[]{ taxonGroup.getName() });
        return new ModelAndView(new RedirectView("/bdrs/admin/taxongroup/listing.htm", true));
    }
}
