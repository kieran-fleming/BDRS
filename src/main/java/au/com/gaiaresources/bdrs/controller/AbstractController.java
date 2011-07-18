package au.com.gaiaresources.bdrs.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.RequestMapping;

import au.com.gaiaresources.bdrs.db.TransactionCallback;
import au.com.gaiaresources.bdrs.servlet.RequestContext;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

public abstract class AbstractController {
    @Autowired
    private PlatformTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;
    
    protected synchronized TransactionTemplate getTransactionTemplate() {
        if (transactionTemplate == null) {
            transactionTemplate = new TransactionTemplate(transactionManager);
        }
        return transactionTemplate;
    }
    
    @SuppressWarnings("unchecked")
    protected <C> C doInTransaction(TransactionCallback<C> callback) {
        return (C) getTransactionTemplate().execute(callback);
    }
    
    protected RequestContext getRequestContext() {
        return RequestContextHolder.getContext();
    }
    
    protected String getControllerRequestMapping(Class<? extends AbstractController> controllerClass) {
        RequestMapping mapping = controllerClass.getAnnotation(RequestMapping.class);
        if (mapping != null) {
            return mapping.value()[0];
        }
        return "";
    }
    
    /**
     * Writes out our json to the http response. Contains support for JSONP
     * 
     * @param request - the http request object
     * @param response - the http response object
     * @param json - the entire json string
     * @throws IOException
     */
    protected void writeJson(HttpServletRequest request, HttpServletResponse response, String json) throws IOException {
        // support for JSONP
        if (request.getParameter("callback") != null) {
                response.setContentType("application/javascript");              
                response.getWriter().write(request.getParameter("callback") + "(");
        } else {
                response.setContentType("application/json");
        }

        // write our content
        response.getWriter().write(json);
        
        if (request.getParameter("callback") != null) {
                response.getWriter().write(");");
        }
    }
    
    protected String getRedirectHome() {
        return "redirect:/home.htm";
    }
    
    protected String getRedirectSecureHome() {
        return "redirect:/secure/home.htm";
    }
    
    protected String getRedirectAdminHome() {
        return "redirect:/admin/home.htm";
    }
    
    protected String getRedirectSecureMobileHome(){
        return "redirect:/bdrs/mobile/home.htm";
    }
    
}
