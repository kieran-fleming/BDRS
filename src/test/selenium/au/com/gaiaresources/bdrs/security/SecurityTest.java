package au.com.gaiaresources.bdrs.security;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.util.StringUtils;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Tests that each page can be opened with the appropriate roles.
 * @author stephanie
 */
@Transactional
public class SecurityTest  extends AbstractControllerTest {

    private static final String ROOT_URL = "BDRS";
    @Autowired
    protected SurveyDAO surveyDAO;
    @Autowired
    protected MetadataDAO metaDAO;
    @Autowired
    protected UserDAO userDAO;
    @Autowired
    protected RecordDAO recordDAO;

    protected Survey currentSurvey;
    protected User currentUser;
    private static final String BASIC_SURVEY_NAME = "Basic Survey";
    
    private static List<Method> testMethods;
    private Map<String, String> paramMap = new HashMap<String, String>();
    
    private Set<User> testUsers;
    
    /** Use this object to run all of your selenium tests */
    protected Selenium selenium;
    
    @BeforeClass
    public static void setUpBeforeClass() throws ClassNotFoundException, IOException {
        testMethods = getMethods("au.com.gaiaresources.bdrs");
    }
    
    @Before
    public void setUp() throws Exception {
        setUp("http://localhost:8080/", "*firefox");
        List<String> roles = Arrays.asList(Role.getAllRoles());
        testUsers = getUsers(roles);
    }
    
    private Survey createSurvey(String name, String desc, Date startDate, Date endDate, SurveyFormRendererType renderType) {
        Survey surv = new Survey();
        surv.setName(name);
        surv.setDescription(desc);
        surv.setStartDate(startDate);
        surv.setActive(true);
        surv.setPublic(true);
        
        surv.setRunThreshold(false);
        
        Metadata rendererTypeMetadata = surv.setFormRendererType(renderType);
        metaDAO.save(rendererTypeMetadata);
        
        return surveyDAO.save(surv);
    }
    
    /**
     * Ensures that you can view anonymous pages without logging in.
     * @throws Exception
     */
    @Test
    public void testAnonymousPages() throws Exception {
        selenium.open(ROOT_URL + "/");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Sign in"));
        
        selenium.open(ROOT_URL + "/home.htm");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Welcome to the Biological Data Recording System"));
        //assertFalse(selenium.isTextPresent("Sign in"));
        
        selenium.open(ROOT_URL + "/review/sightings/advancedReview.htm");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Advanced Review"));
        //assertFalse(selenium.isTextPresent("Sign in"));
        
        selenium.open(ROOT_URL + "/fieldguide/groups.htm");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Select a Taxonomic Group"));
        //assertFalse(selenium.isTextPresent("Sign in"));

        selenium.open(ROOT_URL + "/about.htm");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("About this Site"));
        //assertFalse(selenium.isTextPresent("Sign in"));

