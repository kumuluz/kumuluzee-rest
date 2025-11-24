/*
 *  Copyright (c) 2014-2017 Kumuluz and/or its affiliates
 *  and other contributors as indicated by the @author tags and
 *  the contributor list.
 *
 *  Licensed under the MIT License (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://opensource.org/licenses/MIT
 *
 *  The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND, express or
 *  implied, including but not limited to the warranties of merchantability,
 *  fitness for a particular purpose and noninfringement. in no event shall the
 *  authors or copyright holders be liable for any claim, damages or other
 *  liability, whether in an action of contract, tort or otherwise, arising from,
 *  out of or in connection with the software or the use or other dealings in the
 *  software. See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.kumuluz.ee.rest.beans;

import com.kumuluz.ee.rest.enums.FilterExpressionOperation;
import com.kumuluz.ee.rest.utils.QueryStringBuilder;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tilen Faganel
 */
public class QueryParameters implements Serializable {

    private final static long serialVersionUID = 1L;

    private Boolean count;

    private Long limit;
    private Long offset;

    private List<QueryOrder> order;
    private List<String> fields;
    private List<QueryFilter> filters;
    private QueryFilterExpression filterExpression;

    public boolean getCount() {
        if (count == null) {
            return true;
        }

        return count;
    }

    public void setCount(Boolean count) {
        this.count = count;
    }

    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit.longValue();
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset.longValue();
    }

    public List<QueryOrder> getOrder() {

        if (order == null)
            order = new ArrayList<>();

        return order;
    }

    public void setOrder(List<QueryOrder> order) {
        this.order = order;
    }

    public List<String> getFields() {

        if (fields == null)
            fields = new ArrayList<>();

        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    /**
     * @deprecated Will be removed in future releases. Use
     * {@link #addFilterExpression(FilterExpressionOperation, QueryFilterExpression)} ()} instead.
     */
    @Deprecated
    public void addFilter(QueryFilter filter) {
        if (filter != null) {
            getFilters().add(filter);
        }
    }

    /**
     * Removes all filter parameters with the specified field name from the filter expression tree.
     * This method rebuilds the filter expression tree, excluding any filters that match the given field.
     * If all filters are removed, the filter expression is set to null.
     *
     * @param field the field name of the filters to remove
     */
    public void removeFilterParameter(String field) {
        if (field == null) {
            return;
        }

        if (filterExpression != null) {
            filterExpression = removeFromExpression(filterExpression, field);
        }
    }

    /**
     * Recursively removes all filters with the specified field name from the expression tree.
     * Returns null if the entire subtree should be removed.
     *
     * @param expression the expression to process
     * @param field the field name to remove
     * @return the rebuilt expression without the specified field, or null if removed
     */
    private QueryFilterExpression removeFromExpression(QueryFilterExpression expression, String field) {
        if (expression == null) {
            return null;
        }

        // If this is a leaf node with the matching field, remove it
        if (expression.isLeaf()) {
            if (expression.value() != null && field.equals(expression.value().getField())) {
                return null;  // Remove this node
            }
            return expression;  // Keep this node
        }

        // If this is a branch node, recursively process left and right
        QueryFilterExpression newLeft = removeFromExpression(expression.left(), field);
        QueryFilterExpression newRight = removeFromExpression(expression.right(), field);

        // If both sides are removed, remove this entire branch
        if (newLeft == null && newRight == null) {
            return null;
        }

        // If one side is removed, return the other side (collapse the tree)
        if (newLeft == null) {
            return newRight;
        }
        if (newRight == null) {
            return newLeft;
        }

        // Both sides still exist, reconstruct the branch with the same operation
        return new QueryFilterExpression(expression.operation(), newLeft, newRight);
    }

    /**
     * @deprecated Will be removed in future releases. Use {@link #setFilterExpression(QueryFilterExpression)} ()}
     * instead.
     */
    @Deprecated
    public void setFilters(List<QueryFilter> filters) {
        this.filters = filters;
    }

    /**
     * @deprecated Will be removed in future releases. Use {@link #getFilterExpression()} instead.
     */
    @Deprecated
    public List<QueryFilter> getFilters() {

        if (filters == null)
            filters = new ArrayList<>();

        return filters;
    }

    public QueryFilterExpression addFilterExpression(FilterExpressionOperation operation, QueryFilterExpression queryFilterExpression) {
        filterExpression = new QueryFilterExpression(operation, filterExpression, queryFilterExpression);
        return filterExpression;
    }

    public QueryFilterExpression getFilterExpression() {
        return filterExpression;
    }

    /**
     * Safely returns all filter values from the filter expression.
     * Returns an empty list if filterExpression is null.
     *
     * @return list of QueryFilter values, never null
     */
    public List<QueryFilter> getFilterValues() {
        if (filterExpression == null) {
            return new ArrayList<>();
        }
        return filterExpression.getAllValues();
    }

    public void setFilterExpression(QueryFilterExpression filterExpression) {
        this.filterExpression = filterExpression;
    }

    // Static methods for creating the query builder

    public static QueryStringBuilder uri(URI uri) {
        return new QueryStringBuilder().uri(uri);
    }

    public static QueryStringBuilder uriEncoded(String uri) {
        return new QueryStringBuilder().uriEncoded(uri);
    }

    public static QueryStringBuilder uri(String uri) {
        return new QueryStringBuilder().uri(uri);
    }

    public static QueryStringBuilder queryEncoded(String queryString) {
        return new QueryStringBuilder().queryEncoded(queryString);
    }

    public static QueryStringBuilder query(String queryString) {
        return new QueryStringBuilder().query(queryString);
    }
}
