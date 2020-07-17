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
import com.kumuluz.ee.rest.enums.OrderDirection;
import com.kumuluz.ee.rest.exceptions.InvalidEntityFieldException;
import com.kumuluz.ee.rest.exceptions.InvalidFieldValueException;
import com.kumuluz.ee.rest.exceptions.NoSuchEntityFieldException;
import com.kumuluz.ee.rest.interfaces.CriteriaFilter;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Tilen Faganel
 */
public class JPAUtils {

    private static final Logger LOG = Logger.getLogger(JPAUtils.class.getSimpleName());

    private static final String PROP_PERSISTENCE_JDBC_DRIVER = "javax.persistence.jdbc.driver";
    private static final String POSTGRES_SQL_DRIVER = "org.postgresql.Driver";

    public static <T> Stream<T> getEntityStream(EntityManager em, Class<T> entity) {

        return getEntityStream(em, entity, new QueryParameters());
    }

    public static <T> Stream<T> getEntityStream(EntityManager em, Class<T> entity, QueryParameters q) {

        return getEntityStream(em, entity, q, null, null, null);
    }

    public static <T> Stream<T> getEntityStream(EntityManager em, Class<T> entity, CriteriaFilter<T> customFilter) {
        return getEntityStream(em, entity, new QueryParameters(), customFilter, null, null);
    }

    public static <T> Stream<T> getEntityStream(EntityManager em, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter) {
        return getEntityStream(em, entity, q, customFilter, null, null);
    }

    public static <T> Stream<T> getEntityStream(EntityManager em, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter,
                                                List<QueryHintPair> queryHints) {
        return getEntityStream(em, entity, q, customFilter, queryHints, null);
    }

    public static <T> Queried<T> getQueried(EntityManager em, Class<T> entity) {

        return getQueried(em, entity, new QueryParameters());
    }

    public static <T> Queried<T> getQueried(EntityManager em, Class<T> entity, QueryParameters q) {

        return getQueried(em, entity, q, null, null, null);
    }

    public static <T> Queried<T> getQueried(EntityManager em, Class<T> entity, CriteriaFilter<T> customFilter) {
        return getQueried(em, entity, new QueryParameters(), customFilter, null, null);
    }

    public static <T> Queried<T> getQueried(EntityManager em, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter) {
        return getQueried(em, entity, q, customFilter, null, null);
    }

    public static <T> Queried<T> getQueried(EntityManager em, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter,
                                            List<QueryHintPair> queryHints) {
        return getQueried(em, entity, q, customFilter, queryHints, null);
    }

    public static <T> List<T> queryEntities(EntityManager em, Class<T> entity) {

        return queryEntities(em, entity, new QueryParameters());
    }

    public static <T> List<T> queryEntities(EntityManager em, Class<T> entity, QueryParameters q) {

        return queryEntities(em, entity, q, null, null, null);
    }

    public static <T> List<T> queryEntities(EntityManager em, Class<T> entity, CriteriaFilter<T> customFilter) {
        return queryEntities(em, entity, new QueryParameters(), customFilter, null, null);
    }

    public static <T> List<T> queryEntities(EntityManager em, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter) {
        return queryEntities(em, entity, q, customFilter, null, null);
    }

    public static <T> List<T> queryEntities(EntityManager em, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter,
                                            List<QueryHintPair> queryHints) {
        return queryEntities(em, entity, q, customFilter, queryHints, null);
    }

    public static <T> Queried<T> getQueried(EntityManager em, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter,
                                            List<QueryHintPair> queryHints, String rootAlias) {

        Long totalCount = queryEntitiesCount(em, entity, q, customFilter);
        Stream<T> entityStream = getEntityStream(em, entity, q, customFilter, queryHints, rootAlias);

        return Queried.result(totalCount, entityStream);
    }

