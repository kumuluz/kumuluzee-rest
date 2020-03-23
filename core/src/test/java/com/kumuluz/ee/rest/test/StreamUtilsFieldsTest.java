package com.kumuluz.ee.rest.test;

import com.kumuluz.ee.rest.beans.QueryFilter;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.enums.FilterOperation;
import com.kumuluz.ee.rest.exceptions.NoSuchEntityFieldException;
import com.kumuluz.ee.rest.test.entities.User;
import com.kumuluz.ee.rest.test.utils.JpaUtil;
import com.kumuluz.ee.rest.utils.JPAUtils;
import com.kumuluz.ee.rest.utils.StreamUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Zvone Gazvoda
 */
@RunWith(Parameterized.class)
public class StreamUtilsFieldsTest {

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
    public void testSingleField() {

        QueryParameters q = new QueryParameters();
        q.getFields().add("firstname");

        List<User> users = new ArrayList<>();
        users = em.createNamedQuery("User.getAll").getResultList();
        em.clear();

        users = StreamUtils.queryEntities(users, User.class, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        Assert.assertNotNull(users.get(0).getFirstname());
        Assert.assertNull(users.get(0).getLastname());
        Assert.assertEquals("Jason", users.get(0).getFirstname());
    }

    @Test
    public void ignoredFieldShouldReturnUnchangedResult() {

        String ignoredFieldName = "userIgnoredField";

        QueryParameters q = new QueryParameters();

        q.getFields().add(ignoredFieldName);

        List<User> users = new ArrayList<>();
        users = em.createNamedQuery("User.getAll").getResultList();
        em.clear();

        users = StreamUtils.queryEntities(users, User.class, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
    }

    //@Test
    public void ignoredFieldOnOneToManyShouldReturnUnchangedResult() {

        String ignoredFieldName = "projects.projectIgnoreField";

        QueryParameters q = new QueryParameters();

        q.getFields().add(ignoredFieldName);

        List<User> users = new ArrayList<>();
        users = em.createNamedQuery("User.getAll").getResultList();
        em.clear();

        users = StreamUtils.queryEntities(users, User.class, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
    }

    @Test
    public void ignoredFieldOnOneToOneShouldReturnUnchangedResult() {

        String ignoredFieldName = "career.careerIgnoreField";

        QueryParameters q = new QueryParameters();

        q.getFields().add(ignoredFieldName);

        List<User> users = new ArrayList<>();
        users = em.createNamedQuery("User.getAll").getResultList();
        em.clear();

        users = StreamUtils.queryEntities(users, User.class, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
    }

    @Test(expected = NoSuchEntityFieldException.class)
    public void unknownSelectionFieldShouldReturnException() {

        String ignoredFieldName = "customIgnoredField2";

        QueryParameters q = new QueryParameters();

        q.getFields().add(ignoredFieldName);

        List<User> users = new ArrayList<>();
        users = em.createNamedQuery("User.getAll").getResultList();
        em.clear();

        users = StreamUtils.queryEntities(users, User.class, q);
    }

    @Test
    public void testSingleFieldWithRestMapping() {

        QueryParameters q = new QueryParameters();
        q.getFields().add("firstnameChanged");

        List<User> users = new ArrayList<>();
        users = em.createNamedQuery("User.getAll").getResultList();
        em.clear();

        users = StreamUtils.queryEntities(users, User.class, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        Assert.assertNotNull(users.get(0).getFirstname());
        Assert.assertNull(users.get(0).getLastname());
        Assert.assertEquals("Jason", users.get(0).getFirstname());
    }

    @Test
    public void testComputedFieldsWithRestMapping() {

        QueryParameters q = new QueryParameters();
        q.getFields().add("firstnameAndLastname");

        List<User> users = new ArrayList<>();
        users = em.createNamedQuery("User.getAll").getResultList();
        em.clear();

        users = StreamUtils.queryEntities(users, User.class, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        Assert.assertNotNull(users.get(0).getFirstname());
        Assert.assertNotNull(users.get(0).getLastname());
        Assert.assertEquals("Jason", users.get(0).getFirstname());
    }

    @Test
    public void testOneToOneChildFieldsWithRestMapping() {

        QueryParameters q = new QueryParameters();
        q.getFilters().add(new QueryFilter("lastname", FilterOperation.EQ, "Torres"));
        q.getFields().add("career.experience");
        q.getFields().add("career.currentPosition");

        List<User> users = new ArrayList<>();
        users = em.createNamedQuery("User.getAll").getResultList();
        em.clear();

        users = StreamUtils.queryEntities(users, User.class, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(1, users.size());
        Assert.assertNotNull(users.get(0).getCareer());
        Assert.assertNotNull(users.get(0).getCareer().getYears());
        Assert.assertNotNull(users.get(0).getCareer().getCurrentPosition());
    }

    @Test
    public void testComputedOneToOneChildFieldsWithRestMapping() {

        QueryParameters q = new QueryParameters();
        q.getFilters().add(new QueryFilter("lastname", FilterOperation.EQ, "Torres"));
        q.getFields().add("emailAndCurrentPosition");

        List<User> users = new ArrayList<>();
        users = em.createNamedQuery("User.getAll").getResultList();
        em.clear();

        users = StreamUtils.queryEntities(users, User.class, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(1, users.size());
        Assert.assertNotNull(users.get(0).getEmail());
        Assert.assertNull(users.get(0).getFirstname());
        Assert.assertNotNull(users.get(0).getCareer());
        Assert.assertNull(users.get(0).getCareer().getYears());
        Assert.assertEquals("Developer", users.get(0).getCareer().getCurrentPosition());
        Assert.assertEquals("jtorres3@comcast.net", users.get(0).getEmail());
    }

    @Test
    public void testMultipleFields() {

        QueryParameters q = new QueryParameters();
        q.getFields().add("firstname");
        q.getFields().add("country");
        q.getFields().add("role");

        List<User> users = new ArrayList<>();
        users = em.createNamedQuery("User.getAll").getResultList();
        em.clear();

        users = StreamUtils.queryEntities(users, User.class, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        Assert.assertNotNull(users.get(0).getFirstname());
        Assert.assertNotNull(users.get(0).getCountry());
        Assert.assertNotNull(users.get(0).getRole());
        Assert.assertNull(users.get(0).getLastname());
        Assert.assertEquals("Jason", users.get(0).getFirstname());
        Assert.assertEquals("China", users.get(0).getCountry());
        Assert.assertEquals(0, users.get(0).getRole().intValue());
    }

    @Test
    public void testIdAvailable() {

        QueryParameters q = new QueryParameters();
        q.getFields().add("lastname");

        List<User> users = new ArrayList<>();
        users = em.createNamedQuery("User.getAll").getResultList();
        em.clear();

        users = StreamUtils.queryEntities(users, User.class, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        Assert.assertNotNull(users.get(0).getLastname());
        Assert.assertNotNull(users.get(0).getId());
        Assert.assertNull(users.get(0).getFirstname());
        Assert.assertEquals("Ramos", users.get(0).getLastname());
        Assert.assertEquals(1, users.get(0).getId().intValue());
    }

    @Test(expected = NoSuchEntityFieldException.class)
    public void testNonExistingField() {

        QueryParameters q = new QueryParameters();
        q.getFields().add("something");

        List<User> users = new ArrayList<>();
        users = em.createNamedQuery("User.getAll").getResultList();
        em.clear();

        users = StreamUtils.queryEntities(users, User.class, q);
    }
}
