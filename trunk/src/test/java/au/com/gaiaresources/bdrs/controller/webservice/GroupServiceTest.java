package au.com.gaiaresources.bdrs.controller.webservice;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.group.Group;
import au.com.gaiaresources.bdrs.model.group.GroupDAO;

public class GroupServiceTest extends AbstractControllerTest {
    
    @Autowired
    GroupDAO groupDAO;
    
    Integer a_id;
    Integer b_id;
    Integer c_id;
    Integer d_id;
    
    @Before
    public void setup() {
        Group a = groupDAO.createGroup("aaa");
        Group b = groupDAO.createGroup("bbb");
        Group c = groupDAO.createGroup("ccc");
        Group d = groupDAO.createGroup("ddd");
        
        // so i heard you liek groups in your groups...
        a.getGroups().add(b);
        a.getGroups().add(c);
        d.getGroups().add(a);
        
        groupDAO.updateGroup(a);
        groupDAO.updateGroup(b);
        groupDAO.updateGroup(c);
        groupDAO.updateGroup(d);
        
        a_id = a.getId();
        b_id = b.getId();
        c_id = c.getId();
        d_id = d.getId();
    }
    
    @Test
    public void searchGroupsWithParentGroupId() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/webservice/group/searchGroups.htm");
       
        // should have 0 matches
        request.setParameter(GroupService.PARENT_GROUP_ID, a_id.toString());
        request.setParameter(JqGridDataHelper.MAX_PER_PAGE_PARAM, "1");
        request.setParameter(JqGridDataHelper.REQUESTED_PAGE_PARAM, "2");
        request.setParameter(JqGridDataHelper.SORT_IDX_PARAM, GroupService.GROUP_NAME);
        request.setParameter(JqGridDataHelper.SORT_ORDER_PARAM, JqGridDataHelper.DESC);
        
        this.handle(request, response);
        
        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        JSONArray rowArray = json.getJSONArray("rows");
        Assert.assertEquals(1, rowArray.size());
        Assert.assertEquals(2, json.getLong("records"));
        Assert.assertEquals("bbb", ((JSONObject)rowArray.get(0)).getString("name"));        
    }
    
    @Test
    public void searchGroups() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/webservice/group/searchGroups.htm");
       
        // should have 0 matches
        request.setParameter(JqGridDataHelper.MAX_PER_PAGE_PARAM, "1");
        request.setParameter(JqGridDataHelper.REQUESTED_PAGE_PARAM, "2");
        request.setParameter(JqGridDataHelper.SORT_IDX_PARAM, GroupService.GROUP_NAME);
        request.setParameter(JqGridDataHelper.SORT_ORDER_PARAM, JqGridDataHelper.DESC);
        
        this.handle(request, response);
        
        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        JSONArray rowArray = json.getJSONArray("rows");
        Assert.assertEquals(1, rowArray.size());
        Assert.assertEquals(4, json.getLong("records"));
        Assert.assertEquals("ccc", ((JSONObject)rowArray.get(0)).getString("name"));        
    }
}
