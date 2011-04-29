package au.com.gaiaresources.bdrs.controller.theme;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.PortalDAO;
import au.com.gaiaresources.bdrs.model.theme.Theme;
import au.com.gaiaresources.bdrs.model.theme.ThemeDAO;
import au.com.gaiaresources.bdrs.model.theme.ThemeElement;
import au.com.gaiaresources.bdrs.model.theme.ThemeElementType;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.servlet.RequestContext;
import au.com.gaiaresources.bdrs.util.ZipUtils;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Allows the creation, update and deletion of Themes and ThemeElements. 
 */
@SuppressWarnings("unchecked")
@Controller
public class ThemeController extends AbstractController {
    /**
     * The name of the configuration file that is expected inside all theme zips.
     */
    public static final String THEME_CONFIG_FILENAME = "config.json";
    
    public static final String THEME_ELEMENT_CUSTOM_VALUE_TEMPLATE = "theme_element_%d_customValue";
    public static final String KEY_REPLACE_REGEX_TEMPLATE = "\\$\\{(\\s*)%s(\\s*)\\}";
    public static final String CSS_CONTENT_TYPE = "text/css";
    public static final String CSS_EXTENSION = "css";
    
    /**
     * The set of content types that should be passed through the search and 
     * replace process but do not start with the word 'text'.
     */
    public static final Set<String> PATTERN_REPLACE_CONTENT_TYPES;
    static {
        Set<String> tempSet = new HashSet<String>();
        tempSet.add("application/json");
        tempSet.add("application/javascript");
        
        PATTERN_REPLACE_CONTENT_TYPES = Collections.unmodifiableSet(tempSet);
    }
    
    private Logger log = Logger.getLogger(getClass());
    
    @Autowired
    private ThemeDAO themeDAO;
    @Autowired
    private PortalDAO portalDAO;
    @Autowired
    private ManagedFileDAO managedFileDAO;
    @Autowired
    private FileService fileService;

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
     * Lists all themes for the portal specified the the portal id.
     * @param portalId the primary key of the portal associated with the themes to be returned.
     */
    @RolesAllowed({ Role.ROOT })
    @RequestMapping(value = "/bdrs/root/theme/listing.htm", method = RequestMethod.GET)
    public ModelAndView listing(HttpServletRequest request,
                                HttpServletResponse response,
                                @RequestParam(value="portalId", required=true) int portalId) {
        Portal portal = portalDAO.getPortal(portalId);
        ModelAndView mv = new ModelAndView("themeListing");
        mv.addObject("themeList", themeDAO.getThemes(portal));
        mv.addObject("portalId", portalId);
        
        return mv;
    }
    
