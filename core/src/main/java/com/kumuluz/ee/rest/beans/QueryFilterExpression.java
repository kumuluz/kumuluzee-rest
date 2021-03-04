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
import com.kumuluz.ee.rest.interfaces.Expression;

import java.util.ArrayList;
import java.util.List;

public class QueryFilterExpression implements Expression<QueryFilter, FilterExpressionOperation, QueryFilterExpression> {

    /**
     * Expression value
     */
    private final QueryFilter value;

    /**
     * Expression operation, AND or OR
     */
    private final FilterExpressionOperation operation;

    private final QueryFilterExpression left;
    private final QueryFilterExpression right;

    public QueryFilterExpression(QueryFilter value) {
        this.value = value;
        operation = null;
        left = null;
        right = null;
    }

    public QueryFilterExpression(FilterExpressionOperation operation, QueryFilterExpression left, QueryFilterExpression right) {
        value = null;
        this.operation = operation;
        this.left = left;
        this.right = right;
    }

    @Override
    public QueryFilter value() {
        return value;
    }

    @Override
    public FilterExpressionOperation operation() {
        return operation;
    }

    @Override
    public QueryFilterExpression left() {
        return left;
    }

    @Override
    public QueryFilterExpression right() {
        return right;
    }

    public boolean isLeaf() {
        return value != null && operation == null && left == null && right == null;
    }

    public boolean isEmptyLeaf() {
        return value == null && operation == null && left == null && right == null;
    }

    /**
     * A utility method for retrieving a list of all leaf values of this expressions
     *
     * @return List of leaf values
     */
    public List<QueryFilter> getAllValues() {
        List<QueryFilter> values = new ArrayList<>();

        if (isLeaf()) {
            values.add(value);
            return values;
        }

        if (left != null) {
            values.addAll(left.getAllValues());
        }
        if (right != null) {
            values.addAll(right.getAllValues());
        }

        return values;
    }
}
