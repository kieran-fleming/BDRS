package au.com.gaiaresources.bdrs.controller.showcase;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataHelper;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
import au.com.gaiaresources.bdrs.model.showcase.Gallery;
import au.com.gaiaresources.bdrs.model.showcase.GalleryDAO;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.image.ImageService;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

public class GalleryControllerTest extends AbstractControllerTest {

    @Autowired
    GalleryDAO galleryDAO;
    
    @Autowired
    private ManagedFileDAO managedFileDAO;
    @Autowired
    private FileService fileService;
    @Autowired
    private ImageService imageService;
    
    Gallery g1;
    Gallery g2;
    
    ManagedFile mf;
    
    String savedFilename = "test_image.jpeg"; 
    
    @Before
    public void setup() throws IOException {
        String filename = "sample_JPEG.jpg";
        File file = new File(getClass().getResource(filename).getFile());
        
        
        
        mf = new ManagedFile();
        mf.setFilename(savedFilename);
        mf.setContentType("image/jpeg");
        mf.setWeight(0);
        mf.setDescription("This is a test image");
        mf.setCredit("Creative Commons");
        mf.setLicense("Nobody");
        mf.setPortal(RequestContextHolder.getContext().getPortal());
        managedFileDAO.save(mf);
        
        fileService.createFile(mf, file, savedFilename);
        
        g1 = new Gallery();
        g1.setName("one");
        g1.setDescription("gallery 1 description");
        
        g2 = new Gallery();
        g2.setName("two");
        g2.setDescription("description of gallery 2");
        g2.getFileUUIDS().add(mf.getUuid());
        
        galleryDAO.save(g1);
        galleryDAO.save(g2);
    }
    
    @Test
    public void testListing() throws Exception {
        request.setRequestURI(GalleryController.LIST_URL);
        request.setMethod("GET");
        ModelAndView mv = this.handle(request, response);
        Assert.assertEquals("galleryListing", mv.getViewName());
    }
    
    @Test
    public void testViewNew() throws Exception {
        request.setRequestURI(GalleryController.EDIT_URL);
        request.setMethod("GET");
        
        ModelAndView mv = this.handle(request, response);
        
        Assert.assertEquals("galleryEdit", mv.getViewName());
        
        Gallery g = (Gallery)mv.getModel().get("gallery");
        Assert.assertNotNull(g);
        Assert.assertNull(g.getId());
    }
    
    @Test
    public void testViewExisting() throws Exception {
        request.setRequestURI(GalleryController.EDIT_URL);
        request.setMethod("GET");
        
        request.setParameter(GalleryController.GALLERY_PK_VIEW, g1.getId().toString());
        
        ModelAndView mv = this.handle(request, response);
        
        Assert.assertEquals("galleryEdit", mv.getViewName());
        
        Gallery g = (Gallery)mv.getModel().get("gallery");
        Assert.assertNotNull(g);
        Assert.assertEquals(g1.getId().intValue(), g.getId().intValue());
    }
    
    @Test
    public void testSaveNew() throws Exception {
        request.setRequestURI(GalleryController.EDIT_URL);
        request.setMethod("POST");
        
        request.setParameter(GalleryController.PARAM_NAME, "new name");
        request.setParameter(GalleryController.PARAM_DESCRIPTION, "new description");
        
        ModelAndView mv = this.handle(request, response);
        
        Assert.assertEquals(3, galleryDAO.count().longValue());
        
        List<Gallery> listAll = galleryDAO.search(null, null, null).getList();
        Assert.assertTrue(listAll.remove(g1));
        Assert.assertTrue(listAll.remove(g2));
        
        Gallery g = listAll.get(0);
        Assert.assertEquals("new name", g.getName());
        Assert.assertEquals("new description", g.getDescription());
        
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals(GalleryController.EDIT_URL + "?" + GalleryController.GALLERY_PK_VIEW + "=" + g.getId().toString(), redirect.getUrl());
    }
    
