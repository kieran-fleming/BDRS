package au.com.gaiaresources.bdrs.servlet.filter;

import static org.junit.Assert.*;

import java.security.Principal;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

public class TestLog4JNDCFilter {
    
    private Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private final HttpServletRequest mockRequest = context.mock(HttpServletRequest.class);
    private final HttpServletResponse mockResponse = context.mock(HttpServletResponse.class);
    private final FilterChain mockFilterChain = context.mock(FilterChain.class);
    private final HttpSession mockSession = context.mock(HttpSession.class);
    private final Principal mockPrincipal = context.mock(Principal.class);

    @Before
    public void setUp() {
        
        context.checking(new Expectations() {{ 
            allowing(mockSession).getId(); will(returnValue("SessionID"));
            allowing(mockPrincipal).getName(); will(returnValue("UserName"));
        }});
    }
    
    @Test
    public void testGetMessageWithNoSessionOrPrincipal() {
        context.checking(new Expectations() {{
            allowing(mockRequest).getSession(false); will(returnValue(null));
            allowing(mockRequest).getUserPrincipal(); will(returnValue(null));
        }});
        
        Log4JNDCFilter f = new Log4JNDCFilter();
        String message = f.getNestedDiagnosticContextMessage(mockRequest);
        String expectedMessage = "Thread: " + Thread.currentThread().getName();
        assertEquals(expectedMessage, message);
    }
    
    @Test
    public void testGetMessageWithSessionButNotPrincipal() {
        context.checking(new Expectations() {{
            allowing(mockRequest).getSession(false); will(returnValue(mockSession));
            allowing(mockRequest).getUserPrincipal(); will(returnValue(null));
        }});
        
        Log4JNDCFilter f = new Log4JNDCFilter();
        String message = f.getNestedDiagnosticContextMessage(mockRequest);
        String expectedMessage = "Session: SessionID";
        assertEquals(expectedMessage, message);
    }
    
    @Test
    public void testGetMessageWithPrincipal() {
        context.checking(new Expectations() {{
            allowing(mockRequest).getSession(false); will(returnValue(mockSession));
            allowing(mockRequest).getUserPrincipal(); will(returnValue(mockPrincipal));
        }});
        
        Log4JNDCFilter f = new Log4JNDCFilter();
        String message = f.getNestedDiagnosticContextMessage(mockRequest);
        String expectedMessage = "UserName";
        assertEquals(expectedMessage, message);
    }
    
    @Test
    public void testDoFilterInternal() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockRequest).getSession(false); will(returnValue(null));
            allowing(mockRequest).getUserPrincipal(); will(returnValue(null));
            allowing(mockFilterChain).doFilter(mockRequest, mockResponse);
        }});
        
        Log4JNDCFilter f = new Log4JNDCFilter();
        f.doFilterInternal(mockRequest, mockResponse, mockFilterChain);
    }
}
