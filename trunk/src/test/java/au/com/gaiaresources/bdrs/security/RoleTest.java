package au.com.gaiaresources.bdrs.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import junit.framework.TestCase;

public class RoleTest extends TestCase {
    
    private String HIGHEST = Role.ROOT;
    private String SECOND_HIGHEST = Role.ADMIN;
    
    private String LOWEST = Role.USER;
    private String SECOND_LOWEST = Role.POWERUSER;
    
    private String[] allRoles = Role.getAllRoles();
    
    @Test
    public void testTotalNumRoles() {
        // just to make sure the rest of the tests don't fail because we hit an edge case in total # of roles
        Assert.assertTrue(allRoles.length >= 4);
    }
    
    @Test
    public void testLowerThan() {
        String[] result = Role.getRolesLowerThan(HIGHEST);
        Assert.assertEquals(allRoles.length - 1, result.length);
        String[] result2 = Role.getRolesLowerThan(LOWEST);
        Assert.assertEquals(0, result2.length);
        String[] result3 = Role.getRolesLowerThan(SECOND_LOWEST);
        Assert.assertEquals(1, result3.length);
        
        //ROOT, ADMIN, SUPERVISOR, POWERUSER, USER
        Assert.assertEquals(Role.ADMIN, result[0]);
        Assert.assertEquals(Role.SUPERVISOR, result[1]);
        Assert.assertEquals(Role.POWERUSER, result[2]);
        Assert.assertEquals(Role.USER, result[3]);
    }
    
    @Test
    public void testLowerThanOrEqualTo() {
        String[] result = Role.getRolesLowerThanOrEqualTo(HIGHEST);
        Assert.assertEquals(allRoles.length, result.length);
        String[] result2 = Role.getRolesLowerThanOrEqualTo(LOWEST);
        Assert.assertEquals(1, result2.length);
        String[] result3 = Role.getRolesLowerThanOrEqualTo(SECOND_LOWEST);
        Assert.assertEquals(2, result3.length);
    }
    
    @Test
    public void testHigherThan() {
        String[] result = Role.getRolesHigherThan(LOWEST);
        Assert.assertEquals(allRoles.length - 1, result.length);
        String[] result2 = Role.getRolesHigherThan(HIGHEST);
        Assert.assertEquals(0, result2.length);
        String[] result3 = Role.getRolesHigherThan(SECOND_HIGHEST);
        Assert.assertEquals(1, result3.length);
        
        // ROOT, ADMIN, SUPERVISOR, POWERUSER, USER
        Assert.assertEquals(Role.ROOT, result[0]);
        Assert.assertEquals(Role.ADMIN, result[1]);
        Assert.assertEquals(Role.SUPERVISOR, result[2]);
        Assert.assertEquals(Role.POWERUSER, result[3]);
    }
    
    @Test
    public void testHigherThanOrEqualTo() {
        String[] result = Role.getRolesHigherThanOrEqualTo(LOWEST);
        Assert.assertEquals(allRoles.length, result.length);
        String[] result2 = Role.getRolesHigherThanOrEqualTo(HIGHEST);
        Assert.assertEquals(1, result2.length);
        String[] result3 = Role.getRolesHigherThanOrEqualTo(SECOND_HIGHEST);
        Assert.assertEquals(2, result3.length);
    }
    
    @Test
    public void testGetHighest() {
        Assert.assertEquals(Role.ROOT, Role.getHighestRole(allRoles));
    }
    
    @Test
    public void testGetLowest() {
        Assert.assertEquals(Role.USER, Role.getLowestRole(allRoles));
    }
    
    @Test
    public void testIsHigherThanOrEqualTo() {
        Assert.assertTrue(Role.isRoleHigherThanOrEqualTo(Role.ROOT, Role.ADMIN));
        Assert.assertTrue(Role.isRoleHigherThanOrEqualTo(Role.ADMIN, Role.ADMIN));
        Assert.assertFalse(Role.isRoleHigherThanOrEqualTo(Role.SUPERVISOR, Role.ADMIN));
    }

    @Test
    public void testIsHigher() {
        Assert.assertTrue(Role.isRoleHigher(Role.ROOT, Role.ADMIN));
        Assert.assertFalse(Role.isRoleHigher(Role.ADMIN, Role.ADMIN));
        Assert.assertFalse(Role.isRoleHigher(Role.SUPERVISOR, Role.ADMIN));
    }
}