        selenium.open(ROOT_URL + "/help.htm");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Public Help for Biological Data Recording System"));
        //assertFalse(selenium.isTextPresent("Sign in"));
    }
    
    /**
     * Ensures that you can view pages with appropriate log in or anonymously where applicable.
     * @throws Exception
     */
    @Test
    public void testUserPages() throws Exception {
        List<String> roles = new ArrayList<String>(Arrays.asList(Role.getAllRoles()));
        for (User user : testUsers) {
            currentUser = user;
            String highestRole = Role.getHighestRole(user.getRoles());
            if (roles.contains(highestRole)) {
                testPages(user);
                roles.remove(highestRole);
            }
        }
        
        // do anonymous page test
        currentUser = null;
        testPages(null);
    }
    
    private Set<User> getUsers(List<String> roles) {
        // create a list of users for testing
        Set<User> users = new HashSet<User>(roles.size());
        for (String role : roles) {
            if (!role.equals(Role.ANONYMOUS)) {
                String roleName = role.toLowerCase().replaceAll("_", "").substring(4);
                User user = userDAO.getUser(roleName);
                if (user != null) {
                    users.add(user);
                } else {
                    users.add(userDAO.createUser(roleName, roleName, roleName, roleName+"@gaiabdrs.com.au", "password", "testRegKey", new String[]{role}));
                }
            }
        }
        return users;
    }

    private void testPages(User user) {
        List<Survey> surveys = null;
        if (user != null) {
            surveys = surveyDAO.getSurveys(user);
        } else {
            surveys = surveyDAO.getActivePublicSurveys(false);
        }
        
        for (Method method : testMethods) {
            // get the url and parameters from the RequestMapping annotation
            RequestMapping rmAnn = method.getAnnotation(RequestMapping.class);
            RolesAllowed raAnn = method.getAnnotation(RolesAllowed.class);

            String url = rmAnn.value()[0];
            boolean shouldPass = true;
            
            if (raAnn == null) {
                //fail("No RolesAllowed annotation specified for RequestMapping "+url);
                System.out.println("WARNING: No RolesAllowed annotation specified for RequestMapping, skipping page: "+url);
                continue;
            } else {
                String rolesAllowedStr = Arrays.toString(raAnn.value());
                shouldPass = (user == null && rolesAllowedStr.contains(Role.ANONYMOUS)) || 
                             (user != null && rolesAllowedStr.contains(Role.getHighestRole(user.getRoles())));
            }
            
            String[] paramNames = rmAnn.params();
            // if params are null, check the method parameters for @RequestParam annotations
            if (paramNames == null || paramNames.length < 1) {
                Annotation[][] paramAnns = method.getParameterAnnotations();
                List<String> annotationParams = new ArrayList<String>();
                for (Annotation[] annotations : paramAnns) {
                    for (Annotation annotation : annotations) {
                        if (annotation.annotationType().equals(RequestParam.class)) {
                            annotationParams.add(((RequestParam) annotation).value());
                        }
                    }
                }
                paramNames = annotationParams.toArray(new String[annotationParams.size()]);
            }
            // ignore the report pages for this test and 
            // don't attempt a POST
            if (!url.startsWith("/report/") && 
                    !Arrays.toString(rmAnn.method()).contains("POST")
                    // skip urls with parameters at this stage because we can't populate them
                    && (paramNames == null || paramNames.length < 1) &&
                    // skip the portal redirects since a direct call to this page will fail
                    !url.endsWith("/**") &&
                    // skip this page for now as I'm not sure it is used
                    !url.endsWith("/fieldGuide.htm") &&
                    // skip these pages for now as they ask you to save a file
                    // will need special handling for this to click the save file dialog and do something with the download
                    !url.startsWith("/review/sightings/advancedReviewKMLSightings.htm") &&
                    !url.contains("/webservice/") && 
                    // skip embedded widgets
                    !url.contains("/embedded") && 
                    // skip the mobile urls for now
                    !url.contains("/mobile") && 
                    // skip the error urls
                    !url.contains("/error") && 
                    // skip javascript urls
                    !url.endsWith(".js")) {
                
                String[] testText = null;
                shouldPass = !url.equals("/loginfailed.htm");
                if (shouldPass) {
                    if ((url.contains("/authenticated/") ||
                        url.endsWith("home.htm") || 
                        (url.contains("/admin/") && user == null))) {
                        // should be on the home page or sign in page for these urls
                        // when they are passing tests
                        testText = new String[]{
                                "Sign in", "Welcome to the"
                        };
                    } else if (url.equals("/privacyStatement.htm")) {
                        testText = new String[]{
                                "Privacy Statement"
                        };
                    }
                }
                testPage(user, url, paramNames, testText, shouldPass);
            }
        }
    }
    
    private Survey getOrCreateSurvey() {
        List<Survey> surveys = null;
        if (currentUser == null) {
            surveys = surveyDAO.getActivePublicSurveys(false);
        } else {
            surveys = surveyDAO.getSurveys(currentUser);
        }
        if (surveys != null && !surveys.isEmpty()) {
            // return the first survey in the list
            currentSurvey = surveys.get(0);
        } else {
            currentSurvey = createSurvey(BASIC_SURVEY_NAME, BASIC_SURVEY_NAME, new Date(), null, SurveyFormRendererType.DEFAULT);
        }
        return currentSurvey;
    }

    private Record getOrCreateRecord() {
        List<Record> records = null;
        if (currentUser != null) {
            records = recordDAO.getRecords(currentUser);
        } else if (currentSurvey != null) {
            records = recordDAO.getRecords(currentSurvey, testUsers);
        } else if (currentSurvey == null) {
            getOrCreateSurvey();
            records = recordDAO.getRecords(currentSurvey, testUsers);
        }
        if (records == null || records.size() < 1) {
            // create a record
            Record rec = new Record();
            if (currentSurvey == null) {
                getOrCreateSurvey();
            }
            rec.setSurvey(currentSurvey);
            if (currentUser == null) {
                return null;
            }
            rec.setUser(currentUser);
            return recordDAO.saveRecord(rec);
        }
        return records.get(0);
    }
    
    /**
     * Helper method to log in as the given user and test the given url
     */
    
    private void testPage(User user, String url, String[] paramNames, String[] testText, boolean shouldPass) {
        if (paramNames != null && paramNames.length > 0) {
            // add the parameters to the url
            url += "?";
            for (String paramName : paramNames) {
                String value = getParamValue(paramName);
                if (!StringUtils.nullOrEmpty(value)) {
                    url += paramName + "=" + value + "&";
                } else {
                    System.out.println("no value for parameter "+paramName);
                }
            }
            // remove the trailing &
            if (url.endsWith("&")) {
                url = url.substring(0, url.length()-1);
            }
        }
        
        // go to the url
        selenium.open(ROOT_URL + url);
        selenium.waitForPageToLoad("60000");
        if (user != null && selenium.isTextPresent("Sign in")) {
            // log in 
            selenium.type("j_username", user.getName());
            selenium.type("j_password", "password");
            selenium.click("j_submit");
            selenium.waitForPageToLoad("30000");
            // make sure there was not a sign in error
            assertFalse("Login failed for user"+user.getName(), selenium.isTextPresent("Invalid user name or password"));
            assertFalse("Error occurred on login for page "+url+" with user "+user.getName(), selenium.isTextPresent("500"));
        }
        if (shouldPass) {
            if (testText != null) {
                boolean foundSomeText = false;
                for (String string : testText) {
                    foundSomeText |= selenium.isTextPresent(string);
                }
                assertTrue("Didn't find any of "+testText, foundSomeText);
            } else {
                // if no test is specified, just make sure it is not an error code
                // or the signin page
                assertFalse("'Sign in' should not be on the page", selenium.isTextPresent("Sign in"));
                assertFalse("Page should not be a 500 error: "+url, selenium.isTextPresent("500"));
                assertFalse("Page should not be a 404 error: "+url, selenium.isTextPresent("404"));
            }
        } else {
            if (testText != null) {
                for (String string : testText) {
                    assertFalse(selenium.isTextPresent(string));
                }
            } else {
                // check for error codes or the signin page or home page
                assertTrue("Should be the login page or an error.", 
                           selenium.isTextPresent("Sign in") || 
                           selenium.isTextPresent("500") || 
                           selenium.isTextPresent("404") || 
                           selenium.isTextPresent("Access is denied") || 
                           selenium.isTextPresent("Welcome to the"));
            }
        }
        // remove the cookies so log in is required for each page
        selenium.deleteAllVisibleCookies();
    }
    
    private String getParamValue(String paramName) {
        // get the parameter from the map, or create it if it doesn't exist
        String value = paramMap.get(paramName);
        if (value == null) {
            if (paramName.equals("surveyId")) {
                Survey survey = getOrCreateSurvey();
                value = survey.getId().toString();
            } else if (paramName.equals("recordId")) {
                Record record = getOrCreateRecord();
                value = record == null ? null : record.getId().toString();
            }
            if (!StringUtils.nullOrEmpty(value)) {
                paramMap.put(paramName, value);
            }
        }
        return value;
    }
    
    /**
     * Scans all classes accessible from the context class loader which belong 
     * to the given package and subpackages for methods with @RequestMapping 
     * annotations and gets an array of those methods.
     *
     * @param packageName The base package
     * @return The methods
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static List<Method> getMethods(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Method> methods = new ArrayList<Method>();
        for (File directory : dirs) {
            methods.addAll(findMethods(directory, packageName));
        }
        return methods;
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs
     * that have an @Controller annotation and all of the methods in each Controller 
     * class that have an @RequestMapping annotation.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Method> findMethods(File directory, String packageName) throws ClassNotFoundException {
        List<Method> methods = new ArrayList<Method>();
        if (!directory.exists()) {
            return methods;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                methods.addAll(findMethods(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                // only add classes with an @RequestMapping annotation
                Class clazz = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));

                // TODO: handle classes with an @RequestMapping class level annotation
                //       for now, just ignoring them
                if (clazz.getAnnotation(Controller.class) != null && 
                    clazz.getAnnotation(RequestMapping.class) == null) {
                    Method[] clazzMethods = clazz.getMethods();
                    for (Method method : clazzMethods) {
                        Annotation reqMapAnn = method.getAnnotation(RequestMapping.class);
                        if (reqMapAnn != null) {
                            methods.add(method);
                        }
                    }
                }
            }
        }
        return methods;
    }
    
    // HELPER METHODS FROM SeleneseTestBase, included here instead of inheriting
    // because we need spring junit stuff from AbstractControllerTest

    private static final boolean THIS_IS_WINDOWS = File.pathSeparator.equals(";");
    
    protected String runtimeBrowserString() {
        String defaultBrowser = System.getProperty("selenium.defaultBrowser");
        if (null != defaultBrowser && defaultBrowser.startsWith("${")) {
            defaultBrowser = null;
        }
        if (defaultBrowser == null) {
            if(THIS_IS_WINDOWS){
                defaultBrowser = "*iexplore";
            } else{
                 defaultBrowser = "*firefox";
            }
        }
        return defaultBrowser;
    }
    
    /**
     * Creates a new DefaultSelenium object and starts it using the specified
     * baseUrl and browser string. The port is selected as follows: if the
     * server package's RemoteControlConfiguration class is on the classpath,
     * that class' default port is used. Otherwise, if the "server.port" system
     * property is specified, that is used - failing that, the default of 4444
     * is used.
     *
     * @see #setUp(String, String, int)
     * @param url the baseUrl for your tests
     * @param browserString the browser to use, e.g. *firefox
     * @throws Exception
     */
    public void setUp(String url, String browserString) throws Exception {
        setUp(url, browserString, getDefaultPort());
    }
    
    protected int getDefaultPort() {
        try {
            Class c = Class.forName("org.openqa.selenium.server.RemoteControlConfiguration");
            Method getDefaultPort = c.getMethod("getDefaultPort", new Class[0]);
            Integer portNumber = (Integer)getDefaultPort.invoke(null);
            return portNumber.intValue();
        } catch (Exception e) {
            return Integer.getInteger("selenium.port", 4444).intValue();
        }
    }
    
    /**
     * Creates a new DefaultSelenium object and starts it using the specified
     * baseUrl and browser string. The port is selected as follows: if the
     * server package's RemoteControlConfiguration class is on the classpath,
     * that class' default port is used. Otherwise, if the "server.port" system
     * property is specified, that is used - failing that, the default of 4444
     * is used.
     *
     * @see #setUp(String, String, int)
     * @param url the baseUrl for your tests
     * @param browserString the browser to use, e.g. *firefox
     * @param port the port that you want to run your tests on
     * @throws Exception
     */
    public void setUp(String url, String browserString, int port) {
        if (url == null) {
            url = "http://localhost:" + port;
        }
        selenium = new DefaultSelenium("localhost", port, browserString, url);
        selenium.start();
    }
    
    public static void fail(String message) {
        throw new AssertionError(message);
    }
    
    static public void assertTrue(String message, boolean condition) {
        if (!condition)
            fail(message);
    }
    
    static public void assertTrue(boolean condition) {
        assertTrue(null, condition);
    }
    
    static public void assertFalse(String message, boolean condition) {
        assertTrue(message, !condition);
    }
    
    static public void assertFalse(boolean condition) {
        assertTrue(null, !condition);
    }
}
