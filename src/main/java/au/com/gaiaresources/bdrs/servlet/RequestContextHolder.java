package au.com.gaiaresources.bdrs.servlet;

import org.apache.log4j.Logger;


public class RequestContextHolder {
    
    private static ThreadLocal<RequestContext> context = new ThreadLocal<RequestContext>();
    
    public static RequestContext getContext() {
        if (context.get() == null) {
            return new RequestContext();
        }
        return context.get();
    }
    
    public static void clear() {
        context.remove();
    }
    
    public static void set(RequestContext c) {
        context.set(c);
    }
}
