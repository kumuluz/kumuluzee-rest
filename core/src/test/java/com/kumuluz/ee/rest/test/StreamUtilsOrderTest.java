package com.kumuluz.ee.rest.test;

import com.kumuluz.ee.rest.beans.QueryOrder;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.enums.OrderDirection;
import com.kumuluz.ee.rest.enums.OrderNulls;
import com.kumuluz.ee.rest.exceptions.InvalidEntityFieldException;
import com.kumuluz.ee.rest.exceptions.NoSuchEntityFieldException;
import com.kumuluz.ee.rest.test.entities.Project;
import com.kumuluz.ee.rest.test.entities.User;
import com.kumuluz.ee.rest.test.utils.JpaUtil;
import com.kumuluz.ee.rest.utils.JPAUtils;
import com.kumuluz.ee.rest.utils.StreamUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Zvone Gazvoda
 */
@RunWith(Parameterized.class)
public class StreamUtilsOrderTest {

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
    public void testSingleStringOrder() {

        QueryOrder qo = new QueryOrder();
        qo.setField("firstname");
        qo.setOrder(OrderDirection.ASC);

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        Assert.assertNotNull(users.get(0).getFirstname());
        Assert.assertEquals("Amanda", users.get(0).getFirstname());
        Assert.assertNotNull(users.get(99).getFirstname());
        Assert.assertEquals("Victor", users.get(99).getFirstname());
    }

    @Test
    public void testSingleLocalDateOrder() {

        QueryOrder qo = new QueryOrder();
        qo.setField("birthDate");
        qo.setOrder(OrderDirection.ASC);

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        Assert.assertNotNull(users.get(0).getFirstname());
        Assert.assertEquals("Bonnie", users.get(0).getFirstname());
        Assert.assertNotNull(users.get(99).getFirstname());
        Assert.assertEquals("Karen", users.get(1).getFirstname());
    }

    @Test
    public void testSingleLocalDateTimeOrder() {

        QueryOrder qo = new QueryOrder();
        qo.setField("registrationDate");
        qo.setOrder(OrderDirection.ASC);

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        Assert.assertNotNull(users.get(97).getFirstname());
        Assert.assertEquals("Annie", users.get(97).getFirstname());
        Assert.assertNotNull(users.get(98).getFirstname());
        Assert.assertEquals("Susan", users.get(98).getFirstname());
        Assert.assertNotNull(users.get(99).getFirstname());
        Assert.assertEquals("John", users.get(99).getFirstname());
    }

    @Test
    public void testSingleBigDecimalOrder() {

        QueryOrder qo = new QueryOrder();
        qo.setField("score");
        qo.setOrder(OrderDirection.ASC);

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        Assert.assertNotNull(users.get(0).getFirstname());
        Assert.assertEquals("Kathy", users.get(0).getFirstname());
        Assert.assertNotNull(users.get(1).getFirstname());
        Assert.assertEquals("Helen", users.get(1).getFirstname());
        Assert.assertNotNull(users.get(2).getFirstname());
        Assert.assertEquals("Debra", users.get(2).getFirstname());
    }

    @Test
    public void testSingleOrderWithRestMapping() {

        QueryOrder qo = new QueryOrder();
        qo.setField("firstnameChanged");
        qo.setOrder(OrderDirection.ASC);

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        Assert.assertNotNull(users.get(0).getFirstname());
        Assert.assertEquals("Amanda", users.get(0).getFirstname());
        Assert.assertNotNull(users.get(99).getFirstname());
        Assert.assertEquals("Victor", users.get(99).getFirstname());
    }

    @Test
    public void testSingleOrderDesc() {

        QueryOrder qo = new QueryOrder();
        qo.setField("lastname");
        qo.setOrder(OrderDirection.DESC);

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        Assert.assertNotNull(users.get(0).getLastname());
        Assert.assertEquals("Willis", users.get(0).getLastname());
        Assert.assertNotNull(users.get(99).getLastname());
        Assert.assertEquals("Austin", users.get(99).getLastname());
    }

    @Test
    public void testMultipleOrders() {

        QueryParameters q = new QueryParameters();

        QueryOrder qo = new QueryOrder();
        qo.setField("role");
        qo.setOrder(OrderDirection.DESC);
        q.getOrder().add(qo);

        qo = new QueryOrder();
        qo.setField("country");
        qo.setOrder(OrderDirection.ASC);
        q.getOrder().add(qo);

        qo = new QueryOrder();
        qo.setField("lastname");
        qo.setOrder(OrderDirection.DESC);
        q.getOrder().add(qo);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        Assert.assertNotNull(users.get(0).getFirstname());
        Assert.assertNotNull(users.get(0).getLastname());
        Assert.assertEquals("Mark", users.get(0).getFirstname());
        Assert.assertEquals("West", users.get(0).getLastname());
        Assert.assertNotNull(users.get(99).getFirstname());
        Assert.assertNotNull(users.get(99).getLastname());
        Assert.assertEquals("Julia", users.get(99).getFirstname());
        Assert.assertEquals("Gonzalez", users.get(99).getLastname());
    }

