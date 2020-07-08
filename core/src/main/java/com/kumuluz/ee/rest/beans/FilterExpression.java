package com.kumuluz.ee.rest.beans;

import com.kumuluz.ee.rest.enums.FilterExpressionOperation;
import org.parboiled.trees.ImmutableBinaryTreeNode;

import java.util.ArrayList;
import java.util.List;

public class FilterExpression extends ImmutableBinaryTreeNode<FilterExpression> {

    private QueryFilter value;
    private FilterExpressionOperation operation;

    public FilterExpression(QueryFilter value) {
        super(null, null);
        this.value = value;
    }

    public FilterExpression(FilterExpressionOperation operation, FilterExpression left, FilterExpression right) {
        super(left, right);
        this.operation = operation;
    }

    public boolean isLeaf() {
        return value != null && left() == null && right() == null;
    }

    public boolean isEmptyLeaf() {
        return value == null && left() == null && right() == null;
    }

    public QueryFilter value() {
        return value;
    }

    public FilterExpressionOperation operation() {
        return operation;
    }

    public List<QueryFilter> getAllValues() {
        List<QueryFilter> values = new ArrayList<>();

        if (isLeaf()) {
            values.add(value);
            return values;
        }

        if (left() != null) {
            values.addAll(left().getAllValues());
        }
        if (right() != null) {
            values.addAll(right().getAllValues());
        }

        return values;
    }
}

