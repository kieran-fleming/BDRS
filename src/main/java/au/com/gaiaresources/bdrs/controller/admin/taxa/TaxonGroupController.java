package au.com.gaiaresources.bdrs.controller.admin.taxa;

import javax.annotation.security.RolesAllowed;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.db.TransactionCallback;
import au.com.gaiaresources.bdrs.model.taxa.TaxaService;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.security.Role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for dealing with taxon groups.
 * @author Tim Carpenter
 *
 */
@RolesAllowed({Role.ADMIN})
@Controller
public class TaxonGroupController extends AbstractController {
    @Autowired
    private TaxaService taxaService;
    @Autowired
    private TaxonGroupFormValidator validator;
    
    /**
     * @deprecated this has been replaced with /bdrs/admin/taxongroup/edit.htm in TaxonGroupManagementController.edit
     * Render the initial taxon groups page.
     * @return <code>String</code>.
     */
    //@RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR})
    @RequestMapping(value = "/admin/taxonGroups.htm", method = RequestMethod.GET)
    public ModelAndView render() {
        return new ModelAndView("adminTaxonGroups", "groups", taxaService.getTaxonGroups());
    }
    
    /**
     * @deprecated this has been replaced with /bdrs/admin/taxongroup/edit.htm in TaxonGroupManagementController.edit
     * @return
     */
    //@RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR})
    @RequestMapping(value = "/admin/addTaxonGroup.htm", method = RequestMethod.GET)
    public ModelAndView renderAddGroup() {
        return new ModelAndView("enterTaxonGroup", "taxonGroup", new TaxonGroupForm());
    }
    
    /**
     * @deprecated this has been replaced with /bdrs/admin/taxongroup/edit.htm in TaxonGroupManagementController.edit
     * @param taxonGroup
     * @param result
     * @return
     */
    //@RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR})
    @RequestMapping(value = "/admin/addTaxonGroup.htm", method = RequestMethod.POST)
    public String saveGroup(@ModelAttribute("taxonGroup") final TaxonGroupForm taxonGroup, BindingResult result) {
        validator.validate(taxonGroup, result);
        if (result.hasErrors()) {
            return "enterTaxonGroup";
        }
        
        //TaxonGroup group = 
        	doInTransaction(new TransactionCallback<TaxonGroup>() {
            public TaxonGroup doInTransaction(TransactionStatus txnStatus) {
                TaxonGroup g = null;
                if (taxonGroup.getId() == null) {
                    g = taxaService.createTaxonGroup(taxonGroup.getName(), taxonGroup.getBehaviour(), 
                                                     taxonGroup.getFirstAppearance(), taxonGroup.getLastAppearance(),
                                                     taxonGroup.getHabitat(), taxonGroup.getWeather(),
                                                     taxonGroup.getNumber());
                } else {
                    g = taxaService.updateTaxonGroup(taxonGroup.getId(), 
                                                     taxonGroup.getName(), taxonGroup.getBehaviour(), 
                                                     taxonGroup.getFirstAppearance(), taxonGroup.getLastAppearance(),
                                                     taxonGroup.getHabitat(), taxonGroup.getWeather(),
                                                     taxonGroup.getNumber());
                }
                return g;
            }
        });
        
        return "redirect:/admin/taxonGroups.htm";
    }
    
    /**
     * @deprecated this has been replaced with /bdrs/admin/taxongroup/edit.htm in TaxonGroupManagementController.edit
     * @param groupID
     * @return
     */
    //@RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR})
    @RequestMapping(value = "/admin/editTaxonGroup.htm", method = RequestMethod.GET)
    public ModelAndView renderEdit(@RequestParam(value = "groupID", required = true) Integer groupID) {
        TaxonGroup group = taxaService.getTaxonGroup(groupID);
        TaxonGroupForm form = new TaxonGroupForm();
        form.setId(group.getId());
        form.setName(group.getName());
        form.setBehaviour(group.isBehaviourIncluded());
        form.setFirstAppearance(group.isFirstAppearanceIncluded());
        form.setLastAppearance(group.isLastAppearanceIncluded());
        form.setHabitat(group.isHabitatIncluded());
        form.setWeather(group.isWeatherIncluded());
        form.setNumber(group.isNumberIncluded());
        return new ModelAndView("enterTaxonGroup", "taxonGroup", form);
    }
    
    /**
     * @deprecated this has been replaced with /bdrs/admin/taxongroup/edit.htm in TaxonGroupManagementController.edit
     * @param taxonGroup
     * @param result
     * @return
     */
    //@RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR})
    @RequestMapping(value = "/admin/editTaxonGroup.htm", method = RequestMethod.POST)
    public String saveEditGroup(@ModelAttribute("taxonGroup") final TaxonGroupForm taxonGroup, BindingResult result) {
        return saveGroup(taxonGroup, result);
    }
    
}
