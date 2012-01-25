/**
 * 
 */
package au.com.gaiaresources.bdrs.service.theme;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.activation.FileDataSource;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.theme.Theme;
import au.com.gaiaresources.bdrs.model.theme.ThemeDAO;
import au.com.gaiaresources.bdrs.model.theme.ThemeElement;
import au.com.gaiaresources.bdrs.model.theme.ThemeElementType;
import au.com.gaiaresources.bdrs.model.theme.ThemePage;
import au.com.gaiaresources.bdrs.util.ZipUtils;
import au.com.gaiaresources.bdrs.util.file.FileSystem;
import au.com.gaiaresources.bdrs.util.file.FileSystemFactory;
import edu.emory.mathcs.backport.java.util.Collections;


/**
 * Services for creating, saving, and updating themes.
 * @author stephanie
 */
@Component
public class ThemeService {
    /**
     * The name of the configuration file that is expected inside all theme zips.
     */
    public static final String THEME_CONFIG_FILENAME = "config.json";
    
    public static final String KEY_REPLACE_REGEX_TEMPLATE = "\\$\\{(\\s*)%s(\\s*)\\}";
    private static final String JSON_KEY_THEME_PAGES = "theme_pages";
    
    @Autowired
    private FileService fileService;
    
    @Autowired
    private ThemeDAO themeDAO;
    @Autowired
    private ManagedFileDAO managedFileDAO;
    
    private Logger log = Logger.getLogger(ThemeService.class);
    
    /**
     * The set of content types that should be passed through the search and 
     * replace process but do not start with the word 'text'.
     */
    public static final Set<String> PATTERN_REPLACE_CONTENT_TYPES;
    static {
        Set<String> tempSet = new HashSet<String>();
        tempSet.add("application/json");
        tempSet.add("application/javascript");
        tempSet.add("application/xml");
        tempSet.add("image/svg+xml");
        
        PATTERN_REPLACE_CONTENT_TYPES = Collections.unmodifiableSet(tempSet);
    }
    
    /**
     * Returns true if the specified content type starts with text or
     * is listed in the set of content types that should be processed as a 
     * text file.
     * @param contentType the content type string to be tested.
     * @return
     */
    public static boolean isTextContent(String contentType) {
        return contentType.startsWith("text") || PATTERN_REPLACE_CONTENT_TYPES.contains(contentType);
    }
    
    /**
     * Performs variable replacement on theme files.
     * @param theme The theme to process data from
     * @param themeFiles The source directory of the files to read from.
     * @param destDir The destination directory to save the files in
     * @param assetContext The string replacement for downloadable assets
     * @param paramMap Parameters for replacing content
     * @throws IOException
     */
    public void processThemeData(Theme theme, File themeFiles,
            File destDir, String assetContext) throws IOException {
        processThemeData(theme, FileSystemFactory.getFileSystem(themeFiles.getAbsolutePath()), destDir, assetContext);
    }
    