    @Test
    public void testNullDirection() {

        QueryOrder qo = new QueryOrder();
        qo.setField("lastname");

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        Assert.assertNotNull(users.get(0).getLastname());
        Assert.assertEquals("Austin", users.get(0).getLastname());
        Assert.assertNotNull(users.get(99).getLastname());
        Assert.assertEquals("Willis", users.get(99).getLastname());
    }

    @Test
    public void testNullField() {

        QueryOrder qo = new QueryOrder();

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<User> users = JPAUtils.queryEntities(em, User.class, q)
                .stream().sorted(Comparator.comparing(User::getId)).collect(Collectors.toList());

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        Assert.assertNotNull(users.get(0).getLastname());
        Assert.assertEquals("Ramos", users.get(0).getLastname());
        Assert.assertNotNull(users.get(99).getLastname());
        Assert.assertEquals("Hall", users.get(99).getLastname());
    }

    @Test
    public void testNonExistentColumn() {

        QueryOrder qo = new QueryOrder();
        qo.setField("lstnm");
        qo.setOrder(OrderDirection.DESC);

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        try {

            List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
            StreamUtils.queryEntities(users, q);

            Assert.fail("No exception was thrown");
        } catch (NoSuchEntityFieldException e) {

            Assert.assertEquals("lstnm", e.getField());
        }
    }

    @Test
    public void testCaseSensitiveField() {

        QueryOrder qo = new QueryOrder();
        qo.setField("firsTNAmE");
        qo.setOrder(OrderDirection.ASC);

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        try {

            List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
            StreamUtils.queryEntities(users, q);

            Assert.fail("No exception was thrown");
        } catch (NoSuchEntityFieldException e) {

            Assert.assertEquals("firsTNAmE", e.getField());
        }
    }

    @Test
    public void testManyToOne() {

        QueryOrder qo = new QueryOrder();
        qo.setField("user.firstname");

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<Project> projects = em.createNamedQuery("Project.getAll").getResultList();
        projects = StreamUtils.queryEntities(projects, q);

        Assert.assertNotNull(projects);
        Assert.assertEquals(100, projects.size());
        Assert.assertNotNull(projects.get(0).getName());
        Assert.assertEquals("Red", projects.get(0).getName());
        Assert.assertNotNull(projects.get(99).getName());
        Assert.assertEquals("Turquoise", projects.get(99).getName());
    }

    @Test
    public void testManyToOneOnlyField() {

        QueryOrder qo = new QueryOrder();
        qo.setField("user");

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<Project> projects = em.createNamedQuery("Project.getAll").getResultList();
        projects = StreamUtils.queryEntities(projects, q);

        Assert.assertNotNull(projects);
        Assert.assertEquals(100, projects.size());
        Assert.assertNotNull(projects.get(0).getName());
        Assert.assertEquals("Goldenrod", projects.get(0).getName());
        Assert.assertNotNull(projects.get(99).getName());
        Assert.assertEquals("Yellow", projects.get(99).getName());
    }

    @Test(expected = InvalidEntityFieldException.class)
    public void testOneToManyOnlyField() {

        QueryOrder qo = new QueryOrder();
        qo.setField("projects");

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        StreamUtils.queryEntities(users, q);

        Assert.fail("No exception was thrown");
    }


    @Test
    public void ignoredFieldShouldReturnUnchangedResult() {

        String ignoredFieldName = "userIgnoredField";

        QueryParameters q = new QueryParameters();

        QueryOrder o = new QueryOrder();
        o.setField(ignoredFieldName);
        o.setOrder(OrderDirection.ASC);
        q.getOrder().add(o);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
    }

    @Test
    public void ignoredFieldOnOneToOneShouldReturnUnchangedResult() {

        String ignoredFieldName = "career.careerIgnoreField";

        QueryParameters q = new QueryParameters();

        QueryOrder o = new QueryOrder();
        o.setField(ignoredFieldName);
        o.setOrder(OrderDirection.ASC);
        q.getOrder().add(o);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
    }

    @Test(expected = NoSuchEntityFieldException.class)
    public void unknownOrderFieldShouldReturnException() {

        String ignoredFieldName = "customIgnoredField2";

        QueryParameters q = new QueryParameters();

        QueryOrder o = new QueryOrder();
        o.setField(ignoredFieldName);
        o.setOrder(OrderDirection.ASC);
        q.getOrder().add(o);

        JPAUtils.queryEntities(em, User.class, q);
    }

