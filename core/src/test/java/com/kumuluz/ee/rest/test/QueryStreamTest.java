package com.kumuluz.ee.rest.test;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.test.entities.User;
import com.kumuluz.ee.rest.test.utils.JpaUtil;
import com.kumuluz.ee.rest.utils.JPAUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author gpor0
 */
@RunWith(Parameterized.class)
public class QueryStreamTest {

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
    public void testStreamCount() {
        QueryParameters q = new QueryParameters();

        Stream<User> users = JPAUtils.getEntityStream(em, User.class, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100L, users.count());
    }

    @Test
    public void testStreamsObject() {
        QueryParameters q = new QueryParameters();

        Stream<User> users = JPAUtils.getEntityStream(em, User.class, q);

        Assert.assertNotNull(users);
        Optional<User> first = users.findFirst();
        Assert.assertTrue(first.isPresent());
        Assert.assertNotNull(first.get().getLastname());
    }

    @Test
    public void testStreamCollect() {
        QueryParameters q = new QueryParameters();

        Stream<User> users = JPAUtils.getEntityStream(em, User.class, q);

        int size = users.collect(Collectors.toList()).size();

        Assert.assertNotNull(users);
        Assert.assertEquals(100, size);

    }

    @Test(expected = IllegalStateException.class)
    public void testStreamsCannotBeRestarted() {
        QueryParameters q = new QueryParameters();

        Stream<User> users = JPAUtils.getEntityStream(em, User.class, q);

        int collectedCount = users.collect(Collectors.toList()).size();

        Assert.assertNotNull(users);
        Assert.assertEquals(100L, users.count());
        Assert.assertEquals(100, collectedCount);

        Optional<User> first = users.findFirst();
        Assert.assertTrue(first.isPresent());
        Assert.assertNotNull(first.get().getLastname());
    }
}
