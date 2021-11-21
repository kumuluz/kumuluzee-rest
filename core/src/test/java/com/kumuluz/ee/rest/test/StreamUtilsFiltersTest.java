package com.kumuluz.ee.rest.test;

import com.kumuluz.ee.rest.beans.QueryFilter;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.enums.FilterOperation;
import com.kumuluz.ee.rest.exceptions.InvalidFieldValueException;
import com.kumuluz.ee.rest.exceptions.NoSuchEntityFieldException;
import com.kumuluz.ee.rest.test.entities.Project;
import com.kumuluz.ee.rest.test.entities.User;
import com.kumuluz.ee.rest.test.utils.JpaUtil;
import com.kumuluz.ee.rest.utils.StreamUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.EntityManager;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Zvone Gazvoda
 */
@RunWith(Parameterized.class)
public class StreamUtilsFiltersTest {

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
    public void testSingleStringFilter() {

        QueryFilter qf = new QueryFilter();
        qf.setField("firstname");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("Sandra");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(1, users.size());
        Assert.assertNotNull(users.get(0).getFirstname());
        Assert.assertEquals("Sandra", users.get(0).getFirstname());
    }

    @Test
    public void testSingleLocalDateFilter() {

        QueryFilter qf = new QueryFilter();
        qf.setField("birthDate");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("2015-04-09");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(1, users.size());
        Assert.assertNotNull(users.get(0).getFirstname());
        Assert.assertEquals("Bonnie", users.get(0).getFirstname());
    }

    @Test
    public void testSingleLocalDateTimeFilter() {

        String dateParam = "2015-04-30T10:20:11Z";
        QueryFilter qf = new QueryFilter();
        qf.setField("registrationDate");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue(ZonedDateTime.parse(dateParam, DateTimeFormatter.ISO_DATE_TIME).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime().toString());
        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(1, users.size());
        Assert.assertNotNull(users.get(0).getFirstname());
        Assert.assertEquals("Susan", users.get(0).getFirstname());
    }

    @Test
    public void testSingleBigDecimalFilter() {

        QueryFilter qf = new QueryFilter();
        qf.setField("score");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("7.12");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(1, users.size());
        Assert.assertNotNull(users.get(0).getFirstname());
        Assert.assertEquals("Kathy", users.get(0).getFirstname());
    }

    @Test
    public void testSingleFilterWithMultipleRestMapping() {

        QueryFilter qf = new QueryFilter();
        qf.setField("firstnameAndLastname");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("Sandra");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(1, users.size());
        Assert.assertNotNull(users.get(0).getFirstname());
        Assert.assertEquals("Sandra", users.get(0).getFirstname());
    }

    @Test
    public void testSingleFilterWithRestMapping() {

        QueryFilter qf = new QueryFilter();
        qf.setField("firstnameChanged");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("Sandra");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(1, users.size());
        Assert.assertNotNull(users.get(0).getFirstname());
        Assert.assertEquals("Sandra", users.get(0).getFirstname());
    }

    @Test
    public void testChildSingleFilterWithRestMapping() {

        QueryFilter qf = new QueryFilter();
        qf.setField("career.experience");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("5");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(1, users.size());
        Assert.assertNotNull(users.get(0).getFirstname());
        Assert.assertEquals("Jean", users.get(0).getFirstname());
        Assert.assertNotNull(users.get(0).getLastname());
        Assert.assertEquals("Torres", users.get(0).getLastname());
        Assert.assertNotNull(users.get(0).getCareer());
        Assert.assertEquals(5, users.get(0).getCareer().getYears().intValue());
    }

    @Test
    public void testSingleIntegerFilter() {

        QueryFilter qf = new QueryFilter();
        qf.setField("role");
        qf.setOperation(FilterOperation.GT);
        qf.setValue("0");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(47, users.size());
        Assert.assertEquals(1, users.get(0).getRole().intValue());
    }