    @Test
    public void shouldOrderByRelationHavingInheritance() {

        QueryOrder qo = new QueryOrder();
        qo.setField("projects.projectLocation.externalId");
        qo.setOrder(OrderDirection.ASC);

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        Assert.assertNotNull(users.get(0).getLastname());
        Assert.assertEquals("Fox", users.get(0).getLastname());
        Assert.assertEquals("x", users.get(0).getProjects().get(0).getProjectLocation().getExternalId());
        Assert.assertNotNull(users.get(1).getLastname());
        Assert.assertEquals("Holmes", users.get(1).getLastname());
        Assert.assertEquals("y", users.get(1).getProjects().get(0).getProjectLocation().getExternalId());
        Assert.assertNotNull(users.get(2).getLastname());
        Assert.assertEquals("Mitchell", users.get(2).getLastname());
        Assert.assertEquals("z", users.get(2).getProjects().get(0).getProjectLocation().getExternalId());
    }

    @Test
    public void shouldReturnAscNullsLastOrderedByOneToOneOnOneToManyRelation() {

        QueryOrder qo = new QueryOrder();
        qo.setField("projects.projectLocation.locationName");
        qo.setOrder(OrderDirection.ASC);

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        Assert.assertNotNull(users.get(0).getLastname());
        Assert.assertEquals("Holmes", users.get(0).getLastname());
        Assert.assertEquals("Celje", users.get(0).getProjects().get(0).getProjectLocation().getLocationName());
        Assert.assertNotNull(users.get(1).getLastname());
        Assert.assertEquals("Mitchell", users.get(1).getLastname());
        Assert.assertEquals("Ljubljana", users.get(1).getProjects().get(0).getProjectLocation().getLocationName());
        Assert.assertNotNull(users.get(2).getLastname());
        Assert.assertEquals("Fox", users.get(2).getLastname());
        Assert.assertEquals("Maribor", users.get(2).getProjects().get(0).getProjectLocation().getLocationName());
    }

    @Test
    public void shouldReturnDescNullsLastOrderedByOneToOneOnOneToManyRelation() {

        QueryOrder qo = new QueryOrder();
        qo.setField("projects.projectLocation.locationName");
        qo.setOrder(OrderDirection.DESC);

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        Assert.assertNotNull(users.get(0).getLastname());
        Assert.assertEquals("Fox", users.get(0).getLastname());
        Assert.assertEquals("Maribor", users.get(0).getProjects().get(0).getProjectLocation().getLocationName());
        Assert.assertNotNull(users.get(1).getLastname());
        Assert.assertEquals("Mitchell", users.get(1).getLastname());
        Assert.assertEquals("Ljubljana", users.get(1).getProjects().get(0).getProjectLocation().getLocationName());
        Assert.assertNotNull(users.get(2).getLastname());
        Assert.assertEquals("Holmes", users.get(2).getLastname());
        Assert.assertEquals("Celje", users.get(2).getProjects().get(0).getProjectLocation().getLocationName());
    }

    @Test
    public void shouldReturnAscNullsFirstOrderedByOneToOneOnOneToManyRelation() {

        QueryOrder qo = new QueryOrder();
        qo.setField("projects.projectLocation.locationName");
        qo.setOrder(OrderDirection.ASC);
        qo.setNulls(OrderNulls.FIRST);

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        //fox is not last since it contains projectLocation with locationName = NULL
        Assert.assertNotNull(users.get(98).getLastname());
        Assert.assertEquals("Holmes", users.get(98).getLastname());
        Assert.assertEquals("Celje", users.get(98).getProjects().get(0).getProjectLocation().getLocationName());
        Assert.assertNotNull(users.get(99).getLastname());
        Assert.assertEquals("Mitchell", users.get(99).getLastname());
        Assert.assertEquals("Ljubljana", users.get(99).getProjects().get(0).getProjectLocation().getLocationName());
    }

    @Test
    public void shouldReturnDescNullsFirstOrderedByOneToOneOnOneToManyRelation() {

        QueryOrder qo = new QueryOrder();
        qo.setField("projects.projectLocation.locationName");
        qo.setOrder(OrderDirection.DESC);
        qo.setNulls(OrderNulls.FIRST);

        QueryParameters q = new QueryParameters();
        q.getOrder().add(qo);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        //fox is not last since it contains projectLocation with locationName = NULL
        Assert.assertNotNull(users.get(98).getLastname());
        Assert.assertEquals("Mitchell", users.get(98).getLastname());
        Assert.assertEquals("Ljubljana", users.get(98).getProjects().get(0).getProjectLocation().getLocationName());
        Assert.assertNotNull(users.get(99).getLastname());
        Assert.assertEquals("Holmes", users.get(99).getLastname());
        Assert.assertEquals("Celje", users.get(99).getProjects().get(0).getProjectLocation().getLocationName());
    }

}
