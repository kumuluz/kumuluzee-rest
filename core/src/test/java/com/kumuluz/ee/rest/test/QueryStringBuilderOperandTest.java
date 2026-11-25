package com.kumuluz.ee.rest.test;

import com.kumuluz.ee.rest.beans.QueryFilter;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.enums.FilterOperation;
import com.kumuluz.ee.rest.enums.QueryFormatError;
import com.kumuluz.ee.rest.exceptions.QueryFormatException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Empty clauses and the shape of operands.
 * <p>
 * Frontends often build filters from form inputs and send empty inputs as {@code field:op:}. Such an empty clause means
 * "no condition": it is left out of the expression wherever it appears, instead of dropping the whole filter (which
 * silently discarded the valid clauses next to it) or rejecting it.
 */
public class QueryStringBuilderOperandTest {

    private static List<QueryFilter> filters(String queryString) {
        QueryParameters q = QueryParameters.query(queryString).build();
        return q.getFilterExpression() == null ? Collections.emptyList() : q.getFilterExpression().getAllValues();
    }

    private static void assertMalformed(String queryString) {
        try {
            List<QueryFilter> f = filters(queryString);
            Assert.fail("Expected QueryFormatException for " + queryString + " but got " + f.size() + " filters");
        } catch (QueryFormatException e) {
            Assert.assertEquals(QueryFormatError.MALFORMED, e.getReason());
        }
    }

    // --- empty clauses are left out, the rest of the expression is kept

    @Test
    public void trailingEmptyClauseIsLeftOut() {
        List<QueryFilter> f = filters("filter=id:eq:7 and firstname:eq:");

        Assert.assertEquals(1, f.size());
        Assert.assertEquals("id", f.get(0).getField());
        Assert.assertEquals("7", f.get(0).getValue());
    }

    @Test
    public void leadingEmptyClauseIsLeftOut() {
        List<QueryFilter> f = filters("filter=firstname:eq: and id:eq:7");

        Assert.assertEquals(1, f.size());
        Assert.assertEquals("id", f.get(0).getField());
    }

    @Test
    public void emptyClausesFromFormInputsJoinedWithSpaces() {
        // as built by the management frontend: filter.push(key + ':eqic:' + value); filter.join(' ')
        List<QueryFilter> f = filters("filter=firstname:eqic: lastname:eqic:Doe country:eqic:");

        Assert.assertEquals(1, f.size());
        Assert.assertEquals("lastname", f.get(0).getField());
        Assert.assertEquals(FilterOperation.EQIC, f.get(0).getOperation());
        Assert.assertEquals("Doe", f.get(0).getValue());
    }

    @Test
    public void emptyClauseInsideOrAndParenthesesIsLeftOut() {
        List<QueryFilter> f = filters("filter=(firstname:eq: or id:eq:7) and (lastname:like: or country:eq:China)");

        Assert.assertEquals(2, f.size());
        Assert.assertEquals("id", f.get(0).getField());
        Assert.assertEquals("country", f.get(1).getField());
    }

    @Test
    public void onlyEmptyClausesMeanNoFilter() {
        Assert.assertNull(QueryParameters.query("filter=firstname:eqic: lastname:eqic:").build().getFilterExpression());
        Assert.assertNull(QueryParameters.query("filter=firstname:eq: and (lastname:like: or id:gt:)").build().getFilterExpression());
    }

    @Test
    public void emptyClauseWithTrailingWhitespaceIsLeftOut() {
        Assert.assertNull(QueryParameters.query("filter=firstname:eq: ").build().getFilterExpression());
    }

    @Test
    public void emptyClauseInEncodedQueryString() {
        List<QueryFilter> encoded = QueryParameters.queryEncoded("filter=id%3Aeq%3A7%20firstname%3Aeqic%3A").build()
                .getFilterExpression().getAllValues();

        Assert.assertEquals(1, encoded.size());
        Assert.assertEquals("id", encoded.get(0).getField());
    }

    @Test
    public void quotedEmptyStringIsAValueNotAnEmptyClause() {
        List<QueryFilter> f = filters("filter=firstname:eq:''");

        Assert.assertEquals(1, f.size());
        Assert.assertEquals("", f.get(0).getValue());
    }

    // --- list operators need a list

    @Test
    public void scalarOperandForListOperatorIsRejected() {
        assertMalformed("filter=id:nin:7");
        assertMalformed("filter=id:in:7");
        assertMalformed("filter=id:inic:'a'");
        assertMalformed("filter=id:ninic:a");
        assertMalformed("filter=id:between:7");
        assertMalformed("filter=id:nbetween:'7'");
    }

    @Test
    public void listLiteralForScalarOperatorIsAPlainValue() {
        QueryFilter f = filters("filter=username:neq:[johnf,garryz]").get(0);

        Assert.assertEquals(FilterOperation.NEQ, f.getOperation());
        Assert.assertEquals("[johnf,garryz]", f.getValue());
    }

    @Test
    public void inStillRemovesDuplicateValues() {
        Assert.assertEquals(Arrays.asList("1", "2"), filters("filter=id:in:[1,1,2]").get(0).getValues());
    }

    // --- between keeps both bounds and needs exactly two

    @Test
    public void betweenWithEqualBoundsKeepsBoth() {
        QueryFilter f = filters("filter=id:between:[7,7]").get(0);

        Assert.assertEquals(FilterOperation.BETWEEN, f.getOperation());
        Assert.assertEquals(Arrays.asList("7", "7"), f.getValues());
    }

    @Test
    public void notBetweenWithEqualBoundsKeepsBoth() {
        Assert.assertEquals(Arrays.asList("7", "7"), filters("filter=id:nbetween:[7,7]").get(0).getValues());
    }

    @Test
    public void betweenAcceptsQuotedEmptyStringBound() {
        Assert.assertEquals(Arrays.asList("", "Z"), filters("filter=lastname:between:['','Z']").get(0).getValues());
    }

    @Test
    public void betweenNeedsExactlyTwoBounds() {
        assertMalformed("filter=lastname:between:[,Z]");
        assertMalformed("filter=id:between:[7]");
        assertMalformed("filter=id:between:[1,2,3]");
        assertMalformed("filter=id:between:[]");
        assertMalformed("filter=id:nbetween:[1,,2]");
    }
}
