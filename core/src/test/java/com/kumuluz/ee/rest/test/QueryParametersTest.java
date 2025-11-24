package com.kumuluz.ee.rest.test;

import com.kumuluz.ee.rest.beans.QueryFilter;
import com.kumuluz.ee.rest.beans.QueryFilterExpression;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.enums.FilterExpressionOperation;
import com.kumuluz.ee.rest.enums.FilterOperation;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Tests for QueryParameters methods.
 *
 * @author cen1
 */
public class QueryParametersTest {

    @Test
    public void testRemoveFilterParameterSingleFilter() {
        // Create query with a single filter
        QueryParameters query = new QueryParameters();
        query.addFilter(new QueryFilter("username", FilterOperation.EQ, "test"));

        Assert.assertEquals(1, query.getFilters().size());

        // Remove the filter
        query.removeFilterParameter("username");

        // Verify filter is removed
        Assert.assertEquals(0, query.getFilters().size());
    }

    @Test
    public void testRemoveFilterParameterMultipleFilters() {
        // Create query with multiple filters on different fields
        QueryParameters query = new QueryParameters();
        query.addFilter(new QueryFilter("username", FilterOperation.EQ, "test"));
        query.addFilter(new QueryFilter("email", FilterOperation.LIKE, "test@example.com"));
        query.addFilter(new QueryFilter("age", FilterOperation.GT, "18"));

        Assert.assertEquals(3, query.getFilters().size());

        // Remove one filter
        query.removeFilterParameter("username");

        // Verify only the specified filter is removed
        Assert.assertEquals(2, query.getFilters().size());
        List<QueryFilter> remainingFilters = query.getFilters();
        Assert.assertTrue(remainingFilters.stream().noneMatch(f -> "username".equals(f.getField())));
        Assert.assertTrue(remainingFilters.stream().anyMatch(f -> "email".equals(f.getField())));
        Assert.assertTrue(remainingFilters.stream().anyMatch(f -> "age".equals(f.getField())));
    }

    @Test
    public void testRemoveFilterParameterMultipleSameField() {
        // Create query with multiple filters on the same field using deprecated API
        QueryParameters query = new QueryParameters();
        query.addFilter(new QueryFilter("status", FilterOperation.EQ, "active"));
        query.addFilter(new QueryFilter("status", FilterOperation.EQ, "pending"));

        Assert.assertEquals(2, query.getFilters().size());

        // Remove all filters for this field
        query.removeFilterParameter("status");

        // Verify all filters for the field are removed
        Assert.assertEquals(0, query.getFilters().size());
    }

    @Test
    public void testRemoveFilterParameterNonExistentField() {
        // Create query with a filter using deprecated API
        QueryParameters query = new QueryParameters();
        query.addFilter(new QueryFilter("username", FilterOperation.EQ, "test"));

        Assert.assertEquals(1, query.getFilters().size());

        // Try to remove a non-existent filter
        query.removeFilterParameter("nonexistent");

        // Verify original filter remains
        Assert.assertEquals(1, query.getFilters().size());
        Assert.assertEquals("username", query.getFilters().get(0).getField());
    }

    @Test
    public void testRemoveFilterParameterNullField() {
        // Create query with a filter using deprecated API
        QueryParameters query = new QueryParameters();
        query.addFilter(new QueryFilter("username", FilterOperation.EQ, "test"));

        Assert.assertEquals(1, query.getFilters().size());

        // Try to remove with null field (should not throw exception)
        query.removeFilterParameter(null);

        // Verify original filter remains
        Assert.assertEquals(1, query.getFilters().size());
    }

    @Test
    public void testRemoveFilterParameterEmptyFilters() {
        // Create query with no filters
        QueryParameters query = new QueryParameters();

        // Try to remove a filter (should not throw exception)
        query.removeFilterParameter("username");

        // Verify still no filters
        Assert.assertEquals(0, query.getFilterValues().size());
    }

    @Test
    public void testGetFilterValuesWithFilters() {
        // Create query with multiple filters using AND
        QueryParameters query = QueryParameters
                .query("filter=username:eq:test AND email:like:test@example.com")
                .build();

        List<QueryFilter> filters = query.getFilterValues();

        Assert.assertNotNull(filters);
        Assert.assertEquals(2, filters.size());
        Assert.assertTrue(filters.stream().anyMatch(f -> "username".equals(f.getField())));
        Assert.assertTrue(filters.stream().anyMatch(f -> "email".equals(f.getField())));
    }

    @Test
    public void testGetFilterValuesEmptyExpression() {
        // Create empty query
        QueryParameters query = new QueryParameters();

        List<QueryFilter> filters = query.getFilterValues();

        // Should return empty list, not null
        Assert.assertNotNull(filters);
        Assert.assertEquals(0, filters.size());
    }

    @Test
    public void testGetFilterValuesNullExpression() {
        // Create query and explicitly set null expression
        QueryParameters query = new QueryParameters();
        query.setFilterExpression(null);

        List<QueryFilter> filters = query.getFilterValues();

        // Should return empty list, not null
        Assert.assertNotNull(filters);
        Assert.assertEquals(0, filters.size());
    }

    @Test
    public void testGetFilterValuesComplexExpression() {
        // Create query with complex expression (AND/OR)
        QueryParameters query = QueryParameters
                .query("filter=(username:eq:test AND email:like:test@) OR age:gt:18")
                .build();

        List<QueryFilter> filters = query.getFilterValues();

        Assert.assertNotNull(filters);
        Assert.assertEquals(3, filters.size());
        Assert.assertTrue(filters.stream().anyMatch(f -> "username".equals(f.getField())));
        Assert.assertTrue(filters.stream().anyMatch(f -> "email".equals(f.getField())));
        Assert.assertTrue(filters.stream().anyMatch(f -> "age".equals(f.getField())));
    }

    @Test
    public void testGetFilterValuesAfterRemove() {
        // Create query with filters using deprecated API
        QueryParameters query = new QueryParameters();
        QueryFilter filter1 = new QueryFilter("username", FilterOperation.EQ, "test");
        QueryFilter filter2 = new QueryFilter("email", FilterOperation.LIKE, "test@example.com");

        query.addFilter(filter1);
        query.addFilter(filter2);

        Assert.assertEquals(2, query.getFilters().size());

        // Remove a filter
        query.removeFilterParameter("username");

        // Get filters again
        List<QueryFilter> filters = query.getFilters();

        Assert.assertNotNull(filters);
        Assert.assertEquals(1, filters.size());
        Assert.assertEquals("email", filters.get(0).getField());
    }
}
