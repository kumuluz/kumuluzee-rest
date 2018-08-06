/*
 *  Copyright (c) 2014-2017 Kumuluz and/or its affiliates
 *  and other contributors as indicated by the @author tags and
 *  the contributor list.
 *
 *  Licensed under the MIT License (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://opensource.org/licenses/MIT
 *
 *  The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND, express or
 *  implied, including but not limited to the warranties of merchantability,
 *  fitness for a particular purpose and noninfringement. in no event shall the
 *  authors or copyright holders be liable for any claim, damages or other
 *  liability, whether in an action of contract, tort or otherwise, arising from,
 *  out of or in connection with the software or the use or other dealings in the
 *  software. See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.kumuluz.ee.rest.utils;

import com.kumuluz.ee.rest.beans.QueryFilter;
import com.kumuluz.ee.rest.beans.QueryOrder;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.enums.FilterOperation;
import com.kumuluz.ee.rest.enums.OrderDirection;
import com.kumuluz.ee.rest.enums.QueryFormatError;
import com.kumuluz.ee.rest.exceptions.QueryFormatException;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Tilen Faganel
 */
public class QueryStringBuilder {

    private static final Logger log = Logger.getLogger(QueryStringBuilder.class.getSimpleName());

    public static final String LIMIT_DELIMITER = "limit";
    public static final String LIMIT_DELIMITER_ALT = "max";

    public static final String OFFSET_DELIMITER = "offset";
    public static final String OFFSET_DELIMITER_ALT = "skip";

    public static final String ORDER_DELIMITER = "order";
    public static final String ORDER_DELIMITER_ALT = "sort";

    public static final String FIELDS_DELIMITER = "fields";
    public static final String FIELDS_DELIMITER_ALT = "select";

    public static final String FILTER_DELIMITER = "filter";
    public static final String FILTER_DELIMITER_ALT = "where";

    private String query;

    private Boolean paginationEnabled = true;
    private Boolean filtersEnabled = true;
    private Boolean orderEnabled = true;
    private Boolean fieldsEnabled = true;

    private Long maxLimit;
    private Long defaultLimit;
    private Long defaultOffset;
    private List<QueryFilter> defaultFilters;

    public QueryStringBuilder uri(URI uri) {

        log.finest("Setting uri object: " + uri);

        if (uri == null) throw new IllegalArgumentException("The passed URI cannot be null");

        query = uri.getRawQuery();

        return this;
    }

    public QueryStringBuilder uriEncoded(String uri) {

        return uri(decodeUrl(uri));
    }

    public QueryStringBuilder uri(String uri) {

        log.finest("Setting uri string: " + uri);

        if (uri == null || uri.isEmpty())
            throw new IllegalArgumentException("The passed URI string cannot be empty");

        int idxQuery = uri.indexOf("?");
        int idxFragment = uri.indexOf("#");

        if (idxQuery == -1) throw new IllegalArgumentException();

        if (idxFragment == -1) {

            query = uri.substring(idxQuery + 1);

            return this;
        }

        if (idxFragment < idxQuery) {

            query = "";

            return this;
        }

        query = uri.substring(idxQuery + 1, idxFragment);

        return this;
    }

    public QueryStringBuilder queryEncoded(String queryString) {

        return query(decodeUrl(queryString));
    }

    public QueryStringBuilder query(String queryString) {

        query = queryString;

        return this;
    }

    public QueryStringBuilder enablePagination(Boolean enable) {

        if (enable == null) throw new IllegalArgumentException("The enable boolean cannot be null");

        paginationEnabled = enable;

        return this;
    }

    public QueryStringBuilder enableFilters(Boolean enable) {

        if (enable == null) throw new IllegalArgumentException("The enable boolean cannot be null");

        filtersEnabled = enable;

        return this;
    }

    public QueryStringBuilder enableOrder(Boolean enable) {

        if (enable == null) throw new IllegalArgumentException("The enable boolean cannot be null");

        orderEnabled = enable;

        return this;
    }

    public QueryStringBuilder enableFields(Boolean enable) {

        if (enable == null) throw new IllegalArgumentException("The enable boolean cannot be null");

        fieldsEnabled = enable;

        return this;
    }

    public QueryStringBuilder maxLimit(int limit) {

        return maxLimit((long) limit);
    }

    public QueryStringBuilder maxLimit(Long limit) {

        log.finest("Setting max limit: " + limit);

        if (limit == null) throw new IllegalArgumentException("The passed limit cannot be null");

        if (limit < 0)
            throw new IllegalArgumentException("The passed limit must be a positive number");

        maxLimit = limit;

        return this;
    }

    public QueryStringBuilder defaultLimit(int limit) {

        return defaultLimit((long) limit);
    }

    public QueryStringBuilder defaultLimit(Long limit) {

        log.finest("Setting default limit: " + limit);

        if (limit == null) throw new IllegalArgumentException("The passed limit cannot be null");

        if (limit < 0)
            throw new IllegalArgumentException("The passed limit must be a positive number");

        defaultLimit = limit;

        return this;
    }

