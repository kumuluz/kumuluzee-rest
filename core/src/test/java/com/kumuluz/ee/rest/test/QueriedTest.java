package com.kumuluz.ee.rest.test;

import com.kumuluz.ee.rest.beans.Queried;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.test.entities.User;
import com.kumuluz.ee.rest.test.utils.JpaUtil;
import com.kumuluz.ee.rest.utils.JPAUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import jakarta.persistence.EntityManager;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author gpor0
 */
@RunWith(Parameterized.class)
public class QueriedTest {

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
    public void testQueried() {
        QueryParameters q = new QueryParameters();

        Queried<User> queried = JPAUtils.getQueried(em, User.class, q);

        Assert.assertNotNull(queried);
        Assert.assertEquals(Long.valueOf(100L), queried.getTotalCount());
    }

    @Test
    public void testQueriedAndPaginated() {
        int limit = 24;

        QueryParameters q = new QueryParameters();
        q.setOffset(10);
        q.setLimit(limit);

        Queried<User> queried = JPAUtils.getQueried(em, User.class, q);

        Assert.assertNotNull(queried);
        Assert.assertEquals(Long.valueOf(100L), queried.getTotalCount());
        Assert.assertEquals(limit, queried.stream().count());
    }

    @Test
    public void testQueriedWithCount() {
        QueryParameters q = new QueryParameters();
        q.setCount(true);

        Queried<User> queried = JPAUtils.getQueried(em, User.class, q);

        Assert.assertNotNull(queried);
        Assert.assertEquals(Long.valueOf(100L), queried.getTotalCount());
    }

    @Test
    public void testQueriedWithNoCount() {
        QueryParameters q = new QueryParameters();

        Queried<User> queried = JPAUtils.getQueried(em, User.class, q);

        Assert.assertNotNull(queried);
        Assert.assertEquals(Long.valueOf(100L), queried.getTotalCount());
    }

    @Test
    public void testQueriedWithCountTrue() {
        QueryParameters q = new QueryParameters();
        q.setCount(true);

        Queried<User> queried = JPAUtils.getQueried(em, User.class, q);

        Assert.assertNotNull(queried);
        Assert.assertEquals(Long.valueOf(100L), queried.getTotalCount());
    }

    @Test
    public void testQueriedWithCountFalse() {
        QueryParameters q = new QueryParameters();
        q.setCount(false);

        Queried<User> queried = JPAUtils.getQueried(em, User.class, q);

        Assert.assertNotNull(queried);
        Assert.assertNull(queried.getTotalCount());
    }

}
