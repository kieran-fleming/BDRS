/**
 * 
 */
package au.com.gaiaresources.bdrs.controller.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.email.EmailService;
import au.com.gaiaresources.bdrs.model.content.ContentDAO;
import au.com.gaiaresources.bdrs.model.group.Group;
import au.com.gaiaresources.bdrs.model.group.GroupDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.service.content.ContentService;
import edu.emory.mathcs.backport.java.util.TreeSet;

/**
 * @author stephanie
 *
 */
@Controller
public class AdminEmailUsersController extends AbstractController {


    /**
     * Content access for retrieving and saving email templates.
     */
    @Autowired
    private ContentDAO contentDAO;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private GroupDAO groupDAO;
    @Autowired
    private EmailService emailService;
    
    @RequestMapping(value = "/admin/emailUsers.htm", method = RequestMethod.GET)
    public ModelAndView renderPage(HttpServletRequest request,
            HttpServletResponse response) {
        ModelAndView mav = new ModelAndView("adminEmailUsers");
        List<String> keys = contentDAO.getKeysLike("email");
        // add the default portal initializer email keys as well if not present
        Set<String> uniqueKeys = new TreeSet(keys);
        uniqueKeys.addAll(getItemsStartingWith(ContentService.CONTENT.keySet(),"email"));
        mav.addObject("keys", uniqueKeys);
        return mav;
    }

    @RequestMapping(value = "/admin/sendMessage.htm", method = RequestMethod.POST)
    public void sendMessage(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "to", required = true) String to,
            @RequestParam(value = "subject", required = true) String subject,
            @RequestParam(value = "content", required = true) String content) {
        // send the message to each of the addresses in the to field
        String[] addresses = to.split(",");
        User fromUser = getRequestContext().getUser();
        String from = fromUser.getEmailAddress();
        
        for (String string : addresses) {
            String address = string.trim();
            User toUser = userDAO.getUserByEmailAddress(address);
            Map<String, Object> subParams = createSubstitutionParams(toUser, fromUser, content);
            subParams.put("bdrs.application.url", request.getContextPath() + "/portal/" + getRequestContext().getPortal().getId() + "/home.htm");
            subParams.put("portal.id", getRequestContext().getPortal().getId());
            emailService.sendMessage(address, from, subject, content, subParams);
        }
    }
    
    private Map<String, Object> createSubstitutionParams(User toUser,
            User fromUser, String content) {
        Map<String, Object> subParams = new HashMap<String, Object>();
        // find each variable in the message and replace with appropriate user variable
        int start = content.indexOf("${"), end = content.indexOf("}", start);
        for (; 
                 start > 0 && start < content.length() && end > 0 && end > start && end < content.length(); 
                 start = content.indexOf("${", end+1), end = content.indexOf("}", start)) {
            String replaceName = content.substring(start+2, end);
            
            // get the actual variable name
            String varName = replaceName.lastIndexOf(".") > 0 ? replaceName.substring(replaceName.lastIndexOf(".")+1) : replaceName;
            // it is a user field, get the prefix and add the user object
            String prefix = replaceName.lastIndexOf(".") > 0 ? replaceName.substring(0, replaceName.lastIndexOf(".")) : replaceName;
            if (varName.matches("firstName|lastName|emailAddress|name")) {
                // if it contains another ".", it is not just a simple user and may be an expert
                if (!prefix.contains(".")) {
                    if (prefix.matches("admin|from|teacher"))
                        subParams.put(prefix, fromUser);
                    else
                        subParams.put(prefix, toUser);
                }
            } else if (varName.matches("groups")) {
                List<Group> groups = groupDAO.getGroupsForUser(toUser);
                subParams.put(varName, groups);
            } else if (varName.matches("studentName|fullName")) {
                String fullname;
                fullname = toUser.getFirstName();
                if(toUser.getLastName() != null && !toUser.getLastName().isEmpty()) {
                    fullname = fullname + " " + toUser.getLastName();
                }
                subParams.put(varName, fullname);
            }
        }
        return subParams;
    }

    /**
     * Utility method for finding all the items in a Collection that start 
     * with the given prefix.
     * @param keySet
     * @param string
     * @return
     */
    private List<String> getItemsStartingWith(Collection<? extends String> items,
            String prefix) {
        List<String> foundItems = new ArrayList<String>();
        for (String string : items) {
            if (string.startsWith(prefix))
                foundItems.add(string);
        }
        return foundItems;
    }
}