    /**
     * Perform variable replacement on theme files and copy to the processed directory.
     * @param theme The theme to process data from
     * @param themeFiles The {@link FileSystem FileSystem} object to retrieve files from
     * @param destDir The destination directory to save the files in
     * @param assetContext The string replacement for downloadable assets
     * @throws IOException
     */
    private void processThemeData(Theme theme, FileSystem themeFiles, File destDir, String assetContext) throws IOException {
        byte[] buffer = new byte[4096];
        InputStream fis = null;
        FileOutputStream fos = null;
        File target;
        
        for(FileSystem f : themeFiles.listFiles()) {
            String relativePath = f.getName();
            target = new File(destDir.getAbsolutePath() + File.separator + relativePath);
            
            if(f.isDirectory()) {
                if(!target.exists()) {
                    boolean dirCreateSuccess = target.mkdirs();
                    if(!dirCreateSuccess) {
                        throw new IOException("Unable to create directory (including parents): "+target.getAbsolutePath());
                    }
                }
                //log.debug("Make Dir: "+target.getAbsolutePath());
                processThemeData(theme, f, target, assetContext);
            } else {
                
                File parentFile = target.getParentFile();
                if(!parentFile.exists()) {
                    boolean dirCreateSuccess = parentFile.mkdirs();
                    if(!dirCreateSuccess) {
                        throw new IOException("Unable to create directory path: "+parentFile.getAbsolutePath());
                    }
                }
                
                try {
                    fis = f.getInputStream();
                    fos = new FileOutputStream(target);
                    
                    // Make sure that the file will have a directory to go into.
                    if(!target.getParentFile().exists()) {
                        boolean dirCreateSuccess = target.getParentFile().mkdirs();
                        if(!dirCreateSuccess) {
                            throw new IOException("Unable to create directory path: "+target.getParentFile().getAbsolutePath());
                        }
                    }
                    String contentType = au.com.gaiaresources.bdrs.util.FileUtils.getContentType(fis);
                    // check if the content check has used up the stream or closed it
                    // if so, initialize a new input stream
                    try {
                        if (fis.available() < f.length()) {
                            fis.close();
                            fis = f.getInputStream();
                        }
                    } catch (IOException e) {
                        fis.close();
                        fis = f.getInputStream();
                    }
                    
                    if(isTextContent(contentType)) {
                        // Read the file into memory
                        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                        StringBuilder builder = new StringBuilder();
                        for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                            builder.append(line);
                            builder.append("\n");
                        }
                        reader.close();
                        
                        // Search and Replace
                        String regex;
                        String content = builder.toString();
                        for(ThemeElement themeElement : theme.getThemeElements()) {
                            regex = String.format(KEY_REPLACE_REGEX_TEMPLATE, Pattern.quote(themeElement.getKey()));
                            content = content.replaceAll(regex, Matcher.quoteReplacement(themeElement.getCustomValue()));
                        }
                        
                        // Now for the assets
                        regex = String.format(KEY_REPLACE_REGEX_TEMPLATE, Pattern.quote(Theme.ASSET_KEY));
                        content = content.replaceAll(regex, Matcher.quoteReplacement(assetContext));
                        
                        // Write the file to disk
                        Writer writer = new OutputStreamWriter(fos);
                        writer.write(content);
                        writer.flush();
                        writer.close();
                    } else {
                        for(int read = fis.read(buffer); read > -1; read = fis.read(buffer)) {
                            fos.write(buffer, 0, read);
                        }
                    }
                    fos.flush();
                    fos.close();
                } catch(IOException ioe) {
                    log.error(ioe.getMessage(), ioe);
                    throw ioe;
                } finally {
                    try{
                        if (fis != null) {
                            fis.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch(IOException e) {
                        log.error(e.getMessage(), e);
                        throw e;
                    }
                }
            }
        }
    }

    /**
     * Reverts a theme back to the original uploaded zip file.
     * @param theme The theme to revert
     * @param managedFile The managed file to revert to
     * @param rawTargetDir The raw directory to unzip the files to
     * @param persist Flag indicating whether or not to persist the changes to the database
     * @return The Theme object, reverted to the original zip configuration.
     * @throws IOException
     */
    public Theme revertToZip(Theme theme, ManagedFile managedFile, File rawTargetDir, boolean persist) throws IOException {
        if (rawTargetDir == null) {
            rawTargetDir = fileService.getTargetDirectory(theme, Theme.THEME_DIR_RAW, true);
        }
        
        FileDataSource fileDataSource = fileService.getFile(managedFile, managedFile.getFilename());
        ZipFile zipFile = new ZipFile(fileDataSource.getFile());
        ZipEntry configEntry = zipFile.getEntry(THEME_CONFIG_FILENAME);
        InputStream configInputStream = null;
        try {
            configInputStream = zipFile.getInputStream(configEntry);
            theme = loadThemeConfig(theme, configInputStream, persist);
        } finally {
            if (configInputStream != null) {
                configInputStream.close();
            }
        }
        if (persist) {
            // Unzip and store raw files.
            if(rawTargetDir.exists()) {
                FileUtils.deleteDirectory(rawTargetDir); 
            }
            if(!rawTargetDir.exists()) {
                boolean dirCreateSuccess = rawTargetDir.mkdirs();
                if(!dirCreateSuccess) {
                    throw new IOException("Unable to create directory (including parents): "+rawTargetDir.getAbsolutePath());
                }
            }
            ZipUtils.decompressToDir(zipFile, rawTargetDir);
        }
        return theme;
    }

    /**
     * Copies and processes files from the raw directory to the processed directory.
     * @param theme The theme to copy and process files for
     * @param contextPath The context path of the request (used for determining asset location)
     * @param rawTargetDir The path of the directory to read files from for processing
     * @param processedTargetPath The path of the directory to write files to
     * @throws IOException
     */
    public void processFiles(Theme theme, String contextPath, String rawTargetPath, String processedTargetPath) throws IOException {
        File rawTargetDir = null;
        if (rawTargetPath == null) {
            rawTargetPath = Theme.THEME_DIR_RAW;
        }
        rawTargetDir = fileService.getTargetDirectory(theme, rawTargetPath, true);
        FileSystem targetFS = FileSystemFactory.getFileSystem(rawTargetDir.getAbsolutePath());
        
        File processedTargetDir = null;
        if (processedTargetPath == null) {
            processedTargetPath = Theme.THEME_DIR_PROCESSED;
        } 
        processedTargetDir = fileService.getTargetDirectory(theme, processedTargetPath, true);
        
        processFiles(theme, contextPath, targetFS, processedTargetDir);
    }
    
    /**
     * Copies and processes files from the raw directory to the processed directory.
     * @param theme The theme to copy and process files for
     * @param contextPath The context path of the request (used for determining asset location)
     * @param rawTargetDir The directory to read files from for processing
     * @param processedTargetPath The directory to write files to
     * @throws IOException
     */
    public void processFiles(Theme theme, String contextPath,
            File rawTargetDir, File processedTargetDir) throws IOException {
        FileSystem targetFS = FileSystemFactory.getFileSystem(rawTargetDir.getAbsolutePath());
        processFiles(theme, contextPath, targetFS, processedTargetDir);
    }
    
    /**
     * Copies and processes files from the raw directory to the processed directory.
     * @param theme The theme to copy and process files for
     * @param contextPath The context path of the request (used for determining asset location)
     * @param themeFiles The {@link FileSystem FileSystem} object representing the location to read files from for processing
     * @param processedTargetPath The path of the directory to write files to
     * @throws IOException
     */
    public void processFiles(Theme theme, String contextPath, FileSystem themeFiles, File processedTargetDir) throws IOException {
        // If the processed file directory exists, delete it first before we process the raw files.
        if(processedTargetDir.exists()) {
            FileUtils.deleteDirectory(processedTargetDir); 
        }
        
        String assetContext = String.format(Theme.ASSET_DOWNLOAD_URL_TMPL, contextPath, 
                                            theme.getClass().getName(), theme.getId(), processedTargetDir.getName());
        processThemeData(theme, themeFiles, processedTargetDir, assetContext);
    }
    
    /**
     * Loads a theme from the configuration in the specified zip file
     * @param theme The theme to load the configuration to
     * @param themeZip The zip file to get the configuration from
     * @param persist Flag indicating whether or not to persist the changes to the DAO
     * @return The theme with the configuration loaded.
     * @throws ZipException
     * @throws IOException
     */
    private Theme loadThemeConfig(Theme theme, InputStream configInputStream, boolean persist) throws ZipException, IOException {
        JSONObject config = (JSONObject) au.com.gaiaresources.bdrs.util.FileUtils.readJsonStream(configInputStream);
        try {
            JSONArray cssArray = config.getJSONArray("css_files");
            String[] css = new String[cssArray.size()];
            for(int i=0; i<cssArray.size(); i++) {
                css[i] = cssArray.getString(i);
            }
            theme.setCssFiles(css);
            
            JSONArray jsArray = config.getJSONArray("js_files");
            String[] js = new String[jsArray.size()];
            for(int i=0; i<jsArray.size(); i++) {
                js[i] = jsArray.getString(i);
            }
            theme.setJsFiles(js);
            
            Map<String, ThemeElement> themeElemMap = new HashMap<String, ThemeElement>();
            for(ThemeElement te : theme.getThemeElements()) {
                themeElemMap.put(te.getKey(), te);
            }
            
            JSONObject elem;
            String key;
            ThemeElement themeElement;
            JSONArray themeElemArray = config.getJSONArray("theme_elements");
            List<ThemeElement> themeElementList = new ArrayList<ThemeElement>(themeElemArray.size());
            for(int i=0; i<themeElemArray.size(); i++) {
                elem = themeElemArray.getJSONObject(i);
                key = elem.getString("key");
                
                themeElement = themeElemMap.containsKey(key) ? themeElemMap.remove(key) : new ThemeElement();
                themeElement.setKey(key);
                themeElement.setType(ThemeElementType.valueOf(elem.getString("type")));
                themeElement.setDefaultValue(elem.getString("value"));
                themeElement.setCustomValue(themeElement.getDefaultValue());
                if (persist) {
                    themeElement = themeDAO.save(themeElement);
                }
                themeElementList.add(themeElement);
            }
            
            theme.setThemeElements(themeElementList);
            
            if (persist) {
                themeDAO.save(theme);
                
                for(ThemeElement te : themeElemMap.values()) {
                    themeDAO.delete(te);
                }
            }
            
            if (persist) {
                // delete all of the old theme pages...
                List<ThemePage> pagesToDelete = themeDAO.getThemePages(theme.getId().intValue());
                for (ThemePage delPage : pagesToDelete) {
                    themeDAO.delete(delPage);
                }
                // check for the existance of the key first or we break old theme config files
                if (config.containsKey(JSON_KEY_THEME_PAGES)) {
                    JSONArray themePageArray = config.getJSONArray(JSON_KEY_THEME_PAGES);
                    for (int i=0; i<themePageArray.size(); ++i) {
                        JSONObject jsonPage = themePageArray.getJSONObject(i);
                        ThemePage page = new ThemePage();
                        page.setKey(jsonPage.getString("key"));
                        page.setTitle(getJsonString(jsonPage, "title"));
                        page.setDescription(getJsonString(jsonPage, "description"));
                        page.setTheme(theme);
                        themeDAO.save(page);
                    }
                }
            }
        } catch(JSONException je) {
            throw new JSONException("Error parsing configuration file. There is a format error in the configuration file.", je);
        } finally {
            configInputStream.close();
        }
        
        return theme;
    }

    /**
     * JSONObject will throw an exception if the key does not exist. This
     * returns gracefully with null
     * 
     * @param obj the json object
     * @param key the key of the property you want to retrieve
     * @return
     */
    private String getJsonString(JSONObject obj, String key) {
        if (obj.containsKey(key)) {
            return obj.getString(key);
        }
        return null;
    }

    /**
     * Creates default theme(s) from included resources.
     * @param portal The containing portal for the theme(s)
     * @param context_path The context path for file processing
     * @param paramMap Parameters for file processing.
     * @throws IOException
     */
    public List<Theme> createDefaultThemes(Portal portal, String context_path) throws IOException {
        URL themeURL = Theme.class.getResource("themes");
        FileSystem themeFileSystem = FileSystemFactory.getFileSystem(themeURL.getFile());
        List<Theme> themes = new ArrayList<Theme>();
        // each directory in the themes resources directory will be a theme
        // set only the first one to active
        for (FileSystem themeFiles : themeFileSystem.listFiles()) {
            // check if the theme already exists and update if so
            Theme theme = themeDAO.getTheme(portal, String.format(Theme.DEFAULT_THEME_NAME, themeFiles.getName()));
            if (theme == null) {
                theme = new Theme();
                theme.setActive(false);
            }
            theme.setPortal(portal);
            theme.setName(String.format(Theme.DEFAULT_THEME_NAME, themeFiles.getName()));
            theme.setDefault(true);
            
            theme = themeDAO.save(theme);
            File processedTargetDir = fileService.getTargetDirectory(theme, Theme.THEME_DIR_PROCESSED, true);
            InputStream configInputStream = themeFiles.getChildInputStream("config.json");
            theme = loadThemeConfig(theme, configInputStream, true);
            
            processFiles(theme, context_path, themeFiles, processedTargetDir);
            theme = themeDAO.save(theme);
            themes.add(theme);
        }
        return themes;
    }
    
    private void copyRawFiles(File srcDir, File destinationDir) throws IOException {
        // copy the files from themeDir to themeDirDefault
        if(destinationDir.exists()) {
            FileUtils.deleteDirectory(destinationDir); 
        }
        if(!destinationDir.exists()) {
            boolean dirCreateSuccess = destinationDir.mkdirs();
            if(!dirCreateSuccess) {
                throw new IOException("Unable to create directory (including parents): "+destinationDir.getAbsolutePath());
            }
        }
        FileUtils.copyDirectory(srcDir, destinationDir);
    }

    /**
     * Updates the config file in the raw directory so changes are persisted in 
     * theme file download.
     * @param theme
     * @param rawTargetDir
     * @throws IOException 
     */
    public void updateRawConfigFile(Theme theme, File rawTargetDir) throws IOException {
        Writer writer = null;
        InputStream configInputStream = null;
        try {
            // get the JSONObject from the config file first
            File configFile = new File(rawTargetDir, THEME_CONFIG_FILENAME);
            configInputStream = new FileInputStream(configFile);
            JSONObject config = (JSONObject) au.com.gaiaresources.bdrs.util.FileUtils.readJsonStream(configInputStream);
            
            // update the object with the new theme elements
            // remove the existing theme elements
            JSONArray themeElemArray = (JSONArray) config.remove("theme_elements");
            themeElemArray = new JSONArray();
            List<ThemeElement> themeElementList = theme.getThemeElements();
            for (ThemeElement elem : themeElementList) {
                JSONObject themeElement = new JSONObject();
                
                themeElement.put("type", elem.getType().toString());
                themeElement.put("key", elem.getKey());
                themeElement.put("value", elem.getCustomValue());
                
                themeElemArray.add(themeElement);
            }
            config.put("theme_elements", themeElemArray);
            
            // serialize the changes to the original file
            FileOutputStream fos = new FileOutputStream(configFile);
            // Update the file in the raw dir
            writer = new OutputStreamWriter(fos);
            writer.write(config.toString());
            fos.flush();
            writer.flush();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch(IOException e) {
                    
                }
            }
            if (configInputStream != null) {
                try {
                    configInputStream.close();
                } catch(IOException e) {
                    
                }
            }
        }
    }