    @Test
    public void testSaveExisting() throws Exception {
        request.setRequestURI(GalleryController.EDIT_URL);
        request.setMethod("POST");
        
        request.setParameter(GalleryController.GALLERY_PK_SAVE, g2.getId().toString());
        request.setParameter(GalleryController.PARAM_NAME, "new name");
        request.setParameter(GalleryController.PARAM_DESCRIPTION, "new description");
               
        logger.debug("uuid: " + mf.getUuid());
        request.addParameter(GalleryController.PARAM_FILE_UUID, mf.getUuid());
        request.addParameter(GalleryController.PARAM_FILE_DESCRIPTION, "updated description");
        request.addParameter(GalleryController.PARAM_FILE_CREDIT, "updated credit");
        request.addParameter(GalleryController.PARAM_FILE_LICENSE, "updated license");
        request.addParameter(GalleryController.PARAM_FILE_SLIDESHOW_ACTION, GalleryController.SLIDESHOW_ACTION_NONE);
        ((MockMultipartHttpServletRequest)request).addFile(new MockMultipartFile(GalleryController.PARAM_FILE_FILE, "empty", "image/jpeg", new byte[] {}));
        ((MockMultipartHttpServletRequest)request).addFile(new MockMultipartFile(GalleryController.PARAM_FILE_SLIDESHOW_FILE, "empty", "image/jpeg", new byte[] {}));
        
        ModelAndView mv = this.handle(request, response);
        
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals(GalleryController.EDIT_URL + "?" + GalleryController.GALLERY_PK_VIEW + "=" + g2.getId().toString(), redirect.getUrl());
        
        Assert.assertEquals(2, galleryDAO.count().longValue());
        
        Gallery g = galleryDAO.get(g2.getId());
        
        Assert.assertEquals("new name", g.getName());
        Assert.assertEquals("new description", g.getDescription());
        
        ManagedFile managedFile = managedFileDAO.getManagedFile(mf.getId());
        Assert.assertEquals("updated description", managedFile.getDescription());
        Assert.assertEquals("updated credit", managedFile.getCredit());
        Assert.assertEquals("updated license", managedFile.getLicense());
        // file name should not have changed due to empty file
        Assert.assertEquals("test_image.jpeg", managedFile.getFilename());
    }
    
    @Test
    public void testSaveExistingWithFiles() throws Exception {
        // add a new image type managed file and convert it
        String filename = "sample_JPEG.jpg";
        
        File file = new File(getClass().getResource(filename).getFile());

        byte[] target = imageService.fileToByteArray(file);
        
        // new file in slot 1
        MockMultipartFile testFile = new MockMultipartFile(GalleryController.PARAM_FILE_FILE, filename, "image/jpeg", target);
        request.addParameter(GalleryController.PARAM_FILE_UUID, "");
        request.addParameter(GalleryController.PARAM_FILE_DESCRIPTION, "New Description");
        request.addParameter(GalleryController.PARAM_FILE_CREDIT, "New Credits");
        request.addParameter(GalleryController.PARAM_FILE_LICENSE, "New License");
        request.addParameter(GalleryController.PARAM_FILE_SLIDESHOW_ACTION, GalleryController.SLIDESHOW_ACTION_NONE);
        ((MockMultipartHttpServletRequest)request).addFile(testFile);
        ((MockMultipartHttpServletRequest)request).addFile(new MockMultipartFile(GalleryController.PARAM_FILE_SLIDESHOW_FILE, "empty", "image/jpeg", new byte[] {}));
        
        // existing file in slot 2
        // the managed file is overwritten with a new file and all mf items are overwritten
        MockMultipartFile testFile2 = new MockMultipartFile(GalleryController.PARAM_FILE_FILE, filename, "image/jpeg", target);
        request.addParameter(GalleryController.PARAM_FILE_UUID, mf.getUuid());
        request.addParameter(GalleryController.PARAM_FILE_DESCRIPTION, "saved description");
        request.addParameter(GalleryController.PARAM_FILE_CREDIT, "saved credit");
        request.addParameter(GalleryController.PARAM_FILE_LICENSE, "saved license");
        request.addParameter(GalleryController.PARAM_FILE_SLIDESHOW_ACTION, GalleryController.SLIDESHOW_ACTION_NONE);
        
        
        ((MockMultipartHttpServletRequest)request).addFile(testFile2);
        ((MockMultipartHttpServletRequest)request).addFile(new MockMultipartFile(GalleryController.PARAM_FILE_SLIDESHOW_FILE, "empty", "image/jpeg", new byte[] {}));
        
        request.setRequestURI(GalleryController.EDIT_URL);
        request.setMethod("POST");
        
        request.setParameter(GalleryController.GALLERY_PK_SAVE, g2.getId().toString());
        request.setParameter(GalleryController.PARAM_NAME, "new name");
        request.setParameter(GalleryController.PARAM_DESCRIPTION, "new description");
        
        ModelAndView mv = this.handle(request, response);
        
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals(GalleryController.EDIT_URL + "?" + GalleryController.GALLERY_PK_VIEW + "=" + g2.getId().toString(), redirect.getUrl());
        
        Assert.assertEquals(2, galleryDAO.count().longValue());
        
        Gallery g = galleryDAO.get(g2.getId());
        
        Assert.assertEquals("new name", g.getName());
        Assert.assertEquals("new description", g.getDescription());
        
        Assert.assertEquals(2, g.getFileUUIDS().size());
        Assert.assertEquals(mf.getUuid(), g.getFileUUIDS().get(1));
        
        // must be a new file UUID
        Assert.assertFalse(g.getFileUUIDS().get(0).equals(mf.getUuid()));
        
        ManagedFile newMf = managedFileDAO.getManagedFile(g.getFileUUIDS().get(0));
        Assert.assertNotNull(fileService.getFile(newMf, filename));
        Assert.assertNotNull(fileService.getFile(mf, filename));
    }
    
