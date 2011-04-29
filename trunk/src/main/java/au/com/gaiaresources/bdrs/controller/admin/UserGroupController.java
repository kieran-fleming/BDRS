package au.com.gaiaresources.bdrs.controller.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.controller.DisplayTagHelper;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.message.Message;
import au.com.gaiaresources.bdrs.model.group.Group;
import au.com.gaiaresources.bdrs.model.group.GroupDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;

@Controller
public class UserGroupController extends AbstractController {
    
    public static final String GROUP_ID = "groupId";
    public static final String TABLE_ID = "grouplist";
    public static final String PAGED_GROUP_RESULT = "pagedGroupResult";
    public static final String PAGED_USER_RESULT = "pagedUserResult";
    public static final String GROUP_TO_EDIT = "group";
    public static final String GROUP_NAME = "name";
    public static final String GROUP_DESC = "desc";
    
    public static final String BASE_URL = "/bdrs/admin/group/";
    public static final String DELETE_URL = BASE_URL + "delete.htm";
    public static final String EDIT_URL = BASE_URL + "edit.htm";
    public static final String ADD_USERS_URL = BASE_URL + "addUsers.htm";
    public static final String REMOVE_USERS_URL = BASE_URL + "removeUsers.htm";
    public static final String ADD_GROUPS_URL = BASE_URL + "addGroups.htm";
    public static final String REMOVE_GROUPS_URL = BASE_URL + "removeGroups.htm";
    public static final String LISTING_URL = BASE_URL + "listing.htm";
    public static final String CREATE_URL = BASE_URL + "create.htm";
    
    public static final String USER_ID_LIST = "userIds";
    public static final String GROUP_ID_LIST = "groupIds";
    
    private DisplayTagHelper groupListTableHelper = new DisplayTagHelper(TABLE_ID);
    
    Logger log = Logger.getLogger(getClass());
    
    @Autowired
    private GroupDAO groupDAO;
    @Autowired
    private UserDAO userDAO;
    
    @RequestMapping(value = LISTING_URL, method = RequestMethod.GET)
    public ModelAndView list(HttpServletRequest request, HttpServletResponse response) throws Exception { 
        PaginationFilter filter = groupListTableHelper.createFilter(request);
        PagedQueryResult<Group> queryResult = groupDAO.search(null, filter);
        ModelAndView mv = new ModelAndView("groupList");
        mv.addObject(PAGED_GROUP_RESULT, queryResult);
        return mv;
    }
    
