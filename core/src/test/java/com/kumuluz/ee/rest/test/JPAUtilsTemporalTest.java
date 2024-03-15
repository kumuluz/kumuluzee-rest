package com.kumuluz.ee.rest.test;

import com.kumuluz.ee.rest.beans.QueryFilter;
import com.kumuluz.ee.rest.beans.QueryOrder;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.enums.FilterOperation;
import com.kumuluz.ee.rest.enums.OrderDirection;
import com.kumuluz.ee.rest.test.entities.Event;
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
 * @author cen1
 */
@RunWith(Parameterized.class)
public class JPAUtilsTemporalTest {

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
    public void testOrderInstantAsc() {

        QueryOrder qo = new QueryOrder();
        qo.setField("beginsAt");
        qo.setOrder(OrderDirection.ASC);

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<Event> events = JPAUtils.queryEntities(em, Event.class, q);

        Assert.assertNotNull(events);
        Assert.assertEquals(3, events.size());
        Assert.assertEquals(Integer.valueOf(1), events.get(0).getId());
        Assert.assertEquals(Integer.valueOf(2), events.get(1).getId());
        Assert.assertEquals(Integer.valueOf(3), events.get(2).getId());
    }

    @Test
    public void testOrderInstantDesc() {

        QueryOrder qo = new QueryOrder();
        qo.setField("beginsAt");
        qo.setOrder(OrderDirection.DESC);

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<Event> events = JPAUtils.queryEntities(em, Event.class, q);

        Assert.assertNotNull(events);
        Assert.assertEquals(3, events.size());
        Assert.assertEquals(Integer.valueOf(3), events.get(0).getId());
        Assert.assertEquals(Integer.valueOf(2), events.get(1).getId());
        Assert.assertEquals(Integer.valueOf(1), events.get(2).getId());
    }

    @Test
    public void testFilterEQInstant() {

        //Match with 2020-01-01T00:00:00.00Z
        QueryFilter qf = new QueryFilter();
        qf.setField("beginsAt");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("2020-01-01T00:00:00.00Z");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<Event> events = JPAUtils.queryEntities(em, Event.class, q);

        Assert.assertNotNull(events);
        Assert.assertEquals(1, events.size());
        Assert.assertEquals(Integer.valueOf(1), events.get(0).getId());
    }

    @Test
    public void testOrderOffsetAsc() {

        QueryOrder qo = new QueryOrder();
        qo.setField("intermissionAt");
        qo.setOrder(OrderDirection.ASC);

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<Event> events = JPAUtils.queryEntities(em, Event.class, q);

        Assert.assertNotNull(events);
        Assert.assertEquals(3, events.size());
        Assert.assertEquals(Integer.valueOf(1), events.get(0).getId());
        Assert.assertEquals(Integer.valueOf(2), events.get(1).getId());
        Assert.assertEquals(Integer.valueOf(3), events.get(2).getId());
    }

    @Test
    public void testOrderOffsetDesc() {

        QueryOrder qo = new QueryOrder();
        qo.setField("intermissionAt");
        qo.setOrder(OrderDirection.DESC);

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<Event> events = JPAUtils.queryEntities(em, Event.class, q);

        Assert.assertNotNull(events);
        Assert.assertEquals(3, events.size());
        Assert.assertEquals(Integer.valueOf(3), events.get(0).getId());
        Assert.assertEquals(Integer.valueOf(2), events.get(1).getId());
        Assert.assertEquals(Integer.valueOf(1), events.get(2).getId());
    }

    @Test
    public void testFilterEQOffsetDateTime() {

        //Match with 2020-01-01T09:00:00.00Z
        QueryFilter qf = new QueryFilter();
        qf.setField("intermissionAt");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("2020-01-01T10:00:00.00+01:00");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<Event> events = JPAUtils.queryEntities(em, Event.class, q);

        Assert.assertNotNull(events);
        Assert.assertEquals(1, events.size());
        Assert.assertEquals(Integer.valueOf(1), events.get(0).getId());
    }

    @Test
    public void testOrderZonedAsc() {

        QueryOrder qo = new QueryOrder();
        qo.setField("endsAt");
        qo.setOrder(OrderDirection.ASC);

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<Event> events = JPAUtils.queryEntities(em, Event.class, q);

        Assert.assertNotNull(events);
        Assert.assertEquals(3, events.size());
        Assert.assertEquals(Integer.valueOf(1), events.get(0).getId());
        Assert.assertEquals(Integer.valueOf(2), events.get(1).getId());
        Assert.assertEquals(Integer.valueOf(3), events.get(2).getId());
    }

    @Test
    public void testOrderZonedDesc() {

        QueryOrder qo = new QueryOrder();
        qo.setField("endsAt");
        qo.setOrder(OrderDirection.DESC);

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<Event> events = JPAUtils.queryEntities(em, Event.class, q);

        Assert.assertNotNull(events);
        Assert.assertEquals(3, events.size());
        Assert.assertEquals(Integer.valueOf(3), events.get(0).getId());
        Assert.assertEquals(Integer.valueOf(2), events.get(1).getId());
        Assert.assertEquals(Integer.valueOf(1), events.get(2).getId());
    }

    @Test
    public void testFilterEQZonedDateTime() {

        //Match with 2020-01-01T18:00:00.00Z
        QueryFilter qf = new QueryFilter();
        qf.setField("endsAt");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("2020-01-01T19:00:00.00+01:00[Europe/Ljubljana]");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<Event> events = JPAUtils.queryEntities(em, Event.class, q);

        Assert.assertNotNull(events);
        Assert.assertEquals(1, events.size());
        Assert.assertEquals(Integer.valueOf(1), events.get(0).getId());
    }

    @Test
    public void testFilterEQTime() {

        //Match with 06:20:11
        QueryFilter qf = new QueryFilter();
        qf.setField("registrationTime");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("06:20:11");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = JPAUtils.queryEntities(em, User.class, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(1, users.size());
        Assert.assertEquals(Integer.valueOf(2), users.get(0).getId());
    }
}
