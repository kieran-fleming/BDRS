package au.com.gaiaresources.bdrs.util;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestBeanUtils {
    @Test
    public void testSimpleGetProperty() throws Exception {
        X x = new X("Hello");
        assertEquals("Hello", BeanUtils.extractProperty(x, "property"));
        assertEquals("Hello", BeanUtils.extractProperty(x, "otherProperty"));
    }
    
    @Test
    public void testGetOneAwayProperty() throws Exception {
        X x = new X("Hello");
        Y y = new Y(x);
        assertEquals("Hello", BeanUtils.extractProperty(y, "x.property"));
        assertEquals("Hello", BeanUtils.extractProperty(y, "x.otherProperty"));
    }
    
    @Test
    public void testGetTwoAwayProperty() throws Exception {
        X x = new X("Hello");
        Y y = new Y(x);
        Z z = new Z(x, y);
        assertEquals("Hello", BeanUtils.extractProperty(z, "x.property"));
        assertEquals("Hello", BeanUtils.extractProperty(z, "y.x.otherProperty"));
    }
    
    @Ignore
    private class X {
        private String p;
        X(String p) {
            this.p = p;
        }
        @SuppressWarnings("unused")
		public String getProperty() {
            return p;
        }
        @SuppressWarnings("unused")
		private String getOtherProperty() {
            return p;
        }
    }
    
    @Ignore
    private class Y {
        private X x;
        Y(X x) {
            this.x = x;
        }
        @SuppressWarnings("unused")
		public X getX() {
            return x;
        }
    }
    
    @Ignore
    private class Z {
        private X x;
        private Y y;
        Z(X x, Y y) {
            this.x = x;
            this.y = y;
        }
        @SuppressWarnings("unused")
		private X getX() {
            return x;
        }
        @SuppressWarnings("unused")
		private Y getY() {
            return y;
        }
    }
}