    public QueryStringBuilder defaultOffset(int offset) {

        return defaultOffset((long) offset);
    }

    public QueryStringBuilder defaultOffset(Long offset) {

        log.finest("Setting default offset: " + offset);

        if (offset == null) throw new IllegalArgumentException("The passed offset cannot be null");

        if (offset < 0)
            throw new IllegalArgumentException("The passed offset must be a positive number");

        defaultOffset = offset;

        return this;
    }

    public QueryStringBuilder defaultFilters(List<QueryFilter> queryFilters) {

        log.finest("Setting default filters");

        defaultFilters = queryFilters;

        return this;
    }

    public QueryParameters build() {

        log.finest("Building query string: " + query);

        QueryParameters params = new QueryParameters();

        if (paginationEnabled && defaultLimit != null) params.setLimit(defaultLimit);
        if (paginationEnabled && defaultOffset != null) params.setOffset(defaultOffset);

        if (query == null || query.isEmpty()) {

            addDefaultFilters(params);

            return params;
        }

        for (String pair : query.split("&+(?=([^']*'[^']*')*[^']*$)")) {

            int idxOfPair = pair.indexOf("=");

            if (idxOfPair == -1) {

                buildPair(params, pair, "");
                continue;
            }

            String key, value;

            key = pair.substring(0, idxOfPair);
            value = pair.substring(idxOfPair + 1);

            buildPair(params, key, value);
        }

        return params;
    }

    private void buildPair(QueryParameters params, String key, String value) {

        log.finest("Building query string pair: " + key + " " + value);

        if (params == null) return;

        if (key == null || key.isEmpty()) {

            addDefaultFilters(params);

            return;
        }

        if (value == null || value.isEmpty()) {

            addDefaultFilters(params);

            return;
        }

        switch (key) {

            case LIMIT_DELIMITER:
            case LIMIT_DELIMITER_ALT:

                if (paginationEnabled) {
                    params.setLimit(buildLimit(key, value));
                }

                break;

            case OFFSET_DELIMITER:
            case OFFSET_DELIMITER_ALT:

                if (paginationEnabled) {
                    params.setOffset(buildOffset(key, value));
                }

                break;

            case ORDER_DELIMITER:
            case ORDER_DELIMITER_ALT:

                if (orderEnabled) {
                    params.getOrder().clear();

                    Arrays.stream(value.split(",")).map(o -> buildOrder(key, o))
                            .filter(Objects::nonNull).distinct()
                            .forEach(o -> params.getOrder().add(o));
                }

                break;

            case FIELDS_DELIMITER:
            case FIELDS_DELIMITER_ALT:

                if (fieldsEnabled) {
                    params.getFields().clear();

                    params.getFields().addAll(buildFields(value));
                }

                break;

            case FILTER_DELIMITER:
            case FILTER_DELIMITER_ALT:

                if (filtersEnabled) {
                    params.getFilters().clear();

                    params.getFilters().addAll(buildFilter(key, value));

                }

                break;
        }

        addDefaultFilters(params);
    }

    private Long buildOffset(String key, String value) {

        log.finest("Building offset string: " + value);

        Long offset;

        try {

            offset = Long.parseLong(value);
        } catch (NumberFormatException e) {

            String msg = "Value for '" + key + "' is not a number: '" + value + "'";

            log.finest(msg);

            throw new QueryFormatException(msg, key, QueryFormatError.NOT_A_NUMBER);
        }

        if (offset < 0) {

            String msg = "Value for '" + key + "' is negative: '" + value + "'";

            log.finest(msg);

            throw new QueryFormatException(msg, key, QueryFormatError.NEGATIVE);
        }

        return offset;
    }

    private Long buildLimit(String key, String value) {

        log.finest("Building limit string: " + value);

        Long limit;

        try {

            limit = Long.parseLong(value);
        } catch (NumberFormatException e) {

            String msg = "Value for '" + key + "' is not a number: '" + value + "'";

            log.finest(msg);

            throw new QueryFormatException(msg, key, QueryFormatError.NOT_A_NUMBER);
        }

        if (limit < 0) {

            String msg = "Value for '" + key + "' is negative: '" + value + "'";

            log.finest(msg);

            throw new QueryFormatException(msg, key, QueryFormatError.NEGATIVE);
        }

        if (maxLimit != null && limit > maxLimit) limit = maxLimit;

        return limit;
    }

