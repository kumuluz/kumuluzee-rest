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

import com.kumuluz.ee.rest.annotations.RestIgnore;
import com.kumuluz.ee.rest.annotations.RestMapping;
import com.kumuluz.ee.rest.beans.*;
import com.kumuluz.ee.rest.enums.FilterOperation;
import com.kumuluz.ee.rest.enums.OrderDirection;
import com.kumuluz.ee.rest.exceptions.InvalidEntityFieldException;
import com.kumuluz.ee.rest.exceptions.InvalidFieldValueException;
import com.kumuluz.ee.rest.exceptions.NoGenericTypeException;
import com.kumuluz.ee.rest.exceptions.NoSuchEntityFieldException;
import com.kumuluz.ee.rest.interfaces.CriteriaFilter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Tilen Faganel
 */
public class StreamUtils {

    private static final Logger log = Logger.getLogger(StreamUtils.class.getSimpleName());

    public static <T> List<T> queryEntities(Collection<T> collection, Class<T> entity) {

        return queryEntities(collection, entity, new QueryParameters());
    }

    public static <T> List<T> queryEntities(Collection<T> collection, Class<T> entity, QueryParameters q) {

        return queryEntities(collection, entity, q, null, null, null);
    }

    public static <T> List<T> queryEntities(Collection<T> collection, Class<T> entity, CriteriaFilter<T> customFilter) {
        return queryEntities(collection, entity, new QueryParameters(), customFilter, null, null);
    }

    public static <T> List<T> queryEntities(Collection<T> collection, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter) {
        return queryEntities(collection, entity, q, customFilter, null, null);
    }

