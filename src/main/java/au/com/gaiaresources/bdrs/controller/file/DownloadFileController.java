package au.com.gaiaresources.bdrs.controller.file;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
import au.com.gaiaresources.bdrs.security.Role;

@Controller
public class DownloadFileController extends AbstractDownloadFileController {
    
    public static final String FILE_DOWNLOAD_URL = "/files/download.htm";
    
    public static final String CLASS_NAME_QUERY_PARAM = "className";
    public static final String INSTANCE_ID_QUERY_PARAM = "id";
    public static final String FILENAME_QUERY_PARAM = "fileName";
    
    @Autowired
    private ManagedFileDAO managedFileDAO;
    
    @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR, Role.USER, Role.ANONYMOUS})
    @RequestMapping(value = FILE_DOWNLOAD_URL, method = RequestMethod.GET)
    public ModelAndView downloadFile(@RequestParam(CLASS_NAME_QUERY_PARAM) String className,
                                     @RequestParam(INSTANCE_ID_QUERY_PARAM) Integer id,
                                     @RequestParam(FILENAME_QUERY_PARAM) String fileName,
                                     HttpServletResponse response) {
        try {
            return super.downloadFile(className, id, fileName);
        }
        catch(HTTPException e) {
            response.setStatus(e.getStatusCode());
            return null;
        }
    }
    
    @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR, Role.USER, Role.ANONYMOUS})
    @RequestMapping(value="/files/downloadByUUID.htm", method=RequestMethod.GET)
    public ModelAndView downloadFileByUUID(HttpServletResponse response,
            @RequestParam(value="uuid", required=true) String uuid,
            @RequestParam(value="encode", required=false, defaultValue="false") boolean base64encode) {
        
    	log.debug("starting at file service " + System.currentTimeMillis() + " uuid=" + uuid);
        if(uuid == null) {
            response.setStatus(404);
            return null;
        }
        
        ManagedFile mf = managedFileDAO.getManagedFile(uuid);
        if(mf == null) {
            response.setStatus(404);
            return  null;
        }
        if(base64encode){
        	log.debug("returning from file service " + System.currentTimeMillis() + " uuid=" + uuid);
        	return super.downloadFile(mf.getClass().getName(), mf.getId(), mf.getFilename(), "application/javascript", base64encode);
        }else{
        	return super.downloadFile(mf.getClass().getName(), mf.getId(), mf.getFilename(), mf.getContentType());
        }
    }
}