    /**
     * Returns the appropriate directory for the theme, the "processed" directory 
     * if the theme has been changed and saved, or the "default" one if it has not.
     * @param theme
     * @return
     */
    public static String getThemeDirectory(Theme theme) {
        return Theme.THEME_DIR_PROCESSED;
    }

    /**
     * Reverts a file in the theme.
     * @param theme
     * @param themeFileName
     * @throws ZipException
     * @throws IOException
     */
    public void revertThemeFile(Theme theme, String themeFileName) throws ZipException, IOException {
        // check if there is a managed file for the theme
        FileOutputStream fos = null;
        InputStream is = null;
        try {
            ManagedFile managedFile = managedFileDAO.getManagedFile(theme.getThemeFileUUID());
        
            File rawTargetDir = fileService.getTargetDirectory(theme, Theme.THEME_DIR_RAW, false);
            File targetFile = new File(rawTargetDir, themeFileName);
            
            // if there is a managed file for the theme, 
            // Extract from zip file and place it in the raw dir
            if (managedFile != null) {
                FileDataSource fileDataSource = fileService.getFile(managedFile, managedFile.getFilename());
                ZipFile zipFile = new ZipFile(fileDataSource.getFile());
                ZipEntry zipEntry = zipFile.getEntry(themeFileName);
                fos = new FileOutputStream(targetFile);
                
                is = zipFile.getInputStream(zipEntry);
                byte[] buffer = new byte[4096];
                for(int read = is.read(buffer); read > -1; read = is.read(buffer)) {
                    fos.write(buffer, 0, read);
                }
                fos.flush();
            } else if (theme.isDefault()) {
                // otherwise, copy the files from the default included theme
                File themesDir = new File(Theme.class.getResource("themes").getFile());
                String name = theme.getName();
                boolean copiedFiles = false;
                for (File themeDir : themesDir.listFiles()) {
                    if (String.format(Theme.DEFAULT_THEME_NAME, themeDir.getName()).equals(name)) {
                        copyRawFile(themeFileName, themeDir, targetFile);
                        copiedFiles = true;
                    }
                }
                // if no files were copied (i.e. the theme name was changed), 
                // use the first included theme to avoid failing
                if (!copiedFiles) {
                    copyRawFile(themeFileName, themesDir.listFiles()[0], targetFile);
                }
            } else {
                // send back an error indicating that there is no managed file and it is not a default theme
                // therefore we cannot revert the file.
                throw new IOException("Could not revert file: Managed File does not exist and theme is not default.");
            }
        } finally {
            if (fos != null) {
                fos.close();
            }
            if (is != null) {
                is.close();
            }
        }
    }