    @Test
    public void testSlideshowAutogen() throws Exception {
     // add a new image type managed file and convert it
        String filename = "sample_JPEG.jpg";
        
        File file = new File(getClass().getResource(filename).getFile());

        byte[] target = imageService.fileToByteArray(file);
        
        // new file in slot 1
        MockMultipartFile testFile = new MockMultipartFile(GalleryController.PARAM_FILE_FILE, filename, "image/jpeg", target);
        request.addParameter(GalleryController.PARAM_FILE_UUID, "");
        request.addParameter(GalleryController.PARAM_FILE_DESCRIPTION, "New Description");
        request.addParameter(GalleryController.PARAM_FILE_CREDIT, "New Credits");
        request.addParameter(GalleryController.PARAM_FILE_LICENSE, "New License");
        request.addParameter(GalleryController.PARAM_FILE_SLIDESHOW_ACTION, GalleryController.SLIDESHOW_ACTION_RESIZE);
        ((MockMultipartHttpServletRequest)request).addFile(testFile);
        ((MockMultipartHttpServletRequest)request).addFile(new MockMultipartFile(GalleryController.PARAM_FILE_SLIDESHOW_FILE, "empty", "image/jpeg", new byte[] {}));
        
        // existing file in slot 2
        // the managed file is overwritten with a new file and all mf items are overwritten
        MockMultipartFile testFile2 = new MockMultipartFile(GalleryController.PARAM_FILE_FILE, filename, "image/jpeg", target);
        request.addParameter(GalleryController.PARAM_FILE_UUID, mf.getUuid());
        request.addParameter(GalleryController.PARAM_FILE_DESCRIPTION, "saved description");
        request.addParameter(GalleryController.PARAM_FILE_CREDIT, "saved credit");
        request.addParameter(GalleryController.PARAM_FILE_LICENSE, "saved license");
        request.addParameter(GalleryController.PARAM_FILE_SLIDESHOW_ACTION, GalleryController.SLIDESHOW_ACTION_RESIZE);
        ((MockMultipartHttpServletRequest)request).addFile(testFile2);
        ((MockMultipartHttpServletRequest)request).addFile(new MockMultipartFile(GalleryController.PARAM_FILE_SLIDESHOW_FILE, "empty", "image/jpeg", new byte[] {}));
        
        request.setRequestURI(GalleryController.EDIT_URL);
        request.setMethod("POST");
        
        request.setParameter(GalleryController.GALLERY_PK_SAVE, g2.getId().toString());
        request.setParameter(GalleryController.PARAM_NAME, "new name");
        request.setParameter(GalleryController.PARAM_DESCRIPTION, "new description");
        
        ModelAndView mv = this.handle(request, response);
        
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals(GalleryController.EDIT_URL + "?" + GalleryController.GALLERY_PK_VIEW + "=" + g2.getId().toString(), redirect.getUrl());
        
        Assert.assertEquals(2, galleryDAO.count().longValue());
        
        Gallery g = galleryDAO.get(g2.getId());
        
        Assert.assertEquals("new name", g.getName());
        Assert.assertEquals("new description", g.getDescription());
        
        Assert.assertEquals(2, g.getFileUUIDS().size());
        Assert.assertEquals(mf.getUuid(), g.getFileUUIDS().get(1));
        
        // must be a new file UUID
        Assert.assertFalse(g.getFileUUIDS().get(0).equals(mf.getUuid()));
        
        ManagedFile newMf = managedFileDAO.getManagedFile(g.getFileUUIDS().get(0));
        Assert.assertNotNull(fileService.getFile(newMf, filename));
        Assert.assertNotNull(fileService.getFile(mf, filename));
        Assert.assertNotNull(fileService.getFile(newMf, GalleryController.FILENAME_SLIDESHOW_PREFIX + filename));
        Assert.assertNotNull(fileService.getFile(mf, GalleryController.FILENAME_SLIDESHOW_PREFIX + filename));
        
        //PagedQueryResult<Gallery> gallerySearchResult = galleryDAO.search(null, null, null);
        //Assert.assertEquals(2, gallerySearchResult.getCount())
        Assert.assertEquals(2, galleryDAO.count().longValue());
    }
    
