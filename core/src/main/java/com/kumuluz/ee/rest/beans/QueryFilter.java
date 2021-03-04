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

import com.kumuluz.ee.rest.enums.FilterOperation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Tilen Faganel
 */
public class QueryFilter implements Serializable {

    private final static long serialVersionUID = 1L;

    private String field;

    private FilterOperation operation;

    private String value;

    private Date dateValue;

    private List<String> values;

    public QueryFilter() {
    }

    public QueryFilter(String field, FilterOperation operation) {
        this.field = field;
        this.operation = operation;
    }

    public QueryFilter(String field, FilterOperation operation, String value) {
        this.field = field;
        this.operation = operation;
        this.value = value;
    }

    public QueryFilter(String field, FilterOperation operation, Date dateValue) {
        this.field = field;
        this.operation = operation;
        this.dateValue = dateValue;
    }

    public QueryFilter(String field, FilterOperation operation, List<String> values) {
        this.field = field;
        this.operation = operation;
        this.values = values;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public FilterOperation getOperation() {
        return operation;
    }

    public void setOperation(FilterOperation operation) {
        this.operation = operation;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<String> getValues() {

        if (values == null)
            values = new ArrayList<>();

        return values;
    }

    public void setValues(List<String> values) {

        this.values = values;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }
}
