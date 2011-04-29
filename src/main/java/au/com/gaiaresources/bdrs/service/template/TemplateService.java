package au.com.gaiaresources.bdrs.service.template;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.file.FileService;

@Service
public class TemplateService {
    public static final String VELOCITY_CONFIG_FILE = "velocity.properties";
    public static final String FILESTORE_LOADER_PATH = "filestore.resource.loader.path";
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());
    
    private VelocityEngine templateEngine;
    @Autowired
    private FileService fileService;
    
    @PostConstruct
    public void init() throws Exception {
        InputStream is = null;
        try {
            Properties config = new Properties();
            is = getClass().getResourceAsStream(VELOCITY_CONFIG_FILE);
            config.load(is);
            config.setProperty(FILESTORE_LOADER_PATH, fileService.getStorageDirectory());
            
            templateEngine = new VelocityEngine();
            templateEngine.init(config);
        } finally {
            if(is != null) {
                is.close();
            }
        }
    }
    
    public void mergeTemplate(PersistentImpl p, String fileName, Map<?,?> model, Writer writer) {
        File file = fileService.getFile(p, fileName).getFile();
        String templateLocation = file.getAbsolutePath().replace(fileService.getStorageDirectory(), "");
        VelocityEngineUtils.mergeTemplate(templateEngine, templateLocation, model, writer);
    }

    public String transformToString(String fileName, Class<?> resource, Map<String, Object> subsitutionParams) {
        String resourcePath = resource.getPackage().getName();
        resourcePath = resourcePath.replace('.', '/') + "/" + fileName;
        return VelocityEngineUtils.mergeTemplateIntoString(templateEngine, resourcePath, subsitutionParams);
    }
    
    public String evaluate(String value, Map<String, Object> params) {
        StringWriter writer = new StringWriter();
        VelocityContext vContext = new VelocityContext(params); 

        // if there's an error we're just going to use the non processed string
        try {
            templateEngine.evaluate(vContext, writer, "velocity log tag", value);
            value = writer.toString();
        } catch (IOException e) {
            log.error("Error processing velocity template", e);
        }
        return value;
    }
}