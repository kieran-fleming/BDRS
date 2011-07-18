package au.com.gaiaresources.bdrs.service.managedFile;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
import au.com.gaiaresources.bdrs.util.FileUtils;

// saves managed files

@Service
public class ManagedFileService {
    
    @Autowired
    ManagedFileDAO managedFileDAO;
    @Autowired
    FileService fileService;
    
    Logger log = Logger.getLogger(this.getClass());
    
    public ManagedFile saveManagedFile(String uuid, String description, String credit, String license, MultipartFile file) throws IOException {
        ManagedFile mf = (StringUtils.isEmpty(uuid)) ? new ManagedFile() : managedFileDAO.getManagedFile(uuid);
        return saveManagedFile(mf, description, credit, license, file);
    }
    
    public ManagedFile saveManagedFile(Integer pk, String description, String credit, String license, MultipartFile file) throws IOException {
        ManagedFile mf = (pk == 0 || pk == null) ? new ManagedFile() : managedFileDAO.getManagedFile(pk);
        return saveManagedFile(mf, description, credit, license, file);
    }
    
    private ManagedFile saveManagedFile(ManagedFile mf, String description, String credit, String license, MultipartFile file) throws IOException {
        mf.setDescription(description);
        mf.setCredit(credit);
        mf.setLicense(license);
        
        if(file != null) {
            mf.setContentType(file.getContentType());
            mf.setFilename(file.getOriginalFilename());
        } else {
            log.warn("file is null or empty");
        }
         
        mf = managedFileDAO.saveOrUpdate(mf);
        
        if(file != null) {
            fileService.createFile(mf, file);
            File f = fileService.getFile(mf, mf.getFilename()).getFile();
            mf.setContentType(FileUtils.getContentType(f));
            mf = managedFileDAO.saveOrUpdate(mf);
        }
        return mf;
    }
}
