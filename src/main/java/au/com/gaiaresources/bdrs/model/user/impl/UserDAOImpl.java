package au.com.gaiaresources.bdrs.model.user.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import au.com.gaiaresources.bdrs.db.QueryOperation;
import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.db.impl.HqlQuery;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.db.impl.QueryPaginator;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.db.DeleteCascadeHandler;
import au.com.gaiaresources.bdrs.service.db.DeletionService;

@Repository
public class UserDAOImpl extends AbstractDAOImpl implements UserDAO {
    Logger logger = Logger.getLogger(UserDAOImpl.class);

    @Autowired
    private DeletionService delService;
    
    @PostConstruct
    public void init() throws Exception {
        delService.registerDeleteCascadeHandler(User.class, new DeleteCascadeHandler() {
            @Override
            public void deleteCascade(PersistentImpl instance) {
                delete((User)instance);
            }
        });
    }
    
    @Override
    public void delete(User u) {
        Set<Metadata> metaToDelete = new HashSet<Metadata>(u.getMetadata());
        u.getMetadata().clear();
        save(u);
        DeleteCascadeHandler metadataHandler = delService.getDeleteCascadeHandlerFor(Metadata.class);
        for (Metadata m : metaToDelete) {
            save(m);
            metadataHandler.deleteCascade(m);
        }
        super.delete(u);
    }
    
    @Override
    public User createUser(String name, String firstName, String lastName,
            String emailAddress, String password, String registrationKey,
            String... roles) {
        User u = new User();
        u.setName(name);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmailAddress(emailAddress);
        u.setPassword(password);
        u.setActive(false);
        u.setRegistrationKey(registrationKey);
        u.setRoles(roles);
        save(u);
        return u;
    }

    public User createUser(User u) {
        u.setActive(true);
        if (u.getRoles() == null
                || (u.getRoles() != null && u.getRoles().length == 0)) {
            // Create some default roles if it is unspecified.
            String[] roles = { "ROLE_USER" };
            u.setRoles(roles);
        }
        save(u);
        return u;
    }

    @Override
    public User getUser(String name) {
        return this.getUser(null, name);
    }
    
    @Override
    public User getRootUser(String name) {
        Session sesh = getSessionFactory().openSession();
        sesh.beginTransaction();
        sesh.disableFilter(PortalPersistentImpl.PORTAL_FILTER_NAME);
        
        Query q = sesh.createQuery("from User u where u.name = :username and :role in elements(u.roles)");
        q.setParameter("username", name);
        q.setParameter("role", Role.ROOT);
        q.setMaxResults(1);
        
        List<User> userList=  q.list();
        // Intentionally using the Session.get query format to work around the 
        // portal filter because the user in question is the root user
        // and hence has a null portal.
        User u = userList.isEmpty() ? null : (User)super.getSession().get(User.class, userList.get(0).getId());
        
        sesh.getTransaction().rollback();
        sesh.close();
        
        return u;
    }

