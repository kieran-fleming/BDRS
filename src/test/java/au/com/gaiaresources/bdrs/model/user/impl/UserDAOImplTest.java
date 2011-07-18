package au.com.gaiaresources.bdrs.model.user.impl;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;

import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter.SortOrder;
import au.com.gaiaresources.bdrs.model.group.Group;
import au.com.gaiaresources.bdrs.model.group.GroupDAO;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.test.AbstractTransactionalTest;
import au.com.gaiaresources.bdrs.util.StringUtils;

public class UserDAOImplTest extends AbstractTransactionalTest      {

    @Autowired
    protected UserDAO userDAO;
    @Autowired
    private MetadataDAO metaDAO;
    @Autowired
    private GroupDAO groupDAO;
    
    Integer specialId;

    private static String[][] testnames = {
            { "RoleRoot", "Hughes", Role.ROOT },
            { "RoleAdmin", "Magnusson", Role.ADMIN },
            { "RoleSupervisor", "Jansson", Role.SUPERVISOR },
            { "RolePower", "Pettersson", Role.POWERUSER },
            { "RoleUser", "Carlsson", Role.USER },
            { "None", "Carlsson", "SOMEOTHERROLE" } };

    @Before
    public void setup() {
        // create a test user
        // Create User and the user's Locations
        PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
        String emailAddr = "abigail.ambrose@example.com";
        String encodedPassword = passwordEncoder.encodePassword("password", null);
        String registrationKey = passwordEncoder.encodePassword(StringUtils.generateRandomString(10, 50), emailAddr);

        for (String[] name : testnames) {
            String first = name[0];
            String last = name[1];
            userDAO.createUser(first.toLowerCase(), first, last, fakeEmail(first, last), encodedPassword, registrationKey, new String[] { name[2] });
        }
        Group special = groupDAO.createGroup("special");
        Set<User> specialUsers = new TreeSet<User>();
        specialUsers.add(userDAO.getUser("RoleUser".toLowerCase()));
        specialUsers.add(userDAO.getUser("RolePower".toLowerCase()));
        special.setUsers(specialUsers);
        groupDAO.updateGroup(special);
        specialId = special.getId();
    }

    private String fakeEmail(String first, String last) {
        return first.toLowerCase() + "@" + last.toLowerCase() + ".com.au";
    }

