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
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.beans.StreamCriteriaField;
import com.kumuluz.ee.rest.beans.StreamCriteriaWhereQuery;
import com.kumuluz.ee.rest.enums.FilterOperation;
import com.kumuluz.ee.rest.enums.OrderDirection;
import com.kumuluz.ee.rest.enums.OrderNulls;
import com.kumuluz.ee.rest.exceptions.InvalidEntityFieldException;
import com.kumuluz.ee.rest.exceptions.NoSuchEntityFieldException;
import com.kumuluz.ee.rest.interfaces.CriteriaFilter;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kumuluz.ee.rest.utils.ClassUtils.*;

/**
 * @author Zvone
 * @author gpor0
 */
public class StreamUtils {

    private static final Logger log = Logger.getLogger(StreamUtils.class.getSimpleName());

    public static <T> Stream<T> queryEntities(Stream<T> stream, Class<T> entity, QueryParameters q) {

        return queryEntities(stream, entity, q, null);
    }

    public static <T> List<T> queryEntities(Collection<T> collection) {

        return queryEntities(collection, new QueryParameters(), null);
    }

    public static <T> List<T> queryEntities(Collection<T> collection, QueryParameters q) {

        return queryEntities(collection, q, null);
    }

    public static <T> List<T> queryEntities(Collection<T> collection, CriteriaFilter<T> customFilter) {
        return queryEntities(collection, new QueryParameters(), customFilter);
    }