    @Test
    public void testSingleDateFilter() {

        Date d = Date.from(ZonedDateTime.parse("2014-11-26T11:15:08Z").toInstant());

        QueryFilter qf = new QueryFilter();
        qf.setField("createdAt");
        qf.setOperation(FilterOperation.GT);
        qf.setDateValue(d);

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(39, users.size());
    }

    @Test
    public void testMultipleFilters() {

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

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(2, users.size());
    }

    @Test
    public void testWrongDateField() {

        Date d = Date.from(ZonedDateTime.parse("2014-11-26T11:15:08Z").toInstant());

        QueryFilter qf = new QueryFilter();
        qf.setField("lastname");
        qf.setOperation(FilterOperation.LTE);
        qf.setDateValue(d);

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
    }

    @Test
    public void testNonExistingField() {

        QueryFilter qf = new QueryFilter();
        qf.setField("asdas");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("test");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        try {
            List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
            StreamUtils.queryEntities(users, q);
            Assert.fail("No exception was thrown");
        } catch (NoSuchEntityFieldException e) {

            Assert.assertEquals("asdas", e.getField());
        }
    }

    @Test
    public void testNullValue() {

        QueryFilter qf = new QueryFilter();
        qf.setField("firstname");
        qf.setOperation(FilterOperation.EQ);

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
    }

    @Test
    public void testNullField() {

        QueryFilter qf = new QueryFilter();
        qf.setOperation(FilterOperation.NEQ);
        qf.setValue("test");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        try {
            List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
            StreamUtils.queryEntities(users, q);
            Assert.fail("No exception was thrown");
        } catch (NoSuchEntityFieldException e) {

            Assert.assertNull(e.getField());
        }
    }

    @Test
    public void testInFilter() {

        QueryFilter qf = new QueryFilter();
        qf.setField("firstname");
        qf.setOperation(FilterOperation.IN);
        qf.getValues().add("Bruce");
        qf.getValues().add("Karen");
        qf.getValues().add("Sandra");
        qf.getValues().add("Laura");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(9, users.size());
    }

    @Test
    public void testNinFilter() {

        QueryFilter qf = new QueryFilter();
        qf.setField("firstname");
        qf.setOperation(FilterOperation.NIN);
        qf.getValues().add("Bruce");
        qf.getValues().add("Karen");
        qf.getValues().add("Sandra");
        qf.getValues().add("Laura");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(91, users.size());
    }

    @Test
    public void testNotEqual() {

        QueryFilter qf = new QueryFilter();
        qf.setField("lastname");
        qf.setOperation(FilterOperation.NEQ);
        qf.setValue("Willis");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(97, users.size());
    }

    @Test
    public void testGte() {

        QueryFilter qf = new QueryFilter();
        qf.setField("role");
        qf.setOperation(FilterOperation.GTE);
        qf.setValue("1");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(47, users.size());

        Date d = Date.from(ZonedDateTime.parse("2014-11-26T11:15:08Z").toInstant());

        qf = new QueryFilter();
        qf.setField("createdAt");
        qf.setOperation(FilterOperation.GTE);
        qf.setDateValue(d);

        q = new QueryParameters();
        q.getFilters().add(qf);

        users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(40, users.size());
    }

    @Test
    public void testLt() {

        QueryFilter qf = new QueryFilter();
        qf.setField("role");
        qf.setOperation(FilterOperation.LT);
        qf.setValue("1");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(53, users.size());

        Date d = Date.from(ZonedDateTime.parse("2014-11-26T11:15:08Z").toInstant());

        qf = new QueryFilter();
        qf.setField("createdAt");
        qf.setOperation(FilterOperation.LT);
        qf.setDateValue(d);

        q = new QueryParameters();
        q.getFilters().add(qf);

        users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(60, users.size());
    }