    public static <T> Stream<T> getEntityStream(EntityManager em, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter,
                                                List<QueryHintPair> queryHints, String rootAlias) {

        TypedQuery<?> tq = buildQuery(em, entity, q, customFilter, queryHints, rootAlias);

        if (q.getFields().isEmpty()) {

            return (Stream<T>) tq.getResultStream();
        } else {

            return createEntitiesFromTuples((List<Tuple>) tq.getResultList(), entity, getEntityIdField(em, entity));
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> queryEntities(EntityManager em, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter,
                                            List<QueryHintPair> queryHints, String rootAlias) {

        TypedQuery<?> tq = buildQuery(em, entity, q, customFilter, queryHints, rootAlias);

        if (q.getFields().isEmpty()) {

            return (List<T>) tq.getResultList();
        } else {

            return createEntitiesFromTuples((List<Tuple>) tq.getResultList(), entity, getEntityIdField(em, entity)).collect(Collectors.toList());
        }
    }

    private static <T> TypedQuery<?> buildQuery(EntityManager em, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter,
                                                List<QueryHintPair> queryHints, String rootAlias) {
        if (em == null || entity == null)
            throw new IllegalArgumentException("The entity manager and the entity cannot be null.");

        if (q == null)
            throw new IllegalArgumentException("Query parameters can't be null. " +
                    "If you don't have any parameters either pass a empty object or " +
                    "use the queryEntities(EntityManager, Class<T>) method.");

        LOG.finest("Querying entity: '" + entity.getSimpleName() + "' with parameters: " + q);

        Boolean requiresDistinct = false;

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<?> cq;

        if (q.getFields().isEmpty()) {

            cq = cb.createQuery(entity);
        } else {

            cq = cb.createTupleQuery();
        }

        Root<T> r = cq.from(entity);
        if (rootAlias != null) {
            r.alias(rootAlias);
        }

        Predicate wherePredicate = null;

        if (!q.getFilters().isEmpty()) {

            CriteriaWhereQuery criteriaWhereQuery = createWhereQueryInternal(em, cb, r, q);

            requiresDistinct = criteriaWhereQuery.containsToMany();
            wherePredicate = criteriaWhereQuery.getPredicate();
        }

        if (customFilter != null) {

            wherePredicate = customFilter.createPredicate(
                    wherePredicate == null ? cb.conjunction() : wherePredicate, cb, r);
        }

        if (wherePredicate != null) {
            cq.where(wherePredicate);
        }

        if (!q.getOrder().isEmpty()) {

            List<Order> orders = createOrderQuery(cb, r, q, getEntityIdField(em, entity));

            cq.orderBy(orders);
        }

        if (q.getFields().isEmpty()) {

            cq.select((Selection) r).distinct(requiresDistinct);
        } else {

            cq.multiselect(createFieldsSelect(r, q, getEntityIdField(em, entity))).distinct(requiresDistinct);
        }

        TypedQuery<?> tq = em.createQuery(cq);

        if (q.getLimit() != null && q.getLimit() > -1) {

            tq.setMaxResults(q.getLimit().intValue());
        }

        if (q.getOffset() != null && q.getOffset() > -1) {

            tq.setFirstResult(q.getOffset().intValue());
        }

        if (queryHints != null) {
            queryHints.stream().forEach(i ->
                    tq.setHint(i.getKey(), i.getValue())
            );
        }

        return tq;
    }

    public static <T> Long queryEntitiesCount(EntityManager em, Class<T> entity) {

        return queryEntitiesCount(em, entity, new QueryParameters());
    }

    public static <T> Long queryEntitiesCount(EntityManager em, Class<T> entity, QueryParameters q) {

        return queryEntitiesCount(em, entity, q, null);
    }

    public static <T> Long queryEntitiesCount(EntityManager em, Class<T> entity, CriteriaFilter<T> customFilter) {

        return queryEntitiesCount(em, entity, new QueryParameters(), customFilter);
    }

    public static <T> Long queryEntitiesCount(EntityManager em, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter) {

        if (em == null || entity == null)
            throw new IllegalArgumentException("The entity manager and the entity cannot be null.");

        if (q == null)
            throw new IllegalArgumentException("Query parameters can't be null. " +
                    "If you don't have any parameters either pass a empty object or " +
                    "use the queryEntitiesCount(EntityManager, Class<T>) method.");

        LOG.finest("Querying entity count: '" + entity.getSimpleName() + "' with parameters: " + q);

        Boolean requiresDistinct = false;

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> cq = cb.createQuery(Long.class);

        Root<T> r = cq.from(entity);

        Predicate wherePredicate = null;

        if (!q.getFilters().isEmpty()) {

            CriteriaWhereQuery criteriaWhereQuery = createWhereQueryInternal(em, cb, r, q);

            requiresDistinct = criteriaWhereQuery.containsToMany();
            wherePredicate = criteriaWhereQuery.getPredicate();
        }

        if (customFilter != null) {

            wherePredicate = customFilter.createPredicate(
                    wherePredicate == null ? cb.conjunction() : wherePredicate, cb, r);
        }

        if (wherePredicate != null) {
            cq.where(wherePredicate);
        }

        cq.select(cb.count(r)).distinct(requiresDistinct);

        return em.createQuery(cq).getSingleResult();
    }

    public static List<Order> createOrderQuery(CriteriaBuilder cb, Root<?> r, QueryParameters q) {
        return createOrderQuery(cb, r, q, null);
    }

    public static List<Order> createOrderQuery(CriteriaBuilder cb, Root<?> r, QueryParameters q, String id) {

        List<Order> orders = new ArrayList<>();

        q.getOrder().stream().filter(qo -> qo.getField() != null).forEach(qo -> {

            try {

                CriteriaField field = getCriteriaField(qo.getField(), r);

                if (null != field) {

                    if (field.containsToMany()) {
                        throw new InvalidEntityFieldException(
                                "OneToMany and ManyToMany relations are not supported by the order query",
                                qo.getField(), r.getJavaType().getSimpleName());
                    }

                    if (qo.getOrder() == OrderDirection.DESC) {

                        orders.add(cb.desc(field.getPath()));
                    } else {

                        orders.add(cb.asc(field.getPath()));
                    }
                }
            } catch (IllegalArgumentException e) {

                throw new NoSuchEntityFieldException(e.getMessage(), qo.getField(), r.getJavaType().getSimpleName());
            }
        });

        //Add sort by id for correct pagination when field has same values
        if (id != null) {
            CriteriaField criteriaField = getCriteriaField(id, r);
            if (null != criteriaField) {
                orders.add(cb.asc(criteriaField.getPath()));
            }
        }

        return orders;
    }

    @Deprecated
    public static Predicate createWhereQuery(CriteriaBuilder cb, Root<?> r, QueryParameters q) {
        return createWhereQueryInternal(null, cb, r, q).getPredicate();
    }

    public static Predicate createWhereQuery(EntityManager em, CriteriaBuilder cb, Root<?> r, QueryParameters q) {
        return createWhereQueryInternal(em, cb, r, q).getPredicate();
    }

    public static List<Selection<?>> createFieldsSelect(Root<?> r, QueryParameters q, String
            idField) {

        final List<Selection<?>> fields = q.getFields().stream().distinct().map(restField ->
                getRestFieldMappings(r, restField).map(f -> {

                    String[] fSplit = f.split("\\.");

                    Path<?> p = null;
                    for (String fS : fSplit) {
                        try {

                            if (isRestIgnored(null == p ? r.getJavaType() : p.getJavaType(), fS)) {
                                Optional<Selection<?>> empty = Optional.empty();
                                return empty;
                            }

                            p = p == null ? r.get(fS) : p.get(fS);
                        } catch (IllegalArgumentException e) {
                            throw new NoSuchEntityFieldException(e.getMessage(), f, r.getJavaType().getSimpleName());
                        }
                    }

                    if (p == null) {
                        throw new NoSuchEntityFieldException("", f, r.getJavaType().getSimpleName());
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
    }

    // Temporary methods to not break the public API

    private static CriteriaWhereQuery createWhereQueryInternal(EntityManager em, CriteriaBuilder cb, Root<?> r, QueryParameters q) {

        Predicate predicate = cb.conjunction();
        Boolean containsToMany = false;

        for (QueryFilter f : q.getFilters()) {

            Predicate np = null;

            try {
                CriteriaField criteriaField = getCriteriaField(f.getField(), r);

                if (null == criteriaField) {
                    continue;
                }

                if (criteriaField.containsToMany()) {
                    containsToMany = true;
                }

                Path entityField = criteriaField.getPath();

                if (entityField.getModel() == null || !((Attribute) entityField.getModel()).getPersistentAttributeType()
                        .equals(Attribute.PersistentAttributeType.BASIC)) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                Path<String> stringField = (Path<String>) entityField;
                @SuppressWarnings("unchecked")
                Path<Date> dateField = (Path<Date>) entityField;
                @SuppressWarnings("unchecked")
                Path<Comparable> compField = (Path<Comparable>) entityField;

                switch (f.getOperation()) {

                    case EQ:
                        if (f.getDateValue() != null && entityField.getJavaType().equals(Date.class)) {
                            np = cb.equal(entityField, f.getDateValue());
                        } else if (f.getValue() != null) {
                            np = cb.equal(entityField, getValueForPath(entityField, f.getValue()));
                        }
                        break;
                    case EQIC:
                        if (entityField.getJavaType().equals(String.class) && f.getValue() != null) {
                            np = cb.equal(cb.lower(stringField), f.getValue().toLowerCase());
                        }
                        break;
                    case NEQ:
                        if (f.getDateValue() != null && entityField.getJavaType().equals(Date.class)) {
                            np = cb.notEqual(entityField, f.getDateValue());
                        } else if (f.getValue() != null) {
                            np = cb.notEqual(entityField, getValueForPath(entityField, f.getValue()));
                        }
                        break;
                    case NEQIC:
                        if (entityField.getJavaType().equals(String.class) && f.getValue() != null) {
                            np = cb.notEqual(cb.lower(stringField), f.getValue().toLowerCase());
                        }
                        break;
                    case LIKE:
                        if (entityField.getJavaType().equals(String.class) && f.getValue() != null) {
                            np = cb.like(stringField, f.getValue());
                        } else if (entityField.getJavaType().equals(UUID.class) && f.getValue() != null) {
                            String driver = (null == em ? null : (String) em.getProperties().get(PROP_PERSISTENCE_JDBC_DRIVER));
                            if (POSTGRES_SQL_DRIVER.equalsIgnoreCase(driver)) {
                                np = cb.like(cb.function("text", String.class, r.get(f.getField()).as(String.class)), f.getValue());
                            } else {
                                np = cb.like(r.get(f.getField()).as(String.class), f.getValue());
                            }
                        }
                        break;
                    case LIKEIC:
                        if (entityField.getJavaType().equals(String.class) && f.getValue() != null) {
                            np = cb.like(cb.lower(stringField), f.getValue().toLowerCase());
                        }
                        break;
                    case GT:
                        if (Date.class.isAssignableFrom(entityField.getJavaType()) ||
                                isAssignableToInstantHoldingTemporal(entityField.getJavaType()) ||
                                Number.class.isAssignableFrom(entityField.getJavaType()) ||
                                String.class.isAssignableFrom(entityField.getJavaType())) {

                            if (f.getDateValue() != null && entityField.getJavaType().equals(Date.class)) {
                                np = cb.greaterThan(dateField, f.getDateValue());
                            } else if (f.getValue() != null) {
                                np = cb.greaterThan(compField, (Comparable) getValueForPath(stringField, f.getValue()));
                            }
                        }
                        break;
                    case GTE:
                        if (Date.class.isAssignableFrom(entityField.getJavaType()) ||
                                isAssignableToInstantHoldingTemporal(entityField.getJavaType()) ||
                                Number.class.isAssignableFrom(entityField.getJavaType()) ||
                                String.class.isAssignableFrom(entityField.getJavaType())) {

                            if (f.getDateValue() != null && entityField.getJavaType().equals(Date.class)) {
                                np = cb.greaterThanOrEqualTo(dateField, f.getDateValue());
                            } else if (f.getValue() != null) {
                                np = cb.greaterThanOrEqualTo(compField, (Comparable) getValueForPath(stringField, f.getValue()));
                            }
                        }
                        break;
                    case LT:
                        if (Date.class.isAssignableFrom(entityField.getJavaType()) ||
                                isAssignableToInstantHoldingTemporal(entityField.getJavaType()) ||
                                Number.class.isAssignableFrom(entityField.getJavaType()) ||
                                String.class.isAssignableFrom(entityField.getJavaType())) {

                            if (f.getDateValue() != null && entityField.getJavaType().equals(Date.class)) {
                                np = cb.lessThan(dateField, f.getDateValue());
                            } else if (f.getValue() != null) {
                                np = cb.lessThan(compField, (Comparable) getValueForPath(stringField, f.getValue()));
                            }
                        }
                        break;
                    case LTE:
                        if (Date.class.isAssignableFrom(entityField.getJavaType()) ||
                                isAssignableToInstantHoldingTemporal(entityField.getJavaType()) ||
                                Number.class.isAssignableFrom(entityField.getJavaType()) ||
                                String.class.isAssignableFrom(entityField.getJavaType())) {

                            if (f.getDateValue() != null && entityField.getJavaType().equals(Date.class)) {
                                np = cb.lessThanOrEqualTo(dateField, f.getDateValue());
                            } else if (f.getValue() != null) {
                                np = cb.lessThanOrEqualTo(compField, (Comparable) getValueForPath(stringField, f.getValue()));
                            }
                        }
                        break;
                    case IN:
                        np = stringField.in(f.getValues().stream()
                                .filter(Objects::nonNull)
                                .map(s -> getValueForPath(entityField, s)).collect(Collectors
                                        .toList()));
                        break;
                    case INIC:
                        if (entityField.getJavaType().equals(String.class)) {
                            np = cb.lower(stringField)
                                    .in(f.getValues().stream()
                                            .filter(Objects::nonNull)
                                            .map(String::toLowerCase)
                                            .collect(Collectors.toList()));
                        }
                        break;
                    case NIN:
                        np = cb.not(stringField.in(f.getValues().stream()
                                .filter(Objects::nonNull)
                                .map(s -> getValueForPath(entityField, s)).collect(Collectors.toList())));
                        break;
                    case NINIC:
                        if (entityField.getJavaType().equals(String.class)) {
                            np = cb.not(cb.lower(stringField)
                                    .in(f.getValues().stream()
                                            .filter(Objects::nonNull)
                                            .map(String::toLowerCase)
                                            .collect(Collectors.toList())));
                        }
                        break;
                    case ISNULL:
                        np = cb.isNull(entityField);
                        break;
                    case ISNOTNULL:
                        np = cb.isNotNull(entityField);
                        break;
                }
            } catch (IllegalArgumentException e) {

                throw new NoSuchEntityFieldException(e.getMessage(), f.getField(), r.getJavaType().getSimpleName());
            }

            if (np != null) {
                predicate = cb.and(predicate, np);
            }
        }

        return new CriteriaWhereQuery(predicate, containsToMany);
    }

    ///// Private helper methods

    @SuppressWarnings("unchecked")
    private static <T> Stream<T> createEntitiesFromTuples(List<Tuple> tuples, Class<T> entity, String idField) {

        Map<Object, List<Tuple>> tuplesGrouping = getTuplesGroupingById(tuples, idField);

        return tuplesGrouping.entrySet().stream().map(tuplesGroup -> {

            T el;

            try {
                el = entity.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException |
                    NoSuchMethodException | InvocationTargetException e) {

                throw new AssertionError();
            }

            for (Tuple t : tuplesGroup.getValue()) {

                List<TupleElement<?>> tes = t.getElements()
                        .stream()
                        .sorted(Comparator.comparing(TupleElement::getAlias))
                        .collect(Collectors.toList());

                for (TupleElement<?> te : tes) {

                    Object o = t.get(te);

                    try {
                        String[] fName = te.getAlias().split("\\.");

                        if (fName.length == 1) {

                            Field f = getFieldFromEntity(entity, fName[0]);
                            if (null != f) {
                                setEntityFieldValue(el, f, o);
                            }
                        } else {
                            T el2 = el;

                            Field field = null;
                            Class entity2 = entity;

                            try {

                                for (int i = 0; i < fName.length; i++) {

                                    field = getFieldFromEntity(entity2, fName[i]);
                                    if (null == field) {
                                        continue;
                                    }
                                    entity2 = field.getType();

                                    if (i < fName.length - 1) {

                                        el2 = (T) initializeField(el2, field, entity2);
                                    }
                                }
                            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {

                                throw new AssertionError();
                            }

                            setEntityFieldValue(el2, field, o);
                        }
                    } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {

                        throw new NoSuchEntityFieldException(e.getMessage(), te.getAlias(), entity.getSimpleName());
                    }
                }
            }

            return el;
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T initializeField(T entity, Field field, Class<T> entity2Class)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        field.setAccessible(true);

        if (isCollectionField(field)) {
            Object collection = field.get(entity);

            if (collection == null) {
                field.set(entity, new ArrayList<>());
            }
        } else if (isObjectField(field)) {
            Object object = field.get(entity);

            if (object == null) {
                field.set(entity, entity2Class.getConstructor().newInstance());
            }
        }

        return (T) field.get(entity);
    }

    private static <T> void setEntityFieldValue(T entity, Field field, Object value)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        field.setAccessible(true);

        if (isCollectionField(field)) {
            Object collection = field.get(entity);
            if (collection == null) {
                field.set(entity, new ArrayList<>());
                collection = field.get(entity);
            }

            Method add = Collection.class.getDeclaredMethod("add", Object.class);
            add.invoke(collection, value);
        } else if (isObjectField(field)) {
            field.set(entity, value);
        } else {
            field.set(entity, value);
        }
    }

    @SuppressWarnings("unchecked")
    private static String getEntityIdField(EntityManager em, Class entityClass) {

        String idProperty = "";

        Metamodel metamodel = em.getMetamodel();
        EntityType e = metamodel.entity(entityClass);
        Set<SingularAttribute> singularAttributes = e.getSingularAttributes();

        for (SingularAttribute singularAttribute : singularAttributes) {

            if (singularAttribute.isId()) {

                idProperty = singularAttribute.getName();
                break;
            }
        }

        return idProperty;
    }

    private static Field getFieldFromEntity(Class entityClass, String fieldName) throws NoSuchFieldException {

        try {
            if (isRestIgnored(entityClass, fieldName)) {
                return null;
            }
            return entityClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {

            if (entityClass.getSuperclass() == null) {
                throw e;
            }

            return getFieldFromEntity(entityClass.getSuperclass(), fieldName);
        }
    }

    private static Object getValueForPath(Path path, String value) {

        if (value == null) return null;

        Class c = path.getModel().getBindableJavaType();

        try {

            if (c.equals(Date.class))
                return Date.from(ZonedDateTime.parse(value).toInstant());

            if (c.equals(Instant.class)) {
                return ZonedDateTime.parse(value).toInstant();
            }

            if (c.equals(OffsetDateTime.class)) {
                return ZonedDateTime.parse(value).toOffsetDateTime();
            }

            if (c.equals(ZonedDateTime.class)) {
                return ZonedDateTime.parse(value);
            }

            if (c.equals(Boolean.class))
                return Boolean.parseBoolean(value);

            if (c.isEnum())
                return Enum.valueOf(c, value);

            if (c.equals(UUID.class))
                return UUID.fromString(value);
        } catch (IllegalArgumentException | DateTimeParseException e) {

            throw new InvalidFieldValueException(e.getMessage(), path.getAlias(), value);
        }

        return value;
    }

    private static CriteriaField getCriteriaField(String fieldName, Root<?> r) {

        if (fieldName == null) fieldName = "";

        String fields[] = fieldName.split("\\.");

        From from = r;
        Path path = r;
        Boolean containsToMany = false;

        final Set<Join> joins = new HashSet<>();
        for (String field : fields) {

            //only first since multiple field operations are not supported
            final Optional<String> mappedFieldOptional = getRestFieldMappings(from, field).findFirst();

            //ignored
            if (!mappedFieldOptional.isPresent()) {
                r.getJoins().removeIf(o -> {
                    //remove joins for ignored field
                    return joins.contains(o);
                });
                return null;
            }
            String mappedField = mappedFieldOptional.get();

            path = from.get(mappedField);

            if (path.getModel() == null &&
                    (path.getJavaType().equals(List.class) || path.getJavaType().equals(Set.class))) {

                containsToMany = true;
                Join join = from.join(mappedField);
                from = join;
                joins.add(join);
            } else {

                switch (((Attribute) path.getModel()).getPersistentAttributeType()) {
                    case ONE_TO_MANY:
                    case MANY_TO_MANY:
                        containsToMany = true;
                    case ONE_TO_ONE:
                    case MANY_TO_ONE:
                    case EMBEDDED:
                        Join join = from.join(mappedField);
                        from = join;
                        joins.add(join);
                        break;
                }
            }
        }

        return new CriteriaField(path, containsToMany);
    }

    private static Stream<String> getRestFieldMappings(final Path path, final String restField) {

        if (null == restField || isRestIgnored(path.getJavaType(), restField)) {
            return Stream.empty();
        }

        Class<?> javaType = path.getJavaType();

        Stream<Field> declaredFields = Stream.of(javaType.getDeclaredFields());
        while (javaType.getSuperclass() != null) {
            declaredFields = Stream.concat(declaredFields, Stream.of(javaType.getSuperclass().getDeclaredFields()));
            javaType = javaType.getSuperclass();
        }

        List<String> mappingList = declaredFields
                .map(entityField -> Stream.of(entityField.getAnnotationsByType(RestMapping.class))
                        .map(annotation -> {
                                    String restFieldName = annotation.value();
                                    String jpaFieldPath = annotation.toChildField().isEmpty() ? entityField.getName() : entityField.getName() + "." + annotation.toChildField();
                                    return new AbstractMap.SimpleEntry<>(restFieldName, jpaFieldPath);
                                }
                        )
                        .filter(e -> restField.equals(e.getKey())).map(AbstractMap.SimpleEntry::getValue)
                )
                .flatMap(Function.identity()).collect(Collectors.toList());

        return mappingList.isEmpty() ? Stream.of(restField) : mappingList.stream();
    }

    private static Map<Object, List<Tuple>> getTuplesGroupingById(List<Tuple> tuples, String idField) {
        Map<Object, List<Tuple>> tupleGrouping = new LinkedHashMap<>();

        for (Tuple tuple : tuples) {
            Object id = tuple.get(idField);

            tupleGrouping.computeIfPresent(id, (key, value) -> {
                value.add(tuple);
                return value;
            });

            tupleGrouping.computeIfAbsent(id, (key) -> {
                List<Tuple> value = new ArrayList<>();
                value.add(tuple);
                return value;
            });
        }

        return tupleGrouping;
    }

    private static boolean isCollectionField(Field f) {
        return Collection.class.isAssignableFrom(f.getType());
    }

    private static boolean isRestIgnored(final Class entityClass, final String restField) {
        final RestIgnore restIgnore = (RestIgnore) entityClass.getAnnotation(RestIgnore.class);

        if (restIgnore != null) {
            if (Stream.of(restIgnore.value()).filter(f -> restField.equalsIgnoreCase(f)).findFirst().isPresent()) {
                return true;
            }
        }

        return false;
    }

    private static boolean isObjectField(Field f) {
        return !f.getType().isPrimitive() && !f.getType().isAssignableFrom(String.class);
    }

    private static boolean isAssignableToInstantHoldingTemporal(Class clazz) {
        return Instant.class.isAssignableFrom(clazz) ||
                OffsetDateTime.class.isAssignableFrom(clazz) ||
                ZonedDateTime.class.isAssignableFrom(clazz);
    }
}
