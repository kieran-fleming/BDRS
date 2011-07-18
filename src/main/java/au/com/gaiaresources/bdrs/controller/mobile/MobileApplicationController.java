package au.com.gaiaresources.bdrs.controller.mobile;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.file.AbstractDownloadFileController;

@Controller
public class MobileApplicationController extends AbstractDownloadFileController {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	public static final String APPLICATION_URL = "/mobile/application.htm";
	public static final String ANDROID_APP_URL = "/au/com/gaiaresources/bdrs/controller/mobile/bdrs-mobile.apk";
	
	@RequestMapping(value = APPLICATION_URL, method = RequestMethod.GET)
	public ModelAndView getApplication(HttpServletResponse response) throws IOException{
		
		InputStream inputStream = this.getClass().getResourceAsStream(ANDROID_APP_URL);
		try {
			response.setHeader("Content-Disposition", "attachment;filename=bdrs-mobile.apk");
			response.setContentType("application/vnd.android.package-archive");
			IOUtils.copy(inputStream, response.getOutputStream());
			response.flushBuffer();
		} catch (IOException e) {
			log.warn(" IOException while writing file to output stream. Filename was '"+ ANDROID_APP_URL +"'. Exception log: " + e.getMessage());
		}finally{
			inputStream.close();
		}
		return null;
	}

}
