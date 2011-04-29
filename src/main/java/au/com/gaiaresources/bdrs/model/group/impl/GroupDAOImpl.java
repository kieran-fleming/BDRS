package au.com.gaiaresources.bdrs.model.group.impl;

import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.db.impl.HqlQuery;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.db.impl.QueryPaginator;
import au.com.gaiaresources.bdrs.model.group.Group;
import au.com.gaiaresources.bdrs.model.group.GroupDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.user.User;

/**
 * @author aj
 */
@SuppressWarnings("unchecked")
@Repository
public class GroupDAOImpl extends AbstractDAOImpl implements GroupDAO {
    Logger log = Logger.getLogger(GroupDAOImpl.class);

    @Autowired
    SessionFactory sessionFactory;

    public Group createGroup(String name) {
        Group group = new Group();
        group.setName(name);

        save(group);
        return group;
    }

    public Group updateGroup(Group group) {
        Object o = merge(group);
        update((Group)o);
        return (Group) o;
//		return update(group);
    }

    public List<Group> getGroupsForAdmin(User user) {
        return find(
                "select g from Group g, User u where u.id in (select id from g.admins) and u = ? order by g.createdAt",
                user);
    }

    @Override
    public List<Group> getGroupsForUser(User user) {
        return find(
                "select g from Group g, User u where u.id in (select id from g.users) and u = ? order by g.createdAt",
                user);
    }

    public Group getClassForUser(User user) {
        if(user == null) {
            return null;
        }

        List<Group> g = find("select g from Group g, User u where u.id in (select id from g.users) and u = ?", user);

        if(g.isEmpty()) {
            return null;
        }
        else {
            if(g.size() > 1) {
                log.warn("Multiple groups returned but only using first group.");
            }
            List<Group> c = find("select c from Group c where ? in (select id from c.groups)", g.get(0));

            if(c.isEmpty()) {
                return null;
            } else {
                if(g.size() > 1) {
                    log.warn("Multiple classes returned but only using first class.");
                }
                return c.get(0);
            }
        }
    }

    @Override
    public Group getGroupForUsername(String username) {
        if(username != null && !username.isEmpty()) {
            List<Group> g = find("select g from Group g join g.users u where u.name = ?", username);
            if(g.isEmpty()) {
                return null;
            } else {
                if(g.size() > 1) {
                    log.warn("Multiple groups returned for given username \""+username+"\". Returning the first.");
                }
                return g.get(0);
            }
        }
        else {
            return null;
        }
    }

    @Override
    public Group getClassForGroup(Group group) {
        if(group == null) {
            return null;
        }

        List<Group> c = find("select c from Group c, Group g where g.id in (select id from c.groups) and g = ?", group);
        if(c.isEmpty()) {
            return null;
        }
        else {
            if(c.size() > 1) {
                // This is an abnormal if not degenerate case
                log.error("Multiple classes matched but only returning the first one");
            }
            return c.get(0);
        }
    }

    public Group get(Integer id) {
        return getByID(Group.class, id);
    }

    public List<Group> get(Integer[] ids) {
        if(ids.length == 0) {
            return Collections.emptyList();
        }
        //return find("from Group g where g.id in ?", ids);
        Query query = getSession().createQuery ("select g from Group g where g.id in (:ids)");
        query.setParameterList("ids", ids, Hibernate.INTEGER);
        return query.list();
    }

    public List<Group> getAllGroups() {
        return find("from Group g order by g.createdAt");
    }

    @Override
    public List<Survey> getSurveyForGroup(Group g) {
        return find("select s from Survey s, Group g where g.id in (select id from s.groups) and g = ?", g);
    }

    @Override
    public void delete(int id) {
        // This bit works. It's frakked up, but it works.
        Group g = get(id);
        g = (Group)merge(g);

        Group klass = getClassForGroup(g);
        if(klass != null) {
            klass.getGroups().remove(g);
            klass = (Group)merge(klass);
            update(klass);
        }

        List<Survey> surveyList = getSurveyForGroup(g);
        for(Survey s : surveyList) {
            s.getGroups().remove(g);
            update(s);
        }


        g.setUsers(new TreeSet<User>());
        g.setAdmins(new TreeSet<User>());
        g.setGroups(new TreeSet<Group>());
        update(g);
        delete(g);
    }

    @Override
    public Group getGroupByName(Session sesh, String groupName) {
        if(sesh == null) {
            sesh = getSession();
        }
        
        List<Group> groups = find(sesh, "select g from Group g where g.name = ?", groupName);
        if(groups.isEmpty()) {
            return null;
        } else {
            if(groups.size() > 1) {
                log.warn("Multiple groups with the same name found. Returning the first");

            }
            return groups.get(0);
        }
    }

    @Override
    public Group getGroupByName(String groupName) {
        return this.getGroupByName(null, groupName);
    }

    @Override
    public List<Group> getGroupsByNameSearch(String search) {
        String escapeSearch = StringEscapeUtils.escapeSql(search);

        StringBuilder builder = new StringBuilder();
        builder.append("from Group g where");

        builder.append(" UPPER(name) like UPPER('%");
        builder.append(escapeSearch);
        builder.append("%')");

        builder.append(" order by name, id");

        return this.find(builder.toString());
    }

    @Override
    public PagedQueryResult<Group> search(Integer parentGroupId, PaginationFilter filter) {
        HqlQuery q;
        String sortTargetAlias = null;
        if (parentGroupId == null) {
            q = new HqlQuery("from Group g ");
            sortTargetAlias = "g";
        } else {
            q = new HqlQuery("select distinct sg from Group g ");
            sortTargetAlias = "sg";
            q.join("g.groups", "sg");
            q.and(Predicate.eq("g.id", parentGroupId));
        }
        //log.debug(q.getQueryString());
        return new QueryPaginator<Group>().page(this.getSession(), q.getQueryString(), q.getParametersValue(), filter, sortTargetAlias);
    }
}
