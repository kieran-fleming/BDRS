package au.com.gaiaresources.bdrs.controller.theme;

import java.io.File;
import java.io.FileNotFoundException;

import au.com.gaiaresources.bdrs.model.theme.Theme;
import au.com.gaiaresources.bdrs.util.FileUtils;

/**
 * The <code>ThemeFile</code> is a wrapper around a {@link File} object with 
 * helper functions to retrieve the content type of the file and to generate
 * download URLs.
 */
public class ThemeFile {
    
    private File file;
    private File storageDir;
    private Theme theme;
    private String contextPath;
    private String contentType;

    /**
     * Creates a new Theme File instance.
     * @param theme the theme that owns the wrapped file.
     * @param file the file represented by this instance.
     * @param storageDir the file store directory of the theme containing this file.
     * @param contextPath the context path of the servlet
     */
    public ThemeFile(Theme theme, File file, File storageDir, String contextPath) {
        this.file = file;
        this.storageDir = storageDir;
        this.theme = theme;
        this.contextPath = contextPath;
    }
    
    /**
     * Returns the file represented by this instance.
     * @return the file represented by this instance.
     */
    public File getFile() {
        return this.file;
    }
    
    /**
     * Returns the name of the file relative to the base storage directory.
     * @return the name of the file relative to the base storage directory.
     */
    public String getFileName() {
        return file.getAbsolutePath().substring(storageDir.getAbsolutePath().length()+1);
    }
    
    /**
     * The human readable name of the file represented by this object.
     * @return human readable name of the file represented by this object. 
     */
    public String getDisplayName() {
        return getFileName(); 
    }
    
    /**
     * Lists all files contained by this directory.
     * @return an array of <code>ThemeFile</code>s contained by this directory
     * or null if the represented file is not a directory.
     */
    public ThemeFile[] listFiles() {
        ThemeFile[] themeFiles = null;
        File[] files = this.file.listFiles();
        if(files != null) {
            themeFiles = new ThemeFile[files.length];
            for(int i=0; i<files.length; i++){
                themeFiles[i] = new ThemeFile(this.theme, files[i], this.storageDir, this.contextPath);
            }
        }
        
        return themeFiles;
    }
    
    /**
     * Returns a URL to download this file.
     * @return a URL to download the represented file.
     */
    public String getDownloadURL() {
        return String.format(Theme.ASSET_DOWNLOAD_URL_TMPL, this.contextPath, 
                             Theme.class.getName(), this.theme.getId(), Theme.THEME_DIR_RAW);
    }
    
    /**
     * Returns true if this file is editable. A file is editable if the 
     * content type starts with 'text' or the file is of type 'application/json' or
     * 'application/javascript'.
     * @return true if the file is editable, false otherwise.
     * @throws FileNotFoundException
     */
    public boolean canEdit() throws FileNotFoundException {

        return ThemeController.isTextContent(this.getContentType());
    }
    
    public String getContentType() throws FileNotFoundException {
        if(this.contentType == null) {
            if(this.file.isDirectory()) {
                this.contentType = "";
            } else {
                contentType = FileUtils.getContentType(this.file);
            }
        }
        return this.contentType;
    }
}