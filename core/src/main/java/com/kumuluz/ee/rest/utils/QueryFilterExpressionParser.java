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

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@BuildParseTree
public class QueryFilterExpressionParser extends BaseParser<QueryFilterExpression> {

    private static final Logger log = Logger.getLogger(QueryFilterExpressionParser.class.getSimpleName());

    private static final Set<FilterOperation> LIST_OPERATIONS = EnumSet.of(
            FilterOperation.IN, FilterOperation.NIN, FilterOperation.INIC, FilterOperation.NINIC,
            FilterOperation.BETWEEN, FilterOperation.NBETWEEN);

    private final String key;

    public QueryFilterExpressionParser(String key) {
        this.key = key;
    }

    /**
     * Supplies the {@link MethodHandles.Lookup} that parboiled uses to define the classes it generates for this
     * parser ({@code QueryFilterExpressionParser$$parboiled} and the {@code Action$...} classes) in this package.
     * <p>
     * Parboiled looks for this method before falling back to the JDK-internal trusted lookup obtained via
     * {@code sun.misc.Unsafe}, and then to reflective access to {@code ClassLoader.defineClass} (which requires
     * {@code --add-opens java.base/java.lang=ALL-UNNAMED}). Providing it keeps the parser working regardless of
     * {@code --sun-misc-unsafe-memory-access} settings and without any {@code --add-opens} flags, including under
     * custom class loaders such as Quarkus'.
     * <p>
     * Not part of the public API; it exists only for parboiled.
     *
     * @return a full-privilege lookup on this class
     */
    public static MethodHandles.Lookup lookup() {
        return MethodHandles.lookup();
    }

    public Rule InputLine() {
        // EOI: the whole filter must be consumed. Previously trailing input that did not match (e.g. the "(b)" in
        // an unquoted "name:eq:a(b)") was silently dropped.
        return Sequence(WhiteSpace(), OrExpression(), WhiteSpace(), EOI);
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
                                swap() && push(combine(FilterExpressionOperation.OR, pop(), pop()))
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
                                swap() && push(combine(FilterExpressionOperation.AND, pop(), pop()))
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
                FirstOf(
                        Sequence(
                                FilterValue(),
                                value.set(match()),
                                push(new QueryFilterExpression(buildQueryFilter(field.get(), operator.get(), value.get())))
                        ),
                        // "field:op:" without a value (e.g. an empty form input): no condition
                        push(emptyClause(field.get(), operator.get()))
                )
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

    /**
     * A single-quoted string. A single quote inside the value is escaped by doubling it, as in SQL:
     * {@code 'O''Brien'} is the value {@code O'Brien}.
     */
    Rule QuotedString() {
        return Sequence(
                '\'',
                ZeroOrMore(
                        FirstOf(
                                "''",
                                Sequence(
                                        TestNot(
                                                AnyOf("\r\n'")
                                        ),
                                        ANY
                                )
                        )
                ).suppressSubnodes(),
                '\''
        );
    }

    /**
     * Removes the surrounding quotes of a {@link #QuotedString()} match and un-escapes doubled quotes. Unquoted values
     * are returned unchanged (they cannot contain quotes).
     */
    static String unquote(String value) {
        if (value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length() - 1).replace("''", "'");
        }
        return value;
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

    /**
     * An empty clause ({@code field:op:}) means "no condition". It becomes an empty leaf, which {@link #combine}
     * leaves out of the surrounding expression. A trailing colon after a unary operator is tolerated.
     */
    protected QueryFilterExpression emptyClause(String field, String operation) {
        FilterOperation filterOperation = buildQueryFilterOperation(operation);

        if (filterOperation == FilterOperation.ISNULL || filterOperation == FilterOperation.ISNOTNULL) {
            return new QueryFilterExpression(new QueryFilter(field, filterOperation));
        }

        return new QueryFilterExpression((QueryFilter) null);
    }

    /**
     * Combines two sub-expressions; empty clauses are left out instead of widening or narrowing the expression.
     */
    protected QueryFilterExpression combine(FilterExpressionOperation operation, QueryFilterExpression left,
                                            QueryFilterExpression right) {
        if (left.isEmptyLeaf()) {
            return right;
        }
        if (right.isEmptyLeaf()) {
            return left;
        }
        return new QueryFilterExpression(operation, left, right);
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

        boolean listValue = value.matches("^\\[.*\\]$");
        boolean listOperation = LIST_OPERATIONS.contains(filterOperation);

        // A list operation needs a list. (A list literal for any other operation is just text and used as is.)
        if (listOperation && !listValue) {
            String msg = "Operation '" + operation + "' in '" + key + "' requires a list value, e.g. " + field + ":"
                    + operation + ":[a,b]: '" + value + "'";

            log.finest(msg);

            throw new QueryFormatException(msg, key, QueryFormatError.MALFORMED);
        }

        if (listOperation) {
            String values = value.replaceAll("(^\\[)|(\\]$)", "");

            Stream<String> elements = Arrays.stream(values.split("[,](?=([^']*'[^']*')*[^']*$)", -1));

            List<String> valuesList;
            if (filterOperation == FilterOperation.BETWEEN || filterOperation == FilterOperation.NBETWEEN) {
                // keep both bounds, also when they are equal; check for missing bounds before unquoting, so that a
                // quoted empty string ('') is a valid bound
                List<String> rawBounds = elements.collect(Collectors.toList());

                if (rawBounds.size() != 2 || rawBounds.stream().anyMatch(String::isEmpty)) {
                    String msg = "Operation '" + operation + "' in '" + key + "' requires exactly two values, e.g. "
                            + field + ":" + operation + ":[1,5]: '" + value + "'";

                    log.finest(msg);

                    throw new QueryFormatException(msg, key, QueryFormatError.MALFORMED);
                }

                valuesList = rawBounds.stream().map(QueryFilterExpressionParser::unquote).collect(Collectors.toList());
            } else {
                valuesList = elements
                        .filter(e -> !e.isEmpty()).distinct()
                        .map(QueryFilterExpressionParser::unquote)
                        .collect(Collectors.toList());
            }

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
            String stringValue = unquote(value).trim();

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
            // keeps sub-millisecond digits, so that the field validation in JPAUtils can see them
            return JPAUtils.toDate(ZonedDateTime.parse(date).toInstant());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
