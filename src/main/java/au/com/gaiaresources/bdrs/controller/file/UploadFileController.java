package au.com.gaiaresources.bdrs.controller.file;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import au.com.gaiaresources.bdrs.db.Persistent;
import au.com.gaiaresources.bdrs.file.FileService;

public class UploadFileController {
    @Autowired
    private FileService fileService;
    
    @SuppressWarnings("unchecked")
    public void upload(@RequestParam("file") MultipartFile file, @RequestParam("className") String className,
                       @RequestParam("id") Integer id) throws IOException
    {
        Class<? extends Persistent> persistentClass = null;
        try {
            persistentClass = (Class<? extends Persistent>) Class.forName(className);
        } catch (ClassNotFoundException cnfe) {
            throw new IllegalArgumentException("Class " + className + " does not exist.", cnfe);
        }
        fileService.createFile(persistentClass, id, file);
    }
}
