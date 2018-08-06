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

import java.util.List;

/**
 * @author Tilen Faganel
 */
public class QueryStringDefaults {

    private Boolean paginationEnabled = true;
    private Boolean filtersEnabled = true;
    private Boolean orderEnabled = true;
    private Boolean fieldsEnabled = true;

    private Long maxLimit = 100L;
    private Long defaultLimit = 10L;
    private Long defaultOffset = 0L;
    private List<QueryFilter> defaultFilters = null;

    public QueryStringDefaults enablePagination(Boolean enable) {

        paginationEnabled = enable;

        return this;
    }

    public QueryStringDefaults enableFilters(Boolean enable) {

        filtersEnabled = enable;

        return this;
    }

    public QueryStringDefaults enableOrder(Boolean enable) {

        orderEnabled = enable;

        return this;
    }

    public QueryStringDefaults enableFields(Boolean enable) {

        fieldsEnabled = enable;

        return this;
    }

    public QueryStringDefaults maxLimit(int limit) {

        return maxLimit((long) limit);
    }

    public QueryStringDefaults maxLimit(Long limit) {

        maxLimit = limit;

        return this;
    }

    public QueryStringDefaults defaultLimit(int limit) {

        return defaultLimit((long) limit);
    }

    public QueryStringDefaults defaultLimit(Long limit) {

        defaultLimit = limit;

        return this;
    }

    public QueryStringDefaults defaultOffset(int offset) {

        return defaultOffset((long) offset);
    }

    public QueryStringDefaults defaultOffset(Long offset) {

        defaultOffset = offset;

        return this;
    }

    public QueryStringDefaults defaultFilters(List<QueryFilter> filters) {

        defaultFilters = filters;

        return this;
    }

    public QueryStringBuilder builder() {
        return new QueryStringBuilder()
                .maxLimit(maxLimit)
                .defaultLimit(defaultLimit)
                .defaultOffset(defaultOffset)
                .defaultFilters(defaultFilters)
                .enablePagination(paginationEnabled)
                .enableFilters(filtersEnabled)
                .enableOrder(orderEnabled)
                .enableFields(fieldsEnabled);
    }
}
