package com.kumuluz.ee.rest.test;

import com.kumuluz.ee.rest.beans.QueryFilter;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.enums.FilterOperation;
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

/**
 * @author Tilen Faganel
 */
@RunWith(Parameterized.class)
public class JPAUtilsCountTest {

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
    public void testEmptyQueryCount() {

        Long count = JPAUtils.queryEntitiesCount(em, User.class);

        Assert.assertNotNull(count);
        Assert.assertEquals(100, count.longValue());
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

        Long count = JPAUtils.queryEntitiesCount(em, User.class, q);

        Assert.assertNotNull(count);
        Assert.assertEquals(2, count.longValue());
    }

    @Test
    public void testQueryCount2() {

        QueryParameters q = new QueryParameters();

        QueryFilter qf = new QueryFilter();
        qf.setField("projects.status");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("ACTIVE");
        q.getFilters().add(qf);

        Long count = JPAUtils.queryEntitiesCount(em, User.class, q);

        Assert.assertNotNull(count);
        Assert.assertEquals(39, count.longValue());
    }
}