    @Test
    public void testSlideshowNoAutogen_uploadslideshowimages() throws Exception {
     // add a new image type managed file and convert it
        String filename = "sample_JPEG.jpg";
        
        File file = new File(getClass().getResource(filename).getFile());

        byte[] target = imageService.fileToByteArray(file);
        
        // new file in slot 1
        MockMultipartFile testFile = new MockMultipartFile(GalleryController.PARAM_FILE_FILE, filename, "image/jpeg", target);
        request.addParameter(GalleryController.PARAM_FILE_UUID, "");
        request.addParameter(GalleryController.PARAM_FILE_DESCRIPTION, "New Description");
        request.addParameter(GalleryController.PARAM_FILE_CREDIT, "New Credits");
        request.addParameter(GalleryController.PARAM_FILE_LICENSE, "New License");
        request.addParameter(GalleryController.PARAM_FILE_SLIDESHOW_ACTION, GalleryController.SLIDESHOW_ACTION_RESIZE);
        ((MockMultipartHttpServletRequest)request).addFile(testFile);
        ((MockMultipartHttpServletRequest)request).addFile(new MockMultipartFile(GalleryController.PARAM_FILE_SLIDESHOW_FILE, "whatever.jpeg", "image/jpeg", target));
        
        // existing file in slot 2
        // the managed file is overwritten with a new file and all mf items are overwritten
        MockMultipartFile testFile2 = new MockMultipartFile(GalleryController.PARAM_FILE_FILE, filename, "image/jpeg", target);
        request.addParameter(GalleryController.PARAM_FILE_UUID, mf.getUuid());
        request.addParameter(GalleryController.PARAM_FILE_DESCRIPTION, "saved description");
        request.addParameter(GalleryController.PARAM_FILE_CREDIT, "saved credit");
        request.addParameter(GalleryController.PARAM_FILE_LICENSE, "saved license");
        request.addParameter(GalleryController.PARAM_FILE_SLIDESHOW_ACTION, GalleryController.SLIDESHOW_ACTION_RESIZE);
        ((MockMultipartHttpServletRequest)request).addFile(testFile2);
        ((MockMultipartHttpServletRequest)request).addFile(new MockMultipartFile(GalleryController.PARAM_FILE_SLIDESHOW_FILE, "whatever.jpeg", "image/jpeg", target));
        
        request.setRequestURI(GalleryController.EDIT_URL);
        request.setMethod("POST");
        
        request.setParameter(GalleryController.GALLERY_PK_SAVE, g2.getId().toString());
        request.setParameter(GalleryController.PARAM_NAME, "new name");
        request.setParameter(GalleryController.PARAM_DESCRIPTION, "new description");
        
        ModelAndView mv = this.handle(request, response);
        
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals(GalleryController.EDIT_URL + "?" + GalleryController.GALLERY_PK_VIEW + "=" + g2.getId().toString(), redirect.getUrl());
        
        Assert.assertEquals(2, galleryDAO.count().longValue());
        
        Gallery g = galleryDAO.get(g2.getId());
        
        Assert.assertEquals("new name", g.getName());
        Assert.assertEquals("new description", g.getDescription());
        
        Assert.assertEquals(2, g.getFileUUIDS().size());
        Assert.assertEquals(mf.getUuid(), g.getFileUUIDS().get(1));
        
        // must be a new file UUID
        Assert.assertFalse(g.getFileUUIDS().get(0).equals(mf.getUuid()));
        
        ManagedFile newMf = managedFileDAO.getManagedFile(g.getFileUUIDS().get(0));
        Assert.assertNotNull(fileService.getFile(newMf, filename));
        Assert.assertNotNull(fileService.getFile(mf, filename));
        Assert.assertNotNull(fileService.getFile(newMf, GalleryController.FILENAME_SLIDESHOW_PREFIX + filename));
        Assert.assertNotNull(fileService.getFile(mf, GalleryController.FILENAME_SLIDESHOW_PREFIX + filename));
    }
    
