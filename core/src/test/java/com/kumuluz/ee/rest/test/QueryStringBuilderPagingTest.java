package com.kumuluz.ee.rest.test;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.enums.QueryFormatError;
import com.kumuluz.ee.rest.exceptions.QueryFormatException;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Tilen Faganel
 */
public class QueryStringBuilderPagingTest {

    @Test
    public void testEmpty() {

        QueryParameters query = QueryParameters.query("").build();

        Assert.assertNotNull(query);
        Assert.assertNull(query.getLimit());
        Assert.assertNull(query.getOffset());
    }

    @Test
    public void testNull() {

        QueryParameters query = QueryParameters.query(null).build();

        Assert.assertNotNull(query);
        Assert.assertNull(query.getLimit());
        Assert.assertNull(query.getOffset());
    }

    @Test
    public void testLimit() {

        QueryParameters query = QueryParameters.query("limit=123").build();

        Assert.assertNotNull(query.getLimit());
        Assert.assertEquals(123, query.getLimit().longValue());

        query = QueryParameters.query("max=321").build();

        Assert.assertNotNull(query.getLimit());
        Assert.assertEquals(321, query.getLimit().longValue());
    }

    @Test
    public void testMultipleLimits() {

        QueryParameters query = QueryParameters.query("limit=123&limit=111&max=322").build();

        Assert.assertNotNull(query.getLimit());
        Assert.assertEquals(322, query.getLimit().longValue());

        query = QueryParameters.query("max=981&max=682").build();

        Assert.assertNotNull(query.getLimit());
        Assert.assertEquals(682, query.getLimit().longValue());
    }

    @Test
    public void testNegativeLimit() {

        try {

            QueryParameters.query("limit=-123").build();
            Assert.fail("No exception was thrown");
        } catch (QueryFormatException e) {

            Assert.assertEquals("limit", e.getField());
            Assert.assertEquals(QueryFormatError.NEGATIVE, e.getReason());
        }
    }

    @Test
    public void testWrongLimitFormat() {

        try {

            QueryParameters.query("max=122&limit=asd").build();
            Assert.fail("No exception was thrown");
        } catch (QueryFormatException e) {

            Assert.assertEquals("limit", e.getField());
            Assert.assertEquals(QueryFormatError.NOT_A_NUMBER, e.getReason());
        }
    }

    @Test
    public void testEmptyLimit() {

        QueryParameters query = QueryParameters.query("limit=").build();

        Assert.assertNotNull(query);
        Assert.assertNull(query.getLimit());
    }

    @Test
    public void testLimitUri() {

        QueryParameters query = QueryParameters.uri("api.github.com/kumuluz/repos?limit=123").build();

        Assert.assertNotNull(query.getLimit());
        Assert.assertEquals(123, query.getLimit().longValue());
    }

    @Test
    public void testLimitUriWithFragment() {

        QueryParameters query = QueryParameters.uri("api.github.com/kumuluz/repos?limit=123#header1").build();

        Assert.assertNotNull(query.getLimit());
        Assert.assertEquals(123, query.getLimit().longValue());

        query = QueryParameters.uri("api.github.com/kumuluz/repos#header2?skip=98172").build();

        Assert.assertNull(query.getLimit());
    }

    @Test
    public void testLimitUriObject() throws URISyntaxException {

        URI uri = new URI("api.github.com/kumuluz/repos?max=9186");

        QueryParameters query = QueryParameters.uri(uri).build();

        Assert.assertNotNull(query.getLimit());
        Assert.assertEquals(9186, query.getLimit().longValue());
    }

    @Test
    public void testLimitUriObjectWithFragment() throws URISyntaxException {

        URI uri = new URI("api.github.com/kumuluz/repos?max=9186#header3");

        QueryParameters query = QueryParameters.uri(uri).build();

        Assert.assertNotNull(query.getLimit());
        Assert.assertEquals(9186, query.getLimit().longValue());

        uri = new URI("api.github.com/kumuluz/repos#header4?max=9186");

        query = QueryParameters.uri(uri).build();

        Assert.assertNull(query.getLimit());
    }

    @Test
    public void testOffset() {

        QueryParameters query = QueryParameters.query("offset=921").build();

        Assert.assertNotNull(query.getOffset());
        Assert.assertEquals(921, query.getOffset().longValue());

        query = QueryParameters.query("skip=824").build();

        Assert.assertNotNull(query.getOffset());
        Assert.assertEquals(824, query.getOffset().longValue());
    }

    @Test
    public void testMultipleOffsets() {

        QueryParameters query = QueryParameters.query("skip=2199&offset=95461&skip=411").build();

        Assert.assertNotNull(query.getOffset());
        Assert.assertEquals(411, query.getOffset().longValue());

        query = QueryParameters.query("offset=9881&offset=871263").build();

        Assert.assertNotNull(query.getOffset());
        Assert.assertEquals(871263, query.getOffset().longValue());
    }

    @Test
    public void testNegativeOffset() {

        try {

            QueryParameters.query("offset=-123").build();
            Assert.fail("No exception was thrown");
        } catch (QueryFormatException e) {

            Assert.assertEquals("offset", e.getField());
            Assert.assertEquals(QueryFormatError.NEGATIVE, e.getReason());
        }
    }

