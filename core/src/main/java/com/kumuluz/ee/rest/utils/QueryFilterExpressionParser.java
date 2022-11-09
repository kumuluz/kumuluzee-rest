package com.kumuluz.ee.rest.utils;

import com.kumuluz.ee.rest.beans.QueryFilter;
import com.kumuluz.ee.rest.beans.QueryFilterExpression;
import com.kumuluz.ee.rest.enums.FilterExpressionOperation;
import com.kumuluz.ee.rest.enums.FilterOperation;
import com.kumuluz.ee.rest.enums.QueryFormatError;
import com.kumuluz.ee.rest.exceptions.QueryFormatException;
import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.support.Var;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@BuildParseTree
public class QueryFilterExpressionParser extends BaseParser<QueryFilterExpression> {

    private static final Logger log = Logger.getLogger(QueryFilterExpressionParser.class.getSimpleName());

    private final String key;

    public QueryFilterExpressionParser(String key) {
        this.key = key;
    }

    public Rule InputLine() {
        return Sequence(OrExpression(), EMPTY);
    }

    Rule OrExpression() {
        return Sequence(
                AndExpression(),
                ZeroOrMore(
                        Sequence(
                                WhiteSpace(),
                                FirstOf( // ',' and 'or' represent an 'OR' expression
                                        ',',
                                        IgnoreCase("OR")
                                ),
                                WhiteSpace(),
                                AndExpression(),
                                swap() && push(new QueryFilterExpression(FilterExpressionOperation.OR, pop(), pop()))
                        )
                )
        );
    }

    Rule AndExpression() {
        return Sequence(
                Expression(),
                ZeroOrMore(
                        Sequence(
                                FirstOf(
                                        Sequence(
                                                WhiteSpace(),
                                                FirstOf( // ' ', '+', ';' or 'and' represent an 'AND' expression (' ' and '+' for backward compatibility)
                                                        AnyOf("+;"),
                                                        IgnoreCase("AND")
                                                ),
                                                WhiteSpace()
                                        ),
                                        WhiteSpace()
                                ),
                                Expression(),
                                swap() && push(new QueryFilterExpression(FilterExpressionOperation.AND, pop(), pop()))
                        )
                )
        );
    }

    Rule Expression() {
        return FirstOf(
                Filter(),
                Parens()
        );
    }

    Rule Parens() {
        return Sequence(
                '(',
                OrExpression(),
                ')'
        );
    }

    Rule Filter() {
        return FirstOf(
                BinaryFilter(),
                UnaryFilter()
        );
    }

    Rule UnaryFilter() {
        Var<String> field = new Var<>();
        Var<String> operator = new Var<>();

        return Sequence(
                FilterField(),
                field.set(match()),
                ':',
                FilterOperator(),
                operator.set(match()),
                push(new QueryFilterExpression(buildQueryFilter(field.get(), operator.get())))
        );
    }

    Rule BinaryFilter() {
        Var<String> field = new Var<>();
        Var<String> operator = new Var<>();
        Var<String> value = new Var<>();

        return Sequence(
                FilterField(),
                field.set(match()),
                ':',
                FilterOperator(),
                operator.set(match()),
                ':',
                FilterValue(),
                value.set(match()),
                push(new QueryFilterExpression(buildQueryFilter(field.get(), operator.get(), value.get())))
        );
    }

    Rule FilterField() {
        return Sequence(
                String(),
                ZeroOrMore(
                        Sequence(
                                '.',
                                String()
                        )
                )
        );
    }

    Rule FilterOperator() {
        return OneOrMore(
                FirstOf(
                        CharRange('a', 'z'),
                        CharRange('A', 'Z')
                )
        );
//        return FirstOf(
//                IgnoreCase("EQIC"),
//                IgnoreCase("EQ"),
//                IgnoreCase("NEQIC"),
//                IgnoreCase("NEQ"),
//                IgnoreCase("LIKEIC"),
//                IgnoreCase("LIKE"),
//                IgnoreCase("GTE"),
//                IgnoreCase("GT"),
//                IgnoreCase("LTE"),
//                IgnoreCase("LT"),
//                IgnoreCase("NINIC"),
//                IgnoreCase("INIC"),
//                IgnoreCase("NIN"),
//                IgnoreCase("IN"),
//                IgnoreCase("ISNOTNULL"),
//                IgnoreCase("ISNULL")
//        );
    }