    private Boolean isUsernameInList(String name, List<User> users) {
        for (User u : users) {
            if (u.getName().toLowerCase().equals(name.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testRoleSearch() {
        PagedQueryResult<User> result = userDAO.search(null, null, null, null, Role.getRolesLowerThan(Role.ROOT), null);
        Assert.assertTrue(isUsernameInList("RoleAdmin", result.getList()));
        Assert.assertTrue(isUsernameInList("RoleSupervisor", result.getList()));
        Assert.assertTrue(isUsernameInList("RolePower", result.getList()));
        Assert.assertTrue(isUsernameInList("RoleUser", result.getList()));
        Assert.assertFalse(isUsernameInList("RoleRoot", result.getList()));
    }

    @Test
    public void testRoleSearchWithUsername() {
        PagedQueryResult<User> result = userDAO.search("role", null, null, null, Role.getRolesLowerThan(Role.ROOT), null);
        Assert.assertTrue(isUsernameInList("RoleAdmin", result.getList()));
        Assert.assertTrue(isUsernameInList("RoleSupervisor", result.getList()));
        Assert.assertTrue(isUsernameInList("RolePower", result.getList()));
        Assert.assertTrue(isUsernameInList("RoleUser", result.getList()));
        Assert.assertFalse(isUsernameInList("RoleRoot", result.getList()));
    }

    @Test
    public void testRoleSearchWithFullname() {
        PagedQueryResult<User> result = userDAO.search(null, null, "role", null, Role.getRolesLowerThan(Role.ROOT), null);
        Assert.assertTrue(isUsernameInList("RoleAdmin", result.getList()));
        Assert.assertTrue(isUsernameInList("RoleSupervisor", result.getList()));
        Assert.assertTrue(isUsernameInList("RolePower", result.getList()));
        Assert.assertTrue(isUsernameInList("RoleUser", result.getList()));
        Assert.assertFalse(isUsernameInList("RoleRoot", result.getList()));
    }

    @Test
    public void testSingleRoleSearch() {
        PagedQueryResult<User> result = userDAO.search(null, null, null, null, Role.getRolesLowerThanOrEqualTo(Role.USER), null);
        Assert.assertFalse(isUsernameInList("RoleAdmin", result.getList()));
        Assert.assertFalse(isUsernameInList("RoleSupervisor", result.getList()));
        Assert.assertFalse(isUsernameInList("RolePower", result.getList()));
        Assert.assertTrue(isUsernameInList("RoleUser", result.getList()));
        Assert.assertFalse(isUsernameInList("RoleRoot", result.getList()));
    }

    @Test
    public void testNoRoleSearch() {
        PagedQueryResult<User> result = userDAO.search("role", null, null, null, null, null);
        Assert.assertTrue(isUsernameInList("RoleAdmin", result.getList()));
        Assert.assertTrue(isUsernameInList("RoleSupervisor", result.getList()));
        Assert.assertTrue(isUsernameInList("RolePower", result.getList()));
        Assert.assertTrue(isUsernameInList("RoleUser", result.getList()));
        Assert.assertTrue(isUsernameInList("RoleRoot", result.getList()));
    }

    @Test
    public void testGetAllNoArgs() {
        PagedQueryResult<User> result = userDAO.search(null, null, null, null, null, null);

        Assert.assertTrue(isUsernameInList("RoleAdmin", result.getList()));
        Assert.assertTrue(isUsernameInList("RoleSupervisor", result.getList()));
        Assert.assertTrue(isUsernameInList("RolePower", result.getList()));
        Assert.assertTrue(isUsernameInList("RoleUser", result.getList()));
        Assert.assertTrue(isUsernameInList("RoleRoot", result.getList()));
    }

    @Test
    public void testExcludeRole() {
        User root = userDAO.getUser("roleroot");
        root.setRoles(new String[] { Role.ADMIN, Role.ROOT });
        userDAO.updateUser(root);

        PagedQueryResult<User> result = userDAO.search(null, null, null, null, new String[] {
                Role.ADMIN, Role.SUPERVISOR, Role.POWERUSER, Role.USER }, new String[] { Role.ROOT });

        Assert.assertTrue(isUsernameInList("RoleAdmin", result.getList()));
        Assert.assertTrue(isUsernameInList("RoleSupervisor", result.getList()));
        Assert.assertTrue(isUsernameInList("RolePower", result.getList()));
        Assert.assertTrue(isUsernameInList("RoleUser", result.getList()));
        Assert.assertFalse(isUsernameInList("RoleRoot", result.getList()));
    }
    
    @Test
    public void testExcludeRoleOverlap() {
        PagedQueryResult<User> result = userDAO.search(null, null, null, null, new String[] {
                Role.ADMIN, Role.SUPERVISOR, Role.POWERUSER, Role.USER, Role.ROOT }, new String[] { Role.ROOT });

        Assert.assertTrue(isUsernameInList("RoleAdmin", result.getList()));
        Assert.assertTrue(isUsernameInList("RoleSupervisor", result.getList()));
        Assert.assertTrue(isUsernameInList("RolePower", result.getList()));
        Assert.assertTrue(isUsernameInList("RoleUser", result.getList()));
        Assert.assertFalse(isUsernameInList("RoleRoot", result.getList()));
    }
    
    // indirectly testing AbstractDAOImpl.count()
    @Test
    public void testCount() {
        Assert.assertEquals(8, userDAO.count(User.class).intValue());
    }
    
    @Test
    public void testDeleteCascade() {
        // metadata should be empty at this point...
        Assert.assertEquals(0, metaDAO.count(Metadata.class).intValue());
        User u = userDAO.getUser("roleroot");
        Metadata m = new Metadata("hello", "world");
        metaDAO.save(m);
        u.addMetaDataObj(m);
        userDAO.updateUser(u);
        
        Assert.assertEquals(1, metaDAO.count(Metadata.class).intValue());
        
        userDAO.delete(u);
        
        Assert.assertEquals(0, metaDAO.count(Metadata.class).intValue());
    }
    
    @Test
    public void testGetByGroup() {
        PaginationFilter filter = new PaginationFilter(0, 10);
        filter.addSortingCriteria("name", SortOrder.ASCENDING);
        PagedQueryResult<User> result = userDAO.search(null, null, null, filter, null, null, specialId);
        Assert.assertEquals(2, result.getCount());
    }
    
    // testing whether the sort target is correctly set....
    @Test
    public void testGetByNoGroupWithFilter() {
        PaginationFilter filter = new PaginationFilter(0, 10);
        filter.addSortingCriteria("name", SortOrder.ASCENDING);
        PagedQueryResult<User> result = userDAO.search("role", null, null, filter, null, null, null);
        Assert.assertEquals(5, result.getCount());
    }
}
