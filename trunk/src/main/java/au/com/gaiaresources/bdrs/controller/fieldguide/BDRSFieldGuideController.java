package au.com.gaiaresources.bdrs.controller.fieldguide;

import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.displaytag.tags.TableTagParameters;
import org.displaytag.util.ParamEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter.SortOrder;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;

@Controller
public class BDRSFieldGuideController  extends AbstractController {
    private static final String TAXA_LISTING_TABLE_ID = "fieldGuideTaxaListingTable";
    private static final int TAXA_LISTING_PAGE_SIZE = 50;
    
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());
    
    @Autowired
    private TaxaDAO taxaDAO;
    
    private ParamEncoder taxonListingParamEncoder = new ParamEncoder(TAXA_LISTING_TABLE_ID);
    
    @RequestMapping(value = "/fieldguide/groups.htm", method = RequestMethod.GET)
    public ModelAndView listGroups(  HttpServletRequest request,
                                HttpServletResponse response) {
        
        ModelAndView mv = new ModelAndView("fieldGuideGroupListing");
        mv.addObject("taxonGroups", taxaDAO.getTaxonGroups());
        return mv;
    }
    
    @RequestMapping(value = "/fieldguide/taxa.htm", method = RequestMethod.GET)
    public ModelAndView listTaxa(  HttpServletRequest request,
                                HttpServletResponse response,
                                @RequestParam(value="groupId", required=true) int groupPk) throws NullPointerException, ParseException {
        
        TaxonGroup taxonGroup = taxaDAO.getTaxonGroup(groupPk);
        String pnArg = request.getParameter(getTaxonPageNumberParamName());

        int pageNum = pnArg != null && !pnArg.isEmpty() ? Integer.parseInt(pnArg) : 1;
        pageNum = pageNum < 1 ? 1 : pageNum;
        int start = (pageNum - 1) * TAXA_LISTING_PAGE_SIZE;
        
        PaginationFilter filter = new PaginationFilter(start, TAXA_LISTING_PAGE_SIZE);
        
        if (StringUtils.hasLength(request.getParameter(getTaxonSortParamName()))
                && StringUtils.hasLength(request.getParameter(getTaxonOrderParamName()))) {
            
            String sortArg = request.getParameter(getTaxonSortParamName());
            String sortOrder = request.getParameter(getTaxonOrderParamName());
            
            filter.addSortingCriteria(sortArg, SortOrder.fromString(sortOrder));
        }
        
        ModelAndView mv = new ModelAndView("fieldGuideTaxaListing");
        mv.addObject("taxonGroup", taxonGroup);
        mv.addObject("taxaPaginator", taxaDAO.getIndicatorSpecies(taxonGroup, filter));
        return mv;
    }
    
    @RequestMapping(value = "/fieldguide/taxon.htm", method = RequestMethod.GET)
    public ModelAndView viewTaxon(  HttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestParam(value="id", required=true) int taxonPk) {
        
        ModelAndView mv = new ModelAndView("fieldGuideViewTaxon");
        mv.addObject("taxon", taxaDAO.getIndicatorSpecies(taxonPk));
        return mv;
    }
    
    public String getTaxonPageNumberParamName() {
        return taxonListingParamEncoder.encodeParameterName(TableTagParameters.PARAMETER_PAGE);
    }

    public String getTaxonSortParamName() {
        return taxonListingParamEncoder.encodeParameterName(TableTagParameters.PARAMETER_SORT);
    }

    public String getTaxonOrderParamName() {
        return taxonListingParamEncoder.encodeParameterName(TableTagParameters.PARAMETER_ORDER);
    }
}