    @RequestMapping(value = EDIT_URL, method = RequestMethod.GET)
    public ModelAndView view(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = GROUP_ID) Integer groupId) {
        Group groupToEdit = (groupId == null) ? new Group() : groupDAO.get(groupId);
        ModelAndView mv = createBaseEditModelAndView(groupToEdit);
                
        return mv;
    }
    
    @RequestMapping(value = EDIT_URL, method = RequestMethod.POST)
    public ModelAndView save(
            @RequestParam(value = GROUP_ID, required=true) Integer[] groupId,
            @RequestParam(value = GROUP_NAME, required=true) String name,
            @RequestParam(value = GROUP_DESC, required=true) String desc,
            HttpServletRequest request, HttpServletResponse response) {
        
        Group groupToEdit = groupDAO.get(groupId[0]);
        groupToEdit.setName(name);
        groupToEdit.setDescription(desc);
        groupDAO.updateGroup(groupToEdit);
        
        ModelAndView mv = createBaseEditModelAndView(groupToEdit);
        
        getRequestContext().addMessage(new Message("Group details successfully saved."));
        
        return mv;
    }
    
    @RequestMapping(value = CREATE_URL, method = RequestMethod.POST)
    public ModelAndView createGroup(
            HttpServletRequest request, HttpServletResponse response) {
        
        Group newgroup = groupDAO.createGroup("ENTER GROUP NAME");
        newgroup.setDescription("ENTER GROUP DESCRIPTION");
        groupDAO.updateGroup(newgroup);
        return redirectToEditPage(newgroup.getId());
    }
    
    // deleting groups seems to clean up the many-to-many tables correctly
    @RequestMapping(value = DELETE_URL, method = RequestMethod.POST)
    public ModelAndView deleteGroup(
            @RequestParam(value = GROUP_ID, required=true) Integer[] groupId,
            HttpServletRequest request, HttpServletResponse response) {
        // A cascaded delete would be good here....
        groupDAO.delete(groupId[0]);    
        return new ModelAndView(new RedirectView(LISTING_URL, true));
    }
    
    @RequestMapping(value = ADD_USERS_URL, method = RequestMethod.POST)
    public ModelAndView addUserPost(
            @RequestParam(value = GROUP_ID, required=true) Integer groupId,
            @RequestParam(value = USER_ID_LIST, defaultValue="") String userIdsToAdd,
            HttpServletRequest request, HttpServletResponse response) {
        Group groupToEdit = groupDAO.get(groupId);

        if (!StringUtils.hasLength(userIdsToAdd)) {
            return redirectToEditPage(groupToEdit.getId());
        }
        String[] ids = userIdsToAdd.split(",");
        for (String s : ids) {
            Integer userId = Integer.parseInt(s);
            User u = userDAO.getUser(userId);
            if (u != null && !groupToEdit.getUsers().contains(u)) {
                groupToEdit.getUsers().add(u);
                userDAO.updateUser(u);
            }
        }
        groupToEdit = groupDAO.updateGroup(groupToEdit);
        
        return redirectToEditPage(groupToEdit.getId());
    }

    @RequestMapping(value = ADD_GROUPS_URL, method = RequestMethod.POST)
    public ModelAndView addGroupPost(
            @RequestParam(value = GROUP_ID, required=true) Integer groupId,
            @RequestParam(value = GROUP_ID_LIST, defaultValue="") String groupIdsToAdd,
            HttpServletRequest request, HttpServletResponse response) {

        Group groupToEdit = groupDAO.get(groupId);
        
        if (!StringUtils.hasLength(groupIdsToAdd)) {
            return redirectToEditPage(groupToEdit.getId());
        }
        String[] ids = groupIdsToAdd.split(",");
        
        for (String s : ids) {
            Integer gid = Integer.parseInt(s);

            Group g = groupDAO.get(gid);
            if (g != null) {
                if (!groupToEdit.getGroups().contains(g)) {
                     if (!groupToEdit.getId().equals(g.getId())) {
                        groupToEdit.getGroups().add(g);
                        groupDAO.updateGroup(g);
                     }
                }
            }
        }
        groupToEdit = groupDAO.updateGroup(groupToEdit);
        
        return redirectToEditPage(groupToEdit.getId());
    }
    
    @RequestMapping(value = REMOVE_USERS_URL, method = RequestMethod.POST)
    public ModelAndView removeUser(
            @RequestParam(value = GROUP_ID, required=true) Integer groupId,
            @RequestParam(value = USER_ID_LIST, defaultValue="") String userIdsToRemove,
            HttpServletRequest request, HttpServletResponse response) {
        
        Group groupToEdit = groupDAO.get(groupId);

        if (!StringUtils.hasLength(userIdsToRemove)) {
            return redirectToEditPage(groupToEdit.getId());
        }
        String[] ids = userIdsToRemove.split(",");
        for (String s : ids) {
            Integer userId = Integer.parseInt(s);
            User u = userDAO.getUser(userId);
            if (u != null) {
                if (groupToEdit.getUsers().contains(u)) {
                    groupToEdit.getUsers().remove(u);
                    userDAO.updateUser(u);
                }
            }
        }
        groupToEdit = groupDAO.updateGroup(groupToEdit);
        
        return redirectToEditPage(groupToEdit.getId());
    }
    
    @RequestMapping(value = REMOVE_GROUPS_URL, method = RequestMethod.POST)
    public ModelAndView removeGroup(
            @RequestParam(value = GROUP_ID, required=true) Integer groupId,
            @RequestParam(value = GROUP_ID_LIST, defaultValue="") String groupIdsToRemove,
            HttpServletRequest request, HttpServletResponse response) {
        
        Group groupToEdit = groupDAO.get(groupId);

        if (!StringUtils.hasLength(groupIdsToRemove)) {
            return redirectToEditPage(groupToEdit.getId());
        }
        String[] ids = groupIdsToRemove.split(",");
        for (String s : ids) {
            Integer gid = Integer.parseInt(s);
            Group g = groupDAO.get(gid);
            if (g != null) {
                if (groupToEdit.getGroups().contains(g))
                {
                    groupToEdit.getGroups().remove(g);
                    groupDAO.updateGroup(g);
                }
            }
        }
        groupToEdit = groupDAO.updateGroup(groupToEdit);
        
        return redirectToEditPage(groupToEdit.getId());
    }
    
    private ModelAndView redirectToEditPage(Integer groupId) {
        StringBuilder sb = new StringBuilder();
        sb.append(EDIT_URL);
        sb.append("?");
        sb.append(GROUP_ID);
        sb.append("=");
        sb.append(groupId.toString());
        
        ModelAndView mv = new ModelAndView(new RedirectView(sb.toString(), true));
        return mv;
    }
    
    private ModelAndView createBaseEditModelAndView(Group groupToEdit) {
        ModelAndView mv = new ModelAndView("groupEdit");
        mv.addObject(GROUP_TO_EDIT, groupToEdit);
        return mv;
    }
}
