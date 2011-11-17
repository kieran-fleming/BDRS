package au.com.gaiaresources.bdrs.controller.theme;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.file.AbstractDownloadFileController;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.PortalDAO;
import au.com.gaiaresources.bdrs.model.theme.Theme;
import au.com.gaiaresources.bdrs.model.theme.ThemeDAO;
import au.com.gaiaresources.bdrs.model.theme.ThemeElement;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.theme.ThemeService;
import au.com.gaiaresources.bdrs.servlet.Interceptor;
import au.com.gaiaresources.bdrs.servlet.RequestContext;

/**
 * Allows the creation, update and deletion of Themes and ThemeElements. 
 */
@SuppressWarnings("unchecked")
@Controller
public class ThemeController extends AbstractDownloadFileController {

    public static final String THEME_ELEMENT_CUSTOM_VALUE_TEMPLATE = "theme_element_%d_customValue";
    public static final String CSS_CONTENT_TYPE = "text/css";
    public static final String CSS_EXTENSION = "css";
    
    public static final String ROOT_EDIT_URL = "/bdrs/root/theme/edit.htm";
    public static final String ADMIN_EDIT_URL = "/bdrs/admin/theme/edit.htm";
    public static final String ADMIN_EDIT_FILE_URL = "/bdrs/admin/theme/editThemeFile.htm";
    
    public static final String REVERT_DEFAULT_THEME_URL = "/bdrs/theme/revertDefault.htm";
    
    private Logger log = Logger.getLogger(getClass());
    