    @Test
    public void testSlideshowNoAutogen_existingmainimage_autogenslideshow() throws Exception {
     // add a new image type managed file and convert it
        //String filename = "sample_JPEG.jpg";
        
        //File file = new File(getClass().getResource(filename).getFile());

        //byte[] target = imageService.fileToByteArray(file);
        
        // existing file in slot 1
        // the managed file is overwritten with a new file and all mf items are overwritten
        //MockMultipartFile testFile2 = new MockMultipartFile(GalleryController.PARAM_FILE_FILE, filename, "image/jpeg", target);
        request.addParameter(GalleryController.PARAM_FILE_UUID, mf.getUuid());
        request.addParameter(GalleryController.PARAM_FILE_DESCRIPTION, "saved description");
        request.addParameter(GalleryController.PARAM_FILE_CREDIT, "saved credit");
        request.addParameter(GalleryController.PARAM_FILE_LICENSE, "saved license");
        request.addParameter(GalleryController.PARAM_FILE_SLIDESHOW_ACTION, GalleryController.SLIDESHOW_ACTION_RESIZE);
        //((MockMultipartHttpServletRequest)request).addFile(testFile2);
        //((MockMultipartHttpServletRequest)request).addFile(new MockMultipartFile(GalleryController.PARAM_FILE_SLIDESHOW_FILE, "whatever.jpeg", "image/jpeg", target));
        
        request.setRequestURI(GalleryController.EDIT_URL);
        request.setMethod("POST");
        
        request.setParameter(GalleryController.GALLERY_PK_SAVE, g2.getId().toString());
        request.setParameter(GalleryController.PARAM_NAME, "new name");
        request.setParameter(GalleryController.PARAM_DESCRIPTION, "new description");
        
        ModelAndView mv = this.handle(request, response);
        
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals(GalleryController.EDIT_URL + "?" + GalleryController.GALLERY_PK_VIEW + "=" + g2.getId().toString(), redirect.getUrl());
        
        Assert.assertEquals(2, galleryDAO.count().longValue());
        
        Gallery g = galleryDAO.get(g2.getId());
        
        Assert.assertEquals("new name", g.getName());
        Assert.assertEquals("new description", g.getDescription());
        
        Assert.assertEquals(1, g.getFileUUIDS().size());
        Assert.assertEquals(mf.getUuid(), g.getFileUUIDS().get(0));
                
        Assert.assertNotNull(fileService.getFile(mf, savedFilename));
        Assert.assertNotNull(fileService.getFile(mf, GalleryController.FILENAME_SLIDESHOW_PREFIX + savedFilename));
    }
    
