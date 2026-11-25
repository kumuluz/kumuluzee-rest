package com.kumuluz.ee.rest.test;

import com.kumuluz.ee.rest.beans.QueryFilter;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.enums.FilterOperation;
import com.kumuluz.ee.rest.enums.QueryFormatError;
import com.kumuluz.ee.rest.exceptions.InvalidFieldValueException;
import com.kumuluz.ee.rest.exceptions.QueryFormatException;
import com.kumuluz.ee.rest.test.entities.LegacyTemporal;
import com.kumuluz.ee.rest.test.entities.Project;
import com.kumuluz.ee.rest.test.entities.User;
import com.kumuluz.ee.rest.test.utils.JpaUtil;
import com.kumuluz.ee.rest.utils.JPAUtils;
import jakarta.persistence.EntityManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filters must either be applied or rejected, never silently dropped (a dropped filter returns more rows than the
 * client asked for). Covers:
 * <ul>
 *     <li>comparison operators on {@code LocalTime} (https://github.com/kumuluz/kumuluzee-rest/issues/22)</li>
 *     <li>operations that are not applicable to the field type</li>
 *     <li>{@code @RestIgnore}d fields inside {@code or} expressions</li>
 *     <li>dotted {@code @RestMapping} names (https://github.com/kumuluz/kumuluzee-rest/issues/16)</li>
 * </ul>
 * Seed data: 100 users; registration times are 04:20:11 (96 users), 05:20:11, 06:20:11, 10:20:11 and 12:20:11;
 * 100 projects belonging to 60 distinct users; exactly one user is named Sandra.
 */
@RunWith(Parameterized.class)
public class JPAUtilsFilterSemanticsTest {

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

    private long countUsers(String queryString) {
        return JPAUtils.queryEntitiesCount(em, User.class, QueryParameters.query(queryString).build());
    }

    private long countProjects(String queryString) {
        return JPAUtils.queryEntitiesCount(em, Project.class, QueryParameters.query(queryString).build());
    }

    private void assertUnsupported(String filter, String field) {
        try {
            long count = countUsers("filter=" + filter);
            Assert.fail("Expected QueryFormatException for '" + filter + "', but the query returned " + count + " rows");
        } catch (QueryFormatException e) {
            Assert.assertEquals(field, e.getField());
            Assert.assertEquals(QueryFormatError.UNSUPPORTED_OPERATION, e.getReason());
        }
    }

    // --- #22: comparison operators on LocalTime

    @Test
    public void localTimeGreaterThanOrEqual() {
        Assert.assertEquals(4, countUsers("filter=registrationTime:gte:'05:00:00'"));
    }

    @Test
    public void localTimeGreaterThan() {
        Assert.assertEquals(2, countUsers("filter=registrationTime:gt:'06:20:11'"));
    }

    @Test
    public void localTimeLessThan() {
        Assert.assertEquals(96, countUsers("filter=registrationTime:lt:'05:20:11'"));
    }

    @Test
    public void localTimeLessThanOrEqual() {
        Assert.assertEquals(97, countUsers("filter=registrationTime:lte:'05:20:11'"));
    }

    @Test
    public void localTimeBetween() {
        Assert.assertEquals(3, countUsers("filter=registrationTime:between:['05:00:00','11:00:00']"));
    }

    // --- comparison operators on primitive numeric fields (User.loginCount is an int, 0 for every user)

    @Test
    public void primitiveIntGreaterThan() {
        Assert.assertEquals(0, countUsers("filter=loginCount:gt:0"));
        Assert.assertEquals(100, countUsers("filter=loginCount:gt:-1"));
    }

    @Test
    public void primitiveIntLessThanOrEqual() {
        Assert.assertEquals(100, countUsers("filter=loginCount:lte:0"));
        Assert.assertEquals(0, countUsers("filter=loginCount:lt:0"));
    }

    @Test
    public void primitiveIntBetween() {
        Assert.assertEquals(100, countUsers("filter=loginCount:between:[0,5]"));
    }

    // --- operations that cannot be applied to the field type are rejected

    @Test
    public void likeOnNumberIsRejected() {
        assertUnsupported("score:like:'1%'", "score");
    }

    @Test
    public void greaterThanOnBooleanIsRejected() {
        assertUnsupported("confirmed:gt:true", "confirmed");
    }

    @Test
    public void caseInsensitiveEqualsOnNumberIsRejected() {
        assertUnsupported("id:eqic:'1'", "id");
    }

    @Test
    public void caseInsensitiveInOnNumberIsRejected() {
        assertUnsupported("id:inic:['1','2']", "id");
    }

    @Test
    public void valueComparisonOnAssociationIsRejected() {
        assertUnsupported("career:eq:1", "career");
    }

    @Test
    public void valueComparisonOnCollectionIsRejected() {
        assertUnsupported("projects:eq:1", "projects");
    }

    @Test
    public void unsupportedOperationInsideOrIsRejectedInsteadOfMatchingEverything() {
        assertUnsupported("firstname:eq:'Sandra' or score:like:'1%'", "score");
    }

    // --- @RestIgnore'd fields are left out of the expression, without widening "or" to everything

    @Test
    public void ignoredFieldAloneReturnsEverything() {
        Assert.assertEquals(100, countUsers("filter=userIgnoredField:eq:'x'"));
    }

    @Test
    public void ignoredFieldInsideAndIsLeftOut() {
        Assert.assertEquals(1, countUsers("filter=firstname:eq:'Sandra' and userIgnoredField:eq:'x'"));
    }

    @Test
    public void ignoredFieldInsideOrIsLeftOut() {
        Assert.assertEquals(1, countUsers("filter=firstname:eq:'Sandra' or userIgnoredField:eq:'x'"));
    }

    @Test
    public void ignoredFieldInsideNestedOrIsLeftOut() {
        Assert.assertEquals(1, countUsers("filter=(userIgnoredField:eq:'x' or firstname:eq:'Sandra') and id:gt:0"));
    }

    // --- #16: dotted @RestMapping name on a basic field (Project.publicId is mapped as "public.id")

    @Test
    public void filterOnDottedRestMapping() {
        Assert.assertEquals(100, countProjects("filter=public.id:isnotnull"));
    }

    @Test
    public void filterOnDottedRestMappingWithValue() {
        Project project = JPAUtils.queryEntities(em, Project.class, QueryParameters.query("filter=id:eq:7").build()).get(0);

        List<Project> found = JPAUtils.queryEntities(em, Project.class,
                QueryParameters.query("filter=public.id:eq:'" + project.getPublicId() + "'").build());

        Assert.assertEquals(1, found.size());
        Assert.assertEquals(Integer.valueOf(7), found.get(0).getId());
    }

    @Test
    public void originalFieldNameStillWorksNextToDottedRestMapping() {
        Assert.assertEquals(100, countProjects("filter=publicId:isnotnull"));
    }

    @Test
    public void orderByDottedRestMapping() {
        List<Integer> byMapping = JPAUtils.queryEntities(em, Project.class, QueryParameters.query("order=public.id DESC").build())
                .stream().map(Project::getId).collect(Collectors.toList());
        List<Integer> byField = JPAUtils.queryEntities(em, Project.class, QueryParameters.query("order=publicId DESC").build())
                .stream().map(Project::getId).collect(Collectors.toList());

        Assert.assertEquals(100, byMapping.size());
        Assert.assertEquals(byField, byMapping);
    }

    @Test
    public void dottedRestMappingOnJoinedEntity() {
        Assert.assertEquals(60, countUsers("filter=projects.public.id:isnotnull"));
    }

    @Test
    public void fieldsSelectionWithDottedRestMapping() {
        List<Project> projects = JPAUtils.queryEntities(em, Project.class, QueryParameters.query("fields=id,public.id&filter=id:eq:7").build());

        Assert.assertEquals(1, projects.size());
        Assert.assertNotNull(projects.get(0).getPublicId());
        Assert.assertNull(projects.get(0).getName());
    }

    @Test
    public void nestedFieldsSelectionWithDottedRestMapping() {
        List<User> users = JPAUtils.queryEntities(em, User.class,
                QueryParameters.query("fields=projects.public.id&filter=projects.id:eq:7").build());

        // the filter selects the user owning project 7 (user 99, who has 2 projects)
        Assert.assertEquals(1, users.size());
        Assert.assertEquals(Integer.valueOf(99), users.get(0).getId());
        Assert.assertEquals(2, users.get(0).getProjects().size());
        Assert.assertEquals(2, users.get(0).getProjects().stream().map(Project::getPublicId).distinct().count());
        users.get(0).getProjects().forEach(p -> {
            Assert.assertNotNull(p.getPublicId());
            Assert.assertNull(p.getName());
        });
    }

    // --- empty clauses (form inputs left empty) are left out; the other clauses still apply

    @Test
    public void trailingEmptyClauseKeepsTheValidClause() {
        Assert.assertEquals(1, countUsers("filter=id:eq:7 and firstname:eq:"));
    }

    @Test
    public void emptyClausesFromFormInputs() {
        Assert.assertEquals(1, countUsers("filter=firstname:eqic: id:eq:7 lastname:eqic:"));
    }

    @Test
    public void emptyClauseInsideOrDoesNotMatchEverything() {
        Assert.assertEquals(1, countUsers("filter=firstname:eqic: or id:eq:7"));
    }

    @Test
    public void onlyEmptyClausesReturnEverything() {
        Assert.assertEquals(100, countUsers("filter=firstname:eqic: lastname:eqic:"));
    }

    // --- list operands: same result on every provider

    @Test
    public void scalarOperandForNotInIsRejected() {
        try {
            long count = countUsers("filter=id:nin:7");
            Assert.fail("Expected QueryFormatException, the query returned " + count + " rows");
        } catch (QueryFormatException e) {
            Assert.assertEquals(QueryFormatError.MALFORMED, e.getReason());
        }
    }

    @Test
    public void inWithEmptyListMatchesNothing() {
        Assert.assertEquals(0, countUsers("filter=id:in:[]"));
    }

    @Test
    public void notInWithEmptyListMatchesEverything() {
        Assert.assertEquals(100, countUsers("filter=id:nin:[]"));
    }

    @Test
    public void betweenWithEqualBounds() {
        Assert.assertEquals(1, countUsers("filter=id:between:[7,7]"));
        Assert.assertEquals(99, countUsers("filter=id:nbetween:[7,7]"));
    }

    @Test
    public void programmaticBetweenWithOneBoundIsRejected() {
        QueryParameters q = new QueryParameters();
        q.getFilters().add(new QueryFilter("id", FilterOperation.BETWEEN,
                java.util.Collections.singletonList("7")));
        try {
            long count = JPAUtils.queryEntitiesCount(em, User.class, q);
            Assert.fail("Expected QueryFormatException, the query returned " + count + " rows");
        } catch (QueryFormatException e) {
            Assert.assertEquals("id", e.getField());
            Assert.assertEquals(QueryFormatError.MALFORMED, e.getReason());
        }
    }

    // --- #22 as reported: java.util.Date with @Temporal(TIME) / @Temporal(DATE) and time-only / date-only values

    private long countLegacy(String queryString) {
        return JPAUtils.queryEntitiesCount(em, LegacyTemporal.class, QueryParameters.query(queryString).build());
    }

    @Test
    public void temporalTimeDateWithTimeOnlyValue() {
        // start times: 04:20:11, 05:20:11, 06:20:11, 10:20:11, 12:20:11
        Assert.assertEquals(4, countLegacy("filter=startTime:gte:'05:00:00'"));
        Assert.assertEquals(1, countLegacy("filter=startTime:eq:'06:20:11'"));
        Assert.assertEquals(1, countLegacy("filter=startTime:lt:'05:00:00'"));
        Assert.assertEquals(3, countLegacy("filter=startTime:between:['05:00:00','11:00:00']"));
    }

    @Test
    public void temporalDateDateWithDateOnlyValue() {
        // start dates: 2015-04-09, 2015-04-19, 2015-04-29 (2x), 2015-04-30
        Assert.assertEquals(2, countLegacy("filter=startDate:lt:'2015-04-28'"));
        Assert.assertEquals(2, countLegacy("filter=startDate:eq:'2015-04-29'"));
    }

    // --- programmatic list operations: a scalar or missing operand is rejected, an explicit [] keeps its meaning

    private void assertMalformed(QueryFilter filter) {
        QueryParameters q = new QueryParameters();
        q.getFilters().add(filter);
        try {
            long count = JPAUtils.queryEntitiesCount(em, User.class, q);
            Assert.fail("Expected QueryFormatException, the query returned " + count + " rows");
        } catch (QueryFormatException e) {
            Assert.assertEquals(filter.getField(), e.getField());
            Assert.assertEquals(QueryFormatError.MALFORMED, e.getReason());
        }
    }

    private long countUsers(QueryFilter filter) {
        QueryParameters q = new QueryParameters();
        q.getFilters().add(filter);
        return JPAUtils.queryEntitiesCount(em, User.class, q);
    }

    @Test
    public void programmaticListOperationWithScalarIsRejected() {
        assertMalformed(new QueryFilter("id", FilterOperation.NIN, "7"));
        assertMalformed(new QueryFilter("id", FilterOperation.IN, "7"));
        assertMalformed(new QueryFilter("firstname", FilterOperation.INIC, "Sandra"));
        assertMalformed(new QueryFilter("firstname", FilterOperation.NINIC, "Sandra"));
        assertMalformed(new QueryFilter("id", FilterOperation.BETWEEN, "7"));
    }

    @Test
    public void programmaticListOperationWithoutOperandIsRejected() {
        assertMalformed(new QueryFilter("id", FilterOperation.NIN));
        assertMalformed(new QueryFilter("id", FilterOperation.IN));
    }

    @Test
    public void programmaticExplicitEmptyList() {
        Assert.assertEquals(0, countUsers(new QueryFilter("id", FilterOperation.IN, new ArrayList<>())));
        Assert.assertEquals(100, countUsers(new QueryFilter("id", FilterOperation.NIN, new ArrayList<>())));
        Assert.assertEquals(0, countUsers(new QueryFilter("firstname", FilterOperation.INIC, new ArrayList<>())));
        Assert.assertEquals(100, countUsers(new QueryFilter("firstname", FilterOperation.NINIC, new ArrayList<>())));
    }

    // --- java.util.Date values must fit the field's temporal mapping

    private void assertInvalidValue(Class<?> entity, String filter, String field) {
        try {
            long count = JPAUtils.queryEntitiesCount(em, entity, QueryParameters.query("filter=" + filter).build());
            Assert.fail("Expected InvalidFieldValueException for '" + filter + "', the query returned " + count + " rows");
        } catch (InvalidFieldValueException e) {
            Assert.assertEquals(field, e.getField());
        }
    }

    @Test
    public void temporalTimeRejectsFractionalSeconds() {
        // java.util.Date / java.sql.Time cannot carry them through JDBC; they used to be dropped silently
        assertInvalidValue(LegacyTemporal.class, "startTime:eq:'06:20:11.500'", "startTime");
        assertInvalidValue(LegacyTemporal.class, "startTime:gt:'06:20:11.5'", "startTime");
    }

    @Test
    public void temporalTimeAcceptsZeroFraction() {
        Assert.assertEquals(1, countLegacy("filter=startTime:eq:'06:20:11.000'"));
    }

    @Test
    public void temporalTimeRejectsDateOnlyValue() {
        assertInvalidValue(LegacyTemporal.class, "startTime:eq:'2015-04-29'", "startTime");
    }

    @Test
    public void temporalDateRejectsTimeOnlyValue() {
        assertInvalidValue(LegacyTemporal.class, "startDate:eq:'06:20:11'", "startDate");
    }

    @Test
    public void temporalDateStillAcceptsFullDateTime() {
        Assert.assertEquals(2, countLegacy("filter=startDate:eq:'2015-04-29T00:00:00Z'"));
    }

    @Test
    public void temporalTimestampRejectsDateOnlyAndTimeOnlyValues() {
        // User.createdAt is @Temporal(TIMESTAMP): only full date-times, as before
        assertInvalidValue(User.class, "createdAt:gt:'2015-01-01'", "createdAt");
        assertInvalidValue(User.class, "createdAt:gt:'10:00:00'", "createdAt");
    }

    // --- between: a quoted empty string is a valid bound

    @Test
    public void betweenWithQuotedEmptyStringBound() {
        Assert.assertEquals(57, countUsers("filter=lastname:between:['','M']"));
    }

    // --- reading a filter does not change its meaning

    @Test
    public void readingTheValuesOfAFilterWithoutOperandKeepsItInvalid() {
        QueryFilter filter = new QueryFilter("id", FilterOperation.NIN);
        Assert.assertEquals(0, filter.getValues().size());

        assertMalformed(filter);
    }

    @Test
    public void valuesAddedThroughTheGetterAreASuppliedList() {
        QueryFilter filter = new QueryFilter("id", FilterOperation.NIN);
        filter.getValues().add("7");

        Assert.assertEquals(99, countUsers(filter));
    }

    // --- @Temporal(TIME) / @Temporal(DATE): full date-times are checked and normalised like the short forms

    @Test
    public void temporalTimeRejectsFractionalSecondsInFullDateTime() {
        assertInvalidValue(LegacyTemporal.class, "startTime:eq:'2015-04-29T06:20:11.500Z'", "startTime");
    }

    @Test
    public void temporalTimeComparesTheTimeOfAFullDateTime() {
        // tests run in UTC; the date part is irrelevant for a time field
        Assert.assertEquals(1, countLegacy("filter=startTime:eq:'2015-04-29T06:20:11Z'"));
        Assert.assertEquals(4, countLegacy("filter=startTime:gte:'2015-04-29T05:00:00Z'"));
    }

    @Test
    public void temporalDateComparesTheDateOfAFullDateTime() {
        Assert.assertEquals(2, countLegacy("filter=startDate:eq:'2015-04-29T10:00:00Z'"));
    }

    @Test
    public void temporalDateBetweenWithMixedBoundForms() {
        // 2015-04-09, 2015-04-19, 2015-04-29 (2x)
        Assert.assertEquals(4, countLegacy("filter=startDate:between:['2015-04-09','2015-04-29T00:00:00Z']"));
    }

    @Test
    public void temporalTimeBetweenWithMixedBoundForms() {
        // 05:20:11, 06:20:11, 10:20:11
        Assert.assertEquals(3, countLegacy("filter=startTime:between:['05:00:00','2015-04-29T11:00:00Z']"));
    }

    // --- dt'...' date values get the same temporal validation and normalisation as plain values

    @Test
    public void dtValueOnTimeFieldRejectsFractionalSeconds() {
        assertInvalidValue(LegacyTemporal.class, "startTime:eq:dt'2015-04-29T06:20:11.500Z'", "startTime");
    }

    @Test
    public void dtValueOnTimeFieldComparesTheTimeOfDay() {
        Assert.assertEquals(1, countLegacy("filter=startTime:eq:dt'2015-04-29T06:20:11Z'"));
        Assert.assertEquals(4, countLegacy("filter=startTime:gte:dt'2015-04-29T05:00:00Z'"));
    }

    @Test
    public void dtValueOnDateFieldComparesTheDate() {
        Assert.assertEquals(2, countLegacy("filter=startDate:eq:dt'2015-04-29T10:00:00Z'"));
        Assert.assertEquals(2, countLegacy("filter=startDate:lt:dt'2015-04-28T23:00:00Z'"));
    }

    @Test
    public void dtValueOnTimestampFieldIsUnchanged() {
        Assert.assertEquals(33, countUsers("filter=createdAt:gt:dt'2015-01-01T00:00:00Z'"));
    }

    // --- programmatic java.sql.Date / Time / Timestamp values

    private long countLegacy(QueryFilter filter) {
        QueryParameters q = new QueryParameters();
        q.getFilters().add(filter);
        return JPAUtils.queryEntitiesCount(em, LegacyTemporal.class, q);
    }

    private void assertInvalidValue(Class<?> entity, QueryFilter filter) {
        QueryParameters q = new QueryParameters();
        q.getFilters().add(filter);
        try {
            long count = JPAUtils.queryEntitiesCount(em, entity, q);
            Assert.fail("Expected InvalidFieldValueException, the query returned " + count + " rows");
        } catch (InvalidFieldValueException e) {
            Assert.assertEquals(filter.getField(), e.getField());
        }
    }

    @Test
    public void programmaticSqlDateValue() {
        Assert.assertEquals(2, countLegacy(new QueryFilter("startDate", FilterOperation.EQ, java.sql.Date.valueOf("2015-04-29"))));
    }

    @Test
    public void programmaticSqlTimeValue() {
        Assert.assertEquals(1, countLegacy(new QueryFilter("startTime", FilterOperation.EQ, java.sql.Time.valueOf("06:20:11"))));
        Assert.assertEquals(4, countLegacy(new QueryFilter("startTime", FilterOperation.GTE, java.sql.Time.valueOf("05:00:00"))));
    }

    @Test
    public void programmaticSqlTimestampValueOnTimeField() {
        Assert.assertEquals(1, countLegacy(new QueryFilter("startTime", FilterOperation.EQ,
                java.sql.Timestamp.valueOf("2015-04-29 06:20:11"))));
        assertInvalidValue(LegacyTemporal.class, new QueryFilter("startTime", FilterOperation.EQ,
                java.sql.Timestamp.valueOf("2015-04-29 06:20:11.000001")));
    }

    // --- sub-millisecond precision is not lost on the way to the field validation

    @Test
    public void dtValueOnTimeFieldRejectsSubMillisecondFraction() {
        assertInvalidValue(LegacyTemporal.class, "startTime:eq:dt'2015-04-29T06:20:11.000001Z'", "startTime");
    }

    @Test
    public void timestampFieldKeepsSubMillisecondPrecision() {
        // exactly one user was created at 2014-07-10T21:55:06Z
        Assert.assertEquals(1, countUsers("filter=createdAt:eq:'2014-07-10T21:55:06Z'"));
        Assert.assertEquals(0, countUsers("filter=createdAt:eq:'2014-07-10T21:55:06.000001Z'"));
        Assert.assertEquals(0, countUsers("filter=createdAt:eq:dt'2014-07-10T21:55:06.000001Z'"));
        Assert.assertEquals(1, countUsers("filter=createdAt:lt:dt'2014-07-10T21:55:06.000001Z' and createdAt:gte:dt'2014-07-10T21:55:06Z'"));
    }

    @Test
    public void timestampRangeWithBoundsOfDifferentPrecision() {
        // exactly one user was created at 2014-07-10T21:55:06Z
        Assert.assertEquals(1, countUsers("filter=createdAt:between:['2014-07-10T21:55:06Z','2014-07-10T21:55:06.000001Z']"));
        Assert.assertEquals(99, countUsers("filter=createdAt:nbetween:['2014-07-10T21:55:06Z','2014-07-10T21:55:06.000001Z']"));
        Assert.assertEquals(0, countUsers("filter=createdAt:between:['2014-07-10T21:55:06.000001Z','2014-07-10T21:55:07Z']"));
    }
}
