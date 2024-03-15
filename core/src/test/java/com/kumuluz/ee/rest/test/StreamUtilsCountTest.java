package com.kumuluz.ee.rest.test;

import com.kumuluz.ee.rest.beans.QueryFilter;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.enums.FilterOperation;
import com.kumuluz.ee.rest.test.entities.User;
import com.kumuluz.ee.rest.test.utils.JpaUtil;
import com.kumuluz.ee.rest.utils.StreamUtils;
import jakarta.persistence.EntityManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Zvone Gazvoda
 */
@RunWith(Parameterized.class)
public class StreamUtilsCountTest {

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
    public void testEmptyQueryCount() {

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        Long count = StreamUtils.queryEntitiesCount(users);

        Assert.assertNotNull(count);
        Assert.assertEquals(100L, count.longValue());
    }

    @Test
    public void testQueryCount() {

        QueryParameters q = new QueryParameters();

        QueryFilter qf = new QueryFilter();
        qf.setField("firstname");
        qf.setOperation(FilterOperation.IN);
        qf.getValues().add("Bruce");
        qf.getValues().add("Karen");
        qf.getValues().add("Sandra");
        qf.getValues().add("Laura");
        q.getFilters().add(qf);

        qf = new QueryFilter();
        qf.setField("country");
        qf.setOperation(FilterOperation.LIKE);
        qf.setValue("%ina");
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        Long count = StreamUtils.queryEntitiesCount(users, q);

        Assert.assertNotNull(count);
        Assert.assertEquals(2L, count.longValue());
    }

    @Test
    public void testQueryCount2() {

        QueryParameters q = new QueryParameters();

        QueryFilter qf = new QueryFilter();
        qf.setField("projects.status");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("ACTIVE");
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        Long count = StreamUtils.queryEntitiesCount(users, q);

        Assert.assertNotNull(count);
        Assert.assertEquals(39, count.longValue());
    }
}