    Rule FilterValue() {
        return FirstOf(
                FilterDateValue(),
                FilterListValue(),
                FilterStringValue()
        );
    }

    Rule FilterDateValue() {
        return Sequence(
                "dt",
                QuotedString() // eg. dt'2014-11-26T11:15:08Z'
        );
    }

    Rule FilterListValue() {
        return StringArray();
    }

    Rule FilterStringValue() {
        return FirstOf(
                QuotedString(),
                String()
        );
    }

    Rule StringArray() {
        return Sequence(
                '[',
                Optional(
                        FirstOf(
                                QuotedString(),
                                String()
                        ),
                        ZeroOrMore(
                                Sequence(
                                        ",",
                                        Optional(
                                                FirstOf(
                                                        QuotedString(),
                                                        String()
                                                )
                                        )
                                )
                        )
                ),
                ']'
        );
    }

    Rule QuotedString() {
        return Sequence(
                '\'',
                ZeroOrMore(
                        Sequence(
                                TestNot(
                                        AnyOf("\r\n'")
                                ),
                                ANY
                        )
                ).suppressSubnodes(),
                '\''
        );
    }

    Rule String() {
        return OneOrMore(
                Sequence(
                        TestNot(
                                AnyOf(" ,;:'()[]\r\n")
                        ),
                        ANY
                )
        ).suppressSubnodes();
    }

    Rule WhiteSpace() {
        return ZeroOrMore(
                AnyOf(" \t\f")
        );
    }

    protected QueryFilter buildQueryFilter(String field, String operation) {
        FilterOperation filterOperation = buildQueryFilterOperation(operation);

        if (!filterOperation.equals(FilterOperation.ISNULL) && !filterOperation.equals(FilterOperation.ISNOTNULL)) {
            throw new QueryFormatException("Wrong use of unary and/or binary operators", key, QueryFormatError.MALFORMED);
        }

        return new QueryFilter(field, filterOperation);
    }

    protected QueryFilter buildQueryFilter(String field, String operation, String value) {
        FilterOperation filterOperation = buildQueryFilterOperation(operation);

        if (value.matches("^\\[.*\\]$") &&
                (filterOperation == FilterOperation.IN ||
                        filterOperation == FilterOperation.NIN ||
                        filterOperation == FilterOperation.NINIC ||
                        filterOperation == FilterOperation.INIC ||
                        filterOperation == FilterOperation.BETWEEN ||
                        filterOperation == FilterOperation.NBETWEEN)) {
            String values = value.replaceAll("(^\\[)|(\\]$)", "");

            List<String> valuesList = Arrays.stream(values.split("[,]+(?=([^']*'[^']*')*[^']*$)"))
                    .filter(e -> !e.isEmpty()).distinct()
                    .map(e -> e.replaceAll("(^')|('$)", ""))
                    .collect(Collectors.toList());

            return new QueryFilter(field, filterOperation, valuesList);
        } else if (value.matches("^dt'.*'$")) {
            Date dateValue = parseDate(value.replaceAll("(^dt')|('$)", ""));

            if (dateValue == null) {
                String msg = "Value for '" + key + "' is malformed: '" + value + "'";

                log.finest(msg);

                throw new QueryFormatException(msg, key, QueryFormatError.MALFORMED);
            }

            return new QueryFilter(field, filterOperation, dateValue);
        } else {
            String stringValue = value.replaceAll("(^')|('$)", "").trim();

            return new QueryFilter(field, filterOperation, stringValue);
        }
    }

    protected FilterOperation buildQueryFilterOperation(String operation) {
        try {
            return FilterOperation.valueOf(operation.toUpperCase());
        } catch (IllegalArgumentException e) {
            String msg = "Constant in '" + key + "' does not exist: '" + operation + "'";

            log.finest(msg);

            throw new QueryFormatException(msg, key, QueryFormatError.NO_SUCH_CONSTANT);
        }
    }

    protected Date parseDate(String date) {
        try {
            return Date.from(ZonedDateTime.parse(date).toInstant());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