    @SuppressWarnings("unchecked")
    @Override
    public User getUser(Session sesh, String username) {
        // This is when we're called by spring-security and miss all the transaction
        // initialization code in the interceptor
        if (sesh == null && !getSession().getTransaction().isActive()) {
            getSession().beginTransaction();
        }
        sesh = sesh == null ? getSession() : sesh;

        Query qry = sesh.createQuery("from User where name = ? order by id");
        qry.setString(0, username);
        List<User> results = qry.list();
        if (results.size() > 0) {
            return results.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int getUserCountByUsernamePrefix(String prefix) {
        org.hibernate.Query q = getSession().createQuery("select count(*) from User u where u.name like :prefix");
        q.setParameter("prefix", prefix + "%");
        List result = q.list();
        return Integer.parseInt(result.get(0).toString(), 10);
    }

    @Override
    public int countUsers() {
        org.hibernate.Query q = getSession().createQuery("select count(*) from User u");
        List result = q.list();
        return Integer.parseInt(result.get(0).toString(), 10);
    }

    @Override
    public User getUserByEmailAddress(String emailAddress) {
        return newQueryCriteria(User.class).add("emailAddress", QueryOperation.EQUAL, emailAddress).runAndGetFirst();
    }

    @Override
    public User getUser(Integer id) {
        return getByID(User.class, id);
    }

    @Override
    public List<User> getUsersByEmailAddress(String emailAddress) {
        return newQueryCriteria(User.class).add("emailAddress", QueryOperation.LIKE, emailAddress
                + "%").run();
    }

    @Override
    public List<User> getUsers() {
        return newQueryCriteria(User.class).run();
    }

    @Override
    public List<User> get(Integer[] pks) {
        Query q = getSession().createQuery("from User u where u.id in (:pks)");
        q.setParameterList("pks", pks);
        return q.list();
    }

    @Override
    public List<User> getUsersByNameSearch(String search) {
        String escapeSearch = StringEscapeUtils.escapeSql(search);

        StringBuilder builder = new StringBuilder();
        builder.append("from User u where");

        builder.append(" UPPER(name) like UPPER('%");
        builder.append(escapeSearch);
        builder.append("%')");

        builder.append(" or");
        builder.append(" UPPER(firstName) like UPPER('%");
        builder.append(escapeSearch);
        builder.append("%')");

        builder.append(" or");
        builder.append(" UPPER(lastName) like UPPER('%");
        builder.append(escapeSearch);
        builder.append("%')");

        builder.append(" order by lastName, firstName, name, id");

        return this.find(builder.toString());
    }

    @Override
    public User getUserByRegistrationKey(String registrationKey) {
        return newQueryCriteria(User.class).add("registrationKey", QueryOperation.EQUAL, registrationKey).runAndGetFirst();
    }

    @Override
    public User makeUserActive(User user, boolean active) {
        user.setActive(active);
        user = (User) merge(user);
        this.update(user);
        return user;
    }

    @Override
    public User updatePassword(User user, String password) {
        user = (User) merge(user);
        user.setPassword(password);
        return update(user);
    }

    @Override
    public User updateUser(User user) {
        Object o = merge(user);
        update((User) o);
        return (User) o;
    }

    @Override
    public PagedQueryResult<User> search(String username, String email,
            String name, PaginationFilter filter) {
        return search(username, email, name, filter, null, null);
    }

    @Override
    public PagedQueryResult<User> search(String username, String email,
            String name, PaginationFilter filter, String[] roles, String[] excludeRoles) {
        return search(username, email, name, filter, roles, excludeRoles, null);
    }
    
    
    @Override
    public PagedQueryResult<User> search(String username, String email, String name, PaginationFilter 
                                         filter, String[] roles, String[] excludeRoles, Integer groupId) {
        HqlQuery q;
        String sortTargetAlias = "u";
        if (groupId == null) {
            q = new HqlQuery("from User u ");
        } else {
            q = new HqlQuery("select distinct u from Group g ");
            q.join("g.users", "u");
            q.and(Predicate.eq("g.id", groupId));
        }

        if (StringUtils.hasLength(username)) {
            q.and(Predicate.ilike("u.name", username + "%"));
        }
        if (StringUtils.hasLength(email)) {
            q.and(Predicate.ilike("u.emailAddress", email + "%"));
        }
        if (StringUtils.hasLength(name)) {
            q.and(Predicate.ilike("u.lastName", name + "%").or(Predicate.ilike("u.firstName", name
                    + "%")));
        }

        // long winded but it works...
        if (roles != null && roles.length > 0) {
            Predicate rolePredicate = new Predicate();
            int count = 0;
            for (String r : roles) {
                if (count == 0) {
                    rolePredicate = Predicate.inElements("elements(u.roles)", r);
                } else {
                    rolePredicate = rolePredicate.or(Predicate.inElements("elements(u.roles)", r));
                }
                ++count;
            }
            q.and(rolePredicate);
        }
        
        // extra long winded but it works...
        if (excludeRoles != null && excludeRoles.length > 0) {
            Predicate excludeRolePredicate = new Predicate();
            int count = 0;
            for (String exr : excludeRoles) {
                if (count == 0) {
                    excludeRolePredicate = Predicate.notInElements("elements(u.roles)", exr);
                } else {
                    excludeRolePredicate = excludeRolePredicate.or(Predicate.notInElements("elements(u.roles)", exr));
                }
                ++count;
            }
            q.and(excludeRolePredicate);
        }
        
        return new QueryPaginator<User>().page(this.getSession(), q.getQueryString(), q.getParametersValue(), filter, sortTargetAlias);
    }
}
