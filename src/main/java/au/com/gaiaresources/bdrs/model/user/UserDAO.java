package au.com.gaiaresources.bdrs.model.user;

import java.util.List;

import org.hibernate.Session;

import au.com.gaiaresources.bdrs.db.TransactionDAO;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;


public interface UserDAO extends TransactionDAO {
    User createUser(String name, String firstName, String lastName, String emailAddress, String password,
                    String registrationKey, String ... roles);

    User createUser(User u);

    User getUser(String name);

    User getUser(Integer id);

    User getUserByEmailAddress(String emailAddress);
    
    List<User> getUsersByEmailAddress(String emailAddress);

    List<User> getUsers();

    User getUserByRegistrationKey(String registrationKey);

    User makeUserActive(User user, boolean active);

    User updatePassword(User user, String password);

    int getUserCountByUsernamePrefix(String prefix);

    int countUsers();

    User updateUser(User user);

    User getUser(Session sesh, String username);

    List<User> getUsersByNameSearch(String query);

    List<User> get(Integer[] pks);

    PagedQueryResult<User> search(String username, String email, String name, PaginationFilter filter);

    User getRootUser(String name);
    
    PagedQueryResult<User> search(String username, String email, String name, PaginationFilter filter, String[] roles, String[] excludeRoles);
    
    PagedQueryResult<User> search(String username, String email, String name, PaginationFilter filter, String[] roles, String[] excludeRoles, Integer groupId, Boolean active);
    
    void delete(User u);

    /**
     * Gets all of the users that have the given roles.
     * @param sesh 
     * @param strings an array of user roles to search for
     * @return A set of users that have the roles
     */
    public List<User> getUsersByRoles(Session sesh, String[] roles);
}
