package com.kumuluz.ee.rest.test;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.enums.FilterOperation;
import com.kumuluz.ee.rest.enums.QueryFormatError;
import com.kumuluz.ee.rest.exceptions.QueryFormatException;

import org.junit.Assert;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author Tilen Faganel
 */
public class QueryStringBuilderFiltersTest {

    @Test
    public void testQueryFieldsObject() {

        QueryParameters query = new QueryParameters();

        Assert.assertNotNull(query.getFields());
        Assert.assertEquals(0, query.getFields().size());
    }

    @Test
    public void testSingleFilter() {

        QueryParameters query = QueryParameters.query("filter=username:eq:test").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());
        Assert.assertEquals("username", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.EQ, query.getFilters().get(0).getOperation());
        Assert.assertEquals("test", query.getFilters().get(0).getValue());
    }

    @Test
    public void testSingleFilterWithQuotes() {

        QueryParameters query = QueryParameters.query("filter=username:eq:'test test'").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());
        Assert.assertEquals("username", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.EQ, query.getFilters().get(0).getOperation());
        Assert.assertEquals("test test", query.getFilters().get(0).getValue());
    }

    @Test
    public void testMultipleFilters() {

        QueryParameters query = QueryParameters.query("where=username:eq:'test test' lastname:gte:gale").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(2, query.getFilters().size());
        Assert.assertEquals("username", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.EQ, query.getFilters().get(0).getOperation());
        Assert.assertEquals("test test", query.getFilters().get(0).getValue());
        Assert.assertEquals("lastname", query.getFilters().get(1).getField());
        Assert.assertNotNull(query.getFilters().get(1).getOperation());
        Assert.assertEquals(FilterOperation.GTE, query.getFilters().get(1).getOperation());
        Assert.assertEquals("gale", query.getFilters().get(1).getValue());
    }

    @Test
    public void testMultipleFiltersWithPlus() {

        QueryParameters query = QueryParameters.query("where=username:eq:'test test'+lastname:gte:gale").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(2, query.getFilters().size());
        Assert.assertEquals("username", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.EQ, query.getFilters().get(0).getOperation());
        Assert.assertEquals("test test", query.getFilters().get(0).getValue());
        Assert.assertEquals("lastname", query.getFilters().get(1).getField());
        Assert.assertNotNull(query.getFilters().get(1).getOperation());
        Assert.assertEquals(FilterOperation.GTE, query.getFilters().get(1).getOperation());
        Assert.assertEquals("gale", query.getFilters().get(1).getValue());
    }

    @Test
    public void testMalformedUniaryFilter() {

        try {

            QueryParameters.query("filter=usernameeq:test test").build();
            Assert.fail("No exception was thrown");
        } catch (QueryFormatException e) {

            Assert.assertEquals("filter", e.getField());
            Assert.assertNotNull(e.getReason());
            Assert.assertEquals(QueryFormatError.NO_SUCH_CONSTANT, e.getReason());
        }
    }

    @Test
    public void testMalformedBinaryFilter() {

        QueryParameters query = QueryParameters.query("filter=usernameeq:eq test").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(0, query.getFilters().size());
    }

    @Test
    public void testUnsupportedOperationFilter() {

        try {

            QueryParameters.query("filter=username:equal:test test").build();
            Assert.fail("No exception was thrown");
        } catch (QueryFormatException e) {

            Assert.assertEquals("filter", e.getField());
            Assert.assertNotNull(e.getReason());
            Assert.assertEquals(QueryFormatError.NO_SUCH_CONSTANT, e.getReason());
        }
    }

    @Test
    public void testEmptyFilter() {

        QueryParameters query = QueryParameters.query("filter=").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(0, query.getFilters().size());
    }

    @Test
    public void testMultipleKeyFilters() {

        QueryParameters query = QueryParameters.query("where=username:eq:'test test' " +
                "lastname:gte:gale&filter=country:neq:SI").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());
        Assert.assertEquals("country", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.NEQ, query.getFilters().get(0).getOperation());
        Assert.assertEquals("SI", query.getFilters().get(0).getValue());
    }

    @Test
    public void testInFilter() {

        QueryParameters query = QueryParameters.query("where=username:in:[johnf,garryz]").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());
        Assert.assertEquals("username", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.IN, query.getFilters().get(0).getOperation());
        Assert.assertNull(query.getFilters().get(0).getValue());
        Assert.assertEquals(2, query.getFilters().get(0).getValues().size());
        Assert.assertEquals("johnf", query.getFilters().get(0).getValues().get(0));
        Assert.assertEquals("garryz", query.getFilters().get(0).getValues().get(1));
    }

    @Test
    public void testInicFilter() {

        QueryParameters query = QueryParameters.query("where=username:inic:[Garryz,johnF]").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());
        Assert.assertEquals("username", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.INIC, query.getFilters().get(0).getOperation());
        Assert.assertNull(query.getFilters().get(0).getValue());
        Assert.assertEquals(2, query.getFilters().get(0).getValues().size());
        Assert.assertEquals("Garryz", query.getFilters().get(0).getValues().get(0));
        Assert.assertEquals("johnF", query.getFilters().get(0).getValues().get(1));
    }

    @Test
    public void testNinFilter() {

        QueryParameters query = QueryParameters.query("where=username:nin:[garryz,johnf,johng]").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());
        Assert.assertEquals("username", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.NIN, query.getFilters().get(0).getOperation());
        Assert.assertNull(query.getFilters().get(0).getValue());
        Assert.assertEquals(3, query.getFilters().get(0).getValues().size());
        Assert.assertEquals("garryz", query.getFilters().get(0).getValues().get(0));
        Assert.assertEquals("johnf", query.getFilters().get(0).getValues().get(1));
        Assert.assertEquals("johng", query.getFilters().get(0).getValues().get(2));
    }

    @Test
    public void testNinicFilter() {

        QueryParameters query = QueryParameters.query("where=username:ninic:[joHnf,johng]").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());
        Assert.assertEquals("username", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.NINIC, query.getFilters().get(0).getOperation());
        Assert.assertNull(query.getFilters().get(0).getValue());
        Assert.assertEquals(2, query.getFilters().get(0).getValues().size());
        Assert.assertEquals("joHnf", query.getFilters().get(0).getValues().get(0));
        Assert.assertEquals("johng", query.getFilters().get(0).getValues().get(1));
    }

    @Test
    public void testInFilterEmptyElements() {

        QueryParameters query = QueryParameters.query("where=username:in:[johnf,,,,garryz]").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());
        Assert.assertEquals("username", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.IN, query.getFilters().get(0).getOperation());
        Assert.assertNull(query.getFilters().get(0).getValue());
        Assert.assertEquals(2, query.getFilters().get(0).getValues().size());
        Assert.assertEquals("johnf", query.getFilters().get(0).getValues().get(0));
        Assert.assertEquals("garryz", query.getFilters().get(0).getValues().get(1));
    }

    @Test
    public void testArrayValueWhenNotInOperation() {

        QueryParameters query = QueryParameters.query("where=username:neq:[johnf,garryz]").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());
        Assert.assertEquals("username", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.NEQ, query.getFilters().get(0).getOperation());
        Assert.assertEquals("[johnf,garryz]", query.getFilters().get(0).getValue());
    }

    @Test
    public void testDateValueFilter() {

        Date d = Date.from(ZonedDateTime.parse("2014-11-26T11:15:08Z").toInstant());

        QueryParameters query = QueryParameters.query("where=username:gte:dt'2014-11-26T11:15:08Z'").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());
        Assert.assertEquals("username", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.GTE, query.getFilters().get(0).getOperation());
        Assert.assertEquals(d, query.getFilters().get(0).getDateValue());
    }

    @Test
    public void testMalformedDateFilter() {

        try {
            QueryParameters.query("where=username:gte:dt'2014-11-26T1sdf1:15:08Z'").build();
            Assert.fail("No exception was thrown");
        } catch (QueryFormatException e) {

            Assert.assertEquals("where", e.getField());
            Assert.assertNotNull(e.getReason());
            Assert.assertEquals(QueryFormatError.MALFORMED, e.getReason());
        }
    }

    @Test
    public void testNoDateIdentifier() {

        QueryParameters query = QueryParameters.query("where=username:gte:'2014-11-26T11:15:08Z'").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());
        Assert.assertEquals("username", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.GTE, query.getFilters().get(0).getOperation());
        Assert.assertEquals("2014-11-26T11:15:08Z", query.getFilters().get(0).getValue());
    }

    @Test
    public void testQueryDecoded() {

        QueryParameters query = QueryParameters.query("where=firstname:like:Kar%").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());
        Assert.assertEquals("firstname", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.LIKE, query.getFilters().get(0).getOperation());
        Assert.assertEquals("Kar%", query.getFilters().get(0).getValue());
    }

    @Test
    public void testQueryEncoded() {

        QueryParameters query = QueryParameters.queryEncoded("where=firstname:like:Kar%25").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());
        Assert.assertEquals("firstname", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.LIKE, query.getFilters().get(0).getOperation());
        Assert.assertEquals("Kar%", query.getFilters().get(0).getValue());
    }

    @Test
    public void testQueryEncodedNull() {

        QueryParameters query = QueryParameters.queryEncoded(null).build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(0, query.getFilters().size());
    }

    @Test
    public void testUriEncoded() {

        QueryParameters query = QueryParameters.uriEncoded("api.github.com/kumuluz/repos?where=firstname:like:Kar%25%20").build();

        Boolean a = Boolean.parseBoolean("asdadas");

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());
        Assert.assertEquals("firstname", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.LIKE, query.getFilters().get(0).getOperation());
        Assert.assertEquals("Kar%", query.getFilters().get(0).getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUriEncodedNull() {

        QueryParameters query = QueryParameters.uriEncoded(null).build();
    }

    @Test
     public void testQuotesInInFilter() {

        QueryParameters query = QueryParameters.query("where=country:in:['Czech Republic',China]").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());
        Assert.assertEquals("country", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.IN, query.getFilters().get(0).getOperation());
        Assert.assertNull(query.getFilters().get(0).getValue());
        Assert.assertEquals(2, query.getFilters().get(0).getValues().size());
        Assert.assertEquals("Czech Republic", query.getFilters().get(0).getValues().get(0));
        Assert.assertEquals("China", query.getFilters().get(0).getValues().get(1));
    }

    @Test
    public void testQuotesInInicFilter() {

        QueryParameters query = QueryParameters.query("where=country:inic:['Czech Republic',China,'United States']").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());
        Assert.assertEquals("country", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.INIC, query.getFilters().get(0).getOperation());
        Assert.assertNull(query.getFilters().get(0).getValue());
        Assert.assertEquals(3, query.getFilters().get(0).getValues().size());
        Assert.assertEquals("Czech Republic", query.getFilters().get(0).getValues().get(0));
        Assert.assertEquals("China", query.getFilters().get(0).getValues().get(1));
        Assert.assertEquals("United States", query.getFilters().get(0).getValues().get(2));
    }

    @Test
    public void testQuotesInNinFilter() {

        QueryParameters query = QueryParameters.query("where=country:nin:['Czech Republic',Nigeria]").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());
        Assert.assertEquals("country", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.NIN, query.getFilters().get(0).getOperation());
        Assert.assertNull(query.getFilters().get(0).getValue());
        Assert.assertEquals(2, query.getFilters().get(0).getValues().size());
        Assert.assertEquals("Czech Republic", query.getFilters().get(0).getValues().get(0));
        Assert.assertEquals("Nigeria", query.getFilters().get(0).getValues().get(1));
    }

    @Test
    public void testQuotesInNinicFilter() {

        QueryParameters query = QueryParameters.query("where=country:ninic:['Czech Republic','United States']").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());
        Assert.assertEquals("country", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.NINIC, query.getFilters().get(0).getOperation());
        Assert.assertNull(query.getFilters().get(0).getValue());
        Assert.assertEquals(2, query.getFilters().get(0).getValues().size());
        Assert.assertEquals("Czech Republic", query.getFilters().get(0).getValues().get(0));
        Assert.assertEquals("United States", query.getFilters().get(0).getValues().get(1));
    }

    @Test
    public void testIsNullFilter() {

        QueryParameters query = QueryParameters.query("where=description:isnull").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());
        Assert.assertEquals("description", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.ISNULL, query.getFilters().get(0).getOperation());
        Assert.assertNull(query.getFilters().get(0).getValue());
    }

    @Test
    public void testAndSignInFilter() {

        QueryParameters query = QueryParameters.query("limit=30&where=title:like:'Tools & Furniture'").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());
        Assert.assertEquals("title", query.getFilters().get(0).getField());
        Assert.assertNotNull(query.getFilters().get(0).getOperation());
        Assert.assertEquals(FilterOperation.LIKE, query.getFilters().get(0).getOperation());
        Assert.assertEquals("Tools & Furniture", query.getFilters().get(0).getValue());
        Assert.assertNotNull(query.getLimit());
        Assert.assertEquals(30, query.getLimit().longValue());
    }

    @Test
    public void testEnabledFilters() {

        QueryParameters query = QueryParameters.query("filter=username:eq:test").enableFilters(true).build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());

        Assert.assertNotNull(query.getFilters().get(0));
        Assert.assertEquals("username", query.getFilters().get(0).getField());
        Assert.assertEquals(FilterOperation.EQ, query.getFilters().get(0).getOperation());
        Assert.assertEquals("test", query.getFilters().get(0).getValue());

        query = QueryParameters.query("filter=username:eq:test").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilters().size());

        Assert.assertNotNull(query.getFilters().get(0));
        Assert.assertEquals("username", query.getFilters().get(0).getField());
        Assert.assertEquals(FilterOperation.EQ, query.getFilters().get(0).getOperation());
        Assert.assertEquals("test", query.getFilters().get(0).getValue());
    }

    @Test
    public void testDisabledFilters() {

        QueryParameters query = QueryParameters.query("filter=username:eq:test").enableFilters(false).build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(0, query.getFilters().size());
    }
}
