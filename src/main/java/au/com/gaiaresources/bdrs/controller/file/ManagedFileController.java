package au.com.gaiaresources.bdrs.controller.file;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.displaytag.tags.TableTagParameters;
import org.displaytag.util.ParamEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter.SortOrder;
import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
import au.com.gaiaresources.bdrs.service.managedFile.ManagedFileService;

/**
 * The <code>TaxonomyManagementControllers</code> handles all view requests
 * pertaining to the creating and updating of taxonomy (indicator species) and
 * taxonomy related objects.
 */
@Controller
public class ManagedFileController extends AbstractController {

    private static final String MANAGED_FILE_LISTING_TABLE_ID = "managedFileListingTable";
    private static final int MANAGED_FILE_LISTING_PAGE_SIZE = 50;
    
    public static final String MANAGED_FILE_EDIT_AJAX_URL = "/bdrs/user/managedfile/service/edit.htm";
    public static final String MANAGED_FILE_PK = "pk";
    
    public static final String AJAX_PROP_ID = "id";
    public static final String AJAX_PROP_UUID = "uuid";
    public static final String AJAX_PROP_DESCRIPTION = "description";
    public static final String AJAX_PROP_LICENSE = "license";
    public static final String AJAX_PROP_CREDIT = "credit";
    
    private Logger log = Logger.getLogger(getClass());
    
    @Autowired
    private ManagedFileDAO managedFileDAO;
    
    @Autowired
    private ManagedFileService mfService;
    
    private ParamEncoder managedFileListingParamEncoder = new ParamEncoder(MANAGED_FILE_LISTING_TABLE_ID);
    
    @RequestMapping(value = "/bdrs/user/managedfile/edit.htm", method = RequestMethod.GET)
    public ModelAndView edit(HttpServletRequest request,
                             HttpServletResponse response,
                             @RequestParam(value="id", required=false, defaultValue="0") int pk) {
        ManagedFile mf = pk == 0 ? new ManagedFile() : managedFileDAO.getManagedFile(pk);
        
        ModelAndView mv = new ModelAndView("managedFileEdit");
        mv.addObject("managedFile", mf);
        return mv;
    }
    
    @RequestMapping(value = "/bdrs/user/managedfile/edit.htm", method = RequestMethod.POST)
    public ModelAndView editSubmit(MultipartHttpServletRequest request,
                                 HttpServletResponse response,
                                 @RequestParam(value="managedFilePk", required=false, defaultValue="0") int pk,
                                 @RequestParam(value="description", required=true) String description,
                                 @RequestParam(value="credit", required=true) String credit,
                                 @RequestParam(value="license", required=true) String license) throws IOException {      
        MultipartFile file = request.getFile("file").getSize() > 0 ? request.getFile("file") : null;
        mfService.saveManagedFile(pk, description, credit, license, file);
        return new ModelAndView(new RedirectView("/bdrs/user/managedfile/listing.htm", true));
    }

    @RequestMapping(value = "/bdrs/user/managedfile/listing.htm", method = RequestMethod.GET)
    public ModelAndView listing(HttpServletRequest request,
                                HttpServletResponse response) throws NullPointerException, ParseException {

        String pnArg = request.getParameter(getPageNumberParamName());

        int pageNum = pnArg != null && !pnArg.isEmpty() ? Integer.parseInt(pnArg) : 1;
        pageNum = pageNum < 1 ? 1 : pageNum;
        int start = (pageNum - 1) * MANAGED_FILE_LISTING_PAGE_SIZE;
        
        PaginationFilter filter = new PaginationFilter(start, MANAGED_FILE_LISTING_PAGE_SIZE);
        
        if (StringUtils.hasLength(request.getParameter(getSortParamName()))
                && StringUtils.hasLength(request.getParameter(getOrderParamName()))) {
            String sortArg = request.getParameter(getSortParamName());
            String sortOrder = request.getParameter(getOrderParamName());
            filter.addSortingCriteria(sortArg, SortOrder.fromString(sortOrder));
        }
        
        ModelAndView mv = new ModelAndView("managedFileList");
        mv.addObject("managedFilePaginator", managedFileDAO.getManagedFiles(filter));
        
        return mv;
    }
    