    @Test
    public void testLte() {

        QueryFilter qf = new QueryFilter();
        qf.setField("role");
        qf.setOperation(FilterOperation.LTE);
        qf.setValue("0");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(53, users.size());

        Date d = Date.from(ZonedDateTime.parse("2014-11-26T11:15:08Z").toInstant());

        qf = new QueryFilter();
        qf.setField("createdAt");
        qf.setOperation(FilterOperation.LTE);
        qf.setDateValue(d);

        q = new QueryParameters();
        q.getFilters().add(qf);

        users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(61, users.size());
    }

    @Test
    public void testEqic() {

        QueryFilter qf = new QueryFilter();
        qf.setField("firstname");
        qf.setOperation(FilterOperation.EQIC);
        qf.setValue("jULIa");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(2, users.size());
    }

    @Test
    public void testNeqic() {

        QueryFilter qf = new QueryFilter();
        qf.setField("firstname");
        qf.setOperation(FilterOperation.NEQIC);
        qf.setValue("JaCK");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(98, users.size());
    }

    @Test
    public void testLikeic() {

        QueryFilter qf = new QueryFilter();
        qf.setField("firstname");
        qf.setOperation(FilterOperation.LIKEIC);
        qf.setValue("jA%");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(5, users.size());
    }

    @Test
    public void testNLikeic() {

        QueryFilter qf = new QueryFilter();
        qf.setField("firstname");
        qf.setOperation(FilterOperation.NLIKEIC);
        qf.setValue("jA%");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(95, users.size());
    }

    @Test
    public void testIntegerEq() {

        QueryFilter qf = new QueryFilter();
        qf.setField("role");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("0");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(53, users.size());
    }

    @Test
    public void testDateEq() {

        QueryFilter qf = new QueryFilter();
        qf.setField("createdAt");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("2014-09-11T12:35:07Z");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(1, users.size());
    }

    @Test
    public void testInic() {

        QueryFilter qf = new QueryFilter();
        qf.setField("firstname");
        qf.setOperation(FilterOperation.INIC);
        qf.getValues().add("sArAH");
        qf.getValues().add("ricHArd");
        qf.getValues().add("jACk");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(4, users.size());
    }

    @Test
    public void testNinic() {

        QueryFilter qf = new QueryFilter();
        qf.setField("firstname");
        qf.setOperation(FilterOperation.NINIC);
        qf.getValues().add("sArAH");
        qf.getValues().add("ricHArd");
        qf.getValues().add("jACk");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(96, users.size());
    }

    @Test
    public void testManyToOneRelation() {

        QueryFilter qf = new QueryFilter();
        qf.setField("user.id");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("28");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<Project> projects = em.createNamedQuery("Project.getAll").getResultList();
        projects = StreamUtils.queryEntities(projects, q);

        Assert.assertNotNull(projects);
        Assert.assertEquals(5, projects.size());

        qf = new QueryFilter();
        qf.setField("user.firstname");
        qf.setOperation(FilterOperation.INIC);
        qf.getValues().add("sArAH");
        qf.getValues().add("ricHArd");
        qf.getValues().add("jACk");

        q = new QueryParameters();
        q.getFilters().add(qf);

        projects = em.createNamedQuery("Project.getAll").getResultList();
        projects = StreamUtils.queryEntities(projects, q);

        Assert.assertNotNull(projects);
        Assert.assertEquals(8, projects.size());
    }

/*    @Test
    public void testManyToOneRelationOnlyField() {

        QueryFilter qf = new QueryFilter();
        qf.setField("user");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("28");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<Project> projects = new ArrayList<>();
        projects = em.createNamedQuery("Project.getAll").getResultList();
        projects = StreamUtils.queryEntities(projects, q);

        Assert.assertNotNull(projects);
        Assert.assertEquals(100, projects.size());
    }*/

    @Test
    public void testOneToManyRelation() {

        QueryFilter qf = new QueryFilter();
        qf.setField("projects.id");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("10");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(1, users.size());

        qf = new QueryFilter();
        qf.setField("projects.name");
        qf.setOperation(FilterOperation.NIN);
        qf.getValues().add("Green");
        qf.getValues().add("Violet");

        q = new QueryParameters();
        q.getFilters().add(qf);

        users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(55, users.size());
    }

