package au.com.gaiaresources.bdrs.controller.showcase;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.file.AbstractDownloadFileController;
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataBuilder;
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataHelper;
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataRow;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
import au.com.gaiaresources.bdrs.model.showcase.Gallery;
import au.com.gaiaresources.bdrs.model.showcase.GalleryDAO;
import au.com.gaiaresources.bdrs.service.image.ImageService;
import au.com.gaiaresources.bdrs.service.managedFile.ManagedFileService;
import au.com.gaiaresources.bdrs.util.FileUtils;

@Controller
public class GalleryController extends AbstractDownloadFileController {
    
    private static final String BASE_URL = "/bdrs/admin/gallery/";
    public static final String LIST_URL = BASE_URL + "listing.htm";
    public static final String EDIT_URL = BASE_URL + "edit.htm";
    public static final String DELETE_URL = BASE_URL + "delete.htm";
    public static final String LIST_SERVICE_URL = BASE_URL + "listService.htm";
    
    public static final String SLIDESHOW_IMG_URL = "/bdrs/public/gallery/slideshowImg.htm";
    public static final String FULL_IMG_URL = "/bdrs/public/gallery/fullImg.htm";
    
    public static final String GALLERY_PK_VIEW = "galleryId";
    public static final String GALLERY_PK_SAVE = "galleryPk";
    
    public static final String PARAM_NAME = "name";
    public static final String PARAM_DESCRIPTION = "description";
    
    public static final String PARAM_FILE_UUID = "fileUuid";
    public static final String PARAM_FILE_LICENSE = "fileLicense";
    public static final String PARAM_FILE_DESCRIPTION = "fileDescription";
    public static final String PARAM_FILE_CREDIT = "fileCredit";
    public static final String PARAM_FILE_FILE = "fileFile";
    public static final String PARAM_FILE_SLIDESHOW_FILE = "fileSlideshowFile";
    //public static final String PARAM_FILE_AUTOGEN_SLIDESHOW = "autogenSlideshow";
    
    public static final String PARAM_FILE_SLIDESHOW_ACTION = "slideshowAction";
    public static final String SLIDESHOW_ACTION_RESIZE = "autoresize_ss";
    public static final String SLIDESHOW_ACTION_UPLOAD = "upload_ss";
    public static final String SLIDESHOW_ACTION_NONE = "none";
    
    public static final String PARAM_UUID = "uuid";
    
    public static final String FILENAME_SLIDESHOW_PREFIX = "slideshow_";
    
    public static final Integer SLIDESHOW_DEFAULT_WIDTH = 250;
    public static final Integer SLIDESHOW_DEFAULT_HEIGHT = 154;
    public static final Integer JPEG_QUALITY = 95;
    
    @Autowired
    private GalleryDAO galleryDAO;
    @Autowired
    private ManagedFileService mfService;
    @Autowired
    private ManagedFileDAO mfDAO;
    @Autowired
    private ImageService imageService;
    @Autowired
    private FileService fileService;
    
    private Logger log = Logger.getLogger(this.getClass());
    
    // Lists all the galleries in a grid
    @RequestMapping(value=LIST_URL, method=RequestMethod.GET)
    public ModelAndView list(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("galleryListing");
        return mv;
    }
    
