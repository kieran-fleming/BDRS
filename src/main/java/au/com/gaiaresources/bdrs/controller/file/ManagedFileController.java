package au.com.gaiaresources.bdrs.controller.file;

import java.io.IOException;
import java.text.ParseException;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import au.com.gaiaresources.bdrs.json.JSONObject;

import org.displaytag.tags.TableTagParameters;
import org.displaytag.util.ParamEncoder;
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
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataBuilder;
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataHelper;
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataRow;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.managedFile.ManagedFileService;
import au.com.gaiaresources.bdrs.util.DateFormatter;

/**
 * The <code>TaxonomyManagementControllers</code> handles all view requests
 * pertaining to the creating and updating of taxonomy (indicator species) and
 * taxonomy related objects.
 */
@RolesAllowed({ Role.ADMIN })
@Controller
public class ManagedFileController extends AbstractController {

    private static final String MANAGED_FILE_LISTING_TABLE_ID = "managedFileListingTable";
    
    public static final String MANAGED_FILE_EDIT_AJAX_URL = "/bdrs/user/managedfile/service/edit.htm";
    public static final String MANAGED_FILE_SEARCH_AJAX_URL = "/bdrs/user/managedfile/service/search.htm";
    public static final String MANAGED_FILE_PK = "pk";
    
    public static final String AJAX_PROP_ID = "id";
    public static final String AJAX_PROP_UUID = "uuid";
    public static final String AJAX_PROP_DESCRIPTION = "description";
    public static final String AJAX_PROP_LICENSE = "license";
    public static final String AJAX_PROP_CREDIT = "credit";
    public static final String AJAX_PROP_FILENAME = "filename";
    public static final String AJAX_PROP_FILEURL = "fileURL";
    public static final String AJAX_PROP_UPDATED_AT = "updatedAt";
    public static final String AJAX_PROP_UPDATED_BY = "updatedBy";
    public static final String AJAX_PROP_CONTENT_TYPE = "contentType";
    public static final String AJAX_PROP_NAME = "name";
   
    @Autowired
    private ManagedFileDAO managedFileDAO;
    
    @Autowired
    private ManagedFileService mfService;
    
    private ParamEncoder managedFileListingParamEncoder = new ParamEncoder(MANAGED_FILE_LISTING_TABLE_ID);
    
    @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR})
    @RequestMapping(value = "/bdrs/user/managedfile/edit.htm", method = RequestMethod.GET)
    public ModelAndView edit(HttpServletRequest request,
                             HttpServletResponse response,
                             @RequestParam(value="id", required=false, defaultValue="0") int pk) {
        ManagedFile mf = pk == 0 ? new ManagedFile() : managedFileDAO.getManagedFile(pk);
        
        ModelAndView mv = new ModelAndView("managedFileEdit");
        mv.addObject("managedFile", mf);
        return mv;
    }
    
    @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR})
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

    @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR})
    @RequestMapping(value = "/bdrs/user/managedfile/listing.htm", method = RequestMethod.GET)
    public ModelAndView listing(HttpServletRequest request,
                                HttpServletResponse response) {
    
        ModelAndView mv = new ModelAndView("managedFileList");        
        return mv;
    }
    
    @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR})
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
    
    @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR})
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

    @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR})
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
            
            // If the managed file already exists add these to the ajax response
            data.put(AJAX_PROP_ID, mf.getId().toString());
            data.put(AJAX_PROP_UUID, mf.getUuid());
            data.put(AJAX_PROP_CREDIT, mf.getCredit());
            data.put(AJAX_PROP_DESCRIPTION, mf.getDescription());
            data.put(AJAX_PROP_LICENSE, mf.getLicense());
            result.put("data", data);
            
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
    
    /**
     * Performs a search for ManagedFiles using the supplied parameters and returns the results in JSON.
     * @param request the http request we are processing.
     * @param response the response to be returned to the client.
     * @param fileSearchText Restricts the results to ManagedFiles with filename or description properties containing this String.
     * @param userSearchText Restricts the results to ManagedFiles  Restricts the results to ManagedFiles created by or last updated by a user with a firstname 
     * or lastname containing this String.
     * @param imagesOnly If true, will restrict the results to ManagedFiles with a contentType starting with "image".
     * @throws Exception if there is an error performing the search (e.g. a database failure).
     */
    @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR})
    @RequestMapping(value = MANAGED_FILE_SEARCH_AJAX_URL, method = RequestMethod.GET)
    public void searchService(HttpServletRequest request,
                              HttpServletResponse response,
                              @RequestParam(value="fileSearchText", required=false) String fileSearchText,
                              @RequestParam(value="userSearchText", required=false) String userSearchText,
                              @RequestParam(value="imagesOnly", required=false, defaultValue="false") Boolean imagesOnly) throws Exception {
    	 JqGridDataHelper jqGridHelper = new JqGridDataHelper(request);       
         PaginationFilter filter = jqGridHelper.createFilter(request);
         
         String typeFilter = imagesOnly != null && imagesOnly ? ManagedFile.IMAGE_CONTENT_TYPE_PREFIX : "";
         PagedQueryResult<Object[]> queryResult = managedFileDAO.search(filter, fileSearchText, typeFilter, userSearchText);
         
         JqGridDataBuilder builder = new JqGridDataBuilder(jqGridHelper.getMaxPerPage(), queryResult.getCount(), jqGridHelper.getRequestedPage());

         if (queryResult.getCount() > 0) {
             for (Object[] fileInfo : queryResult.getList()) {
            	 ManagedFile file = (ManagedFile)fileInfo[0];
            	 User updatedBy = (User)fileInfo[1];
                 JqGridDataRow row = new JqGridDataRow(file.getId());
                 
                 row.addValue(AJAX_PROP_UUID, file.getUuid())
                    .addValue(AJAX_PROP_UPDATED_AT, DateFormatter.format(file.getUpdatedAt(), DateFormatter.DAY_MONTH_YEAR_TIME))
                    .addValue(AJAX_PROP_UPDATED_BY+"."+AJAX_PROP_NAME, updatedBy.getFullName())
                    .addValue(AJAX_PROP_FILENAME, file.getFilename())
                    .addValue(AJAX_PROP_CONTENT_TYPE, file.getContentType())
                    .addValue(AJAX_PROP_DESCRIPTION, file.getDescription())
                    .addValue(AJAX_PROP_FILEURL, file.getFileURL());
                 builder.addRow(row);
             }
            
         }
         response.setContentType("application/json");
         response.getWriter().write(builder.toJson());
     }
}