    @Autowired
    private ThemeDAO themeDAO;
    @Autowired
    private PortalDAO portalDAO;
    @Autowired
    private ManagedFileDAO managedFileDAO;
    @Autowired
    private FileService fileService;
    @Autowired
    private ThemeService themeService;
    
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
        mv.addObject("editAsRoot", true);
        return mv;
    }
    
    /**
     * Lists all themes for the current portal.
     * @param portalId the primary key of the portal associated with the themes to be returned.
     */
    @RolesAllowed({ Role.ADMIN })
    @RequestMapping(value = "/bdrs/admin/theme/listing.htm", method = RequestMethod.GET)
    public ModelAndView listing(HttpServletRequest request,
                                HttpServletResponse response) {
        Portal portal = getRequestContext().getPortal();
        ModelAndView mv = new ModelAndView("themeListing");
        mv.addObject("themeList", themeDAO.getThemes(portal));
        mv.addObject("portalId", portal.getId());
        mv.addObject("editAsAdmin", true);
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
    public ModelAndView root_editThemeFile(HttpServletRequest request,
                             HttpServletResponse response,
                             @RequestParam(value="themeId", required=true) int themeId,
                             @RequestParam(value="themeFileName", required=true) String themeFileName) throws IOException {
        
        Theme theme = themeDAO.getTheme(themeId);
        ModelAndView mv = editThemeFile(request, response, theme, themeFileName);
        mv.addObject("editAsRoot", true);
        return mv;
    }
    
    @RolesAllowed({ Role.ADMIN })
    @RequestMapping(value = ADMIN_EDIT_FILE_URL, method = RequestMethod.GET)
    public ModelAndView admin_editThemeFile(HttpServletRequest request,
                             HttpServletResponse response,
                             @RequestParam(value="themeId", required=true) int themeId,
                             @RequestParam(value="themeFileName", required=true) String themeFileName) throws IOException {
        Portal portal = getRequestContext().getPortal();
        Theme theme = themeDAO.getTheme(themeId);
        if (theme == null) {
            // return an error that you are trying to edit a theme that does not exist or you do not have permission to edit
            getRequestContext().addMessage("bdrs.theme.nonexistant", new String[]{});
            ModelAndView mv = new ModelAndView("themeListing");
            mv.addObject("themeList", themeDAO.getThemes(portal));
            mv.addObject("portalId", portal.getId());
            mv.addObject("editAsAdmin", true);
            return mv;
        }
        
        if (!theme.getPortal().equals(portal)) {
            // return an error that you are trying to edit a theme you don't have access to
            getRequestContext().addMessage("bdrs.theme.accessDenied", new String[]{theme.getName()});
            ModelAndView mv = new ModelAndView("themeListing");
            mv.addObject("themeList", themeDAO.getThemes(portal));
            mv.addObject("portalId", portal.getId());
            mv.addObject("editAsAdmin", true);
            return mv;
        }
        ModelAndView mv = editThemeFile(request, response, theme, themeFileName);
        mv.addObject("editAsAdmin", true);
        return mv;
    }
    
    @RolesAllowed({ Role.ADMIN, Role.ROOT })
    @RequestMapping(value = "/bdrs/admin/theme/refreshTheme.htm", method = RequestMethod.GET)
    public ModelAndView refreshThemes(HttpServletRequest request,
                             HttpServletResponse response,
                             @RequestParam(value="themeId", required=true) int themeId) throws IOException {
        Portal portal = getRequestContext().getPortal();
        
        Theme activeTheme = themeDAO.getTheme(themeId);
        if (activeTheme == null) {
            // return an error that you are trying to edit a theme that does not exist or you do not have permission to edit
            getRequestContext().addMessage("bdrs.theme.nonexistant", new String[]{});
            ModelAndView mv = new ModelAndView("themeListing");
            mv.addObject("themeList", themeDAO.getThemes(portal));
            mv.addObject("portalId", portal.getId());
            mv.addObject("editAsRoot", true);
            return mv;
        }
        
        portal = activeTheme.getPortal();
        for (Theme theme : themeDAO.getThemes(portal)) {
            theme.setActive(theme.getId() == themeId);
            themeDAO.save(theme);
            if (theme.getId() == themeId) {
                // add the active theme to the request context
                getRequestContext().setTheme(theme);
            }
        }
        ModelAndView mv = new ModelAndView("themeListing");
        mv.addObject("themeList", themeDAO.getThemes(portal));
        mv.addObject("portalId", portal.getId());
        mv.addObject("editAsAdmin", true);
        return mv;
    }
    
    private ModelAndView editThemeFile(HttpServletRequest request,
            HttpServletResponse response,
            Theme theme,
            String themeFileName) throws IOException {
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
    public ModelAndView root_editThemeFileSubmit(HttpServletRequest request,
                             HttpServletResponse response,
                             @RequestParam(value="themePk", required=true) int themeId,
                             @RequestParam(value="themeFileName", required=true) String themeFileName,
                             @RequestParam(value="themeFileContent", required=true) String themeFileContent,
                             @RequestParam(value="revert", required=false) String revert) {
        
        Theme theme = themeDAO.getTheme(themeId);
        Portal portal = getRequestContext().getPortal();
        if (theme == null) {
            // return an error that you are trying to edit a theme that does not exist or you do not have permission to edit
            getRequestContext().addMessage("bdrs.theme.nonexistant", new String[]{});
            ModelAndView mv = new ModelAndView("themeListing");
            mv.addObject("themeList", themeDAO.getThemes(portal));
            mv.addObject("portalId", portal.getId());
            mv.addObject("editAsRoot", true);
            return mv;
        }
        return editThemeFileSubmit(request, response, theme, themeFileName, themeFileContent, revert, ROOT_EDIT_URL);
    }
    
    @RolesAllowed({ Role.ADMIN })
    @RequestMapping(value = ADMIN_EDIT_FILE_URL, method = RequestMethod.POST)
    public ModelAndView admin_editThemeFileSubmit(HttpServletRequest request,
                             HttpServletResponse response,
                             @RequestParam(value="themePk", required=true) int themeId,
                             @RequestParam(value="themeFileName", required=true) String themeFileName,
                             @RequestParam(value="themeFileContent", required=true) String themeFileContent,
                             @RequestParam(value="revert", required=false) String revert) {
        Theme theme = themeDAO.getTheme(themeId);
        Portal portal = getRequestContext().getPortal();
        if (theme == null) {
            // return an error that you are trying to edit a theme that does not exist or you do not have permission to edit
            getRequestContext().addMessage("bdrs.theme.nonexistant", new String[]{});
            ModelAndView mv = new ModelAndView("themeListing");
            mv.addObject("themeList", themeDAO.getThemes(portal));
            mv.addObject("portalId", portal.getId());
            mv.addObject("editAsAdmin", true);
            return mv;
        }
        
        if (!theme.getPortal().equals(portal)) {
            // return an error that you are trying to edit a theme you don't have access to
            getRequestContext().addMessage("bdrs.theme.accessDenied", new String[]{theme.getName()});
            ModelAndView mv = new ModelAndView("themeListing");
            mv.addObject("themeList", themeDAO.getThemes(portal));
            mv.addObject("portalId", portal.getId());
            mv.addObject("editAsAdmin", true);
            return mv;
        }
        ModelAndView mv = editThemeFileSubmit(request, response, theme, themeFileName, themeFileContent, revert, ADMIN_EDIT_URL);
        mv.addObject("editAsAdmin", true);
        return mv;
    }
    
    private ModelAndView editThemeFileSubmit(HttpServletRequest request,
            HttpServletResponse response,
            Theme theme,
            String themeFileName,
            String themeFileContent,
            String revert,
            String redirectUrl) {
        
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
                themeService.revertThemeFile(theme, themeFileName);
            }
            
            fos.flush();
            
            // Process the raw file (perform the necessary search and replace)
            File processedTargetDir = fileService.getTargetDirectory(theme, Theme.THEME_DIR_PROCESSED, false);
            String assetContext = String.format(Theme.ASSET_DOWNLOAD_URL_TMPL, request.getContextPath(), 
                                                theme.getClass().getName(), theme.getId(), Theme.THEME_DIR_PROCESSED);
            themeService.processThemeData(theme, rawTargetDir, processedTargetDir, assetContext);
            getRequestContext().addMessage("bdrs.theme.save.success", new Object[]{theme.getName()});
        
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            
            getRequestContext().addMessage(e.getMessage());
            getRequestContext().addMessage("bdrs.theme.update.fail", new Object[]{theme.getName()});
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
        
        ModelAndView mv = new ModelAndView(new RedirectView(redirectUrl, true));
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
    @RequestMapping(value = ROOT_EDIT_URL, method = RequestMethod.GET)
    public ModelAndView root_edit(HttpServletRequest request,
                             HttpServletResponse response,
                             @RequestParam(value="portalId", required=false, defaultValue="0") int portalId,
                             @RequestParam(value="themeId", required=false, defaultValue="0") int themeId) throws IOException {
        Portal portal = portalId == 0 ? getRequestContext().getPortal() : portalDAO.getPortal(portalId);
        Theme theme = themeId == 0 ? new Theme() : themeDAO.getTheme(themeId);
        if(theme.getPortal() == null) {
            theme.setPortal(portal);
        }
        ModelAndView mv = edit(request, response, portal, theme);
        mv.addObject("editAsRoot", true);
        return mv;
    }
    
    // The admin can only edit the themes for the current portal
    @RolesAllowed({ Role.ADMIN })
    @RequestMapping(value = ADMIN_EDIT_URL, method = RequestMethod.GET)
    public ModelAndView admin_edit(HttpServletRequest request,
                             HttpServletResponse response,
                             @RequestParam(value="themeId", required=false, defaultValue="0") int themeId) throws Exception {
        Portal portal = getRequestContext().getPortal();
        Theme theme = themeId == 0 ? new Theme() : themeDAO.getTheme(themeId);
        if(theme.getPortal() == null) {
            theme.setPortal(portal);
        }
        ModelAndView mv =  edit(request, response, portal, theme);
        mv.addObject("editAsAdmin", true);
        return mv;
    }
    
    private ModelAndView edit(HttpServletRequest request,
            HttpServletResponse response,
            Portal portal,
            Theme theme) throws IOException {

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
        mv.addObject("portalId", portal.getId().intValue());
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
    @RequestMapping(value = ROOT_EDIT_URL, method = RequestMethod.POST)
    public ModelAndView root_editSubmit(HttpServletRequest request,
                                   HttpServletResponse response,
                                   @RequestParam(value="portalPk", required=true) int portalId,
                                   @RequestParam(value="themePk", required=false, defaultValue="0") int themeId,
                                   @RequestParam(value="name", required=true) String name,
                                   @RequestParam(value="themeFileUUID", required=true) String themeFileUUID,
                                   @RequestParam(value="active", required=false, defaultValue="false") boolean active,
                                   @RequestParam(value="revert", required=false) String revert){

        Portal portal = portalDAO.getPortal(portalId);
        Theme theme = themeId == 0 ? new Theme() : themeDAO.getTheme(themeId);
        return editSubmit(request, response, portal, theme, name, themeFileUUID, active, revert, ROOT_EDIT_URL);
    }
    
    @RolesAllowed({ Role.ADMIN })
    @RequestMapping(value = ADMIN_EDIT_URL, method = RequestMethod.POST)
    public ModelAndView admin_editSubmit(HttpServletRequest request,
                                   HttpServletResponse response,
                                   @RequestParam(value="themePk", required=false, defaultValue="0") int themeId,
                                   @RequestParam(value="name", required=true) String name,
                                   @RequestParam(value="themeFileUUID", required=true) String themeFileUUID,
                                   @RequestParam(value="active", required=false, defaultValue="false") boolean active,
                                   @RequestParam(value="revert", required=false) String revert){
        Portal portal = getRequestContext().getPortal();
        Theme theme = themeId == 0 ? new Theme() : themeDAO.getTheme(themeId);
        return editSubmit(request, response, portal, theme, name, themeFileUUID, active, revert, ADMIN_EDIT_URL);
    }
    
    private ModelAndView editSubmit(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Portal portal,
                                   Theme theme,
                                   String name,
                                   String themeFileUUID,
                                   boolean active,
                                   String revert,
                                   String redirectUrl) {
               
        if(active) {
            // Ensure that only a single theme is active at a time.
            for(Theme t : themeDAO.getThemes(portal)) {
                t.setActive(false);
                themeDAO.save(t);
            }
        } else {
            // we are deactivating the current theme, must revert back to the default
            Theme defaultTheme = themeDAO.getDefaultTheme(portal);
            defaultTheme.setActive(true);
            themeDAO.save(defaultTheme);
        }
        
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
            String value = request.getParameter(inputName);
            if (value != null) {
                te.setCustomValue(value);
                themeDAO.save(te);
            } else {
                log.error("Cannot set custom value to null");
            }
        }
        
        try {
            File rawTargetDir = fileService.getTargetDirectory(theme, Theme.THEME_DIR_RAW, true);
            File processedTargetDir = fileService.getTargetDirectory(theme, Theme.THEME_DIR_PROCESSED, true);
            
            // If adding or reverting a theme.
            if(revertRequired) {
                ManagedFile managedFile = managedFileDAO.getManagedFile(themeFileUUID, portal);
                if(managedFile != null) {
                    themeService.revertTheme(theme, request.getContextPath(), request.getParameterMap());
                } else {
                    getRequestContext().addMessage("bdrs.managedFile.missing", new Object[]{themeFileUUID});
                    getRequestContext().addMessage("bdrs.theme.revert.fail", new Object[]{theme.getName()});
                }
            } else {
                // update the raw config file with any changes
                themeService.updateRawConfigFile(theme, rawTargetDir);
            }
            // copy and process files
            themeService.processFiles(theme, request.getContextPath(), rawTargetDir, processedTargetDir);
            if (!getRequestContext().getMessageCodes().contains("bdrs.managedFile.missing")) {
                getRequestContext().addMessage("bdrs.theme.save.success", new Object[]{theme.getName()});
            }
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            
            RequestContext requestContext = getRequestContext();
            requestContext.addMessage(Interceptor.REQUEST_ROLLBACK);

            getRequestContext().addMessage("bdrs.managedFile.missing", new Object[]{themeFileUUID});
            getRequestContext().addMessage("bdrs.theme.update.fail", new Object[]{theme.getName(), e.getMessage()});
        }

        ModelAndView mv = new ModelAndView(new RedirectView(redirectUrl, true));
        mv.addObject("portalId", portal.getId().intValue());
        mv.addObject("themeId", theme.getId());
        
        return mv;
    }
    
    /**
     * Downloads a theme zip file by zipping the contents of the theme raw 
     * directory.
     * @param themeId The unique id of the theme.
     * @return
     */
    @RolesAllowed({ Role.ADMIN })
    @RequestMapping(value = "/bdrs/admin/theme/downloadTheme.htm", method = RequestMethod.GET)
    public ModelAndView download(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("themeId") Integer themeId) {
        try {
            Theme theme = themeDAO.getTheme(themeId);
            File rawTargetDir = themeService.getThemeRawDirectory(theme);
            downloadFilesAsZip(rawTargetDir.listFiles(), response, theme.getName());
        } catch (IOException e) {
            log.error("Error downloading theme file: ", e);
            getRequestContext().addMessage("bdrs.theme.download.fail", new Object[]{});
        } 
        return null;
    }
    
    /**
     * Dev helper mapping to make editing the default theme easier.
     * @throws IOException 
     */
    // public so no login required
    @RequestMapping(value = REVERT_DEFAULT_THEME_URL, method = RequestMethod.GET)
    public void revertDefaultTheme(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String contextPath = request.getContextPath();
        Portal portal = getRequestContext().getPortal();
        // if no portal in the request context, use the default portal.
        if (portal == null) {
            portal = portalDAO.getPortal(true);
			if (portal == null) {
	            response.getWriter().write("Error reverting default theme : no portal specified and no default portal found!");
	            return;
	        }
        }
        
        try {
            themeService.createDefaultThemes(portal, contextPath);
            
            response.getWriter().write("Successfully reverted default theme of portal.");
            response.getWriter().write("\n");
            response.getWriter().write("portal id : ");
            response.getWriter().write(portal.getId().toString());
            response.getWriter().write("\n");
            response.getWriter().write("portal name : ");
            response.getWriter().write(portal.getName());
            response.getWriter().write("\n\n");
        } catch (IOException ioe) {
            response.getWriter().write("IOException reverting default theme : \n");
            response.getWriter().write(ioe.getMessage());
            response.getWriter().write("\n");
            ioe.printStackTrace(new PrintStream(response.getOutputStream()));
        }
    }
}
