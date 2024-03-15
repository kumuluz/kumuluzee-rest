package com.kumuluz.ee.rest.test;

import com.kumuluz.ee.rest.beans.QueryFilter;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.enums.FilterOperation;
import com.kumuluz.ee.rest.test.entities.User;
import com.kumuluz.ee.rest.test.utils.JpaUtil;
import com.kumuluz.ee.rest.utils.JPAUtils;
import jakarta.persistence.EntityManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Tilen Faganel
 */
@RunWith(Parameterized.class)
public class JPAUtilsCriteriaFilterTest {

    @Parameterized.Parameters
    public static Collection<EntityManager> data() {

        JpaUtil jpaUtil = JpaUtil.getInstance();

        return Arrays.asList(
                jpaUtil.getEclipselinkEntityManager(),
                jpaUtil.getHibernateEntityManager()
        );
    }

    @Parameterized.Parameter
    public EntityManager em;

    @Test
    public void testQueryWithoutCriteriaFilter() {

        List<User> users = JPAUtils.queryEntities(em, User.class, new QueryParameters());

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
    }

    @Test
    public void testQueryCriteriaFilter() {

        List<User> users = JPAUtils.queryEntities(em, User.class,
                (p, cb, r) -> cb.and(p, cb.equal(r.get("firstname"), "Antonio")));

        Assert.assertNotNull(users);
        Assert.assertEquals(3, users.size());

        Assert.assertNotNull(users.get(0));
        Assert.assertEquals("Antonio", users.get(0).getFirstname());
        Assert.assertNotNull(users.get(1));
        Assert.assertEquals("Antonio", users.get(1).getFirstname());
        Assert.assertNotNull(users.get(2));
        Assert.assertEquals("Antonio", users.get(2).getFirstname());

        Long usersCount = JPAUtils.queryEntitiesCount(em, User.class,
                (p, cb, r) -> cb.and(p, cb.equal(r.get("firstname"), "Antonio")));

        Assert.assertNotNull(usersCount);
        Assert.assertEquals(3, usersCount.longValue());
    }

    @Test
    public void testQueryCriteriaFilterWithParamsAnd() {

        QueryFilter qf = new QueryFilter();
        qf.setField("lastname");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("Turner");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = JPAUtils.queryEntities(em, User.class, q,
                (p, cb, r) -> cb.and(p, cb.equal(r.get("firstname"), "Antonio")));

        Assert.assertNotNull(users);
        Assert.assertEquals(1, users.size());

        Assert.assertNotNull(users.get(0));
        Assert.assertEquals("Antonio", users.get(0).getFirstname());
        Assert.assertEquals("Turner", users.get(0).getLastname());

        Long usersCount = JPAUtils.queryEntitiesCount(em, User.class, q,
                (p, cb, r) -> cb.and(p, cb.equal(r.get("firstname"), "Antonio")));

        Assert.assertNotNull(usersCount);
        Assert.assertEquals(1, usersCount.longValue());
    }

    @Test
    public void testQueryCriteriaFilterWithParamsOr() {

        QueryFilter qf = new QueryFilter();
        qf.setField("lastname");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("Turner");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = JPAUtils.queryEntities(em, User.class, q,
                (p, cb, r) -> cb.or(p, cb.equal(r.get("firstname"), "Antonio")));

        Assert.assertNotNull(users);
        Assert.assertEquals(4, users.size());

        Assert.assertNotNull(users.get(0));
        Assert.assertEquals("Antonio", users.get(0).getFirstname());
        Assert.assertEquals("Mills", users.get(0).getLastname());
        Assert.assertNotNull(users.get(1));
        Assert.assertEquals("Antonio", users.get(1).getFirstname());
        Assert.assertEquals("Carroll", users.get(1).getLastname());
        Assert.assertNotNull(users.get(2));
        Assert.assertEquals("Antonio", users.get(2).getFirstname());
        Assert.assertEquals("Turner", users.get(2).getLastname());
        Assert.assertNotNull(users.get(3));
        Assert.assertEquals("Bonnie", users.get(3).getFirstname());
        Assert.assertEquals("Turner", users.get(3).getLastname());

        Long usersCount = JPAUtils.queryEntitiesCount(em, User.class, q,
                (p, cb, r) -> cb.or(p, cb.equal(r.get("firstname"), "Antonio")));

        Assert.assertNotNull(usersCount);
        Assert.assertEquals(4, usersCount.longValue());
    }

    @Test
    public void testQueryWithCriteriaFilterAndWithFields() {

        QueryParameters q = new QueryParameters();
        q.getFields().add("firstname");

        List<User> users = JPAUtils.queryEntities(em, User.class, q,
                (p, cb, r) -> cb.and(p, cb.equal(r.get("lastname"), "Stewart")));

        Assert.assertNotNull(users);
        Assert.assertEquals(1, users.size());

        Assert.assertNotNull(users.get(0));
        Assert.assertNotNull(users.get(0).getFirstname());
        Assert.assertNull(users.get(0).getLastname());
        Assert.assertEquals("Donald", users.get(0).getFirstname());
    }
}
