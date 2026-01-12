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
        // Create query with a single filter using new API
        QueryParameters query = QueryParameters.query("filter=username:eq:test").build();

        Assert.assertEquals(1, query.getFilterValues().size());

        // Remove the filter
        query.removeFilterParameter("username");

        // Verify filter is removed
        Assert.assertEquals(0, query.getFilterValues().size());
        Assert.assertNull(query.getFilterExpression());
    }

    @Test
    public void testRemoveFilterParameterMultipleFilters() {
        // Create query with multiple filters on different fields using new API
        QueryParameters query = QueryParameters
                .query("filter=username:eq:test AND email:like:test@example.com AND age:gt:18")
                .build();

        Assert.assertEquals(3, query.getFilterValues().size());

        // Remove one filter
        query.removeFilterParameter("username");

        // Verify only the specified filter is removed
        Assert.assertEquals(2, query.getFilterValues().size());
        List<QueryFilter> remainingFilters = query.getFilterValues();
        Assert.assertTrue(remainingFilters.stream().noneMatch(f -> "username".equals(f.getField())));
        Assert.assertTrue(remainingFilters.stream().anyMatch(f -> "email".equals(f.getField())));
        Assert.assertTrue(remainingFilters.stream().anyMatch(f -> "age".equals(f.getField())));
    }

    @Test
    public void testRemoveFilterParameterMultipleSameField() {
        // Create query with multiple filters on the same field using new API
        QueryParameters query = QueryParameters
                .query("filter=status:eq:active OR status:eq:pending")
                .build();

        Assert.assertEquals(2, query.getFilterValues().size());

        // Remove all filters for this field
        query.removeFilterParameter("status");

        // Verify all filters for the field are removed
        Assert.assertEquals(0, query.getFilterValues().size());
        Assert.assertNull(query.getFilterExpression());
    }

    @Test
    public void testRemoveFilterParameterNonExistentField() {
        // Create query with a filter using new API
        QueryParameters query = QueryParameters.query("filter=username:eq:test").build();

        Assert.assertEquals(1, query.getFilterValues().size());

        // Try to remove a non-existent filter
        query.removeFilterParameter("nonexistent");

        // Verify original filter remains
        Assert.assertEquals(1, query.getFilterValues().size());
        Assert.assertEquals("username", query.getFilterValues().get(0).getField());
    }

    @Test
    public void testRemoveFilterParameterNullField() {
        // Create query with a filter using new API
        QueryParameters query = QueryParameters.query("filter=username:eq:test").build();

        Assert.assertEquals(1, query.getFilterValues().size());

        // Try to remove with null field (should not throw exception)
        query.removeFilterParameter(null);

        // Verify original filter remains
        Assert.assertEquals(1, query.getFilterValues().size());
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
    public void testRemoveFilterParameterFromComplexExpression() {
        // Create query with complex nested expression: (username AND email) OR age
        QueryParameters query = QueryParameters
                .query("filter=(username:eq:test AND email:like:@example.com) OR age:gt:18")
                .build();

        Assert.assertEquals(3, query.getFilterValues().size());

        // Remove username - should leave "email OR age"
        query.removeFilterParameter("username");

        List<QueryFilter> remainingFilters = query.getFilterValues();
        Assert.assertEquals(2, remainingFilters.size());
        Assert.assertTrue(remainingFilters.stream().anyMatch(f -> "email".equals(f.getField())));
        Assert.assertTrue(remainingFilters.stream().anyMatch(f -> "age".equals(f.getField())));
        Assert.assertFalse(remainingFilters.stream().anyMatch(f -> "username".equals(f.getField())));
    }

    @Test
    public void testRemoveFilterParameterCollapseTree() {
        // Create query: username AND email
        QueryParameters query = QueryParameters
                .query("filter=username:eq:test AND email:like:@example.com")
                .build();

        Assert.assertEquals(2, query.getFilterValues().size());

        // Remove username - should collapse to just email (no AND needed)
        query.removeFilterParameter("username");

        Assert.assertEquals(1, query.getFilterValues().size());
        Assert.assertEquals("email", query.getFilterValues().get(0).getField());

        // The tree should be collapsed to a single leaf node
        Assert.assertNotNull(query.getFilterExpression());
        Assert.assertTrue(query.getFilterExpression().isLeaf());
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
        // Create query with filters using new API
        QueryParameters query = QueryParameters
                .query("filter=username:eq:test AND email:like:test@example.com")
                .build();

        Assert.assertEquals(2, query.getFilterValues().size());

        // Remove a filter
        query.removeFilterParameter("username");

        // Get filters again
        List<QueryFilter> filters = query.getFilterValues();

        Assert.assertNotNull(filters);
        Assert.assertEquals(1, filters.size());
        Assert.assertEquals("email", filters.get(0).getField());
    }
}
