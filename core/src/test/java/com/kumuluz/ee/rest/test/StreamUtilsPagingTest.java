package com.kumuluz.ee.rest.test;

import com.kumuluz.ee.rest.beans.QueryOrder;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.enums.OrderDirection;
import com.kumuluz.ee.rest.test.entities.User;
import com.kumuluz.ee.rest.test.utils.JpaUtil;
import com.kumuluz.ee.rest.utils.StreamUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Zvone Gazvoda
 */
@RunWith(Parameterized.class)
public class StreamUtilsPagingTest {

    @Parameterized.Parameter
    public EntityManager em;

    @Parameterized.Parameters
    public static Collection<EntityManager> data() {

        JpaUtil jpaUtil = JpaUtil.getInstance();

        return Arrays.asList(
                jpaUtil.getEclipselinkEntityManager(),
                jpaUtil.getHibernateEntityManager()
        );
    }

    @Test
    public void testEmptyQuery() {
        List<User> users = em.createNamedQuery("User.getAll").getResultList();
        users = StreamUtils.queryEntities(users, new QueryParameters());

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
    }

    @Test
    public void testNullQuery() {

        List<User> users = em.createNamedQuery("User.getAll").getResultList();
        users = StreamUtils.queryEntities(users);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
    }

    @Test
    public void testLimit() {

        QueryParameters q = new QueryParameters();
        q.setLimit(10);

        List<User> users = em.createNamedQuery("User.getAll").getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(10, users.size());
        Assert.assertNotNull(users.get(0).getId());
        Assert.assertEquals(1, users.get(0).getId().intValue());
        Assert.assertNotNull(users.get(9).getId());
        Assert.assertEquals(10, users.get(9).getId().intValue());
    }

    @Test
    public void testOffset() {

        QueryParameters q = new QueryParameters();
        q.setOffset(30);

        List<User> users = em.createNamedQuery("User.getAll").getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(70, users.size());
        Assert.assertNotNull(users.get(0).getId());
        Assert.assertEquals(31, users.get(0).getId().intValue());
        Assert.assertNotNull(users.get(69).getId());
        Assert.assertEquals(100, users.get(69).getId().intValue());
    }

    @Test
    public void testLimitWithOffset() {

        QueryOrder qo = new QueryOrder();
        qo.setField("id");
        qo.setOrder(OrderDirection.ASC);

        QueryParameters q = new QueryParameters();
        q.setLimit(25);
        q.setOffset(0);
        q.getOrder().add(qo);

        List<User> users = em.createNamedQuery("User.getAll").getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(25, users.size());

        q.setOffset(24);

        users = em.createNamedQuery("User.getAll").getResultList();

        List<User> usersOffseted = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(usersOffseted);
        Assert.assertEquals(25, usersOffseted.size());

        Assert.assertNotNull(users.get(24).getId());
        Assert.assertNotNull(usersOffseted.get(0).getId());
        Assert.assertEquals(users.get(24).getId().intValue(), usersOffseted.get(0).getId().intValue());
//        Assert.assertEquals(65, users.get(24).getId().intValue());
    }

    @Test
    public void testLimitTooBig() {

        QueryParameters q = new QueryParameters();
        q.setLimit(300);

        List<User> users = em.createNamedQuery("User.getAll").getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
    }

    @Test
    public void testOffsetOutOfBounds() {

        QueryParameters q = new QueryParameters();
        q.setOffset(200);

        List<User> users = em.createNamedQuery("User.getAll").getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(0, users.size());
    }
}