    public static <T> List<T> queryEntities(Collection<T> collection, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter,
                                            List<QueryHintPair> queryHints) {
        return queryEntities(collection, entity, q, customFilter, queryHints, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> queryEntities(Collection<?> collection, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter,
                                            List<QueryHintPair> queryHints, String rootAlias) {

        if (entity == null)
            throw new IllegalArgumentException("The entity cannot be null.");

        if (q == null)
            throw new IllegalArgumentException("Query parameters can't be null. " +
                    "If you don't have any parameters either pass a empty object or " +
                    "use the queryEntities(Class<T>) method.");

        log.finest("Querying entity: '" + entity.getSimpleName() + "' with parameters: " + q);

        Boolean requiresDistinct = false;

        Predicate wherePredicate = null;

        if (!q.getFilters().isEmpty()) {

            StreamCriteriaWhereQuery criteriaWhereQuery = createWhereQueryInternal(entity, q);

            requiresDistinct = criteriaWhereQuery.containsToMany();
            wherePredicate = criteriaWhereQuery.getPredicate();
        }

        /*if (customFilter != null) {

            wherePredicate = customFilter.createPredicate(
                    wherePredicate == null ? cb.conjunction() : wherePredicate, cb, r);
        }

        if (wherePredicate != null) {
            cq.where(wherePredicate);
        }*/
        Stream<T> stream = (Stream<T>) collection.stream();

        if (wherePredicate != null) {
            stream = collection.parallelStream().filter(wherePredicate);
        }

        if (!q.getOrder().isEmpty()) {

            Comparator comparator = createOrderQuery(entity, q, null);

            if (comparator != null) {
                stream = stream.sorted(comparator);
            }
        }

        /*if (q.getFields().isEmpty()) {

            cq.select((Selection) r).distinct(requiresDistinct);
        } else {

            cq.multiselect(createFieldsSelect(r, q, getEntityIdField(em, entity))).distinct(requiresDistinct);
        }

        TypedQuery<?> tq = em.createQuery(cq);*/

        if (q.getOffset() != null && q.getOffset() > -1) {
            stream = stream.skip(q.getOffset().intValue());
        }

        if (q.getLimit() != null && q.getLimit() > -1) {
            stream = stream.limit(q.getLimit().intValue());
        }

        /*if (queryHints != null) {
            queryHints.stream().forEach(i ->
                    tq.setHint(i.getKey(), i.getValue())
            );
        }
        if (q.getFields().isEmpty()) {

            return (List<T>) tq.getResultList();
        } else {

            return createEntitiesFromTuples((List<Tuple>) tq.getResultList(), entity, getEntityIdField(em, entity));
        }*/

        return stream.collect(Collectors.toList());
    }

    public static <T> Long queryEntitiesCount(Collection<T> collection, Class<T> entity) {

        return queryEntitiesCount(collection, entity, new QueryParameters());
    }

    public static <T> Long queryEntitiesCount(Collection<T> collection, Class<T> entity, QueryParameters q) {

        return queryEntitiesCount(collection, entity, q, null);
    }

    public static <T> Long queryEntitiesCount(Collection<T> collection, Class<T> entity, CriteriaFilter<T> customFilter) {

        return queryEntitiesCount(collection, entity, new QueryParameters(), customFilter);
    }

    public static <T> Long queryEntitiesCount(Collection<T> collection, Class<T> entity, QueryParameters q,
                                              CriteriaFilter<T> customFilter) {

        if (entity == null)
            throw new IllegalArgumentException("The entity manager and the entity cannot be null.");

        if (q == null)
            throw new IllegalArgumentException("Query parameters can't be null. " +
                    "If you don't have any parameters either pass a empty object or " +
                    "use the queryEntitiesCount(EntityManager, Class<T>) method.");

        log.finest("Querying entity count: '" + entity.getSimpleName() + "' with parameters: " + q);

        Boolean requiresDistinct = false;

        Predicate wherePredicate = null;

        if (!q.getFilters().isEmpty()) {

            StreamCriteriaWhereQuery criteriaWhereQuery = createWhereQueryInternal(entity, q);

            requiresDistinct = criteriaWhereQuery.containsToMany();
            wherePredicate = criteriaWhereQuery.getPredicate();
        }

        Stream stream = (Stream<T>) collection.stream();

        if (wherePredicate != null) {
            stream = collection.parallelStream().filter(wherePredicate);
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

                    if (qo.getOrder() == OrderDirection.DESC) {
                        Comparator c = comparator(clazz, field.getPath()).reversed();

                        if (comparator[0] == null) {
                            comparator[0] = c;
                        } else {
                            comparator[0] = comparator[0].thenComparing(c);
                        }
                    } else {
                        Comparator c = comparator(clazz, field.getPath());

                        if (comparator[0] == null) {
                            comparator[0] = c;
                        } else {
                            comparator[0] = comparator[0].thenComparing(c);
                        }
                    }
                }
            } catch (IllegalArgumentException | NoSuchFieldException e) {

                throw new NoSuchEntityFieldException(e.getMessage(), qo.getField(), clazz.getSimpleName());
            }
        });

        //Add sort by id for correct pagination when field has same values
        if (id != null) {
            try {
                StreamCriteriaField criteriaField = getStreamCriteriaField(clazz, id);
                if (null != criteriaField) {
                    Comparator c = comparator(clazz, criteriaField.getPath());

                    if (comparator[0] == null) {
                        comparator[0] = c;
                    } else {
                        comparator[0] = comparator[0].thenComparing(c);
                    }
                }
            } catch (NoSuchFieldException e) {
                throw new NoSuchEntityFieldException(e.getMessage(), id, clazz.getSimpleName());
            }
        }