    @Test
    public void testSlideshowAutogen_testBoolArgs() throws Exception {
     // add a new image type managed file and convert it
        String filename = "sample_JPEG.jpg";
        
        File file = new File(getClass().getResource(filename).getFile());

        byte[] target = imageService.fileToByteArray(file);
        
        // new file in slot 1
        MockMultipartFile testFile = new MockMultipartFile(GalleryController.PARAM_FILE_FILE, filename, "image/jpeg", target);
        request.addParameter(GalleryController.PARAM_FILE_UUID, "");
        request.addParameter(GalleryController.PARAM_FILE_DESCRIPTION, "New Description");
        request.addParameter(GalleryController.PARAM_FILE_CREDIT, "New Credits");
        request.addParameter(GalleryController.PARAM_FILE_LICENSE, "New License");
        //request.addParameter(GalleryController.PARAM_FILE_AUTOGEN_SLIDESHOW, "true");
        request.addParameter(GalleryController.PARAM_FILE_SLIDESHOW_ACTION, GalleryController.SLIDESHOW_ACTION_RESIZE);
        ((MockMultipartHttpServletRequest)request).addFile(testFile);
        //((MockMultipartHttpServletRequest)request).addFile(new MockMultipartFile(GalleryController.PARAM_FILE_SLIDESHOW_FILE, "empty", "image/jpeg", new byte[] {}));
        
        // existing file in slot 2
        // the managed file is overwritten with a new file and all mf items are overwritten
        MockMultipartFile testFile2 = new MockMultipartFile(GalleryController.PARAM_FILE_FILE, filename, "image/jpeg", target);
        request.addParameter(GalleryController.PARAM_FILE_UUID, mf.getUuid());
        request.addParameter(GalleryController.PARAM_FILE_DESCRIPTION, "saved description");
        request.addParameter(GalleryController.PARAM_FILE_CREDIT, "saved credit");
        request.addParameter(GalleryController.PARAM_FILE_LICENSE, "saved license");
        request.addParameter(GalleryController.PARAM_FILE_SLIDESHOW_ACTION, GalleryController.SLIDESHOW_ACTION_RESIZE);
        ((MockMultipartHttpServletRequest)request).addFile(testFile2);
        //((MockMultipartHttpServletRequest)request).addFile(new MockMultipartFile(GalleryController.PARAM_FILE_SLIDESHOW_FILE, "empty", "image/jpeg", new byte[] {}));
        
        request.setRequestURI(GalleryController.EDIT_URL);
        request.setMethod("POST");
        
        request.setParameter(GalleryController.GALLERY_PK_SAVE, g2.getId().toString());
        request.setParameter(GalleryController.PARAM_NAME, "new name");
        request.setParameter(GalleryController.PARAM_DESCRIPTION, "new description");
        
        ModelAndView mv = this.handle(request, response);
        
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals(GalleryController.EDIT_URL + "?" + GalleryController.GALLERY_PK_VIEW + "=" + g2.getId().toString(), redirect.getUrl());
        
        Assert.assertEquals(2, galleryDAO.count().longValue());
        
        Gallery g = galleryDAO.get(g2.getId());
        
        Assert.assertEquals("new name", g.getName());
        Assert.assertEquals("new description", g.getDescription());
        
        Assert.assertEquals(2, g.getFileUUIDS().size());
        Assert.assertEquals(mf.getUuid(), g.getFileUUIDS().get(1));
        
        // must be a new file UUID
        Assert.assertFalse(g.getFileUUIDS().get(0).equals(mf.getUuid()));
        
        ManagedFile newMf = managedFileDAO.getManagedFile(g.getFileUUIDS().get(0));
        Assert.assertNotNull(fileService.getFile(newMf, filename));
        Assert.assertNotNull(fileService.getFile(mf, filename));
        //Assert.assertNotNull(fileService.getFile(newMf, GalleryController.FILENAME_SLIDESHOW_PREFIX + filename));
        Assert.assertNotNull(fileService.getFile(mf, GalleryController.FILENAME_SLIDESHOW_PREFIX + filename));
    }
    
    @Test
    public void testDelete() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        request.setMethod("POST");
        request.setRequestURI(GalleryController.DELETE_URL);
        request.setParameter(GalleryController.GALLERY_PK_SAVE, g2.getId().toString());
       
        this.handle(request,response);
        
        Gallery deletedGallery = galleryDAO.get(g2.getId());
        Assert.assertNull(deletedGallery);
    }
    
    @Test
    public void testListService() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        request.setMethod("GET");
        request.setRequestURI(GalleryController.LIST_SERVICE_URL);
        
        request.setParameter(JqGridDataHelper.MAX_PER_PAGE_PARAM, "1");
        request.setParameter(JqGridDataHelper.REQUESTED_PAGE_PARAM, "1");
        request.setParameter(JqGridDataHelper.SORT_IDX_PARAM, "id");
        request.setParameter(JqGridDataHelper.SORT_ORDER_PARAM, JqGridDataHelper.DESC);

        this.handle(request, response);
        
        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        
        JSONArray rowArray = json.getJSONArray("rows");
        Assert.assertEquals(1, rowArray.size());
        Assert.assertEquals(2, json.getLong("records"));
        Assert.assertEquals(g2.getName(), ((JSONObject)rowArray.get(0)).getString("name"));      
        Assert.assertEquals(g2.getDescription(), ((JSONObject)rowArray.get(0)).getString("description"));
        Assert.assertEquals(String.format("%d", g2.getFileUUIDS().size()), ((JSONObject)rowArray.get(0)).getString("numImages"));
    }
    
    @Override
    protected MockHttpServletRequest createMockHttpServletRequest() {
        return new MockMultipartHttpServletRequest();
    }
}
