package com.kumuluz.ee.rest.test;

import com.kumuluz.ee.rest.beans.QueryFilter;
import com.kumuluz.ee.rest.enums.FilterOperation;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Tilen Faganel
 */
public class QueryFilterTest {

    @Test
    public void testFilterConstructorEmpty() {

        QueryFilter filter = new QueryFilter();

        Assert.assertNotNull(filter);
        Assert.assertNull(filter.getField());
        Assert.assertNull(filter.getOperation());
        Assert.assertNull(filter.getValue());
        Assert.assertNull(filter.getDateValue());
        Assert.assertNotNull(filter.getValues());
        Assert.assertEquals(0, filter.getValues().size());
    }

    @Test
    public void testFilterConstrutorNoValue() {

        QueryFilter filter = new QueryFilter("username", FilterOperation.EQ);

        Assert.assertNotNull(filter);
        Assert.assertEquals("username", filter.getField());
        Assert.assertEquals(FilterOperation.EQ, filter.getOperation());
        Assert.assertNull(filter.getValue());
        Assert.assertNull(filter.getDateValue());
        Assert.assertNotNull(filter.getValues());
        Assert.assertEquals(0, filter.getValues().size());
    }

    @Test
    public void testFilterConstrutorValue() {

        QueryFilter filter = new QueryFilter("username", FilterOperation.EQ, "test");

        Assert.assertNotNull(filter);
        Assert.assertEquals("username", filter.getField());
        Assert.assertEquals(FilterOperation.EQ, filter.getOperation());
        Assert.assertEquals("test", filter.getValue());
        Assert.assertNull(filter.getDateValue());
        Assert.assertNotNull(filter.getValues());
        Assert.assertEquals(0, filter.getValues().size());
    }

    @Test
    public void testFilterConstructorDate() {

        Date date = new Date();

        QueryFilter filter = new QueryFilter("username", FilterOperation.EQ, date);

        Assert.assertNotNull(filter);
        Assert.assertEquals("username", filter.getField());
        Assert.assertEquals(FilterOperation.EQ, filter.getOperation());
        Assert.assertNull(filter.getValue());
        Assert.assertEquals(date, filter.getDateValue());
        Assert.assertNotNull(filter.getValues());
        Assert.assertEquals(0, filter.getValues().size());
    }

    @Test
    public void testFilterConstructorList() {

        List<String> values = new ArrayList<>();
        values.add("test");

        QueryFilter filter = new QueryFilter("username", FilterOperation.EQ, values);

        Assert.assertNotNull(filter);
        Assert.assertEquals("username", filter.getField());
        Assert.assertEquals(FilterOperation.EQ, filter.getOperation());
        Assert.assertNull(filter.getValue());
        Assert.assertNull(filter.getDateValue());
        Assert.assertNotNull(filter.getValues());
        Assert.assertEquals(1, filter.getValues().size());
        Assert.assertTrue(values.equals(filter.getValues()));
    }
}
