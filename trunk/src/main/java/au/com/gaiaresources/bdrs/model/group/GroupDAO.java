package au.com.gaiaresources.bdrs.model.group;

import java.util.List;

import org.hibernate.Session;

import au.com.gaiaresources.bdrs.db.TransactionDAO;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.user.User;


public interface GroupDAO extends TransactionDAO {

    /**
     * Creates a new group with the given name
     * @param name for the new group
     * @return the persistent instance of the group.
     */
    public Group createGroup(String name);

    /**
     * Updates the persistent instance of this group.
     * @param group
     * @return the persistent instance of the group.
     */
    public Group updateGroup(Group group);

    /**
     * Returns a group with the given ID
     * @param id the id of the group to retrieve
     * @return the group with the corresponding ID.
     */
    public Group get(Integer id);

    /**
     * Returns the list of groups with ids in the array provided.
     * @param ids the ids of the groups to be returned.
     * @return the groups with the corresponding ids.
     */
    public List<Group> get(Integer[] ids);

    /**
     * Returns the groups that this user has admin privileges for
     * @param user the group administrator
     * @return a list of groups that this user administrates.
     */
    List<Group> getGroupsForAdmin(User user);

    /**
     * Returns the groups that this user is a member.
     * @param user the group member
     * @return a list of groups that contains this user.
     */
    List<Group> getGroupsForUser(User user);

    /**
     * Returns the class containing this user.
     * @param user the user
     * @return a class containing this user or null if this user is not in a class.
     */
    Group getClassForUser(User user);

    /**
     * Returns all groups.
     * @return
     */
    List<Group> getAllGroups();

    /**
     * Deletes the specified group from the database.
     * @param id the primary key of the group to delete.
     */
    void delete(int id);

    /**
     * Returns a group that contains a user with the specified username.
     * @param username the username of the user in the group.
     * @return the group containing the user with the specified username.
     */
    Group getGroupForUsername(String username);

    /**
     * Returns the class that contains the specified group.
     * @param group the group within the returned class.
     * @return the class containing the specified group.
     */
    Group getClassForGroup(Group group);

    /**
     * Returns the group with the specified name, or null if one does not exist.
     * @param sesh the session to use when executing this query.
     * @param groupName the name of the group
     * @return the group with the specified name or null if one does not exist.
     */
    Group getGroupByName(Session sesh, String groupName);

    /**
     * @see #getGroupByName(Session, String)
     */
    Group getGroupByName(String groupName);

    /**
     * Retrieves the list of surveys that are associated with this group.
     * @param group the group associated with the surveys.
     * @return the list of surveys associated with this group.
     */
    List<Survey> getSurveyForGroup(Group group);

    /**
     * Retrieves a list of Groups that have a name containing the specified
     * search text.
     * @param search the text to search within the group name.
     * @return the list of Groups with a matching name.
     */
    List<Group> getGroupsByNameSearch(String search);

    PagedQueryResult<Group> search(Integer parentGroupId, PaginationFilter filter);
}