    private QueryOrder buildOrder(String key, String value) {

        log.finest("Building order string: " + value);

        if (value == null || value.isEmpty()) return null;

        QueryOrder o = new QueryOrder();

        String[] pair = value.split("(\\s|\\+)");

        if (pair[0].isEmpty()) {

            String msg = "Value for '" + key + "' is malformed: '" + value + "'";

            log.finest(msg);

            throw new QueryFormatException(msg, key, QueryFormatError.MALFORMED);
        }

        o.setField(pair[0]);

        if (pair.length > 1) {

            try {

                o.setOrder(OrderDirection.valueOf(pair[1].toUpperCase()));
            } catch (IllegalArgumentException e) {

                String msg = "Constant in '" + key + "' does not exist: '" + value + "'";

                log.finest(msg);

                throw new QueryFormatException(msg, key, QueryFormatError.NO_SUCH_CONSTANT);
            }
        } else {

            o.setOrder(OrderDirection.ASC);
        }

        return o;
    }

    private List<String> buildFields(String value) {

        log.finest("Building fields string: " + value);

        return Arrays.stream(value.split(",")).filter(f -> !f.isEmpty()).distinct()
                .collect(Collectors.toList());
    }

    private List<QueryFilter> buildFilter(String key, String value) {

        log.finest("Building filter string: " + value);

        List<QueryFilter> filterList = new ArrayList<>();

        if (value == null || value.isEmpty()) return filterList;

        List<String[]> filters = Arrays.stream(value.split("[(\\s|\\+)]+(?=([^']*'[^']*')*[^']*$)"))
                .map(f -> f.split("[:]+(?=([^']*'[^']*')*[^']*$)"))
                .collect(Collectors.toList());

        filters.stream().filter(f -> f.length == 2).forEach(f -> {

            QueryFilter qf = new QueryFilter();
            qf.setField(f[0]);

            try {

                qf.setOperation(FilterOperation.valueOf(f[1].toUpperCase()));
            } catch (IllegalArgumentException e) {

                String msg = "Constant in '" + key + "' does not exist: '" + value + "'";

                log.finest(msg);

                throw new QueryFormatException(msg, key, QueryFormatError.NO_SUCH_CONSTANT);
            }

            if (qf.getOperation() == FilterOperation.ISNULL || qf.getOperation() ==
                    FilterOperation.ISNOTNULL) {

                filterList.add(qf);
            }
        });

        filters.stream()
                .filter(f -> f.length == 3)
                .forEach(f -> {

                    QueryFilter qf = new QueryFilter();
                    qf.setField(f[0]);

                    try {

                        qf.setOperation(FilterOperation.valueOf(f[1].toUpperCase()));
                    } catch (IllegalArgumentException e) {

                        String msg = "Constant in '" + key + "' does not exist: '" + value + "'";

                        log.finest(msg);

                        throw new QueryFormatException(msg, key, QueryFormatError.NO_SUCH_CONSTANT);
                    }

                    if (f[2].matches("^\\[.*\\]$") &&
                            (qf.getOperation() == FilterOperation.IN ||
                            qf.getOperation() == FilterOperation.NIN ||
                            qf.getOperation() == FilterOperation.NINIC ||
                            qf.getOperation() == FilterOperation.INIC)) {

                        String values = f[2].replaceAll("(^\\[)|(\\]$)", "");

                        Arrays.stream(values.split("[,]+(?=([^']*'[^']*')*[^']*$)"))
                                .filter(e -> !e.isEmpty()).distinct()
                                .map(e -> e.replaceAll("(^')|('$)", ""))
                                .forEach(e -> qf.getValues().add(e));

                    } else if (f[2].matches("^dt'.*'$")) {

                        Date d = parseDate(f[2].replaceAll("(^dt')|('$)", ""));

                        if (d == null) {

                            String msg = "Value for '" + key + "' is malformed: '" + value + "'";

                            log.finest(msg);

                            throw new QueryFormatException(msg, key, QueryFormatError.MALFORMED);
                        }

                        qf.setDateValue(d);
                    } else {

                        qf.setValue(f[2].replaceAll("(^')|('$)", ""));
                    }

                    filterList.add(qf);
                });

        return filterList;
    }

    private void addDefaultFilters(QueryParameters params) {

        if (defaultFilters == null || defaultFilters.isEmpty()) return;

        params.getFilters().addAll(
                defaultFilters
                        .stream()
                        .filter(df ->
                                params.getFilters()
                                        .stream()
                                        .noneMatch(fl -> fl.getField().equals(df.getField()))
                        ).collect(Collectors.toList())
        );
    }

    private Date parseDate(String date) {

        try {
            return Date.from(ZonedDateTime.parse(date).toInstant());
        } catch (DateTimeParseException e) {

            return null;
        }
    }

    private String decodeUrl(String url) {

        if (url == null) return null;

        try {
            if (!URLEncoder.encode(url, StandardCharsets.UTF_8.displayName()).equals(url)) {
                return URLDecoder.decode(url, StandardCharsets.UTF_8.displayName());
            } else {
                return url;
            }
        } catch (UnsupportedEncodingException e) {

            log.severe("UTF-8 encoding is not supported on this system");

            throw new AssertionError();
        }
    }
}