    @Test
    public void testWrongOffsetFormat() {

        try {

            QueryParameters.query("skip=122&skip=asd").build();
            Assert.fail("No exception was thrown");
        } catch (QueryFormatException e) {

            Assert.assertEquals("skip", e.getField());
            Assert.assertEquals(QueryFormatError.NOT_A_NUMBER, e.getReason());
        }
    }

    @Test
    public void testEmptyOffset() {

        QueryParameters query = QueryParameters.query("skip=").build();

        Assert.assertNotNull(query);
        Assert.assertNull(query.getOffset());
    }

    @Test
    public void testOffsetUri() {

        QueryParameters query = QueryParameters.uri("api.github.com/kumuluz/repos?skip=98172").build();

        Assert.assertNotNull(query.getOffset());
        Assert.assertEquals(98172, query.getOffset().longValue());
    }

    @Test
    public void testOffsetUriWithFragment() {

        QueryParameters query = QueryParameters.uri("api.github.com/kumuluz/repos?skip=98172#id1").build();

        Assert.assertNotNull(query.getOffset());
        Assert.assertEquals(98172, query.getOffset().longValue());

        query = QueryParameters.uri("api.github.com/kumuluz/repos#id2?skip=98172").build();

        Assert.assertNull(query.getOffset());
    }

    @Test
    public void testOffsetUriObject() throws URISyntaxException {

        URI uri = new URI("api.github.com/kumuluz/repos?offset=12312");

        QueryParameters query = QueryParameters.uri(uri).build();

        Assert.assertNotNull(query.getOffset());
        Assert.assertEquals(12312, query.getOffset().longValue());
    }

    @Test
    public void testOffsetUriObjectWithFragment() throws URISyntaxException {

        URI uri = new URI("api.github.com/kumuluz/repos?offset=12312#id3");

        QueryParameters query = QueryParameters.uri(uri).build();

        Assert.assertNotNull(query.getOffset());
        Assert.assertEquals(12312, query.getOffset().longValue());

        uri = new URI("api.github.com/kumuluz/repos#id4?offset=12312");

        query = QueryParameters.uri(uri).build();

        Assert.assertNull(query.getOffset());
    }

    @Test
    public void testOffsetWithLimit() {

        QueryParameters query = QueryParameters.query("offset=123&limit=22").build();

        Assert.assertNotNull(query.getLimit());
        Assert.assertNotNull(query.getOffset());
        Assert.assertEquals(22, query.getLimit().longValue());
        Assert.assertEquals(123, query.getOffset().longValue());

        query = QueryParameters.query("skip=123&max=22&skip=444").build();

        Assert.assertNotNull(query.getLimit());
        Assert.assertNotNull(query.getOffset());
        Assert.assertEquals(22, query.getLimit().longValue());
        Assert.assertEquals(444, query.getOffset().longValue());
    }

    @Test
    public void testDefaultLimit() {

        QueryParameters query = QueryParameters.query("offset=222").defaultLimit(100).build();

        Assert.assertNotNull(query.getLimit());
        Assert.assertNotNull(query.getOffset());
        Assert.assertEquals(100, query.getLimit().longValue());
        Assert.assertEquals(222, query.getOffset().longValue());
    }

    @Test
    public void testDefaultOffset() {

        QueryParameters query = QueryParameters.query("limit=321").defaultOffset(0).build();

        Assert.assertNotNull(query.getLimit());
        Assert.assertNotNull(query.getOffset());
        Assert.assertEquals(321, query.getLimit().longValue());
        Assert.assertEquals(0, query.getOffset().longValue());
    }

    @Test
    public void testMaxLimit() {

        QueryParameters query = QueryParameters.query("limit=321").maxLimit(200).build();

        Assert.assertNotNull(query.getLimit());
        Assert.assertEquals(200, query.getLimit().longValue());
    }

    @Test
    public void testDefaultValuesWithQuery() {

        QueryParameters query = QueryParameters.query("limit=222&offset=4335").defaultLimit(100).defaultOffset(0).build();

        Assert.assertNotNull(query.getLimit());
        Assert.assertNotNull(query.getOffset());
        Assert.assertEquals(222, query.getLimit().longValue());
        Assert.assertEquals(4335, query.getOffset().longValue());
    }

    @Test
    public void testEnabledPagination() {

        QueryParameters query = QueryParameters.query("offset=123&limit=22").enablePagination(true).build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getLimit());
        Assert.assertNotNull(query.getOffset());
        Assert.assertEquals(22, query.getLimit().longValue());
        Assert.assertEquals(123, query.getOffset().longValue());

        query = QueryParameters.query("offset=123&limit=22").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getLimit());
        Assert.assertNotNull(query.getOffset());
        Assert.assertEquals(22, query.getLimit().longValue());
        Assert.assertEquals(123, query.getOffset().longValue());
    }

    @Test
    public void testDisabledPagination() {

        QueryParameters query = QueryParameters.query("offset=123&limit=22").enablePagination(false).build();

        Assert.assertNotNull(query);
        Assert.assertNull(query.getLimit());
        Assert.assertNull(query.getOffset());
    }
}
