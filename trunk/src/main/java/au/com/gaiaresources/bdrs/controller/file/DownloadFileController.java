package au.com.gaiaresources.bdrs.controller.file;

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

@Controller
public class DownloadFileController extends AbstractDownloadFileController {
    @Autowired
    private ManagedFileDAO managedFileDAO;
    
    @RequestMapping(value = "/files/download.htm", method = RequestMethod.GET)
    public ModelAndView downloadFile(@RequestParam("className") String className,
                                     @RequestParam("id") Integer id,
                                     @RequestParam("fileName") String fileName,
                                     HttpServletResponse response) {
        try {
            return super.downloadFile(className, id, fileName);
        }
        catch(HTTPException e) {
            response.setStatus(e.getStatusCode());
            return null;
        }
    }
    
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
