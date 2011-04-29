package au.com.gaiaresources.bdrs.controller.admin.taxa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.FileDataSource;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.db.TransactionCallback;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.region.Region;
import au.com.gaiaresources.bdrs.model.region.RegionService;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaService;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class IndicatorSpeciesController extends AbstractController {
    @Autowired
    private TaxaService taxaService;

    @Autowired
    private RegionService regionService;

    @Autowired
    private FileService fileService;

    @RequestMapping(value = "/admin/indicatorSpecies.htm", method = RequestMethod.GET)
    public ModelAndView render() {
        Map<TaxonGroup, List<IndicatorSpecies>> groupedSpecies = taxaService.getGroupedIndicatorSpecies();
        Map<TaxonGroup, List<IndicatorSpeciesForm>> groupedDisplay = new HashMap<TaxonGroup, List<IndicatorSpeciesForm>>();
        for (Map.Entry<TaxonGroup, List<IndicatorSpecies>> e : groupedSpecies.entrySet()) {
            groupedDisplay.put(e.getKey(), new ArrayList<IndicatorSpeciesForm>());
            for (IndicatorSpecies i : e.getValue()) {
                groupedDisplay.get(e.getKey()).add(new IndicatorSpeciesForm(i));
            }
        }
        return new ModelAndView("adminIndicatorSpecies", "species", groupedDisplay);
    }

    @RequestMapping(value = "/admin/addIndicatorSpecies.htm", method = RequestMethod.GET)
    public ModelAndView renderAdd() {
        return buildModelAndView("addIndicatorSpecies", new IndicatorSpeciesForm());
    }

    @RequestMapping(value = { "/admin/addIndicatorSpecies.htm", "/admin/editIndicatorSpecies.htm" }, 
                    method = RequestMethod.POST)
    public String addIndicatorSpecies(@ModelAttribute("species") final IndicatorSpeciesForm species,
                                      BindingResult result) 
    {
        //IndicatorSpecies is = 
        	doInTransaction(new TransactionCallback<IndicatorSpecies>() {
            public IndicatorSpecies doInTransaction(TransactionStatus status) {
                TaxonGroup group = taxaService.getTaxonGroup(species.getTaxonGroup());
                IndicatorSpecies is = null;
                if (species.getId() != null && species.getId() > 0) {
                    is = taxaService.updateIndicatorSpecies(species.getId(), species.getScientificName(), 
                                                            species.getCommonName(), group, species.getRegions(), null);
                    //TODO add species profile support.
                } else {
                    is = taxaService.createIndicatorSpecies(species.getScientificName(), species.getCommonName(),
                                                            group, species.getRegions(), null);
                }
                if (species.getFile() != null) {
                    try {
                        fileService.createFile(IndicatorSpecies.class, is.getId(), species.getFile());
                    } catch (IOException ioe) {
                        throw new RuntimeException("Failed to write file to storage.", ioe);
                    }
                }
                return is;
            }
        });
        return "redirect:/admin/indicatorSpecies.htm";
    }

    @RequestMapping(value = "/admin/editIndicatorSpecies.htm", method = RequestMethod.GET)
    public ModelAndView renderEdit(@RequestParam("speciesID") Integer speciesID) {
        if (speciesID != null && speciesID > 0) {
            IndicatorSpecies species = taxaService.getIndicatorSpecies(speciesID);
            return buildModelAndView("addIndicatorSpecies", new IndicatorSpeciesForm(species));
        } else {
            return new ModelAndView(new RedirectView("/admin/indicatorSpecies.htm"));
        }
    }

    private ModelAndView buildModelAndView(String viewName, IndicatorSpeciesForm form) {
        Map<String, Object> model = new HashMap<String, Object>();
        // Taxon group names
        List<String> taxonGroupNames = new ArrayList<String>();
        for (TaxonGroup g : taxaService.getTaxonGroups()) {
            taxonGroupNames.add(g.getName());
        }
        model.put("taxonGroupNames", taxonGroupNames);
        // Region names
        List<String> regionNames = new ArrayList<String>();
        for (Region r : regionService.getRegions()) {
            regionNames.add(r.getRegionName());
        }
        model.put("regionNames", regionNames);
        // Form
        model.put("species", form);

        // Files
        List<FileDataSource> files = new ArrayList<FileDataSource>();
        if (form.getId() != null && form.getId() > 0) {
            files = fileService.getFiles(IndicatorSpecies.class, form.getId());
        }
        model.put("files", files);

        return new ModelAndView(viewName, model);
    }
}
