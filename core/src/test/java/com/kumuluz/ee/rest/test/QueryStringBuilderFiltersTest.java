package com.kumuluz.ee.rest.test;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.enums.FilterExpressionOperation;
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
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals("username", query.getFilterExpression().value().getField());
        Assert.assertNotNull(query.getFilterExpression().value().getOperation());
        Assert.assertEquals(FilterOperation.EQ, query.getFilterExpression().value().getOperation());
        Assert.assertEquals("test", query.getFilterExpression().value().getValue());
    }

    @Test
    public void testSingleFilterWithQuotes() {

        QueryParameters query = QueryParameters.query("filter=username:eq:'test test'").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals("username", query.getFilterExpression().value().getField());
        Assert.assertNotNull(query.getFilterExpression().value().getOperation());
        Assert.assertEquals(FilterOperation.EQ, query.getFilterExpression().value().getOperation());
        Assert.assertEquals("test test", query.getFilterExpression().value().getValue());
    }

    @Test
    public void testMultipleFilters() {

        QueryParameters query = QueryParameters.query("where=username:eq:'test test' lastname:gte:gale").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(2, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals(FilterExpressionOperation.AND, query.getFilterExpression().operation());
        Assert.assertEquals("username", query.getFilterExpression().left().value().getField());
        Assert.assertNotNull(query.getFilterExpression().left().value().getOperation());
        Assert.assertEquals(FilterOperation.EQ, query.getFilterExpression().left().value().getOperation());
        Assert.assertEquals("test test", query.getFilterExpression().left().value().getValue());
        Assert.assertEquals("lastname", query.getFilterExpression().right().value().getField());
        Assert.assertNotNull(query.getFilterExpression().right().value().getOperation());
        Assert.assertEquals(FilterOperation.GTE, query.getFilterExpression().right().value().getOperation());
        Assert.assertEquals("gale", query.getFilterExpression().right().value().getValue());
    }

    @Test
    public void testMultipleFiltersWithRedundantWhiteSpace() {

        QueryParameters query = QueryParameters.query("where=username:eq:'test test'         lastname:gte:gale").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(2, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals(FilterExpressionOperation.AND, query.getFilterExpression().operation());
        Assert.assertEquals("username", query.getFilterExpression().left().value().getField());
        Assert.assertNotNull(query.getFilterExpression().left().value().getOperation());
        Assert.assertEquals(FilterOperation.EQ, query.getFilterExpression().left().value().getOperation());
        Assert.assertEquals("test test", query.getFilterExpression().left().value().getValue());
        Assert.assertEquals("lastname", query.getFilterExpression().right().value().getField());
        Assert.assertNotNull(query.getFilterExpression().right().value().getOperation());
        Assert.assertEquals(FilterOperation.GTE, query.getFilterExpression().right().value().getOperation());
        Assert.assertEquals("gale", query.getFilterExpression().right().value().getValue());
    }

    @Test
    public void testMultipleFiltersWithPrecedence() {

        QueryParameters query = QueryParameters.query("where=firstname:eq:janez or username:eq:'test   test' and lastname:gte:gale").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(3, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals(FilterExpressionOperation.OR, query.getFilterExpression().operation());
        Assert.assertEquals("firstname", query.getFilterExpression().left().value().getField());
        Assert.assertNotNull(query.getFilterExpression().left().value().getOperation());
        Assert.assertEquals(FilterOperation.EQ, query.getFilterExpression().left().value().getOperation());
        Assert.assertEquals("janez", query.getFilterExpression().left().value().getValue());
        Assert.assertEquals(FilterExpressionOperation.AND, query.getFilterExpression().right().operation());
        Assert.assertEquals("username", query.getFilterExpression().right().left().value().getField());
        Assert.assertNotNull(query.getFilterExpression().right().left().value().getOperation());
        Assert.assertEquals(FilterOperation.EQ, query.getFilterExpression().right().left().value().getOperation());
        Assert.assertEquals("test   test", query.getFilterExpression().right().left().value().getValue());
        Assert.assertEquals("lastname", query.getFilterExpression().right().right().value().getField());
        Assert.assertNotNull(query.getFilterExpression().right().right().value().getOperation());
        Assert.assertEquals(FilterOperation.GTE, query.getFilterExpression().right().right().value().getOperation());
        Assert.assertEquals("gale", query.getFilterExpression().right().right().value().getValue());
    }

    @Test
    public void testMultipleFiltersWithParentheses() {

        QueryParameters query = QueryParameters.query("where=(firstname:eq:janez,username:eq:'test test') lastname:gte:gale").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(3, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals(FilterExpressionOperation.AND, query.getFilterExpression().operation());
        Assert.assertEquals(FilterExpressionOperation.OR, query.getFilterExpression().left().operation());
        Assert.assertEquals("firstname", query.getFilterExpression().left().left().value().getField());
        Assert.assertNotNull(query.getFilterExpression().left().left().value().getOperation());
        Assert.assertEquals(FilterOperation.EQ, query.getFilterExpression().left().left().value().getOperation());
        Assert.assertEquals("janez", query.getFilterExpression().left().left().value().getValue());
        Assert.assertEquals("username", query.getFilterExpression().left().right().value().getField());
        Assert.assertNotNull(query.getFilterExpression().left().right().value().getOperation());
        Assert.assertEquals(FilterOperation.EQ, query.getFilterExpression().left().right().value().getOperation());
        Assert.assertEquals("test test", query.getFilterExpression().left().right().value().getValue());
        Assert.assertEquals("lastname", query.getFilterExpression().right().value().getField());
        Assert.assertNotNull(query.getFilterExpression().right().value().getOperation());
        Assert.assertEquals(FilterOperation.GTE, query.getFilterExpression().right().value().getOperation());
        Assert.assertEquals("gale", query.getFilterExpression().right().value().getValue());
    }

    @Test
    public void testMultipleFiltersWithPlus() {

        QueryParameters query = QueryParameters.query("where=username:eq:'test test'+lastname:gte:gale").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(2, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals(FilterExpressionOperation.AND, query.getFilterExpression().operation());
        Assert.assertEquals("username", query.getFilterExpression().left().value().getField());
        Assert.assertNotNull(query.getFilterExpression().left().value().getOperation());
        Assert.assertEquals(FilterOperation.EQ, query.getFilterExpression().left().value().getOperation());
        Assert.assertEquals("test test", query.getFilterExpression().left().value().getValue());
        Assert.assertEquals("lastname", query.getFilterExpression().right().value().getField());
        Assert.assertNotNull(query.getFilterExpression().right().value().getOperation());
        Assert.assertEquals(FilterOperation.GTE, query.getFilterExpression().right().value().getOperation());
        Assert.assertEquals("gale", query.getFilterExpression().right().value().getValue());
    }

    @Test
    public void testMultipleFiltersWithAnd() {

        QueryParameters query = QueryParameters.query("where=username:eq:'test test'    and   lastname:gte:gale").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(2, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals(FilterExpressionOperation.AND, query.getFilterExpression().operation());
        Assert.assertEquals("username", query.getFilterExpression().left().value().getField());
        Assert.assertNotNull(query.getFilterExpression().left().value().getOperation());
        Assert.assertEquals(FilterOperation.EQ, query.getFilterExpression().left().value().getOperation());
        Assert.assertEquals("test test", query.getFilterExpression().left().value().getValue());
        Assert.assertEquals("lastname", query.getFilterExpression().right().value().getField());
        Assert.assertNotNull(query.getFilterExpression().right().value().getOperation());
        Assert.assertEquals(FilterOperation.GTE, query.getFilterExpression().right().value().getOperation());
        Assert.assertEquals("gale", query.getFilterExpression().right().value().getValue());
    }

    @Test
    public void testMultipleFiltersWithSemicolon() {

        QueryParameters query = QueryParameters.query("where=username:eq:'test test';lastname:gte:gale").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(2, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals(FilterExpressionOperation.AND, query.getFilterExpression().operation());
        Assert.assertEquals("username", query.getFilterExpression().left().value().getField());
        Assert.assertNotNull(query.getFilterExpression().left().value().getOperation());
        Assert.assertEquals(FilterOperation.EQ, query.getFilterExpression().left().value().getOperation());
        Assert.assertEquals("test test", query.getFilterExpression().left().value().getValue());
        Assert.assertEquals("lastname", query.getFilterExpression().right().value().getField());
        Assert.assertNotNull(query.getFilterExpression().right().value().getOperation());
        Assert.assertEquals(FilterOperation.GTE, query.getFilterExpression().right().value().getOperation());
        Assert.assertEquals("gale", query.getFilterExpression().right().value().getValue());
    }

    @Test
    public void testMultipleFiltersWithComma() {

        QueryParameters query = QueryParameters.query("where=username:eq:'test test'   ,  lastname:gte:gale").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(2, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals(FilterExpressionOperation.OR, query.getFilterExpression().operation());
        Assert.assertEquals("username", query.getFilterExpression().left().value().getField());
        Assert.assertNotNull(query.getFilterExpression().left().value().getOperation());
        Assert.assertEquals(FilterOperation.EQ, query.getFilterExpression().left().value().getOperation());
        Assert.assertEquals("test test", query.getFilterExpression().left().value().getValue());
        Assert.assertEquals("lastname", query.getFilterExpression().right().value().getField());
        Assert.assertNotNull(query.getFilterExpression().right().value().getOperation());
        Assert.assertEquals(FilterOperation.GTE, query.getFilterExpression().right().value().getOperation());
        Assert.assertEquals("gale", query.getFilterExpression().right().value().getValue());
    }

    @Test
    public void testMultipleFiltersWithOr() {

        QueryParameters query = QueryParameters.query("where=username:eq:'test     test'  or   lastname:gte:gale").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(2, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals(FilterExpressionOperation.OR, query.getFilterExpression().operation());
        Assert.assertEquals("username", query.getFilterExpression().left().value().getField());
        Assert.assertNotNull(query.getFilterExpression().left().value().getOperation());
        Assert.assertEquals(FilterOperation.EQ, query.getFilterExpression().left().value().getOperation());
        Assert.assertEquals("test     test", query.getFilterExpression().left().value().getValue());
        Assert.assertEquals("lastname", query.getFilterExpression().right().value().getField());
        Assert.assertNotNull(query.getFilterExpression().right().value().getOperation());
        Assert.assertEquals(FilterOperation.GTE, query.getFilterExpression().right().value().getOperation());
        Assert.assertEquals("gale", query.getFilterExpression().right().value().getValue());
    }

    @Test
    public void testMalformedUnaryFilter() {

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

        try {

            QueryParameters query = QueryParameters.query("filter=usernameeq:eq test").build();
            Assert.fail("No exception was thrown");
        } catch (QueryFormatException e) {

            Assert.assertEquals("filter", e.getField());
            Assert.assertNotNull(e.getReason());
            Assert.assertEquals(QueryFormatError.MALFORMED, e.getReason());
        }
    }

    @Test
    public void testUnsupportedOperationFilter() {

        try {

            QueryParameters.query("filter=username:equal:'test test'").build();
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
        Assert.assertNull(query.getFilterExpression());
    }

    @Test
    public void testMultipleKeyFilters() {

        QueryParameters query = QueryParameters.query("where=username:eq:'test test' " +
                "lastname:gte:gale&filter=country:neq:SI").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilterExpression());
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals("country", query.getFilterExpression().value().getField());
        Assert.assertNotNull(query.getFilterExpression().value().getOperation());
        Assert.assertEquals(FilterOperation.NEQ, query.getFilterExpression().value().getOperation());
        Assert.assertEquals("SI", query.getFilterExpression().value().getValue());
    }

    @Test
    public void testInFilter() {

        QueryParameters query = QueryParameters.query("where=username:in:[johnf,garryz]").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilters());
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals("username", query.getFilterExpression().value().getField());
        Assert.assertNotNull(query.getFilterExpression().value().getOperation());
        Assert.assertEquals(FilterOperation.IN, query.getFilterExpression().value().getOperation());
        Assert.assertNull(query.getFilterExpression().value().getValue());
        Assert.assertEquals(2, query.getFilterExpression().value().getValues().size());
        Assert.assertEquals("johnf", query.getFilterExpression().value().getValues().get(0));
        Assert.assertEquals("garryz", query.getFilterExpression().value().getValues().get(1));
    }

    @Test
    public void testInicFilter() {

        QueryParameters query = QueryParameters.query("where=username:inic:[Garryz,johnF]").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilterExpression());
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals("username", query.getFilterExpression().value().getField());
        Assert.assertNotNull(query.getFilterExpression().value().getOperation());
        Assert.assertEquals(FilterOperation.INIC, query.getFilterExpression().value().getOperation());
        Assert.assertNull(query.getFilterExpression().value().getValue());
        Assert.assertEquals(2, query.getFilterExpression().value().getValues().size());
        Assert.assertEquals("Garryz", query.getFilterExpression().value().getValues().get(0));
        Assert.assertEquals("johnF", query.getFilterExpression().value().getValues().get(1));
    }

    @Test
    public void testNinFilter() {

        QueryParameters query = QueryParameters.query("where=username:nin:[garryz,johnf,johng]").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilterExpression());
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals("username", query.getFilterExpression().value().getField());
        Assert.assertNotNull(query.getFilterExpression().value().getOperation());
        Assert.assertEquals(FilterOperation.NIN, query.getFilterExpression().value().getOperation());
        Assert.assertNull(query.getFilterExpression().value().getValue());
        Assert.assertEquals(3, query.getFilterExpression().value().getValues().size());
        Assert.assertEquals("garryz", query.getFilterExpression().value().getValues().get(0));
        Assert.assertEquals("johnf", query.getFilterExpression().value().getValues().get(1));
        Assert.assertEquals("johng", query.getFilterExpression().value().getValues().get(2));
    }

    @Test
    public void testNinicFilter() {

        QueryParameters query = QueryParameters.query("where=username:ninic:[joHnf,johng]").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilterExpression());
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals("username", query.getFilterExpression().value().getField());
        Assert.assertNotNull(query.getFilterExpression().value().getOperation());
        Assert.assertEquals(FilterOperation.NINIC, query.getFilterExpression().value().getOperation());
        Assert.assertNull(query.getFilterExpression().value().getValue());
        Assert.assertEquals(2, query.getFilterExpression().value().getValues().size());
        Assert.assertEquals("joHnf", query.getFilterExpression().value().getValues().get(0));
        Assert.assertEquals("johng", query.getFilterExpression().value().getValues().get(1));
    }

    @Test
    public void testInFilterEmptyElements() {

        QueryParameters query = QueryParameters.query("where=username:in:[johnf,,,,garryz]").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilterExpression());
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals("username", query.getFilterExpression().value().getField());
        Assert.assertNotNull(query.getFilterExpression().value().getOperation());
        Assert.assertEquals(FilterOperation.IN, query.getFilterExpression().value().getOperation());
        Assert.assertNull(query.getFilterExpression().value().getValue());
        Assert.assertEquals(2, query.getFilterExpression().value().getValues().size());
        Assert.assertEquals("johnf", query.getFilterExpression().value().getValues().get(0));
        Assert.assertEquals("garryz", query.getFilterExpression().value().getValues().get(1));
    }

    @Test
    public void testArrayValueWhenNotInOperation() {

        QueryParameters query = QueryParameters.query("where=username:neq:[johnf,garryz]").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilterExpression());
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals("username", query.getFilterExpression().value().getField());
        Assert.assertNotNull(query.getFilterExpression().value().getOperation());
        Assert.assertEquals(FilterOperation.NEQ, query.getFilterExpression().value().getOperation());
        Assert.assertEquals("[johnf,garryz]", query.getFilterExpression().value().getValue());
    }

    @Test
    public void testDateValueFilter() {

        Date d = Date.from(ZonedDateTime.parse("2014-11-26T11:15:08Z").toInstant());

        QueryParameters query = QueryParameters.query("where=username:gte:dt'2014-11-26T11:15:08Z'").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilterExpression());
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals("username", query.getFilterExpression().value().getField());
        Assert.assertNotNull(query.getFilterExpression().value().getOperation());
        Assert.assertEquals(FilterOperation.GTE, query.getFilterExpression().value().getOperation());
        Assert.assertEquals(d, query.getFilterExpression().value().getDateValue());
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
        Assert.assertNotNull(query.getFilterExpression());
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals("username", query.getFilterExpression().value().getField());
        Assert.assertNotNull(query.getFilterExpression().value().getOperation());
        Assert.assertEquals(FilterOperation.GTE, query.getFilterExpression().value().getOperation());
        Assert.assertEquals("2014-11-26T11:15:08Z", query.getFilterExpression().value().getValue());
    }

    @Test
    public void testQueryDecoded() {

        QueryParameters query = QueryParameters.query("where=firstname:like:Kar%").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilterExpression());
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals("firstname", query.getFilterExpression().value().getField());
        Assert.assertNotNull(query.getFilterExpression().value().getOperation());
        Assert.assertEquals(FilterOperation.LIKE, query.getFilterExpression().value().getOperation());
        Assert.assertEquals("Kar%", query.getFilterExpression().value().getValue());
    }

    @Test
    public void testQueryEncoded() {

        QueryParameters query = QueryParameters.queryEncoded("where=firstname:like:Kar%25").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilterExpression());
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals("firstname", query.getFilterExpression().value().getField());
        Assert.assertNotNull(query.getFilterExpression().value().getOperation());
        Assert.assertEquals(FilterOperation.LIKE, query.getFilterExpression().value().getOperation());
        Assert.assertEquals("Kar%", query.getFilterExpression().value().getValue());
    }

    @Test
    public void testQueryEncodedNull() {

        QueryParameters query = QueryParameters.queryEncoded(null).build();

        Assert.assertNotNull(query);
        Assert.assertNull(query.getFilterExpression());
    }

    @Test
    public void testUriEncoded() {

        QueryParameters query = QueryParameters.uriEncoded("api.github.com/kumuluz/repos?where=firstname:like:Kar%25%20").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilterExpression());
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals("firstname", query.getFilterExpression().value().getField());
        Assert.assertNotNull(query.getFilterExpression().value().getOperation());
        Assert.assertEquals(FilterOperation.LIKE, query.getFilterExpression().value().getOperation());
        Assert.assertEquals("Kar%", query.getFilterExpression().value().getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUriEncodedNull() {

        QueryParameters query = QueryParameters.uriEncoded(null).build();
    }

    @Test
     public void testQuotesInInFilter() {

        QueryParameters query = QueryParameters.query("where=country:in:['Czech Republic',China]").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilterExpression());
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals("country", query.getFilterExpression().value().getField());
        Assert.assertNotNull(query.getFilterExpression().value().getOperation());
        Assert.assertEquals(FilterOperation.IN, query.getFilterExpression().value().getOperation());
        Assert.assertNull(query.getFilterExpression().value().getValue());
        Assert.assertEquals(2, query.getFilterExpression().value().getValues().size());
        Assert.assertEquals("Czech Republic", query.getFilterExpression().value().getValues().get(0));
        Assert.assertEquals("China", query.getFilterExpression().value().getValues().get(1));
    }

    @Test
    public void testQuotesInInicFilter() {

        QueryParameters query = QueryParameters.query("where=country:inic:['Czech Republic',China,'United States']").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilterExpression());
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals("country", query.getFilterExpression().value().getField());
        Assert.assertNotNull(query.getFilterExpression().value().getOperation());
        Assert.assertEquals(FilterOperation.INIC, query.getFilterExpression().value().getOperation());
        Assert.assertNull(query.getFilterExpression().value().getValue());
        Assert.assertEquals(3, query.getFilterExpression().value().getValues().size());
        Assert.assertEquals("Czech Republic", query.getFilterExpression().value().getValues().get(0));
        Assert.assertEquals("China", query.getFilterExpression().value().getValues().get(1));
        Assert.assertEquals("United States", query.getFilterExpression().value().getValues().get(2));

    }

    @Test
    public void testQuotesInNinFilter() {

        QueryParameters query = QueryParameters.query("where=country:nin:['Czech Republic',Nigeria]").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilterExpression());
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals("country", query.getFilterExpression().value().getField());
        Assert.assertNotNull(query.getFilterExpression().value().getOperation());
        Assert.assertEquals(FilterOperation.NIN, query.getFilterExpression().value().getOperation());
        Assert.assertNull(query.getFilterExpression().value().getValue());
        Assert.assertEquals(2, query.getFilterExpression().value().getValues().size());
        Assert.assertEquals("Czech Republic", query.getFilterExpression().value().getValues().get(0));
        Assert.assertEquals("Nigeria", query.getFilterExpression().value().getValues().get(1));
    }

    @Test
    public void testQuotesInNinicFilter() {

        QueryParameters query = QueryParameters.query("where=country:ninic:['Czech Republic','United States']").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilterExpression());
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals("country", query.getFilterExpression().value().getField());
        Assert.assertNotNull(query.getFilterExpression().value().getOperation());
        Assert.assertEquals(FilterOperation.NINIC, query.getFilterExpression().value().getOperation());
        Assert.assertNull(query.getFilterExpression().value().getValue());
        Assert.assertEquals(2, query.getFilterExpression().value().getValues().size());
        Assert.assertEquals("Czech Republic", query.getFilterExpression().value().getValues().get(0));
        Assert.assertEquals("United States", query.getFilterExpression().value().getValues().get(1));
    }

    @Test
    public void testIsNullFilter() {

        QueryParameters query = QueryParameters.query("where=description:isnull").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilterExpression());
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals("description", query.getFilterExpression().value().getField());
        Assert.assertNotNull(query.getFilterExpression().value().getOperation());
        Assert.assertEquals(FilterOperation.ISNULL, query.getFilterExpression().value().getOperation());
        Assert.assertNull(query.getFilterExpression().value().getValue());
    }

    @Test
    public void testAndSignInFilter() {

        QueryParameters query = QueryParameters.query("limit=30&where=title:like:'Tools & Furniture'").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilterExpression());
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());
        Assert.assertEquals("title", query.getFilterExpression().value().getField());
        Assert.assertNotNull(query.getFilterExpression().value().getOperation());
        Assert.assertEquals(FilterOperation.LIKE, query.getFilterExpression().value().getOperation());
        Assert.assertEquals("Tools & Furniture", query.getFilterExpression().value().getValue());
        Assert.assertNotNull(query.getLimit());
        Assert.assertEquals(30, query.getLimit().longValue());
    }

    @Test
    public void testEnabledFilters() {

        QueryParameters query = QueryParameters.query("filter=username:eq:test").enableFilters(true).build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilterExpression());
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());

        Assert.assertNotNull(query.getFilterExpression().value());
        Assert.assertEquals("username", query.getFilterExpression().value().getField());
        Assert.assertEquals(FilterOperation.EQ, query.getFilterExpression().value().getOperation());
        Assert.assertEquals("test", query.getFilterExpression().value().getValue());

        query = QueryParameters.query("filter=username:eq:test").build();

        Assert.assertNotNull(query);
        Assert.assertNotNull(query.getFilterExpression());
        Assert.assertEquals(1, query.getFilterExpression().getAllValues().size());

        Assert.assertNotNull(query.getFilterExpression().value());
        Assert.assertEquals("username", query.getFilterExpression().value().getField());
        Assert.assertEquals(FilterOperation.EQ, query.getFilterExpression().value().getOperation());
        Assert.assertEquals("test", query.getFilterExpression().value().getValue());
    }

    @Test
    public void testDisabledFilters() {

        QueryParameters query = QueryParameters.query("filter=username:eq:test").enableFilters(false).build();

        Assert.assertNotNull(query);
        Assert.assertNull(query.getFilterExpression());
    }


}