    @RequestMapping(value = "/bdrs/user/managedfile/delete.htm", method = RequestMethod.POST)
    public ModelAndView delete(HttpServletRequest request,
                               HttpServletResponse response,
                               @RequestParam(value="managedFilePk", required=false) int[] managedFilePk) throws NullPointerException, ParseException {
        
        if(managedFilePk != null) {
            for(int pk : managedFilePk) {
                managedFileDAO.delete(managedFileDAO.getManagedFile(pk));
            }
        }
        
        return new ModelAndView(new RedirectView("/bdrs/user/managedfile/listing.htm", true));
    }
    
    public String getPageNumberParamName() {
        return managedFileListingParamEncoder.encodeParameterName(TableTagParameters.PARAMETER_PAGE);
    }

    public String getSortParamName() {
        return managedFileListingParamEncoder.encodeParameterName(TableTagParameters.PARAMETER_SORT);
    }

    public String getOrderParamName() {
        return managedFileListingParamEncoder.encodeParameterName(TableTagParameters.PARAMETER_ORDER);
    }
    
    @RequestMapping(value = MANAGED_FILE_EDIT_AJAX_URL, method = RequestMethod.GET)
    public void viewService(HttpServletRequest request,
                            HttpServletResponse response,
                            @RequestParam(value="id", required=false, defaultValue="0") int pk) throws IOException {
        ManagedFile mf = pk == 0 ? new ManagedFile() : managedFileDAO.getManagedFile(pk);
        
        JSONObject result = new JSONObject();
        JSONObject data = new JSONObject();
        
        // If the managed file already exists add these to the ajax response
        if (mf.getId() != null) {
            data.put(AJAX_PROP_ID, mf.getId().toString());
            data.put(AJAX_PROP_UUID, mf.getUuid());
        }
        data.put(AJAX_PROP_CREDIT, mf.getCredit());
        data.put(AJAX_PROP_DESCRIPTION, mf.getDescription());
        data.put(AJAX_PROP_LICENSE, mf.getLicense());
        
        // you need to add the child JSON objects to the parent AFTER
        // you have populated the child's properties. Otherwise the child's
        // properties do not show up in the final JSON object!
        result.put("data", data);
        result.put("success", true);
        
        response.setContentType("application/json");
        response.getWriter().write(result.toString());
    }

    @RequestMapping(value = MANAGED_FILE_EDIT_AJAX_URL, method = RequestMethod.POST)
    public void saveService(MultipartHttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value="managedFilePk", required=false, defaultValue="0") int pk,
            @RequestParam(value="description", required=true) String description,
            @RequestParam(value="credit", required=true) String credit,
            @RequestParam(value="license", required=true) String license) throws IOException {      
        MultipartFile file = request.getFile("file").getSize() > 0 ? request.getFile("file") : null;
        
        ManagedFile mf = null;
        try {
            mf = mfService.saveManagedFile(pk, description, credit, license, file);
        } catch (IOException e) {
            
        }

        JSONObject result = new JSONObject();
        
        if (mf != null) {
            JSONObject data = new JSONObject();
            result.put("data", data);
            // If the managed file already exists add these to the ajax response
            data.put(AJAX_PROP_ID, mf.getId().toString());
            data.put(AJAX_PROP_UUID, mf.getUuid());
            data.put(AJAX_PROP_CREDIT, mf.getCredit());
            data.put(AJAX_PROP_DESCRIPTION, mf.getDescription());
            data.put(AJAX_PROP_LICENSE, mf.getLicense());
            
            result.put("success", true);
            result.put("message", "File successfully uploaded.");
        } else {
            response.setStatus(500);
            result.put("success", false);
            result.put("message", "Error while uploading file.");
        }
        response.setContentType("application/json");
        response.getWriter().write(result.toString());
    }
}