    public static <T> List<T> queryEntities(Collection<T> collection, QueryParameters q, CriteriaFilter<T> customFilter) {
        if (null == collection || collection.isEmpty()) {
            return new ArrayList<>();
        }

        Class<T> entity = (Class<T>) collection.iterator().next().getClass();

        return queryEntities(collection.stream(), entity, q, customFilter).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<T> queryEntities(Stream<T> stream, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter) {

        if (null == stream) {
            return Stream.empty();
        }

        if (q == null)
            throw new IllegalArgumentException("Query parameters can't be null. " +
                    "If you don't have any parameters either pass a empty object or " +
                    "use the queryEntities(Class<T>) method.");

        if (log.isLoggable(Level.FINEST)) {
            log.finest("Querying entity: '" + entity.getSimpleName() + "' with parameters: " + q);
        }

        Predicate wherePredicate = null;

        if (!q.getFilters().isEmpty()) {

            StreamCriteriaWhereQuery criteriaWhereQuery = createWhereQueryInternal(entity, q);

            wherePredicate = criteriaWhereQuery.getPredicate();
        }

        if (wherePredicate != null) {
            stream = stream.filter(wherePredicate);
        }

        if (!q.getOrder().isEmpty()) {

            Comparator comparator = createOrderQuery(entity, q, null);

            if (comparator != null) {
                stream = stream.sorted(comparator);
            }
        }

        if (!q.getFields().isEmpty()) {
            stream = stream.map(createFieldsSelect(entity, q));
        }

        if (q.getOffset() != null && q.getOffset() > -1) {
            stream = stream.skip(q.getOffset().intValue());
        }

        if (q.getLimit() != null && q.getLimit() > -1) {
            stream = stream.limit(q.getLimit().intValue());
        }

        return stream;
    }

    public static <T> Long queryEntitiesCount(Collection<T> collection) {

        return queryEntitiesCount(collection, new QueryParameters(), null);
    }

    public static <T> Long queryEntitiesCount(Collection<T> collection, QueryParameters q) {

        return queryEntitiesCount(collection, q, null);
    }

    public static <T> Long queryEntitiesCount(Collection<T> collection, CriteriaFilter<T> customFilter) {
        return queryEntitiesCount(collection, new QueryParameters(), customFilter);
    }

    public static <T> Long queryEntitiesCount(Collection<T> collection, QueryParameters q, CriteriaFilter<T> customFilter) {
        if (null == collection || collection.isEmpty()) {
            return 0L;
        }

        Class<T> entity = (Class<T>) collection.iterator().next().getClass();

        return queryEntitiesCount(collection.parallelStream(), entity, q, customFilter);
    }

    public static <T> Long queryEntitiesCount(Stream<T> stream, Class<T> entity, QueryParameters q,
                                              CriteriaFilter<T> customFilter) {

        if (entity == null)
            throw new IllegalArgumentException("The entity manager and the entity cannot be null.");

        if (q == null)
            throw new IllegalArgumentException("Query parameters can't be null. " +
                    "If you don't have any parameters either pass a empty object or " +
                    "use the queryEntitiesCount(EntityManager, Class<T>) method.");

        log.finest("Querying entity count: '" + entity.getSimpleName() + "' with parameters: " + q);

        Predicate wherePredicate = null;

        if (!q.getFilters().isEmpty()) {

            StreamCriteriaWhereQuery criteriaWhereQuery = createWhereQueryInternal(entity, q);

            wherePredicate = criteriaWhereQuery.getPredicate();
        }

        if (wherePredicate != null) {
            return stream.filter(wherePredicate).count();
        }

        return stream.count();
    }

    public static Comparator createOrderQuery(Class<?> clazz, QueryParameters q) {
        return createOrderQuery(clazz, q, null);
    }

    public static Comparator createOrderQuery(Class<?> clazz, QueryParameters q, String id) {

        final Comparator[] comparator = {null};

        q.getOrder().stream().filter(qo -> qo.getField() != null).forEach(qo -> {

            try {

                StreamCriteriaField field = getStreamCriteriaField(clazz, qo.getField());

                if (null != field) {
                    Comparator c = comparator(clazz, field.getPath(), qo.getOrder(), qo.getNulls());

                    if (comparator[0] == null) {
                        comparator[0] = c;
                    } else {
                        comparator[0] = comparator[0].thenComparing(c);
                    }
                }
            } catch (IllegalArgumentException e) {

                throw new NoSuchEntityFieldException(e.getMessage(), qo.getField(), clazz.getSimpleName());
            }
        });

        //Add sort by id for correct pagination when field has same values
        if (id != null) {
            StreamCriteriaField criteriaField = getStreamCriteriaField(clazz, id);
            if (null != criteriaField) {
                Comparator c = comparator(clazz, criteriaField.getPath(), OrderDirection.ASC, OrderNulls.LAST);

                if (comparator[0] == null) {
                    comparator[0] = c;
                } else {
                    comparator[0] = comparator[0].thenComparing(c);
                }
            }
        }

        return comparator[0];
    }

    public static <T, R> Function<T, R> createFieldsSelect(Class<?> r, QueryParameters q) {
        HashMap<String, HashSet<String>> fieldsMap = new HashMap<>();

        final List<String> fields = q.getFields().stream().distinct().flatMap(restField ->
                ClassUtils.getRestFieldMappings(r, restField).map(f -> {

                    String[] fSplit = f.split("\\.");

                    Class<?> p = null;
                    for (String fS : fSplit) {
                        try {
                            if (isRestIgnored(null == p ? r : p, fS)) {
                                return null;
                            }

                            p = p == null ? ClassUtils.fieldLookup(r, fS).getType() : ClassUtils.fieldLookup(p, fS).getType();
                        } catch (IllegalArgumentException | NoSuchFieldException e) {
                            throw new NoSuchEntityFieldException(e.getMessage(), f, r.getSimpleName());
                        }
                    }

                    if (p == null) {
                        throw new NoSuchEntityFieldException("", f, r.getSimpleName());
                    }

                    return f;

                }).filter(Objects::nonNull)
        ).collect(Collectors.toList());

        fields.add("id");

        for (String finalField : fields) {
            String[] fSplit = finalField.split("\\.");

            while (fSplit.length >= 1) {
                String[] fSplitNew = Arrays.copyOfRange(fSplit, 0, fSplit.length - 1);
                String key = String.join(".", fSplitNew);

                if (fieldsMap.containsKey(key)) {
                    fieldsMap.get(key).add(fSplit[fSplit.length - 1]);
                } else {
                    HashSet<String> values = new HashSet<>();
                    values.add(fSplit[fSplit.length - 1]);
                    fieldsMap.putIfAbsent(key, values);
                }

                fSplit = fSplitNew;
            }
        }

        return (Function<T, R>) nullify(r, fieldsMap);
    }

    // Temporary methods to not break the public API

    private static StreamCriteriaWhereQuery createWhereQueryInternal(Class<?> clazz, QueryParameters q) {

        Predicate predicate = null;
        Boolean containsToMany = false;

        for (QueryFilter f : q.getFilters()) {

            Predicate np = null;

            try {
                StreamCriteriaField criteriaField = getStreamCriteriaField(clazz, f.getField());

                if (null == criteriaField) {
                    continue;
                }

                String entityField = criteriaField.getPath();

                String[] fieldNames = entityField.split("\\.");

                Field field = ClassUtils.fieldLookup(clazz, fieldNames[0]);

                Class<?> clazzTarget = ClassUtils.fieldLookup(clazz, fieldNames[0]).getType();
                field.setAccessible(true);

                if (fieldNames.length > 1) {

                    String newFieldName = entityField.substring(fieldNames[0].length() + 1);
                    do {

                        if (Collection.class.isAssignableFrom(clazzTarget)) {
                            clazzTarget = getGenericType(field);
                        }

                        fieldNames = newFieldName.split("\\.");

                        field = ClassUtils.fieldLookup(clazzTarget, fieldNames[0]);
                        field.setAccessible(true);

                        clazzTarget = field.getType();

                        if (fieldNames.length > 1)
                            newFieldName = newFieldName.substring(fieldNames[0].length() + 1);

                    } while (newFieldName.contains("."));
                }

                switch (f.getOperation()) {

                    case EQ:
                        if (f.getDateValue() != null && clazzTarget.equals(Date.class)) {
                            np = filter(clazz, entityField, f.getDateValue(), FilterOperation.EQ);
                        } else if (f.getValue() != null) {
                            np = filter(clazz, entityField, getValueForField(field, f.getValue()), FilterOperation.EQ);
                        }
                        break;
                    case EQIC:
                        if (clazzTarget.equals(String.class) && f.getValue() != null) {
                            np = filter(clazz, entityField, f.getValue().toLowerCase(), FilterOperation.EQIC);
                        }
                        break;
                    case NEQ:
                        if (f.getDateValue() != null && clazzTarget.equals(Date.class)) {
                            np = filter(clazz, entityField, f.getDateValue(), FilterOperation.NEQ);
                        } else if (f.getValue() != null) {
                            np = filter(clazz, entityField, getValueForField(field, f.getValue()), FilterOperation.NEQ);
                        }
                        break;
                    case NEQIC:
                        if (clazzTarget.equals(String.class) && f.getValue() != null) {
                            np = filter(clazz, entityField, f.getValue().toLowerCase(), FilterOperation.NEQIC);
                        }
                        break;
                    case LIKE:
                        if (clazzTarget.equals(String.class) && f.getValue() != null) {
                            np = filter(clazz, entityField, f.getValue(), FilterOperation.LIKE);
                        }
                        break;
                    case LIKEIC:
                        if (clazzTarget.equals(String.class) && f.getValue() != null) {
                            np = filter(clazz, entityField, f.getValue().toLowerCase(), FilterOperation.LIKEIC);
                        }
                        break;
                    case NLIKE:
                        if (clazzTarget.equals(String.class) && f.getValue() != null) {
                            np = filter(clazz, entityField, f.getValue(), FilterOperation.NLIKE);
                        }
                        break;
                    case NLIKEIC:
                        if (clazzTarget.equals(String.class) && f.getValue() != null) {
                            np = filter(clazz, entityField, f.getValue().toLowerCase(), FilterOperation.NLIKEIC);
                        }
                        break;
                    case GT:
                        if (Date.class.isAssignableFrom(clazzTarget) ||
                                Instant.class.isAssignableFrom(clazzTarget) ||
                                Number.class.isAssignableFrom(clazzTarget) ||
                                String.class.isAssignableFrom(clazzTarget)) {

                            if (f.getDateValue() != null && clazzTarget.equals(Date.class)) {
                                np = filter(clazz, entityField, f.getDateValue(), FilterOperation.GT);
                            } else if (f.getValue() != null) {
                                np = filter(clazz, entityField, getValueForField(field, f.getValue()), FilterOperation.GT);
                            }
                        }
                        break;
                    case GTE:
                        if (Date.class.isAssignableFrom(clazzTarget) ||
                                Instant.class.isAssignableFrom(clazzTarget) ||
                                Number.class.isAssignableFrom(clazzTarget) ||
                                String.class.isAssignableFrom(clazzTarget)) {

                            if (f.getDateValue() != null && clazzTarget.equals(Date.class)) {
                                np = filter(clazz, entityField, f.getDateValue(), FilterOperation.GTE);
                            } else if (f.getValue() != null) {
                                np = filter(clazz, entityField, getValueForField(field, f.getValue()), FilterOperation.GTE);
                            }
                        }
                        break;
                    case LT:
                        if (Date.class.isAssignableFrom(clazzTarget) ||
                                Instant.class.isAssignableFrom(clazzTarget) ||
                                Number.class.isAssignableFrom(clazzTarget) ||
                                String.class.isAssignableFrom(clazzTarget)) {

                            if (f.getDateValue() != null && clazzTarget.equals(Date.class)) {
                                np = filter(clazz, entityField, f.getDateValue(), FilterOperation.LT);
                            } else if (f.getValue() != null) {
                                np = filter(clazz, entityField, getValueForField(field, f.getValue()), FilterOperation.LT);
                            }
                        }
                        break;
                    case LTE:
                        if (Date.class.isAssignableFrom(clazzTarget) ||
                                Instant.class.isAssignableFrom(clazzTarget) ||
                                Number.class.isAssignableFrom(clazzTarget) ||
                                String.class.isAssignableFrom(clazzTarget)) {

                            if (f.getDateValue() != null && clazzTarget.equals(Date.class)) {
                                np = filter(clazz, entityField, f.getDateValue(), FilterOperation.LTE);
                            } else if (f.getValue() != null) {
                                np = filter(clazz, entityField, getValueForField(field, f.getValue()), FilterOperation.LTE);
                            }
                        }
                        break;
                    case IN:
                        np = filter(clazz, entityField, f.getValues(), FilterOperation.IN);
                        break;
                    case INIC:
                        if (clazzTarget.equals(String.class)) {
                            np = filter(clazz, entityField, f.getValues(), FilterOperation.INIC);
                        }
                        break;
                    case NIN:
                        np = filter(clazz, entityField, f.getValues(), FilterOperation.NIN);
                        break;
                    case NINIC:
                        if (clazzTarget.equals(String.class)) {

                            np = filter(clazz, entityField, f.getValues(), FilterOperation.NINIC);
                        }
                        break;
                    case ISNULL:
                        np = filter(clazz, entityField, f.getValues(), FilterOperation.ISNULL);
                        break;
                    case ISNOTNULL:
                        np = filter(clazz, entityField, f.getValues(), FilterOperation.ISNOTNULL);
                        break;
                }
            } catch (IllegalArgumentException | NoSuchFieldException e) {

                throw new NoSuchEntityFieldException(e.getMessage(), f.getField(), clazz.getSimpleName());
            }

            if (predicate == null) {
                predicate = np;
            } else {
                if (np != null) {
                    predicate = predicate.and(np);
                }
            }
        }

        return new StreamCriteriaWhereQuery(predicate, containsToMany);
    }

    private static StreamCriteriaField getStreamCriteriaField(Class<?> clazz, String fieldName) {

        if (fieldName == null) throw new NoSuchEntityFieldException("No such entity field", fieldName, clazz.getSimpleName());

        String[] fields = fieldName.split("\\.");

        StringBuilder path = new StringBuilder();

        Class<?> from = clazz;

        boolean isCollectionField = false;

        for (String field : fields) {

            final Optional<String> mappedFieldOptional = ClassUtils.getRestFieldMappings(from, field).findFirst();

            //ignored
            if (!mappedFieldOptional.isPresent()) {
                return null;
            }

            String mappedField = mappedFieldOptional.get();

            try {
                Field declaredField = ClassUtils.fieldLookup(from, mappedField);
                if (path.toString().equals("")) {
                    path.append(declaredField.getName());
                } else {
                    path.append(".").append(declaredField.getName());
                }

                if (Collection.class.isAssignableFrom(declaredField.getType())) {
                    isCollectionField = true;

                    from = getGenericType(declaredField);
                } else {
                    from = declaredField.getType();
                }

            } catch (NoSuchFieldException e) {
                throw new NoSuchEntityFieldException(e.getMessage(), mappedField, clazz.getSimpleName());
            }
        }

        return new StreamCriteriaField(path.toString(), isCollectionField);
    }

    private static <T> Predicate<T> filter(Class<T> clazz, String fieldName, Object fieldValue, FilterOperation operation) {

        return (T instance) -> {
            try {
                String[] fieldNames = fieldName.split("\\.");

                Field f = ClassUtils.fieldLookup(clazz, fieldNames[0]);

                if (null == f) {
                    return true;
                }

                f.setAccessible(true);

                final Field field = f;

                Object value = field.get(instance);

                if (operation.equals(FilterOperation.EQ)) {
                    if (value == null) {
                        return false;
                    } else if (value instanceof Integer) {
                        return value.equals(Integer.parseInt((String) fieldValue));
                    } else if (value instanceof String) {
                        return value.equals(fieldValue);
                    } else if (value instanceof BigDecimal) {
                        return value.equals(new BigDecimal((String) fieldValue));
                    } else if (value instanceof LocalDate) {
                        return value.equals(LocalDate.parse((String) fieldValue));
                    } else if (value instanceof LocalDateTime) {
                        return value.equals(LocalDateTime.parse((String) fieldValue));
                    } else if (value instanceof Double) {
                        return value.equals(Double.parseDouble((String) fieldValue));
                    } else if (value instanceof Float) {
                        return value.equals(Float.parseFloat((String) fieldValue));
                    } else if (value instanceof Long) {
                        return value.equals(Long.parseLong((String) fieldValue));
                    } else if (value instanceof Boolean) {
                        return value.equals(fieldValue);
                    } else if (value instanceof Byte) {
                        return value.equals(Byte.parseByte((String) fieldValue));
                    } else if (value instanceof Short) {
                        return value.equals(Short.parseShort((String) fieldValue));
                    } else if (value instanceof Date) {
                        return (((Date) value).toInstant()).equals(((Date) fieldValue).toInstant());
                    } else if (value instanceof Enum) {
                        return value.equals(getValueForField(field, fieldValue.toString()));
                    } else if (Collection.class.isAssignableFrom(value.getClass())) {

                        Predicate newPredicate = filter(getGenericType(field), fieldName.substring(fieldNames[0].length() + 1),
                                fieldValue,
                                FilterOperation.EQ);

                        return ((List<?>) value).stream().anyMatch(newPredicate);

                    } else { // assume entity class
                        Predicate newPredicate = filter(field.getType(), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                FilterOperation.EQ);
                        return newPredicate.test(value);
                    }
                } else if (operation.equals(FilterOperation.EQIC)) {
                    if (value == null) {
                        return false;
                    } else if (value instanceof String) {
                        return ((String) fieldValue).equalsIgnoreCase((String) value);
                    } else if (Collection.class.isAssignableFrom(value.getClass())) {

                        Predicate newPredicate = filter(getGenericType(field), fieldName.substring(fieldNames[0].length() + 1),
                                fieldValue,
                                FilterOperation.EQIC);

                        return ((List<?>) value).stream().anyMatch(newPredicate);

                    } else { // assume entity class
                        Predicate newPredicate = filter(field.getType(), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                FilterOperation.EQIC);
                        return newPredicate.test(value);
                    }
                } else if (operation.equals(FilterOperation.NEQ)) {
                    if (value == null) {
                        return true;
                    } else if (value instanceof Integer) {
                        return !value.equals(Integer.parseInt((String) fieldValue));
                    } else if (value instanceof String) {
                        return !value.equals(fieldValue);
                    } else if (value instanceof Double) {
                        return !value.equals(Double.parseDouble((String) fieldValue));
                    } else if (value instanceof Float) {
                        return !value.equals(Float.parseFloat((String) fieldValue));
                    } else if (value instanceof Long) {
                        return !value.equals(Long.parseLong((String) fieldValue));
                    } else if (value instanceof Boolean) {
                        return !value.equals(fieldValue);
                    } else if (value instanceof Byte) {
                        return !value.equals(Byte.parseByte((String) fieldValue));
                    } else if (value instanceof Short) {
                        return !value.equals(Short.parseShort((String) fieldValue));
                    } else if (value instanceof Date) {
                        return !(((Date) value).toInstant()).equals(((Date) fieldValue).toInstant());
                    } else if (value instanceof Enum) {
                        return !value.equals(getValueForField(field, fieldValue.toString()));
                    } else if (Collection.class.isAssignableFrom(value.getClass())) {

                        Predicate newPredicate = filter(getGenericType(field), fieldName.substring(fieldNames[0].length() + 1),
                                fieldValue,
                                FilterOperation.NEQ);

                        return ((List<?>) value).stream().noneMatch(newPredicate);

                    } else { // assume entity class
                        Predicate newPredicate = filter(field.getType(), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                FilterOperation.NEQ);
                        return newPredicate.test(value);
                    }
                } else if (operation.equals(FilterOperation.NEQIC) && field.getType().equals(String.class)) {
                    if (value == null) {
                        return false;
                    } else if (value instanceof String) {
                        return !((String) fieldValue).equalsIgnoreCase((String) field.get(instance));
                    } else if (Collection.class.isAssignableFrom(value.getClass())) {

                        Predicate newPredicate = filter(getGenericType(field), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                FilterOperation.NEQIC);

                        return ((List<?>) value).stream().noneMatch(newPredicate);

                    } else { // assume entity class
                        Predicate newPredicate = filter(field.getType(), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                FilterOperation.NEQIC);
                        return newPredicate.test(value);
                    }
                } else if (operation.equals(FilterOperation.LIKE)) {
                    if (value == null) {
                        return false;
                    } else if (value instanceof String) {
                        String expr = ((String) fieldValue);
                        expr = expr.replace(".", "\\.");
                        expr = expr.replace("?", ".");
                        expr = expr.replace("%", ".*");

                        return ((String) value).matches(expr);

                    } else if (Collection.class.isAssignableFrom(value.getClass())) {

                        Predicate newPredicate = filter(getGenericType(field), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                FilterOperation.LIKE);

                        return ((List<?>) value).stream().anyMatch(newPredicate);

                    } else { // assume entity class
                        Predicate newPredicate = filter(field.getType(), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                FilterOperation.LIKE);
                        return newPredicate.test(value);
                    }
                } else if (operation.equals(FilterOperation.LIKEIC)) {
                    if (value == null) {
                        return false;
                    } else if (value instanceof String) {
                        String expr = ((String) fieldValue).toLowerCase();
                        expr = expr.replace(".", "\\.");
                        expr = expr.replace("?", ".");
                        expr = expr.replace("%", ".*");

                        return ((String) value).toLowerCase().matches(expr);
                    } else if (Collection.class.isAssignableFrom(value.getClass())) {

                        Predicate newPredicate = filter(getGenericType(field), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                FilterOperation.LIKEIC);

                        return ((List<?>) value).stream().anyMatch(newPredicate);

                    } else { // assume entity class
                        Predicate newPredicate = filter(field.getType(), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                FilterOperation.LIKEIC);
                        return newPredicate.test(value);
                    }
                } else if (operation.equals(FilterOperation.NLIKE)) {
                    if (value == null) {
                        return false;
                    } else if (value instanceof String) {
                        String expr = ((String) fieldValue);
                        expr = expr.replace(".", "\\.");
                        expr = expr.replace("?", ".");
                        expr = expr.replace("%", ".*");

                        return !((String) value).matches(expr);

                    } else if (Collection.class.isAssignableFrom(value.getClass())) {

                        Predicate newPredicate = filter(getGenericType(field), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                FilterOperation.NLIKE);

                        return ((List<?>) value).stream().anyMatch(newPredicate);

                    } else { // assume entity class
                        Predicate newPredicate = filter(field.getType(), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                FilterOperation.NLIKE);
                        return newPredicate.test(value);
                    }
                } else if (operation.equals(FilterOperation.NLIKEIC)) {
                    if (value == null) {
                        return false;
                    } else if (value instanceof String) {
                        String expr = ((String) fieldValue).toLowerCase();
                        expr = expr.replace(".", "\\.");
                        expr = expr.replace("?", ".");
                        expr = expr.replace("%", ".*");

                        return !((String) value).toLowerCase().matches(expr);
                    } else if (Collection.class.isAssignableFrom(value.getClass())) {

                        Predicate newPredicate = filter(getGenericType(field), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                FilterOperation.NLIKEIC);

                        return ((List<?>) value).stream().anyMatch(newPredicate);

                    } else { // assume entity class
                        Predicate newPredicate = filter(field.getType(), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                FilterOperation.NLIKEIC);
                        return newPredicate.test(value);
                    }
                } else if (operation.equals(FilterOperation.GT)) {
                    if (value == null) {
                        return false;
                    } else if (value instanceof Integer) {
                        return ((Integer) value).compareTo(Integer.parseInt((String) fieldValue)) > 0;
                    } else if (value instanceof Double) {
                        return ((Double) value).compareTo(Double.parseDouble((String) fieldValue)) > 0;
                    } else if (value instanceof Float) {
                        return ((Float) value).compareTo(Float.parseFloat((String) fieldValue)) > 0;
                    } else if (value instanceof Long) {
                        return ((Long) value).compareTo(Long.parseLong((String) fieldValue)) > 0;
                    } else if (value instanceof Boolean) {
                        return ((Boolean) value).compareTo((Boolean) fieldValue) > 0;
                    } else if (value instanceof Byte) {
                        return ((Byte) value).compareTo(Byte.parseByte((String) fieldValue)) > 0;
                    } else if (value instanceof Short) {
                        return ((Short) value).compareTo(Short.parseShort((String) fieldValue)) > 0;
                    } else if (value instanceof Date) {
                        return ((Date) value).toInstant().compareTo(((Date) fieldValue).toInstant()) > 0;
                    } else if (Collection.class.isAssignableFrom(value.getClass())) {

                        Predicate newPredicate = filter(getGenericType(field), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                FilterOperation.GT);

                        return ((List<?>) value).stream().anyMatch(newPredicate);

                    } else { // assume entity class
                        Predicate newPredicate = filter(field.getType(), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                FilterOperation.GT);
                        return newPredicate.test(value);
                    }
                } else if (operation.equals(FilterOperation.GTE)) {
                    if (value == null) {
                        return false;
                    } else if (value instanceof Integer) {
                        return ((Integer) value).compareTo(Integer.parseInt((String) fieldValue)) >= 0;
                    } else if (value instanceof Double) {
                        return ((Double) value).compareTo(Double.parseDouble((String) fieldValue)) >= 0;
                    } else if (value instanceof Float) {
                        return ((Float) value).compareTo(Float.parseFloat((String) fieldValue)) >= 0;
                    } else if (value instanceof Long) {
                        return ((Long) value).compareTo(Long.parseLong((String) fieldValue)) >= 0;
                    } else if (value instanceof Boolean) {
                        return ((Boolean) value).compareTo((Boolean) fieldValue) >= 0;
                    } else if (value instanceof Byte) {
                        return ((Byte) value).compareTo(Byte.parseByte((String) fieldValue)) >= 0;
                    } else if (value instanceof Short) {
                        return ((Short) value).compareTo(Short.parseShort((String) fieldValue)) >= 0;
                    } else if (value instanceof Date) {
                        return ((Date) value).toInstant().compareTo(((Date) fieldValue).toInstant()) >= 0;
                    } else if (Collection.class.isAssignableFrom(value.getClass())) {

                        Predicate newPredicate = filter(getGenericType(field), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                FilterOperation.GTE);

                        return ((List<?>) value).stream().anyMatch(newPredicate);

                    } else { // assume entity class
                        Predicate newPredicate = filter(field.getType(), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                FilterOperation.GTE);
                        return newPredicate.test(value);
                    }
                } else if (operation.equals(FilterOperation.LT)) {

                    if (value == null) {
                        return false;
                    } else if (value instanceof Integer) {
                        return ((Integer) value).compareTo(Integer.parseInt((String) fieldValue)) < 0;
                    } else if (value instanceof Double) {
                        return ((Double) value).compareTo(Double.parseDouble((String) fieldValue)) < 0;
                    } else if (value instanceof Float) {
                        return ((Float) value).compareTo(Float.parseFloat((String) fieldValue)) < 0;
                    } else if (value instanceof Long) {
                        return ((Long) value).compareTo(Long.parseLong((String) fieldValue)) < 0;
                    } else if (value instanceof Boolean) {
                        return ((Boolean) value).compareTo((Boolean) fieldValue) < 0;
                    } else if (value instanceof Byte) {
                        return ((Byte) value).compareTo(Byte.parseByte((String) fieldValue)) < 0;
                    } else if (value instanceof Short) {
                        return ((Short) value).compareTo(Short.parseShort((String) fieldValue)) < 0;
                    } else if (value instanceof Date) {
                        return ((Date) value).toInstant().compareTo(((Date) fieldValue).toInstant()) < 0;
                    } else if (Collection.class.isAssignableFrom(value.getClass())) {

                        Predicate newPredicate = filter(getGenericType(field), fieldName.substring(fieldNames[0].length() + 1),
                                fieldValue,
                                FilterOperation.LT);

                        return ((List<?>) value).stream().anyMatch(newPredicate);

                    } else { // assume entity class
                        Predicate newPredicate = filter(field.getType(), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                FilterOperation.LT);
                        return newPredicate.test(value);
                    }
                } else if (operation.equals(FilterOperation.LTE)) {

                    if (value == null) {
                        return false;
                    } else if (value instanceof Integer) {
                        return ((Integer) value).compareTo(Integer.parseInt((String) fieldValue)) <= 0;
                    } else if (value instanceof Double) {
                        return ((Double) value).compareTo(Double.parseDouble((String) fieldValue)) <= 0;
                    } else if (value instanceof Float) {
                        return ((Float) value).compareTo(Float.parseFloat((String) fieldValue)) <= 0;
                    } else if (value instanceof Long) {
                        return ((Long) value).compareTo(Long.parseLong((String) fieldValue)) <= 0;
                    } else if (value instanceof Boolean) {
                        return ((Boolean) value).compareTo((Boolean) fieldValue) <= 0;
                    } else if (value instanceof Byte) {
                        return ((Byte) value).compareTo(Byte.parseByte((String) fieldValue)) <= 0;
                    } else if (value instanceof Short) {
                        return ((Short) value).compareTo(Short.parseShort((String) fieldValue)) <= 0;
                    } else if (value instanceof Date) {
                        return ((Date) value).toInstant().compareTo(((Date) fieldValue).toInstant()) <= 0;
                    } else if (Collection.class.isAssignableFrom(value.getClass())) {

                        Predicate newPredicate = filter(getGenericType(field), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                FilterOperation.LTE);

                        return ((List<?>) value).stream().anyMatch(newPredicate);

                    } else { // assume entity class
                        Predicate newPredicate = filter(field.getType(), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                FilterOperation.LTE);
                        return newPredicate.test(value);
                    }
                } else if (operation.equals(FilterOperation.IN)) {
                    if (value != null) {
                        if (value instanceof String || value instanceof Double || value instanceof Float || value instanceof Long ||
                                value instanceof Boolean || value instanceof Byte || value instanceof Short || value instanceof Date || value instanceof Enum) {

                            return ((List<?>) fieldValue).stream()
                                    .filter(Objects::nonNull)
                                    .map(s -> getValueForField(field, (String) s)).collect(Collectors
                                            .toList()).contains(value);

                        } else if (Collection.class.isAssignableFrom(value.getClass())) {

                            Predicate newPredicate = filter(getGenericType(field), fieldName.substring(fieldNames[0].length() + 1),
                                    fieldValue,
                                    FilterOperation.IN);

                            return ((List<?>) value).stream().anyMatch(newPredicate);

                        } else { // assume entity class
                            Predicate newPredicate = filter(field.getType(), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                    FilterOperation.IN);
                            return newPredicate.test(value);
                        }
                    } else
                        return false;
                } else if (operation.equals(FilterOperation.INIC)) {
                    if (value != null) {
                        if (value instanceof String) {

                            return ((List<String>) fieldValue).stream()
                                    .filter(Objects::nonNull)
                                    .map(String::toLowerCase).collect(Collectors
                                            .toList()).contains(((String) value).toLowerCase());

                        } else if (Collection.class.isAssignableFrom(value.getClass())) {

                            Predicate newPredicate = filter(getGenericType(field), fieldName.substring(fieldNames[0].length() + 1),
                                    fieldValue,
                                    FilterOperation.INIC);

                            return ((List<?>) value).stream().anyMatch(newPredicate);

                        } else { // assume entity class
                            Predicate newPredicate = filter(field.getType(), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                    FilterOperation.INIC);
                            return newPredicate.test(value);
                        }
                    } else
                        return false;
                } else if (operation.equals(FilterOperation.NIN)) {
                    if (value != null) {
                        if (value instanceof String || value instanceof Double || value instanceof Float || value instanceof Long ||
                                value instanceof Boolean || value instanceof Byte || value instanceof Short || value instanceof Date || value instanceof Enum) {

                            return !((List<?>) fieldValue).stream()
                                    .filter(Objects::nonNull)
                                    .map(s -> getValueForField(field, (String) s)).collect(Collectors
                                            .toList()).contains(value);

                        } else if (Collection.class.isAssignableFrom(value.getClass())) {

                            Predicate newPredicate = filter(getGenericType(field), fieldName.substring(fieldNames[0].length() + 1),
                                    fieldValue,
                                    FilterOperation.NIN);

                            return ((List<?>) value).stream().anyMatch(newPredicate);

                        } else { // assume entity class
                            Predicate newPredicate = filter(field.getType(), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                    FilterOperation.NIN);
                            return newPredicate.test(value);
                        }
                    } else
                        return false;
                } else if (operation.equals(FilterOperation.NINIC)) {
                    if (value != null) {
                        if (value instanceof String) {

                            return !((List<String>) fieldValue).stream()
                                    .filter(Objects::nonNull)
                                    .map(String::toLowerCase).collect(Collectors
                                            .toList()).contains(((String) field.get(instance)).toLowerCase());

                        } else if (Collection.class.isAssignableFrom(value.getClass())) {

                            Predicate newPredicate = filter(getGenericType(field), fieldName.substring(fieldNames[0].length() + 1),
                                    fieldValue,
                                    FilterOperation.NINIC);

                            return ((List<?>) value).stream().anyMatch(newPredicate);

                        } else { // assume entity class

                            Predicate newPredicate = filter(field.getType(), fieldName.substring(fieldNames[0].length() + 1), fieldValue,
                                    FilterOperation.NINIC);
                            return newPredicate.test(value);
                        }
                    } else
                        return true;
                } else if (operation.equals(FilterOperation.ISNULL)) {
                    if (value == null) {
                        return true;
                    } else {
                        if (Collection.class.isAssignableFrom(value.getClass())) {

                            Predicate newPredicate = filter(getGenericType(field), fieldName.substring(fieldNames[0].length() + 1),
                                    fieldValue,
                                    FilterOperation.ISNULL);

                            return ((List<?>) value).stream().anyMatch(newPredicate);

                        } else { // assume entity class

                            if (value instanceof String || value instanceof Double || value instanceof Float || value instanceof Long ||
                                    value instanceof Boolean || value instanceof Byte || value instanceof Short || value instanceof Date || value instanceof Enum) {
                                return false;
                            } else {
                                Predicate newPredicate = filter(field.getType(), fieldName.substring(fieldNames[0].length() + 1),
                                        fieldValue,
                                        FilterOperation.ISNULL);
                                return newPredicate.test(value);
                            }
                        }
                    }
                } else if (operation.equals(FilterOperation.ISNOTNULL)) {
                    return value != null;
                }

            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new NoSuchEntityFieldException(e.getMessage(), fieldName, clazz.getSimpleName());
            }
            return false;
        };
    }

    private static <T> Comparator<T> comparator(Class<T> clazz, String fieldName, OrderDirection orderDirection, OrderNulls orderNulls) {
        //optimization for sorting by children value
        Map<Collection, Optional<Object>> minCollectionValCache = new HashMap<>();
        return (T instance1, T instance2) -> compareInstanceFields(clazz, fieldName, instance1, instance2, orderDirection, orderNulls, minCollectionValCache);
    }

    private static <T> int compareInstanceFields(Class<T> clazz, String fieldName, T instance1, T instance2, OrderDirection orderDirection,
                                                 OrderNulls orderNulls, Map<Collection, Optional<Object>> minCollectionValCache) {

        try {
            String[] fieldNames = fieldName.split("\\.");

            Class clazzTarget = clazz;

            Field f = ClassUtils.fieldLookup(clazzTarget, fieldNames[0]);
            f.setAccessible(true);
            Class fieldClass = f.getType();

            Object value1 = instance1 == null ? null : f.get(instance1);
            Object value2 = instance2 == null ? null : f.get(instance2);

            if (Collection.class.isAssignableFrom(fieldClass)) {
                Collection c1 = (Collection) value1;
                Collection c2 = (Collection) value2;
                if (c1 == null || c1.isEmpty() || c2 == null || c2.isEmpty()) {
                    return compare(c1 == null || c1.isEmpty() ? null : c1.size(), c2 == null || c2.isEmpty() ? null : c2.size(), orderDirection,
                            orderNulls);
                }
                if (fieldNames.length > 1) {

                    //get from cache
                    Optional<Object> cachedOpt1 = minCollectionValCache.get(c1);
                    Optional<Object> cachedOpt2 = minCollectionValCache.get(c2);

                    if (cachedOpt1 == null || cachedOpt2 == null) {
                        String nextLevelFieldName = String.join(".", Arrays.copyOfRange(fieldNames, 1, fieldNames.length));
                        Comparator comparator = comparator(getGenericType(f), nextLevelFieldName, orderDirection, orderNulls);
                        if (cachedOpt1 == null) {
                            cachedOpt1 = c1.stream().min(comparator);
                            minCollectionValCache.put(c1, cachedOpt1);
                        }
                        if (cachedOpt2 == null) {
                            cachedOpt2 = c2.stream().min(comparator);
                            minCollectionValCache.put(c2, cachedOpt2);
                        }
                    }

                    value1 = cachedOpt1.orElse(null);
                    value2 = cachedOpt2.orElse(null);

                } else {
                    throw new InvalidEntityFieldException("OneToMany and ManyToMany relations are not supported by the order query",
                            fieldNames[0], f.getType().getSimpleName());
                }

                fieldClass = getGenericType(f);
            }

            if (fieldNames.length > 1) {
                String nextLevelFieldName = String.join(".", Arrays.copyOfRange(fieldNames, 1, fieldNames.length));
                return compareInstanceFields(fieldClass, nextLevelFieldName, value1, value2, orderDirection, orderNulls, minCollectionValCache);
            }

            return compare(value1, value2, orderDirection, orderNulls);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new NoSuchEntityFieldException(e.getMessage(), fieldName, clazz.getSimpleName());
        }
    }

    protected static <T> int compare(T value1, T value2, OrderDirection orderDirection, OrderNulls orderNulls) {

        int returnVal;
        try {
            if (value1 == null) {
                returnVal = (value2 == null) ? 0 : (orderNulls == OrderNulls.FIRST ? -1 : 1);
            } else if (value2 == null) {
                returnVal = orderNulls == OrderNulls.FIRST ? 1 : -1;
            } else {
                returnVal = ((Comparable<T>) value1).compareTo(value2);
                if (OrderDirection.DESC == orderDirection) {
                    returnVal *= -1;
                }
            }
        } catch (ClassCastException e) {
            //both instances cannot be cast since of same type therefore result should be same
            returnVal = 0;
        }

        return returnVal;
    }

    private static <T> Function<T, T> nullify(Class<T> clazz, HashMap<String, HashSet<String>> fieldNames) {
        return (T instance) -> {

            fieldNames.forEach((key, values) -> {
                try {

                    Class clazzTarget = clazz;
                    Object value1 = instance;

                    if (!"".equals(key)) {

                        String[] fieldNameParts = key.split("\\.");

                        Field f = ClassUtils.fieldLookup(clazzTarget, fieldNameParts[0]);
                        f.setAccessible(true);

                        if (Collection.class.isAssignableFrom(f.getType())) {
                            throw new InvalidEntityFieldException("Unable to sort one to many relations", f.getName(),
                                    f.getType().getSimpleName());
                        }

                        value1 = f.get(instance);
                        clazzTarget = f.getType();

                        int i = 1;
                        while (i < fieldNameParts.length) {

                            clazzTarget = f.getType();

                            f = ClassUtils.fieldLookup(clazzTarget, fieldNameParts[i]);
                            f.setAccessible(true);

                            if (Collection.class.isAssignableFrom(f.getType())) {
                                throw new InvalidEntityFieldException("Unable to sort one to many relations", f.getName(),
                                        f.getType().getSimpleName());
                            }

                            value1 = f.get(value1);

                            i++;
                        }
                    }

                    for (Field fi : clazzTarget.getDeclaredFields()) {
                        if (!values.contains(fi.getName())) {
                            fi.setAccessible(true);
                            fi.set(value1, null);
                        }
                    }

                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new NoSuchEntityFieldException(e.getMessage(), key, clazz.getSimpleName());
                }
            });

            return instance;
        };
    }
}