    // Open up an editing page
    @RequestMapping(value=EDIT_URL, method=RequestMethod.GET)
    public ModelAndView view(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value=GALLERY_PK_VIEW, defaultValue="0") Integer pk) {
        Gallery gallery = (pk == null || pk == 0) ? new Gallery() : galleryDAO.get(pk);
        ModelAndView mv = new ModelAndView("galleryEdit");
        List<ManagedFile> mfList = new ArrayList<ManagedFile>();
        
        for (String uuid : gallery.getFileUUIDS()) {
            mfList.add(mfDAO.getManagedFile(uuid));
        }
        mv.addObject("gallery", gallery);
        mv.addObject("managedFileList", mfList);
        return mv;
    }
    
    // The submission of the edited page
    @RequestMapping(value=EDIT_URL, method=RequestMethod.POST)
    public ModelAndView save(MultipartHttpServletRequest request, HttpServletResponse response,
            @RequestParam(value=GALLERY_PK_SAVE, defaultValue="0") Integer pk,
            @RequestParam(value=PARAM_NAME) String name,
            @RequestParam(value=PARAM_DESCRIPTION) String description) throws IOException {
        
        boolean newItem = (pk == null || pk == 0);
        Gallery gallery = newItem ? new Gallery() : galleryDAO.get(pk);
        
        if (gallery == null) {
            throw new IllegalArgumentException("Invalid id to edit existing Gallery. id = " + pk.toString());
        }
        
        List<String> uuidList = new ArrayList<String>();
        
        if (request.getParameter(PARAM_FILE_UUID) != null) {
            int count = request.getParameterValues(PARAM_FILE_UUID).length;
            String[] slideshowActionParams = request.getParameterValues(PARAM_FILE_SLIDESHOW_ACTION);
            List<MultipartFile> imageFiles = request.getFiles(PARAM_FILE_FILE);
            List<MultipartFile> slideshowFiles = request.getFiles(PARAM_FILE_SLIDESHOW_FILE);
            
            // START SERVER SIDE FORM VALIDATION
            
            // 1. make sure the files are images.
            // 2. make sure the actions are valid.
            boolean errorImages = false;
            boolean errorParams = false;
            boolean slideshowMissing = false;
            boolean imgMissing = false;
            for (int i=0; i<count; ++i) {
                MultipartFile file = getFileFromList(imageFiles, i);
                MultipartFile slideshowFile = getFileFromList(slideshowFiles, i);
                String action = slideshowActionParams[i];
                String uuid = request.getParameterValues(PARAM_FILE_UUID)[i];
                
                if (file != null && !file.getContentType().startsWith("image")) {
                    errorImages = true;
                    break;
                }
                if (slideshowFile != null && !slideshowFile.getContentType().startsWith("image")) {
                    errorImages = true;
                    break;
                }
                if (!SLIDESHOW_ACTION_RESIZE.equals(action) 
                        && !SLIDESHOW_ACTION_UPLOAD.equals(action)
                        && !SLIDESHOW_ACTION_NONE.equals(action)) {
                    errorParams = true;
                }
                // if new image and the file is null / empty...
                if (!StringUtils.hasLength(uuid) && (file == null || file.isEmpty())) {
                    imgMissing = true;
                }
                // if slide show image is indicated to be uploaded but there is no slide show image...
                if (SLIDESHOW_ACTION_UPLOAD.equals(action) && (slideshowFile == null || slideshowFile.isEmpty())) {
                    slideshowMissing = true;
                }
            }
            if (errorImages) {
                ModelAndView mv = new ModelAndView(new RedirectView(getRedirectToEditPageUrl(gallery.getId()), true));
                getRequestContext().addMessage("bdrs.gallery.save.badImages", new Object[]{gallery.getName()});
                return mv;
            }
            if (errorParams) {
                ModelAndView mv = new ModelAndView(new RedirectView(getRedirectToEditPageUrl(gallery.getId()), true));
                getRequestContext().addMessage("bdrs.gallery.save.badParams", new Object[]{gallery.getName()});
                return mv;
            }
            if (imgMissing) {
                ModelAndView mv = new ModelAndView(new RedirectView(getRedirectToEditPageUrl(gallery.getId()), true));
                getRequestContext().addMessage("bdrs.gallery.save.missingFullImage", new Object[]{gallery.getName()});
                return mv;
            }
            if (slideshowMissing) {
                ModelAndView mv = new ModelAndView(new RedirectView(getRedirectToEditPageUrl(gallery.getId()), true));
                getRequestContext().addMessage("bdrs.gallery.save.missingSlideshowImage", new Object[]{gallery.getName()});
                return mv;
            }
            
            // END SERVER SIDE FORM VALIDATION
            
            for (int i=0; i<count; ++i) {
                String uuid = request.getParameterValues(PARAM_FILE_UUID)[i];
                String credit = request.getParameterValues(PARAM_FILE_CREDIT)[i];
                String fileDesc = request.getParameterValues(PARAM_FILE_DESCRIPTION)[i];
                String license = request.getParameterValues(PARAM_FILE_LICENSE)[i];

                MultipartFile file = getFileFromList(imageFiles, i);
                ManagedFile mf = mfService.saveManagedFile(uuid, fileDesc, credit, license, file);
                uuidList.add(mf.getUuid());
                
                // Do the image resizing
                //if (slideshowActionParams != null && slideshowActionParams.length > i && Boolean.parseBoolean(slideshowActionParams[i])) {
                if (SLIDESHOW_ACTION_RESIZE.equals(slideshowActionParams[i])) {
                    File f = fileService.getFile(mf, mf.getFilename()).getFile();
                    mf.setContentType(FileUtils.getContentType(f));
                    mf = mfDAO.save(mf);
 
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(f);

                        // fixed width but height can be variable
                        BufferedImage bf = imageService.resizeImage(new FileInputStream(f), SLIDESHOW_DEFAULT_WIDTH, SLIDESHOW_DEFAULT_HEIGHT);
                        File targetFile = fileService.createTargetFile(mf.getClass(), mf.getId(), FILENAME_SLIDESHOW_PREFIX + mf.getFilename());
                        imageService.saveImage(targetFile, bf, mf.getContentType(), JPEG_QUALITY);                       
                    } catch (IOException e) {
                        log.error("Error doing image resizing", e);
                        throw e;
                    } finally {
                        if (fis != null) {
                            fis.close();
                        }
                    }            
                } else if (SLIDESHOW_ACTION_UPLOAD.equals(slideshowActionParams[i])) {
                    // if a file has been uploaded, store it
                    MultipartFile slideshowFile = getFileFromList(slideshowFiles, i);
                    if(slideshowFile != null && !slideshowFile.isEmpty()) {
                        fileService.createFile(mf, slideshowFile, FILENAME_SLIDESHOW_PREFIX + mf.getFilename());
                    }
                } else if (SLIDESHOW_ACTION_NONE.equals(slideshowActionParams[i])) {
                    // do nothing
                } else {
                    log.error("Cannot have slideshowAction = " + slideshowActionParams[i]);
                    throw new IllegalArgumentException("Cannot have slideshowAction = " + slideshowActionParams[i]);
                }
            }
        }
        
        gallery.setName(name);
        gallery.setDescription(description);
        
        gallery.setFileUUIDS(uuidList);
        
        if (newItem) {
            gallery = galleryDAO.save(gallery);
        } else {
            gallery = galleryDAO.update(gallery);
        }
        ModelAndView mv = new ModelAndView(new RedirectView(getRedirectToEditPageUrl(gallery.getId()), true));
        getRequestContext().addMessage("bdrs.gallery.save.success", new Object[]{gallery.getName()});
        return mv;
    }
    
    private MultipartFile getFileFromList(List<MultipartFile> list, int idx) {
        MultipartFile file = (list != null && list.size() > idx && list.get(idx).getSize() > 0) ? list.get(idx) : null;
        return file;
    }
    
    // delete a gallery
    @RequestMapping(value=DELETE_URL, method=RequestMethod.POST)
    public ModelAndView save(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value=GALLERY_PK_SAVE, defaultValue="0") Integer pk) {
        Gallery gm = galleryDAO.get(pk);
        galleryDAO.delete(gm);
        ModelAndView mv = new ModelAndView(new RedirectView(LIST_URL, true));
        return mv;
    }
    
    // ajax service for the grid to display all galleries
    @RequestMapping(value=LIST_SERVICE_URL, method=RequestMethod.GET)
    public void listService(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value=PARAM_NAME, required=false) String name,
            @RequestParam(value=PARAM_DESCRIPTION, required=false) String description) throws Exception {
        JqGridDataHelper jqGridHelper = new JqGridDataHelper(request);       
        PaginationFilter filter = jqGridHelper.createFilter(request);
        
        PagedQueryResult<Gallery> queryResult = galleryDAO.search(filter, name, description);
        
        JqGridDataBuilder builder = new JqGridDataBuilder(jqGridHelper.getMaxPerPage(), queryResult.getCount(), jqGridHelper.getRequestedPage());
        
        if (queryResult.getCount() > 0) {
            for (Gallery gm : queryResult.getList()) {
                JqGridDataRow row = new JqGridDataRow(gm.getId());
                row
                .addValue("name", gm.getName())
                .addValue("description", gm.getDescription())
                .addValue("numImages", String.format("%d",  gm.getFileUUIDS().size()));
                builder.addRow(row);
            }
        }
        response.setContentType("application/json");
        response.getWriter().write(builder.toJson());
    }
    
    // returns the slideshow image
    @RequestMapping(value=SLIDESHOW_IMG_URL, method=RequestMethod.GET)
    public ModelAndView getSlideshowImg(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value=PARAM_UUID, required=true) String uuid) {
        ManagedFile mf = mfDAO.getManagedFile(uuid);
        if (mf == null) {
            response.setStatus(404);
            return null;
        }
        return super.downloadFile(mf.getClass().getName(), mf.getId(), FILENAME_SLIDESHOW_PREFIX + mf.getFilename());
    }
    
    // returns the full sized image
    @RequestMapping(value=FULL_IMG_URL, method=RequestMethod.GET)
    public ModelAndView getFullImg(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value=PARAM_UUID, required=true) String uuid) {
        ManagedFile mf = mfDAO.getManagedFile(uuid);
        if (mf == null) {
            response.setStatus(404);
            return null;
        }
        return super.downloadFile(mf.getClass().getName(), mf.getId(), mf.getFilename());
    }
    
    // helper for redirection
    private String getRedirectToEditPageUrl(Integer galleryId) {
        if (galleryId != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(EDIT_URL);
            sb.append("?");
            sb.append(GALLERY_PK_VIEW);
            sb.append("=");
            sb.append(galleryId.toString());
            return sb.toString();
        }
        return EDIT_URL;
    }
}