    @Test
    public void testOneToManyRelationMultiple() {

        QueryFilter qf = new QueryFilter();
        qf.setField("projects.name");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("Orange");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(3, users.size());

        qf = new QueryFilter();
        qf.setField("projects.name");
        qf.setOperation(FilterOperation.IN);
        qf.getValues().add("Maroon");
        qf.getValues().add("Orange");

        q = new QueryParameters();
        q.getFilters().add(qf);

        users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(9, users.size());
    }

/*    @Test
    public void testOneToManyRelationOnlyFieldInteger() {

        QueryFilter qf = new QueryFilter();
        qf.setField("projects");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("28");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = new ArrayList<>();
        users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(60, users.size());
    }*/

    @Test
    public void testFilterMultipleLevel() {

        QueryFilter qf = new QueryFilter();
        qf.setField("projects.user.id");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("0");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(0, users.size());
    }

    @Test
    public void testFilterMultipleLevelEnum() {

        QueryFilter qf = new QueryFilter();
        qf.setField("projects.user.status");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("ACTIVE");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(0, users.size());
    }

    @Test
    public void testEnumFilter() {

        QueryFilter qf = new QueryFilter();
        qf.setField("status");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("ACTIVE");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<Project> projects = em.createNamedQuery("Project.getAll").getResultList();

        projects = StreamUtils.queryEntities(projects, q);

        Assert.assertNotNull(projects);
        Assert.assertEquals(50, projects.size());
    }

    @Test
    public void testEnumInFilter() {

        QueryFilter qf = new QueryFilter();
        qf.setField("status");
        qf.setOperation(FilterOperation.IN);
        qf.getValues().add("ACTIVE");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<Project> projects = em.createNamedQuery("Project.getAll").getResultList();

        projects = StreamUtils.queryEntities(projects, q);

        Assert.assertNotNull(projects);
        Assert.assertEquals(50, projects.size());
    }

    @Test
    public void testEnumNinFilter() {

        QueryFilter qf = new QueryFilter();
        qf.setField("status");
        qf.setOperation(FilterOperation.NIN);
        qf.getValues().add("ACTIVE");
        qf.getValues().add("INACTIVE");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<Project> projects = em.createNamedQuery("Project.getAll").getResultList();

        projects = StreamUtils.queryEntities(projects, q);

        Assert.assertNotNull(projects);
        Assert.assertEquals(0, projects.size());
    }

    @Test
    public void testBooleanFilter() {

        QueryFilter qf = new QueryFilter();
        qf.setField("confirmed");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("true");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(55, users.size());
    }

    @Test
    public void testBooleanInFilter() {

        QueryFilter qf = new QueryFilter();
        qf.setField("confirmed");
        qf.setOperation(FilterOperation.IN);
        qf.getValues().add("False");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(45, users.size());
    }

    @Test
    public void testBooleanNinFilter() {

        QueryFilter qf = new QueryFilter();
        qf.setField("confirmed");
        qf.setOperation(FilterOperation.NIN);
        qf.getValues().add("FALSE");
        qf.getValues().add("trUe");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(0, users.size());
    }

    @Test
    public void testUuidFilter() {

        QueryFilter qf = new QueryFilter();
        qf.setField("publicId");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("b3781a0e-fb85-45d1-bd5f-d38802a546d2");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<Project> projects = em.createNamedQuery("Project.getAll").getResultList();

        projects = StreamUtils.queryEntities(projects, q);

        Assert.assertNotNull(projects);
        Assert.assertEquals(0, projects.size());
    }

    @Test
    public void testUuidInFilter() {

        QueryFilter qf = new QueryFilter();
        qf.setField("publicId");
        qf.setOperation(FilterOperation.IN);
        qf.getValues().add("b3781a0e-fb85-45d1-bd5f-d38802a546d2");
        qf.getValues().add("6a749969-9990-4ef0-856a-7e2533b6dc9e");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<Project> projects = em.createNamedQuery("Project.getAll").getResultList();
        projects = StreamUtils.queryEntities(projects, q);

        Assert.assertNotNull(projects);
        Assert.assertEquals(0, projects.size());
    }

