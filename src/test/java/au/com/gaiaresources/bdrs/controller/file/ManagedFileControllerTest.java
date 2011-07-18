package au.com.gaiaresources.bdrs.controller.file;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import junit.framework.Assert;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

public class ManagedFileControllerTest extends AbstractControllerTest {
    
    @Autowired
    private ManagedFileDAO managedFileDAO;
    
    @Before
    public void setUp() throws Exception {
        
        ManagedFile mf;
        
        mf = new ManagedFile();
        mf.setFilename("test_image.png");
        mf.setContentType("image/png");
        mf.setWeight(0);
        mf.setDescription("This is a test image");
        mf.setCredit("Creative Commons");
        mf.setLicense("Nobody");
        mf.setPortal(RequestContextHolder.getContext().getPortal());
        managedFileDAO.save(mf);
        
        mf = new ManagedFile();
        mf.setFilename("test_document.pdf");
        mf.setContentType("application/pdf");
        mf.setWeight(0);
        mf.setDescription("This is a test document");
        mf.setCredit("Copyright Someone");
        mf.setLicense("Someone");
        mf.setPortal(RequestContextHolder.getContext().getPortal());
        managedFileDAO.save(mf);
    }
    
    @Test
    public void testListing() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/bdrs/user/managedfile/listing.htm");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "managedFileList");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "managedFilePaginator");
    }
    
    @Test
    public void testDelete() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("POST");
        request.setRequestURI("/bdrs/user/managedfile/delete.htm");
        for(ManagedFile mf : managedFileDAO.getManagedFiles()) {
            request.addParameter("managedFilePk", mf.getId().toString());
        }

        ModelAndView mv = handle(request, response);
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals("/bdrs/user/managedfile/listing.htm", redirect.getUrl());
        
        Assert.assertEquals(0, managedFileDAO.getManagedFiles().size());
    }
    
    @Test
    public void testEdit() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        ManagedFile expected = managedFileDAO.getManagedFiles().get(0);

        request.setMethod("GET");
        request.setRequestURI("/bdrs/user/managedfile/edit.htm");
        request.setParameter("id", expected.getId().toString());

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "managedFileEdit");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "managedFile");
        
        Assert.assertEquals(expected.getId(), ((ManagedFile)mv.getModel().get("managedFile")).getId());
    }
    
    @Test
    public void testEditSave() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        ManagedFile expected = managedFileDAO.getManagedFiles().get(0);

        request.setMethod("POST");
        request.setRequestURI("/bdrs/user/managedfile/edit.htm");

        MockMultipartFile testFile = new MockMultipartFile("file", "test_text_file.txt", "text/plain", "Spam and Eggs".getBytes());
        request.setParameter("managedFilePk", expected.getId().toString());
        request.setParameter("filename", "C:\\fakepath\\"+testFile.getOriginalFilename());
        request.setParameter("description", "Edited Description");
        request.setParameter("credit", "Edited Credits");
        request.setParameter("license", "Edited License");
        ((MockMultipartHttpServletRequest)request).addFile(testFile);
        
        ModelAndView mv = handle(request, response);
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals("/bdrs/user/managedfile/listing.htm", redirect.getUrl());
        
        ManagedFile actual = managedFileDAO.getManagedFile(expected.getId());
        
        Assert.assertEquals(testFile.getOriginalFilename(), actual.getFilename());
        Assert.assertEquals(request.getParameter("description"), actual.getDescription());
        Assert.assertEquals(request.getParameter("credit"), actual.getCredit());
        Assert.assertEquals(request.getParameter("license"), actual.getLicense());
    }
    
    @Test
    public void testAdd() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        request.setMethod("GET");
        request.setRequestURI("/bdrs/user/managedfile/edit.htm");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "managedFileEdit");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "managedFile");
        Assert.assertNull(((ManagedFile)mv.getModel().get("managedFile")).getId());
    }
    
    @Test
    public void testAddSave() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        request.setMethod("POST");
        request.setRequestURI("/bdrs/user/managedfile/edit.htm");
        
        Set<UUID> uuidSet = new HashSet<UUID>();
        for(ManagedFile existingMF : managedFileDAO.getManagedFiles()) {
           uuidSet.add(UUID.fromString(existingMF.getUuid())); 
        }
        
        MockMultipartFile testFile = new MockMultipartFile("file", "test_text_file.txt", "text/plain", "Spam and Eggs".getBytes());
        request.setParameter("filename", "C:\\fakepath\\"+testFile.getOriginalFilename());
        request.setParameter("description", "New Description");
        request.setParameter("credit", "New Credits");
        request.setParameter("license", "New License");
        ((MockMultipartHttpServletRequest)request).addFile(testFile);
        
        ModelAndView mv = handle(request, response);
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals("/bdrs/user/managedfile/listing.htm", redirect.getUrl());
        
        ManagedFile actual = null;
        for(ManagedFile mf : managedFileDAO.getManagedFiles()) {
            if(!uuidSet.contains(UUID.fromString(mf.getUuid()))) {
                actual = mf;
                
                Assert.assertEquals(testFile.getOriginalFilename(), actual.getFilename());
                Assert.assertEquals(request.getParameter("description"), actual.getDescription());
                Assert.assertEquals(request.getParameter("credit"), actual.getCredit());
                Assert.assertEquals(request.getParameter("license"), actual.getLicense());
            }
        }
        
        // Otherwise you didn't find the new file!
        Assert.assertTrue(actual != null);
    }
    
    @Test
    public void testEditManagedFileWebServiceGet() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        ManagedFile expected = managedFileDAO.getManagedFiles().get(0);

        request.setMethod("GET");
        request.setRequestURI(ManagedFileController.MANAGED_FILE_EDIT_AJAX_URL);
        request.setParameter("id", expected.getId().toString());

        handle(request, response);
        
        JSONObject result = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        JSONObject data = (JSONObject)result.get("data");
        
        Assert.assertEquals(expected.getId().toString(), data.getString("id"));
        Assert.assertEquals(expected.getCredit(), data.getString("credit"));
        Assert.assertEquals(expected.getDescription(), data.getString("description"));
        Assert.assertEquals(expected.getLicense(), data.getString("license"));
        Assert.assertEquals(expected.getUuid(), data.getString("uuid"));
    }
    
    @Test 
    public void testEditManagedFileWebServicePostNew() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        request.setMethod("POST");
        request.setRequestURI("/bdrs/user/managedfile/edit.htm");
        
        Set<UUID> uuidSet = new HashSet<UUID>();
        for(ManagedFile existingMF : managedFileDAO.getManagedFiles()) {
           uuidSet.add(UUID.fromString(existingMF.getUuid())); 
        }
        
        MockMultipartFile testFile = new MockMultipartFile("file", "test_text_file.txt", "text/plain", "Spam and Eggs".getBytes());
        request.setParameter("filename", "C:\\fakepath\\"+testFile.getOriginalFilename());
        request.setParameter("description", "New Description");
        request.setParameter("credit", "New Credits");
        request.setParameter("license", "New License");
        ((MockMultipartHttpServletRequest)request).addFile(testFile);
        
        handle(request, response);
        
        ManagedFile actual = null;
        for(ManagedFile mf : managedFileDAO.getManagedFiles()) {
            if(!uuidSet.contains(UUID.fromString(mf.getUuid()))) {
                actual = mf;
                
                Assert.assertEquals(testFile.getOriginalFilename(), actual.getFilename());
                Assert.assertEquals(request.getParameter("description"), actual.getDescription());
                Assert.assertEquals(request.getParameter("credit"), actual.getCredit());
                Assert.assertEquals(request.getParameter("license"), actual.getLicense());
            }
        }
        
        // Otherwise you didn't find the new file!
        Assert.assertTrue(actual != null);
    }
    

    @Test 
    public void testEditManagedFileWebServicePostExisting() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        ManagedFile expected = managedFileDAO.getManagedFiles().get(0);

        request.setMethod("POST");
        request.setRequestURI(ManagedFileController.MANAGED_FILE_EDIT_AJAX_URL);

        MockMultipartFile testFile = new MockMultipartFile("file", "test_text_file.txt", "text/plain", "Spam and Eggs".getBytes());
        request.setParameter("managedFilePk", expected.getId().toString());
        request.setParameter("filename", "C:\\fakepath\\"+testFile.getOriginalFilename());
        request.setParameter("description", "Edited Description");
        request.setParameter("credit", "Edited Credits");
        request.setParameter("license", "Edited License");
        ((MockMultipartHttpServletRequest)request).addFile(testFile);
        
        handle(request, response);

        ManagedFile actual = managedFileDAO.getManagedFile(expected.getId());
        
        Assert.assertEquals(testFile.getOriginalFilename(), actual.getFilename());
        Assert.assertEquals(request.getParameter("description"), actual.getDescription());
        Assert.assertEquals(request.getParameter("credit"), actual.getCredit());
        Assert.assertEquals(request.getParameter("license"), actual.getLicense());
    }
    
    @Override
    protected MockHttpServletRequest createMockHttpServletRequest() {
        return new MockMultipartHttpServletRequest();
    }
}