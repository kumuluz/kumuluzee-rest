package com.kumuluz.ee.rest.test;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.enums.OrderDirection;
import com.kumuluz.ee.rest.enums.QueryFormatError;
import com.kumuluz.ee.rest.exceptions.QueryFormatException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tilen Faganel
 */
public class QueryStringBuilderOrderTest {

    @Test
    public void testQueryOrderObject() {

        QueryParameters query = new QueryParameters();

        Assert.assertNotNull(query.getOrder());
        Assert.assertEquals(0, query.getOrder().size());
    }

    @Test
    public void testSingleOrder() {

        QueryParameters query = QueryParameters.query("order=username DESC").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getOrder());
        Assert.assertEquals(1, query.getOrder().size());
        Assert.assertEquals("username", query.getOrder().get(0).getField());
        Assert.assertNotNull(query.getOrder().get(0).getOrder());
        Assert.assertEquals(OrderDirection.DESC, query.getOrder().get(0).getOrder());
    }

    @Test
    public void testSingleOrderWithPlus() {

        QueryParameters query = QueryParameters.query("order=username+DESC").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getOrder());
        Assert.assertEquals(1, query.getOrder().size());
        Assert.assertEquals("username", query.getOrder().get(0).getField());
        Assert.assertNotNull(query.getOrder().get(0).getOrder());
        Assert.assertEquals(OrderDirection.DESC, query.getOrder().get(0).getOrder());
    }

    @Test
    public void testOrderWithoutDirection() {

        QueryParameters query = QueryParameters.query("sort=username").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getOrder());
        Assert.assertEquals(1, query.getOrder().size());
        Assert.assertEquals("username", query.getOrder().get(0).getField());
        Assert.assertNotNull(query.getOrder().get(0).getOrder());
        Assert.assertEquals(OrderDirection.ASC, query.getOrder().get(0).getOrder());
    }

    @Test
    public void testEmptyOrder() {

        QueryParameters query = QueryParameters.query("order=").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getOrder());
        Assert.assertEquals(0, query.getOrder().size());
    }

    @Test
    public void testNoOrderField() {

        try {

            QueryParameters.query("order= ASC").build();
            Assert.fail("No exception was thrown");
        } catch (QueryFormatException e) {

            Assert.assertEquals("order", e.getField());
            Assert.assertEquals(QueryFormatError.MALFORMED, e.getReason());
        }
    }

    @Test
    public void testMalformedDirection() {

        try {

            QueryParameters.query("sort=lastname SOMETHING").build();
            Assert.fail("No exception was thrown");
        } catch (QueryFormatException e) {

            Assert.assertEquals("sort", e.getField());
            Assert.assertEquals(QueryFormatError.NO_SUCH_CONSTANT, e.getReason());
        }
    }

    @Test
    public void testMultipleOrders() {

        QueryParameters query = QueryParameters.query("order=username ASC,lastname DESC").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getOrder());
        Assert.assertEquals(2, query.getOrder().size());

        Assert.assertEquals("username", query.getOrder().get(0).getField());
        Assert.assertNotNull(query.getOrder().get(0).getOrder());
        Assert.assertEquals(OrderDirection.ASC, query.getOrder().get(0).getOrder());

        Assert.assertEquals("lastname", query.getOrder().get(1).getField());
        Assert.assertNotNull(query.getOrder().get(1).getOrder());
        Assert.assertEquals(OrderDirection.DESC, query.getOrder().get(1).getOrder());
    }

    @Test
    public void testMultipleOrdersWithoutDirections() {

        QueryParameters query = QueryParameters.query("order=username,lastname DESC,firstname").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getOrder());
        Assert.assertEquals(3, query.getOrder().size());

        Assert.assertEquals("username", query.getOrder().get(0).getField());
        Assert.assertNotNull(query.getOrder().get(0).getOrder());
        Assert.assertEquals(OrderDirection.ASC, query.getOrder().get(0).getOrder());

        Assert.assertEquals("lastname", query.getOrder().get(1).getField());
        Assert.assertNotNull(query.getOrder().get(1).getOrder());
        Assert.assertEquals(OrderDirection.DESC, query.getOrder().get(1).getOrder());

        Assert.assertEquals("firstname", query.getOrder().get(2).getField());
        Assert.assertNotNull(query.getOrder().get(2).getOrder());
        Assert.assertEquals(OrderDirection.ASC, query.getOrder().get(2).getOrder());
    }

    @Test
    public void testMultipleOrdersWithEmptyBetween() {

        QueryParameters query = QueryParameters.query("order=username,,,,firstname DESC").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getOrder());
        Assert.assertEquals(2, query.getOrder().size());

        Assert.assertEquals("username", query.getOrder().get(0).getField());
        Assert.assertNotNull(query.getOrder().get(0).getOrder());
        Assert.assertEquals(OrderDirection.ASC, query.getOrder().get(0).getOrder());

        Assert.assertEquals("firstname", query.getOrder().get(1).getField());
        Assert.assertNotNull(query.getOrder().get(1).getOrder());
        Assert.assertEquals(OrderDirection.DESC, query.getOrder().get(1).getOrder());
    }

    @Test
    public void testMultipleOrdersMalformed() {

        try {

            QueryParameters.query("order=username, firstname DESC").build();
            Assert.fail("No exception was thrown");
        } catch (QueryFormatException e) {

            Assert.assertEquals("order", e.getField());
            Assert.assertEquals(QueryFormatError.MALFORMED, e.getReason());
        }
    }

    @Test
    public void testMultipleOrderKeys() {

        QueryParameters query = QueryParameters.query("order=username,firstname&sort=lastname DESC").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getOrder());
        Assert.assertEquals(1, query.getOrder().size());

        Assert.assertEquals("lastname", query.getOrder().get(0).getField());
        Assert.assertNotNull(query.getOrder().get(0).getOrder());
        Assert.assertEquals(OrderDirection.DESC, query.getOrder().get(0).getOrder());
    }

    @Test
    public void testMultipleOrderRepeatFields() {

        QueryParameters query = QueryParameters.query("order=username,firstname,username DESC").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getOrder());
        Assert.assertEquals(2, query.getOrder().size());

        Assert.assertEquals("username", query.getOrder().get(0).getField());
        Assert.assertNotNull(query.getOrder().get(0).getOrder());
        Assert.assertEquals(OrderDirection.ASC, query.getOrder().get(0).getOrder());

        Assert.assertEquals("firstname", query.getOrder().get(1).getField());
        Assert.assertNotNull(query.getOrder().get(1).getOrder());
        Assert.assertEquals(OrderDirection.ASC, query.getOrder().get(1).getOrder());
    }

    @Test
    public void testEnabledOrder() {

        QueryParameters query = QueryParameters.query("order=username DESC").enableOrder(true).build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getOrder());
        Assert.assertEquals(1, query.getOrder().size());

        Assert.assertNotNull(query.getOrder().get(0));
        Assert.assertEquals("username", query.getOrder().get(0).getField());
        Assert.assertEquals(OrderDirection.DESC, query.getOrder().get(0).getOrder());

        query = QueryParameters.query("order=username DESC").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getOrder());
        Assert.assertEquals(1, query.getOrder().size());

        Assert.assertNotNull(query.getOrder().get(0));
        Assert.assertEquals("username", query.getOrder().get(0).getField());
        Assert.assertEquals(OrderDirection.DESC, query.getOrder().get(0).getOrder());
    }

    @Test
    public void testDisabledOrder() {

        QueryParameters query = QueryParameters.query("order=username DESC").enableOrder(false).build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getOrder());
        Assert.assertEquals(0, query.getOrder().size());
    }
}