        return comparator[0];
    }

    /*public static Predicate createWhereQuery(CriteriaBuilder cb, Root<?> r, QueryParameters q) {
        return createWhereQueryInternal(cb, r, q).getPredicate();
    }

    public static List<Selection<?>> createFieldsSelect(Class<?> r, QueryParameters q, String
            idField) {

        final List<Selection<?>> fields = q.getFields().stream().distinct().map(restField ->
                getRestFieldMappings(r, restField).map(f -> {

                    String[] fSplit = f.split("\\.");

                    Class<?> p = null;
                    for (String fS : fSplit) {
                        try {
                            if (isRestIgnored(null == p ? r : p, fS)) {
                                Optional<Selection<?>> empty = Optional.empty();
                                return empty;
                            }

                            p = p == null ? r.getDeclaredField(fS).getDeclaringClass() : p.getDeclaredField(fS).getDeclaringClass();
                        } catch (IllegalArgumentException e) {
                            throw new NoSuchEntityFieldException(e.getMessage(), f, r.getSimpleName());
                        } catch (NoSuchFieldException e) {
                            throw new NoSuchEntityFieldException(e.getMessage(), f, r.getSimpleName());
                        }
                    }

                    if (p == null) {
                        throw new NoSuchEntityFieldException("", f, r.getSimpleName());
                    }

                    return Optional.of(p.alias(f));
                }).filter(Optional::isPresent).map(Optional::get)
        ).flatMap(Function.identity()).collect(Collectors.toList());

        try {
            boolean exists = fields.stream().anyMatch(f -> f.getAlias().equals(idField));

            if (!exists) {
                fields.add(r.get(idField).alias(idField));
            }
        } catch (IllegalArgumentException e) {

            throw new NoSuchEntityFieldException(e.getMessage(), idField, r.getJavaType().getSimpleName());
        }

        return fields.stream().distinct().collect(Collectors.toList());
    }*/

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

                Field field = clazz.getDeclaredField(fieldNames[0]);

                Class<?> clazzTarget = clazz.getDeclaredField(fieldNames[0]).getType();
                field.setAccessible(true);

                if (fieldNames.length > 1) {

                    String newFieldName = entityField.substring(fieldNames[0].length() + 1);
                    do {

                        if (Collection.class.isAssignableFrom(clazzTarget)) {

                            ParameterizedType pt = (ParameterizedType) field.getGenericType();

                            Type[] fieldArgTypes = pt.getActualTypeArguments();

                            if (fieldArgTypes.length > 0) {
                                clazzTarget = (Class) fieldArgTypes[0];

                            } else {
                                // TODO
                            }
                        }

                        fieldNames = newFieldName.split("\\.");

                        field = clazzTarget.getDeclaredField(fieldNames[0]);
                        field.setAccessible(true);

                        clazzTarget = field.getType();

                        if(fieldNames.length > 1)
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


    private static Object getValueForField(Field field, String value) {

        if (value == null) return null;

        Class c = field.getType();

        try {

            if (c.equals(Date.class))
                return Date.from(ZonedDateTime.parse(value).toInstant());

            if (c.equals(Instant.class))
                return ZonedDateTime.parse(value).toInstant();

            if (c.equals(Boolean.class))
                return Boolean.parseBoolean(value);

            if (c.isEnum())
                return Enum.valueOf(c, value);

            if (c.equals(UUID.class))
                return UUID.fromString(value);
        } catch (IllegalArgumentException | DateTimeParseException e) {

            throw new InvalidFieldValueException(e.getMessage(), field.getName(), value);
        }

        return value;
    }

    private static StreamCriteriaField getStreamCriteriaField(Class<?> clazz, String fieldName) throws NoSuchFieldException {

        if (fieldName == null) throw new NoSuchEntityFieldException("No such entity field", fieldName, clazz.getSimpleName());

        String[] fields = fieldName.split("\\.");

        StringBuilder path = new StringBuilder();

        Class<?> from = clazz;

        boolean isCollectionField = false;

        for (String field : fields) {

            final Optional<String> mappedFieldOptional = getRestFieldMappings(from, field).findFirst();

            //ignored
            if (!mappedFieldOptional.isPresent()) {
                return null;
            }

            String mappedField = mappedFieldOptional.get();

            try {
                if (path.toString().equals("")) {
                    path.append(from.getDeclaredField(mappedField).getName());
                } else {
                    path.append(".").append(from.getDeclaredField(mappedField).getName());
                }

                if (Collection.class.isAssignableFrom(from.getDeclaredField(mappedField).getType())) {
                    isCollectionField = true;

                    ParameterizedType pt = (ParameterizedType) from.getDeclaredField(mappedField).getGenericType();

                    Type[] fieldArgTypes = pt.getActualTypeArguments();

                    if (fieldArgTypes.length > 0) {
                        from = (Class) fieldArgTypes[0];
                    } else {
                        // TODO
                    }
                } else {
                    from = from.getDeclaredField(mappedField).getType();
                }

            } catch (NoSuchFieldException e) {
                throw e;
            }
        }

        return new StreamCriteriaField(path.toString(), isCollectionField);
    }

    private static Stream<String> getRestFieldMappings(final Class<?> clazz, final String restField) {

        if (null == restField || isRestIgnored(clazz, restField)) {
            return Stream.empty();
        }

        List<String> mappingList = Stream.of(clazz.getDeclaredFields())
                .map(entityField -> Stream.of(entityField.getAnnotationsByType(RestMapping.class))
                        .map(annotation -> {
                                    String restFieldName = annotation.value();
                                    String entityFieldPath = annotation.toChildField().isEmpty() ? entityField.getName() :
                                            entityField.getName() + "." + annotation.toChildField();
                                    return new AbstractMap.SimpleEntry<>(restFieldName, entityFieldPath);
                                }
                        )
                        .filter(e -> restField.equals(e.getKey())).map(AbstractMap.SimpleEntry::getValue)
                )
                .flatMap(Function.identity()).collect(Collectors.toList());

        return mappingList.isEmpty() ? Stream.of(restField) : mappingList.stream();
    }

    private static boolean isRestIgnored(final Class entityClass, final String restField) {
        final RestIgnore restIgnore = (RestIgnore) entityClass.getAnnotation(RestIgnore.class);

        if (restIgnore != null) {
            return Stream.of(restIgnore.value()).anyMatch(restField::equalsIgnoreCase);
        }

        return false;
    }

    private static <T> Predicate<T> filter(Class<T> clazz, String fieldName, Object fieldValue, FilterOperation operation) {

        return (T instance) -> {
            try {
                String[] fieldNames = fieldName.split("\\.");

                Field f = clazz.getDeclaredField(fieldNames[0]);
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
                        return Objects.equals(value, fieldValue);
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
                        return !Objects.equals(value, fieldValue);
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
                    }
                    if (value instanceof Integer) {
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
            }
            return false;
        };
    }

    private static Class<?> getGenericType(Field field) {

        ParameterizedType pt = (ParameterizedType) field.getGenericType();
        Type[] fieldArgTypes = pt.getActualTypeArguments();

        if (fieldArgTypes.length > 0) {
            return (Class) fieldArgTypes[0];
        } else {
            throw new NoGenericTypeException("Unable to obtain GenericType from Collection field.", field.getName(),
                    field.getClass().getSimpleName());
        }

    }

    private static <T> Comparator<T> comparator(Class<T> clazz, String fieldName) {
        return (T instance1, T instance2) -> {
            try {
                String[] fieldNames = fieldName.split("\\.");

                Class clazzTarget = clazz;

                Field f = clazzTarget.getDeclaredField(fieldNames[0]);
                f.setAccessible(true);

                if (Collection.class.isAssignableFrom(f.getType())) {
                    throw new InvalidEntityFieldException("Unable to sort one to many relations", f.getName(),
                            f.getType().getSimpleName());
                }

                Object value1 = f.get(instance1);
                Object value2 = f.get(instance2);

                int i = 1;
                while (i < fieldNames.length) {

                    clazzTarget = f.getType();

                    f = clazzTarget.getDeclaredField(fieldNames[i]);
                    f.setAccessible(true);

                    if (Collection.class.isAssignableFrom(f.getType())) {
                        throw new InvalidEntityFieldException("Unable to sort one to many relations", f.getName(),
                                f.getType().getSimpleName());
                    }

                    value1 = f.get(value1);
                    value2 = f.get(value2);

                    i++;
                }

                int result = -1;

                if (value1 instanceof Comparable) {
                    result = ((Comparable<T>) value1).compareTo((T) value2);
                } else {
                    try {
                        result = ((Comparable<T>) value1).compareTo((T) value2);
                    } catch (ClassCastException e) {
                    }
                }

                return result;

            } catch (NoSuchFieldException | IllegalAccessException e) {
            }

            return -1;
        };
    }
}
