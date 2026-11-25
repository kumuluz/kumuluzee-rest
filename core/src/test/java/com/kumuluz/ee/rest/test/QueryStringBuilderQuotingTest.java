package com.kumuluz.ee.rest.test;

import com.kumuluz.ee.rest.beans.QueryFilter;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.enums.FilterOperation;
import com.kumuluz.ee.rest.enums.QueryFormatError;
import com.kumuluz.ee.rest.exceptions.QueryFormatException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Quoting and strict parsing of filter values.
 * <p>
 * Single quotes inside quoted values are escaped by doubling them ({@code 'O''Brien'}), see
 * https://github.com/kumuluz/kumuluzee-rest/issues/28. Input that cannot be parsed completely is rejected instead of
 * being silently truncated, see https://github.com/kumuluz/kumuluzee-rest/issues/30.
 */
public class QueryStringBuilderQuotingTest {

    private static QueryFilter singleFilter(QueryParameters query) {
        Assert.assertNotNull(query.getFilterExpression());
        List<QueryFilter> filters = query.getFilterExpression().getAllValues();
        Assert.assertEquals(1, filters.size());
        return filters.get(0);
    }

    private static void assertMalformed(String queryString) {
        try {
            QueryParameters q = QueryParameters.query(queryString).build();
            Assert.fail("Expected QueryFormatException for " + queryString + " but got filters "
                    + (q.getFilterExpression() == null ? null : q.getFilterExpression().getAllValues()));
        } catch (QueryFormatException e) {
            Assert.assertEquals("filter", e.getField());
            Assert.assertEquals(QueryFormatError.MALFORMED, e.getReason());
        }
    }

    // --- #28: escaped single quotes

    @Test
    public void doubledQuoteInsideQuotedValueIsAnEscapedQuote() {
        QueryFilter f = singleFilter(QueryParameters.query("filter=lastname:eq:'O''Brien'").build());

        Assert.assertEquals("lastname", f.getField());
        Assert.assertEquals(FilterOperation.EQ, f.getOperation());
        Assert.assertEquals("O'Brien", f.getValue());
    }

    @Test
    public void escapedQuoteFromIssue28() {
        QueryFilter f = singleFilter(QueryParameters.query("filter=string:EQ:'ably.qer-z''o pp'").build());

        Assert.assertEquals("ably.qer-z'o pp", f.getValue());
    }

    @Test
    public void valueConsistingOfEscapedQuotesOnly() {
        QueryFilter f = singleFilter(QueryParameters.query("filter=lastname:eq:''''").build());

        Assert.assertEquals("'", f.getValue());
    }

    @Test
    public void escapedQuotesAtStartAndEnd() {
        QueryFilter f = singleFilter(QueryParameters.query("filter=lastname:eq:'''quoted'''").build());

        Assert.assertEquals("'quoted'", f.getValue());
    }

    @Test
    public void escapedQuoteDoesNotBreakSplittingOfOtherParameters() {
        QueryParameters q = QueryParameters.query("filter=lastname:eq:'a''b&c'&limit=5").build();

        Assert.assertEquals("a'b&c", singleFilter(q).getValue());
        Assert.assertEquals(Long.valueOf(5), q.getLimit());
    }

    @Test
    public void escapedQuoteInEncodedQueryString() {
        // %27%27 = ''
        QueryFilter f = singleFilter(QueryParameters.queryEncoded("filter=lastname:eq:%27O%27%27Brien%27").build());

        Assert.assertEquals("O'Brien", f.getValue());
    }

    @Test
    public void escapedQuoteInListValues() {
        QueryFilter f = singleFilter(QueryParameters.query("filter=lastname:in:['O''Brien','D''Arcy',plain]").build());

        Assert.assertEquals(FilterOperation.IN, f.getOperation());
        Assert.assertEquals(Arrays.asList("O'Brien", "D'Arcy", "plain"), f.getValues());
    }

    @Test
    public void escapedQuoteTogetherWithOtherFilters() {
        QueryParameters q = QueryParameters.query("filter=lastname:eq:'O''Brien' and (country:eq:'Cote d''Ivoire' or id:gt:5)").build();

        List<QueryFilter> filters = q.getFilterExpression().getAllValues();
        Assert.assertEquals(3, filters.size());
        Assert.assertEquals("O'Brien", filters.get(0).getValue());
        Assert.assertEquals("Cote d'Ivoire", filters.get(1).getValue());
        Assert.assertEquals("5", filters.get(2).getValue());
    }

    // --- #30: special characters are only allowed inside quotes; anything else is rejected, not dropped

    @Test
    public void specialCharactersInsideQuotesAreKept() {
        Assert.assertEquals("a(b)", singleFilter(QueryParameters.queryEncoded("filter=lastname:eq:'a%28b%29'").build()).getValue());
        Assert.assertEquals("a,b", singleFilter(QueryParameters.queryEncoded("filter=lastname:eq:'a%2Cb'").build()).getValue());
        Assert.assertEquals("a;b:c", singleFilter(QueryParameters.queryEncoded("filter=lastname:eq:'a%3Bb%3Ac'").build()).getValue());
        Assert.assertEquals("[x]", singleFilter(QueryParameters.queryEncoded("filter=lastname:eq:'%5Bx%5D'").build()).getValue());
    }

    @Test
    public void unquotedValueWithParenthesesIsRejected() {
        assertMalformed("filter=lastname:eq:a(b)");
    }

    @Test
    public void unquotedValueWithTrailingGarbageIsRejected() {
        assertMalformed("filter=lastname:eq:a]");
    }

    @Test
    public void unescapedQuoteInsideQuotedValueIsRejected() {
        assertMalformed("filter=lastname:eq:'z'o pp'");
    }

    @Test
    public void unterminatedQuoteIsRejected() {
        assertMalformed("filter=lastname:eq:'abc");
    }

    @Test
    public void unbalancedParenthesesAreRejected() {
        assertMalformed("filter=(lastname:eq:a or id:gt:1");
    }

    @Test
    public void danglingLogicalOperatorIsRejected() {
        assertMalformed("filter=lastname:eq:a or");
    }
}