    private void copyRawFile(String themeFileName, File srcDir, File targetFile) throws IOException {
        // copy the file from srcDir to the targetFile
        if(targetFile.exists()) {
            FileUtils.deleteQuietly(targetFile); 
        }
        File sourceFile = new File(srcDir, themeFileName);
        if (sourceFile.exists()) {
            FileUtils.copyFile(sourceFile, targetFile);
        }
    }

    /**
     * Reverts entire theme.
     * @param theme
     * @param contextPath
     * @param paramMap
     * @return
     * @throws IOException
     */
    public Theme revertTheme(Theme theme, String contextPath, Map<String, Object> paramMap) throws IOException {
     // check if there is a managed file for the theme
        ManagedFile managedFile = managedFileDAO.getManagedFile(theme.getThemeFileUUID());
        File rawTargetDir = fileService.getTargetDirectory(theme, Theme.THEME_DIR_RAW, false);
        
        // if there is a managed file for the theme, 
        // Extract from zip file and place it in the raw dir
        if (theme.isDefault()) {
            log.debug("reverting back to default theme ");
            // otherwise, copy the files from the default included theme
            File themesDir = new File(Theme.class.getResource("themes").getFile());
            String name = theme.getName();
            boolean copiedFiles = false;
            for (File themeDir : themesDir.listFiles()) {
                if (String.format(Theme.DEFAULT_THEME_NAME, themeDir.getName()).equals(name)) {
                    copyRawFiles(themeDir, rawTargetDir);
                    copiedFiles = true;
                }
            }
            // if no files were copied (i.e. the theme name was changed), 
            // use the first included theme to avoid failing
            if (!copiedFiles) {
                copyRawFiles(themesDir.listFiles()[0], rawTargetDir);
            }
            
            InputStream configInputStream = null;
            try {
                configInputStream = new FileInputStream(new File(rawTargetDir, ThemeService.THEME_CONFIG_FILENAME));
                theme = loadThemeConfig(theme, configInputStream, true);
            } finally {
                if (configInputStream != null) {
                    configInputStream.close();
                }
            }
            
            
        } else {
            log.debug("reverting back to managed file");
            theme = revertToZip(theme, managedFile, rawTargetDir, true);
        }
        themeDAO.save(theme);
        return theme;
    }

    public File getThemeRawDirectory(Theme theme) throws IOException {
        // default themes won't have a raw directory
        if (theme.isDefault()) {
            File themesDir = new File(Theme.class.getResource("themes").getFile());
            String name = theme.getName();
            for (File themeDir : themesDir.listFiles()) {
                if (String.format(Theme.DEFAULT_THEME_NAME, themeDir.getName()).equals(name)) {
                    return new File(themeDir.getPath());
                }
            }
        }
        // TODO: add protection for unfound default
        // else create the raw directory?
        return fileService.getTargetDirectory(theme, Theme.THEME_DIR_RAW, true);
    }

    
}
