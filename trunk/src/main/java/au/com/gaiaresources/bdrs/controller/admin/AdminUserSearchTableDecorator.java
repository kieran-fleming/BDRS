package au.com.gaiaresources.bdrs.controller.admin;

import org.displaytag.decorator.TableDecorator;

import au.com.gaiaresources.bdrs.controller.user.UserProfileController;
import au.com.gaiaresources.bdrs.model.user.User;

import java.io.UnsupportedEncodingException;

public class AdminUserSearchTableDecorator extends TableDecorator {
 // decorator for displaytag table
    public String getEmailAddress() throws UnsupportedEncodingException
    {
        User user = (User)getCurrentRowObject();
        String email = user.getEmailAddress();
        String encodedEmail = email.replace("\"", "\\\"");

        StringBuilder b = new StringBuilder();
        b.append("<a href=\"mailto:");
        b.append(encodedEmail);
        b.append("\">");
        b.append(encodedEmail);
        b.append("</a>");
        return b.toString();
    }
    
    public String getActionLinks()
    {
        String contextpath = this.getPageContext().getServletContext().getContextPath();
        User user = (User)getCurrentRowObject();
        Integer id = user.getId();
        
        StringBuilder b = new StringBuilder();
        b.append("<a href=\"");
        b.append(contextpath + "/admin/profile.htm?" + UserProfileController.USER_ID + "=" + id.toString()); // URL to redirect to...
        b.append("\">");
        b.append("Edit");
        b.append("</a>");
        return b.toString();
    }
}
