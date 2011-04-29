package au.com.gaiaresources.bdrs.servlet.view;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.activation.FileDataSource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.security.core.codec.Base64;
import org.springframework.web.servlet.view.AbstractView;

public class FileView extends AbstractView {
    private File f;
    private boolean forceDownload = true;
    private boolean base64 = false;
    private String fileType;
    
    private Logger logger = Logger.getLogger(getClass());
    
    public FileView(FileDataSource dataSource) {
        this(dataSource.getFile(), dataSource.getContentType());
    }
    
    public FileView(File file, String contentType) {
        this.f = file;
        super.setContentType(contentType);
    }
    
    @Override
    protected void renderMergedOutputModel(@SuppressWarnings("unchecked") Map model, HttpServletRequest request, HttpServletResponse response) 
                                           throws IOException 
    {
        logger.info("Streaming file: " + f.getAbsolutePath() + ", Content type: " + getContentType());
        
        response.setContentType(getContentType());
        
        if (forceDownload) {
            response.addHeader("Content-Disposition", "attachment; filename=\"" + f.getName() + "\"");
        } else {
            response.addHeader("Content-Disposition", "inline; filename=\"" + f.getName() + "\"");
        }
        
        FileInputStream fileInput = null;
        OutputStream output = response.getOutputStream();
        
        try{
	        if(base64){
	        	fileInput = new FileInputStream(f);
	        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        	int count;
	        	byte[] bytes = new byte[256];
	        
	        	while ((count = fileInput.read(bytes)) > 0) {
	        		baos.write(bytes, 0, count);
	        	}
	        	//encode file and store result in JSONObject
	    		JSONObject jsonFile = new JSONObject();
	    		jsonFile.put("base64", new String(Base64.encode(baos.toByteArray())));
	    		jsonFile.put("fileType", this.fileType);
	    		
	        	// support
	        	if (request.getParameter("callback") != null) {
	        		output.write((request.getParameter("callback") + "(").getBytes());
	        	}
	            output.write(jsonFile.toString().getBytes());
	        	if (request.getParameter("callback") != null) {
	        		output.write(");".getBytes());
	        	}
	    	}else{
	    		response.setContentLength((int)f.length());
	    		 fileInput = new FileInputStream(f);
	             IOUtils.copy(fileInput, output);
	    	}
        }finally {
            if (fileInput != null) {
                fileInput.close();
            }
            output.flush();
        }
    }
    
    public String toString() {
        return "FileView [file: " + f.getAbsolutePath() + ", contentType: " + getContentType() + "]";
    }
    
    public void setEncoding(boolean encode){
    	this.base64 = encode;
    }

	public void setFileType(String type) {
		this.fileType = type;
		
	}
    
}
