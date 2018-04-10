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
package com.kumuluz.ee.rest.beans;

import com.kumuluz.ee.rest.utils.QueryStringBuilder;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tilen Faganel
 */
public class QueryParameters implements Serializable {

    private final static long serialVersionUID = 1L;

    private Long limit;
    private Long offset;

    private List<QueryOrder> order;
    private List<String> fields;
    private List<QueryFilter> filters;

    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit.longValue();
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset.longValue();
    }

    public void setOrder(List<QueryOrder> order) {
        this.order = order;
    }

    public void setFilters(List<QueryFilter> filters) {
        this.filters = filters;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public List<QueryOrder> getOrder() {

        if (order == null)
            order = new ArrayList<>();

        return order;
    }

    public List<String> getFields() {

        if (fields == null)
            fields = new ArrayList<>();

        return fields;
    }

    public List<QueryFilter> getFilters() {

        if (filters == null)
            filters = new ArrayList<>();

        return filters;
    }

    // Static methods for creating the query builder

    public static QueryStringBuilder uri(URI uri) {
        return new QueryStringBuilder().uri(uri);
    }

    public static QueryStringBuilder uriEncoded(String uri) {
        return new QueryStringBuilder().uriEncoded(uri);
    }

    public static QueryStringBuilder uri(String uri) {
        return new QueryStringBuilder().uri(uri);
    }

    public static QueryStringBuilder queryEncoded(String queryString) {
        return new QueryStringBuilder().queryEncoded(queryString);
    }

    public static QueryStringBuilder query(String queryString) {
        return new QueryStringBuilder().query(queryString);
    }
}