    /**
     * Presents an editing view of a text based theme file.  
     * @param themeId the primary key of the theme that owns the file to be edited.
     * @param themeFileName the name of the file to be edited. The name is 
     * the relative path of the file from the base storage location for the 
     * specified theme.
     * @throws IOException 
     */
    @RolesAllowed({ Role.ROOT })
    @RequestMapping(value = "/bdrs/root/theme/editThemeFile.htm", method = RequestMethod.GET)
    public ModelAndView editThemeFile(HttpServletRequest request,
                             HttpServletResponse response,
                             @RequestParam(value="themeId", required=true) int themeId,
                             @RequestParam(value="themeFileName", required=true) String themeFileName) throws IOException {
        
        Theme theme = themeDAO.getTheme(themeId);
        File rawTargetDir = fileService.getTargetDirectory(theme, Theme.THEME_DIR_RAW, false);
        File template = new File(rawTargetDir, themeFileName);
        // Floor the value if required.
        StringBuilder contentBuilder = new StringBuilder((int)template.length());
        try {
            if(template.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(template));
                for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                    contentBuilder.append(line);
                    contentBuilder.append("\n");
                }
                reader.close();
            }
        } catch(IOException ioe){
            log.error(ioe.getMessage(), ioe);
        }
        
        ModelAndView mv = new ModelAndView("themeFileEdit");
        mv.addObject("editTheme", theme);
        mv.addObject("themeFileName", themeFileName);
        mv.addObject("content", contentBuilder.toString());
        
        return mv;
    }
    
    /**
     * Saves the content of an edited file or reverts the file to its original state. 
     * @param themeId the primary key of the theme that owns the file to be edited.
     * @param themeFileName the name of the file to be edited. The name is 
     * the relative path of the file from the base storage location for the 
     * specified theme.
     * @param themeFileContent the new content to be saved to the file.
     * @param revert non-null if the file should be reverted to its original 
     * uploaded state.
     */
    @RolesAllowed({ Role.ROOT })
    @RequestMapping(value = "/bdrs/root/theme/editThemeFile.htm", method = RequestMethod.POST)
    public ModelAndView editThemeFileSubmit(HttpServletRequest request,
                             HttpServletResponse response,
                             @RequestParam(value="themePk", required=true) int themeId,
                             @RequestParam(value="themeFileName", required=true) String themeFileName,
                             @RequestParam(value="themeFileContent", required=true) String themeFileContent,
                             @RequestParam(value="revert", required=false) String revert) {
        
        Theme theme = themeDAO.getTheme(themeId);

        FileOutputStream fos = null;
        InputStream is = null;
        try {
            File rawTargetDir = fileService.getTargetDirectory(theme, Theme.THEME_DIR_RAW, false);
            File targetFile = new File(rawTargetDir, themeFileName);
            
            fos = new FileOutputStream(targetFile);
            if(revert == null) {
                // Update the file in the raw dir
                Writer writer = new OutputStreamWriter(fos);
                writer.write(themeFileContent);
                writer.flush();
                writer.close();
            } else {
                // Extract from zip file and place it in the raw dir
                ManagedFile managedFile = managedFileDAO.getManagedFile(theme.getThemeFileUUID());
                FileDataSource fileDataSource = fileService.getFile(managedFile, managedFile.getFilename());
                ZipFile zipFile = new ZipFile(fileDataSource.getFile());
                ZipEntry zipEntry = zipFile.getEntry(themeFileName);
                
                is = zipFile.getInputStream(zipEntry);
                byte[] buffer = new byte[4096];
                for(int read = is.read(buffer); read > -1; read = is.read(buffer)) {
                    fos.write(buffer, 0, read);
                }
            }
            
            fos.flush();
            
            // Process the raw file (perform the necessary search and replace)
            File processedTargetDir = fileService.getTargetDirectory(theme, Theme.THEME_DIR_PROCESSED, false);
            String assetContext = String.format(Theme.ASSET_DOWNLOAD_URL_TMPL, request.getContextPath(), 
                                                theme.getClass().getName(), theme.getId(), Theme.THEME_DIR_PROCESSED); 
            processThemeData(theme, rawTargetDir, processedTargetDir, assetContext, processedTargetDir);
            getRequestContext().addMessage("bdrs.theme.save.success", new Object[]{theme.getName()});
        
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            
            getRequestContext().addMessage(e.getMessage());
            getRequestContext().addMessage(theme.getName() + " has not been updated.");
        } finally {
            try {
                if(fos != null) {
                    fos.close();
                }
                if(is != null) {
                    is.close();
                }
            } catch(IOException ioe) {
                log.error(ioe.getMessage(), ioe);
            }
        }
        
        ModelAndView mv = new ModelAndView(new RedirectView("/bdrs/root/theme/edit.htm", true));
        mv.addObject("portalId", theme.getPortal().getId());
        mv.addObject("themeId", theme.getId());
        
        return mv;
    }
    
    /**
     * Presents a view where a theme may be edited or updated.
     * @param portalId the primary key of the portal to be associated with the
     * added or edited theme.
     * @param themeId if specified, this is the primary key of the theme to be edited.
     * @throws IOException 
     */
    @RolesAllowed({ Role.ROOT })
    @RequestMapping(value = "/bdrs/root/theme/edit.htm", method = RequestMethod.GET)
    public ModelAndView edit(HttpServletRequest request,
                             HttpServletResponse response,
                             @RequestParam(value="portalId", required=true) int portalId,
                             @RequestParam(value="themeId", required=false, defaultValue="0") int themeId) throws IOException {
        Portal portal = portalDAO.getPortal(portalId);
        Theme theme = themeId == 0 ? new Theme() : themeDAO.getTheme(themeId);
        if(theme.getPortal() == null) {
            theme.setPortal(portal);
        }
        
        List<ThemeFile> themeFiles = new ArrayList<ThemeFile>();
        if(theme.getId() != null) {
            File rawTargetDir = fileService.getTargetDirectory(theme, Theme.THEME_DIR_RAW, true);
            if(rawTargetDir.exists()) {
                ThemeFile[] themeFileArray = new ThemeFile(theme, rawTargetDir, 
                                                           rawTargetDir, request.getContextPath()).listFiles();
                if(themeFileArray != null) {
                    for(ThemeFile child : themeFileArray) {
                        recurseThemeFileList(themeFiles, child);
                    }
                }
            }
        }
        
        ModelAndView mv = new ModelAndView("themeEdit");
        mv.addObject("editTheme", theme);
        mv.addObject("portalId", portalId);
        mv.addObject("themeFileList", themeFiles);
        
        return mv;
    }
    
    private void recurseThemeFileList(List<ThemeFile> themeFiles, ThemeFile themeFile) {
        themeFiles.add(themeFile);
        ThemeFile[] themeFileArray = themeFile.listFiles();
        if(themeFileArray != null) {
            for(ThemeFile child : themeFileArray) {
                recurseThemeFileList(themeFiles, child);
            }
        }
    }

    /**
     * Saves an added or edited theme.
     * 
     * @param portalId
     *            the primary key of the portal to be associated with the added
     *            or edited theme.
     * @param themeId
     *            if specified, this is the primary key of the theme to be
     *            edited.
     * @param name
     *            the human readable name of this theme.
     * @param themeFileUUID
     *            the UUID of the managed file containing the source data
     *            (images, styles, templates) for this theme.
     * @param active
     *            true if this theme should be activated for this portal, false
     *            otherwise.
     * @param revert
     *            non-null if all existing theme data should be reset to the
     *            state specified by the original theme given by the
     *            themeFileUUID.
     */
    @RolesAllowed({ Role.ROOT })
    @RequestMapping(value = "/bdrs/root/theme/edit.htm", method = RequestMethod.POST)
    public ModelAndView editSubmit(HttpServletRequest request,
                                   HttpServletResponse response,
                                   @RequestParam(value="portalPk", required=true) int portalId,
                                   @RequestParam(value="themePk", required=false, defaultValue="0") int themeId,
                                   @RequestParam(value="name", required=true) String name,
                                   @RequestParam(value="themeFileUUID", required=true) String themeFileUUID,
                                   @RequestParam(value="active", required=false, defaultValue="false") boolean active,
                                   @RequestParam(value="revert", required=false) String revert){

        Portal portal = portalDAO.getPortal(portalId);
        
        if(active) {
            // Ensure that only a single theme is active at a time.
            for(Theme t : themeDAO.getThemes(portal)) {
                t.setActive(false);
                themeDAO.save(t);
            }
        }
        
        Theme theme = themeId == 0 ? new Theme() : themeDAO.getTheme(themeId);
        boolean revertRequired = revert != null || (!themeFileUUID.equals(theme.getThemeFileUUID()));

        // Save the theme
        if(theme.getPortal() == null) {
            theme.setPortal(portal);
        }
        theme.setName(name);
        theme.setThemeFileUUID(themeFileUUID);
        theme.setActive(active);
        theme = themeDAO.save(theme);
        
        String inputName;
        for(ThemeElement te : theme.getThemeElements()) {
            inputName = String.format(THEME_ELEMENT_CUSTOM_VALUE_TEMPLATE, te.getId());
            te.setCustomValue(request.getParameter(inputName));
            themeDAO.save(te);
        }
        
        try {
            // Get the theme file and create or update theme elements
            ManagedFile managedFile = managedFileDAO.getManagedFile(themeFileUUID);
            if(managedFile == null) {
                throw new NullPointerException("Cannot find managed file with UUID: "+themeFileUUID);
            }

            File rawTargetDir = fileService.getTargetDirectory(theme, Theme.THEME_DIR_RAW, true);
            
            // If adding or reverting a theme.
            if(revertRequired) {
                FileDataSource fileDataSource = fileService.getFile(managedFile, managedFile.getFilename());
                ZipFile zipFile = new ZipFile(fileDataSource.getFile());
                loadThemeConfig(theme, zipFile);
                
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
            
            File processedTargetDir = fileService.getTargetDirectory(theme, Theme.THEME_DIR_PROCESSED, true);
            
            // If the processed file directory exists, delete it first before
            // we process the raw files.
            if(processedTargetDir.exists()) {
                FileUtils.deleteDirectory(processedTargetDir); 
            }
            
            String assetContext = String.format(Theme.ASSET_DOWNLOAD_URL_TMPL, request.getContextPath(), 
                                                theme.getClass().getName(), theme.getId(), Theme.THEME_DIR_PROCESSED); 
            processThemeData(theme, rawTargetDir, processedTargetDir, assetContext, processedTargetDir);
            getRequestContext().addMessage("bdrs.theme.save.success", new Object[]{theme.getName()});
            
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            
            RequestContext requestContext = getRequestContext();
            requestContext.getHibernate().getTransaction().rollback();
            
            getRequestContext().addMessage(e.getMessage());
            getRequestContext().addMessage(theme.getName() + " has not been updated.");
        }
        
        ModelAndView mv = new ModelAndView(new RedirectView("/bdrs/root/theme/edit.htm", true));
        mv.addObject("portalId", portalId);
        mv.addObject("themeId", theme.getId());
        
        return mv;
    }
    
    private void processThemeData(Theme theme, File srcDir, File destDir, String assetContext, File baseTargetDir) throws IOException {
        
        byte[] buffer = new byte[4096];
        FileInputStream fis;
        FileOutputStream fos;
        File target;
        
        for(File f : srcDir.listFiles()) {
            String absPath = f.getAbsolutePath();
            String relativePath = absPath.substring(srcDir.getAbsolutePath().length(), absPath.length());
            target = new File(destDir.getAbsolutePath() + relativePath);
            
            if(f.isDirectory()) {
                if(!target.exists()) {
                    boolean dirCreateSuccess = target.mkdirs();
                    if(!dirCreateSuccess) {
                        throw new IOException("Unable to create directory (including parents): "+target.getAbsolutePath());
                    }
                }
                log.debug("Make Dir: "+target.getAbsolutePath());
                processThemeData(theme, f, target, assetContext, baseTargetDir);
            } else {
                
                File parentFile = target.getParentFile();
                if(!parentFile.exists()) {
                    boolean dirCreateSuccess = parentFile.mkdirs();
                    if(!dirCreateSuccess) {
                        throw new IOException("Unable to create directory path: "+parentFile.getAbsolutePath());
                    }
                }
                
                fis = new FileInputStream(f);
                fos = new FileOutputStream(target);
                try {
                    // Make sure that the file will have a directory to go into.
                    if(!target.getParentFile().exists()) {
                        boolean dirCreateSuccess = target.getParentFile().mkdirs();
                        if(!dirCreateSuccess) {
                            throw new IOException("Unable to create directory path: "+target.getParentFile().getAbsolutePath());
                        }
                    }
                    String contentType = au.com.gaiaresources.bdrs.util.FileUtils.getContentType(f);
                    
                    if(ThemeController.isTextContent(contentType)) {
                        // Read the file into memory
                        log.debug("Process: "+target.getAbsolutePath());
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
                        
                        // Special handling for CSS files to account for background images.
                        // Checking for CSS file types in two different ways here
                        // because depending on the OS/JVM the content type is
                        // sometimes reported as text/css and sometimes as text/plain.
                        if(CSS_CONTENT_TYPE.equals(contentType) || 
                                CSS_EXTENSION.equals(FileUtils.getExtension(target.getAbsolutePath()).toLowerCase())) {
                            // Now for the assets
                            // Cut off an extra path separator (slash)
                            String relativePathFromBaseTargetDir = target.getAbsolutePath().substring(baseTargetDir.getAbsolutePath().length()+1);
                            relativePathFromBaseTargetDir = FileUtils.dirname(relativePathFromBaseTargetDir);
                            
                            regex = String.format(KEY_REPLACE_REGEX_TEMPLATE, Pattern.quote(Theme.ASSET_KEY));
                            content = content.replaceAll(regex, Matcher.quoteReplacement(assetContext + relativePathFromBaseTargetDir + File.separatorChar));
                        }
                        
                        // Write the file to disk
                        Writer writer = new OutputStreamWriter(fos);
                        writer.write(content);
                        writer.flush();
                        writer.close();
                        
                    } else {
                        log.debug("File Copy: "+target.getAbsolutePath());
                        fis = new FileInputStream(f);
                        for(int read = fis.read(buffer); read > -1; read = fis.read(buffer)) {
                            fos.write(buffer, 0, read);
                        }
                    }
                    fos.flush();
                    
                } catch(IOException ioe) {
                    log.error(ioe.getMessage(), ioe);
                    throw ioe;
                } finally {
                    try{
                        fis.close();
                        fos.close();
                    } catch(IOException e) {
                        log.error(e.getMessage(), e);
                        throw e;
                    }
                }
            }
        }
    }
    
    private void loadThemeConfig(Theme theme, ZipFile themeZip) throws ZipException, IOException {
        
        ZipEntry configEntry = themeZip.getEntry(THEME_CONFIG_FILENAME);
        InputStream configInputStream = themeZip.getInputStream(configEntry);
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(configInputStream));
        StringBuilder configJsonStr = new StringBuilder();
        for(String line = reader.readLine(); line != null; line = reader.readLine()) {
            configJsonStr.append(line);
        }
        reader.close();
        
        // What about malformatted json or valid json that is not what we want?
        JSONObject config;
        try {
             config = (JSONObject)JSONSerializer.toJSON(configJsonStr.toString());
        } catch(JSONException je) {
            throw new JSONException("Error parsing configuration file. There is a syntax error in the configuration file.", je);
        }
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
                
                themeElement = themeDAO.save(themeElement);
                themeElementList.add(themeElement);
            }
            
            theme.setThemeElements(themeElementList);
            themeDAO.save(theme);
            
            for(ThemeElement te : themeElemMap.values()) {
                themeDAO.delete(te);
            }
        } catch(JSONException je) {
            throw new JSONException("Error parsing configuration file. There is a format error in the configuration file.", je);
        }
    }
}
