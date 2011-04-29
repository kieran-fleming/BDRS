package au.com.gaiaresources.bdrs.controller.admin.region;
import static org.junit.Assert.assertEquals;

import org.hibernate.annotations.common.reflection.ReflectionUtil;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.Errors;

import au.com.gaiaresources.bdrs.model.region.Region;
import au.com.gaiaresources.bdrs.model.region.RegionService;

public class TestRegionFormValidator {
    
    
    private static final Integer EXISTING_REGION_ID = 10;
    private static final String EXISTING_REGION_NAME = "An Existing Region";
    private static final String NEW_REGION_NAME = "A New Region";
    
    private Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private final RegionService mockRegionService = context.mock(RegionService.class);
    private final Region mockRegion = context.mock(Region.class);
    private final Errors mockErrors = context.mock(Errors.class);
    
    @Before
    public void setUp() {
        context.checking(new Expectations() {{
            allowing(mockRegion).getId(); will(returnValue(EXISTING_REGION_ID));
            allowing(mockRegion).getRegionName(); will(returnValue(EXISTING_REGION_NAME));
        }});
    }
    
    @Test
    public void testGetSupportedClass() {
        RegionFormValidator validator = new RegionFormValidator();
        assertEquals(RegionForm.class, validator.getSupportedClass());
    }
    
    @Test
    public void testInternalValidateOnNewInstanceNotDuplicate() throws Exception {
        
        context.checking(new Expectations() {{
            allowing(mockRegionService).getRegion(NEW_REGION_NAME);
            will(returnValue(null));
        }});
        
        RegionForm regionForm = new RegionForm();
        regionForm.setRegionName(NEW_REGION_NAME);
        RegionFormValidator validator = new RegionFormValidator();
        ReflectionTestUtils.setField(validator, "regionService", mockRegionService);
        validator.internalValidate(regionForm, mockErrors);
    }
    
    @Test
    public void testInternalValidateOnNewInstanceDuplicate() throws Exception {
        
        context.checking(new Expectations() {{
            allowing(mockRegionService).getRegion(NEW_REGION_NAME); 
            will(returnValue(mockRegion));
            
            allowing(mockErrors).rejectValue("name", "RegionForm.uniqueName", new Object[] {NEW_REGION_NAME}, "");
        }});
        
        RegionForm regionForm = new RegionForm();
        regionForm.setRegionName(NEW_REGION_NAME);
        RegionFormValidator validator = new RegionFormValidator();
        ReflectionTestUtils.setField(validator, "regionService", mockRegionService);
        validator.internalValidate(regionForm, mockErrors);
    }
    
    @Test
    public void testInternalValidateOnEditInstanceNotDuplicate() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockRegionService).getRegion(EXISTING_REGION_NAME);
            will(returnValue(mockRegion));
        }});
        
        RegionForm regionForm = new RegionForm();
        regionForm.setRegionName(EXISTING_REGION_NAME);
        regionForm.setId(EXISTING_REGION_ID);
        RegionFormValidator validator = new RegionFormValidator();
        ReflectionTestUtils.setField(validator, "regionService", mockRegionService);
        validator.internalValidate(regionForm, mockErrors);
    }
    
    @Test
    public void testInternalValidateOnEditInstanceDuplicate() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockRegionService).getRegion(EXISTING_REGION_NAME);
            will(returnValue(mockRegion));
            
            allowing(mockErrors).rejectValue("name", "RegionForm.uniqueName", new Object[] {EXISTING_REGION_NAME}, "");
        }});
        
        RegionForm regionForm = new RegionForm();
        regionForm.setRegionName(EXISTING_REGION_NAME);
        regionForm.setId(12);
        RegionFormValidator validator = new RegionFormValidator();
        ReflectionTestUtils.setField(validator, "regionService", mockRegionService);
        validator.internalValidate(regionForm, mockErrors);
    }
}
