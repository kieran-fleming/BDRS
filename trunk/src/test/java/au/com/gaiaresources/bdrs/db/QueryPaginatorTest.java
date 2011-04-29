package au.com.gaiaresources.bdrs.db;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.util.StringUtils;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.db.impl.HqlQuery;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.db.impl.QueryPaginator;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter.SortOrder;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;

public class QueryPaginatorTest extends AbstractControllerTest {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private SessionFactory sessionFactory;
    
    private Logger log = Logger.getLogger(this.getClass());

    private static String[][] testnames = { { "Aaaaaa", "Hughes" },
            { "Zzzzzz", "Haugen" }, { "Zimmer", "Magnusson" },
            { "Zoller", "Reid" }, { "Zerwe", "Jansson" },
            { "Zindel", "Jensen" }, { "Zenichowski", "Pettersson" },
            { "Zierau", "Smith" }, { "Zeisler", "Carlsson" },
            { "Yadon", "Haugen" }, { "Yeargin", "Halvorsen" },
            { "Zornes", "Andersen" } };

    @Before
    public void setup() throws Exception {

        // note there is 1 test user called admin

        // create a test user
        // Create User and the user's Locations
        // Ideally this test would not use userDAO but meh...it's close enough
        PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
        String emailAddr = "abigail.ambrose@example.com";
        String encodedPassword = passwordEncoder.encodePassword("password", null);
        String registrationKey = passwordEncoder.encodePassword(au.com.gaiaresources.bdrs.util.StringUtils.generateRandomString(10, 50), emailAddr);

        for (String[] name : testnames) {
            String first = name[0];
            String last = name[1];
            userDAO.createUser(first.toLowerCase(), first, last, fakeEmail(first, last), encodedPassword, registrationKey, new String[] { "ROLE_USER" });
        }
    }

    private String fakeEmail(String first, String last) {
        return first.toLowerCase() + "@" + last.toLowerCase() + ".com.au";
    }

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    private PagedQueryResult<User> critSearch(String username, String email,
            String name, PaginationFilter filter) {
        Criteria crit = this.getSession().createCriteria(User.class);
        if (StringUtils.hasLength(username)) {
            crit.add(Restrictions.ilike("name", username, MatchMode.START));
        }
        if (StringUtils.hasLength(email)) {
            crit.add(Restrictions.ilike("emailAddress", email, MatchMode.START));
        }
        if (StringUtils.hasLength(name)) {
            crit.add(Restrictions.or(Restrictions.ilike("lastName", name, MatchMode.START), Restrictions.ilike("firstName", name, MatchMode.START)));
        }
        return new QueryPaginator<User>().page(crit, filter);
    }

    @Test
    public void testCriteriaPaging() throws Exception {
        PaginationFilter filter = new PaginationFilter(0, 1);
        filter.addSortingCriteria("name", SortOrder.ASCENDING);

        PagedQueryResult<User> result = critSearch("", "", "", filter);
        Assert.assertEquals(14, result.getCount());
        Assert.assertEquals(1, result.getList().size());
        Assert.assertEquals("Aaaaaa", result.getList().get(0).getFirstName());
    }

    @Test
    public void testHqlPaging() throws Exception {
        PaginationFilter filter = new PaginationFilter(0, 1);
        filter.addSortingCriteria("name", SortOrder.ASCENDING);

        PagedQueryResult<User> result = hqlSearch("", "", "", filter);
        Assert.assertEquals(14, result.getCount());
        Assert.assertEquals(1, result.getList().size());
        Assert.assertEquals("Aaaaaa", result.getList().get(0).getFirstName());
    }

    // mwahaha, sneaking a test for object HqlQuery in here...
    @Test
    public void testHqlQuery() throws Exception {
        PaginationFilter filter = new PaginationFilter(0, 1);
        filter.addSortingCriteria("name", SortOrder.ASCENDING);

        PagedQueryResult<User> result = hqlSearch("Aaaaaa", "", "Hughes", filter);
        Assert.assertEquals(1, result.getCount());
        Assert.assertEquals(1, result.getList().size());
        Assert.assertEquals("Aaaaaa", result.getList().get(0).getFirstName());
    }

    public PagedQueryResult<User> hqlSearch(String username, String email,
            String name, PaginationFilter filter) {

        HqlQuery q = new HqlQuery("from User u ");

        if (StringUtils.hasLength(username)) {
            q.and(Predicate.ilike("u.name", username));
        }
        if (StringUtils.hasLength(email)) {
            q.and(Predicate.ilike("u.emailAddress", email));
        }
        if (StringUtils.hasLength(name)) {
            q.and(Predicate.ilike("u.lastName", name).or(Predicate.ilike("u.firstName", name)));
        }
        return new QueryPaginator<User>().page(this.getSession(), q.getQueryString(), q.getParametersValue(), filter);

    }
}