    @Test
    public void testUuidNinFilter() {

        QueryFilter qf = new QueryFilter();
        qf.setField("publicId");
        qf.setOperation(FilterOperation.NIN);
        qf.getValues().add("b3781a0e-fb85-45d1-bd5f-d38802a546d2");
        qf.getValues().add("6a749969-9990-4ef0-856a-7e2533b6dc9e");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<Project> projects = em.createNamedQuery("Project.getAll").getResultList();
        projects = StreamUtils.queryEntities(projects, q);

        Assert.assertNotNull(projects);
        Assert.assertEquals(0, projects.size());
    }

    @Test(expected = InvalidFieldValueException.class)
    public void testEnumNonexistantFilter() {

        QueryFilter qf = new QueryFilter();
        qf.setField("status");
        qf.setOperation(FilterOperation.EQ);
        qf.setValue("NONACTIVE");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<Project> projects = em.createNamedQuery("Project.getAll").getResultList();

        StreamUtils.queryEntities(projects, q);

        Assert.fail("No exception was thrown");
    }

    @Test
    public void testIsNullFilter() {

        QueryFilter qf = new QueryFilter();
        qf.setField("description");
        qf.setOperation(FilterOperation.ISNULL);
        qf.setValue("");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<Project> projects = em.createNamedQuery("Project.getAll").getResultList();
        projects = StreamUtils.queryEntities(projects, q);

        Assert.assertNotNull(projects);
        Assert.assertEquals(1, projects.size());
    }

    @Test
    public void testIsNotNullFilter() {

        QueryFilter qf = new QueryFilter();
        qf.setField("description");
        qf.setOperation(FilterOperation.ISNOTNULL);
        qf.setValue("");

        QueryParameters q = new QueryParameters();
        q.getFilters().add(qf);

        List<Project> projects = em.createNamedQuery("Project.getAll").getResultList();

        projects = StreamUtils.queryEntities(projects, q);

        Assert.assertNotNull(projects);
        Assert.assertEquals(99, projects.size());
    }


    @Test
    public void ignoredFieldShouldReturnUnchangedResult() {

        String ignoredFieldName = "userIgnoredField";

        QueryParameters q = new QueryParameters();

        q.getFilters().add(new QueryFilter(ignoredFieldName, FilterOperation.EQ, "customValue"));

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
    }

    @Test
    public void ignoredFieldOnOneToOneShouldReturnUnchangedResult() {

        String ignoredFieldName = "career.careerIgnoreField";

        QueryParameters q = new QueryParameters();

        q.getFilters().add(new QueryFilter(ignoredFieldName, FilterOperation.EQ, "customValue"));

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
    }

    @Test
    public void includeIgnoreOnMultipleFieldsOnOneToOneShouldReturnUnchangedResult() {

        String ignoredFieldName = "career.careerIgnoreField";

        QueryParameters q = new QueryParameters();

        //joins on other than ignored fields must be preserved
        q.getFilters().add(new QueryFilter("projects.name", FilterOperation.EQ, "Goldenrod"));
        q.getFilters().add(new QueryFilter("career.years", FilterOperation.EQ, "5"));
        q.getFilters().add(new QueryFilter(ignoredFieldName, FilterOperation.EQ, "customValue"));

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();

        users = StreamUtils.queryEntities(users, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(1, users.size());
    }

    @Test(expected = NoSuchEntityFieldException.class)
    public void unknownFilterFieldShouldReturnException() {

        String ignoredFieldName = "customIgnoredField2";

        QueryParameters q = new QueryParameters();

        q.getFilters().add(new QueryFilter(ignoredFieldName, FilterOperation.EQ, "customValue"));

        List<User> users = em.createNamedQuery("User.getAll", User.class).getResultList();
        StreamUtils.queryEntities(users, q);
    }

}
