package au.com.gaiaresources.bdrs.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestStringUtils {
    @Test
    public void testGenerateRandomString() {
        String s = StringUtils.generateRandomString(10, 20);
        String s2 = StringUtils.generateRandomString(10, 20);
        assertNotNull(s);
        if (s.length() < 10) {
            fail("String should be at least 10 characters");
        }
        if (s.length() > 20) {
            fail("String should be no more that 20 characters");
        }
        assertFalse(s.equals(s2));
    }
    
    @Test
    public void testNullOrEmpty() {
        assertTrue(StringUtils.nullOrEmpty(""));
        assertTrue(StringUtils.nullOrEmpty(null));
        assertFalse(StringUtils.nullOrEmpty(" "));
        assertFalse(StringUtils.nullOrEmpty("a"));
    }
    
    @Test
    public void testNotEmpty() {
        assertFalse(StringUtils.notEmpty(""));
        assertFalse(StringUtils.notEmpty(null));
        assertTrue(StringUtils.notEmpty(" "));
        assertTrue(StringUtils.notEmpty("a"));
    }
    
    @Test
    public void testSubsituteNothingRequired() {
        String source = "this is a string";
        assertEquals(source, StringUtils.substitution(source, "%"));
    }
    
    @Test
    public void testSubstituteTooFewSubstitutes() {
        String source = "%0 %1";
        String[] subs = {"s1"};
        assertEquals("s1 %1", StringUtils.substitution(source, "%", subs));
    }
    
    @Test
    public void testSubstituteTooManySubstitutes() {
        String source = "%0 %1";
        String[] subs = {"s1", "s2", "s3"};
        assertEquals("s1 s2", StringUtils.substitution(source, "%", subs));
    }
    
    @Test
    public void testSubsitute() {
        String source = "text%0text%1text%2";
        String[] subs = {"a", "ab", "abc"};
        String result = StringUtils.substitution(source, "%", subs);
        assertEquals("textatextabtextabc", result);
    }
}
