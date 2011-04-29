package au.com.gaiaresources.bdrs.controller.webservice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.model.group.Group;
import au.com.gaiaresources.bdrs.model.group.GroupDAO;

@Controller
public class GroupService extends AbstractController {
    
    @Autowired
    GroupDAO groupDAO;
    
    public static final String PARENT_GROUP_ID = "parentGroupId";
    public static final String GROUP_NAME = "name";
    public static final String GROUP_DESC = "description";
    
    @RequestMapping(value="/webservice/group/searchGroups.htm", method=RequestMethod.GET)
    public void searchUsers(
                @RequestParam(value = "parentGroupId", defaultValue = "") Integer parentGroupId,
                HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        JqGridDataHelper jqGridHelper = new JqGridDataHelper(request);       
        PaginationFilter filter = jqGridHelper.createFilter(request);
        
        PagedQueryResult<Group> queryResult = groupDAO.search(parentGroupId, filter);
        
        JqGridDataBuilder builder = new JqGridDataBuilder(jqGridHelper.getMaxPerPage(), queryResult.getCount(), jqGridHelper.getRequestedPage());

        if (queryResult.getCount() > 0) {
            for (Group group : queryResult.getList()) {
                JqGridDataRow row = new JqGridDataRow(group.getId());
                row
                .addValue("name", group.getName())
                .addValue("description", group.getDescription());
                builder.addRow(row);
            }
        }
        response.setContentType("application/json");
        response.getWriter().write(builder.toJson());
    }
}
