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
import com.kumuluz.ee.rest.enums.FilterExpressionOperation;
import com.kumuluz.ee.rest.enums.OrderDirection;
import com.kumuluz.ee.rest.enums.QueryFormatError;
import com.kumuluz.ee.rest.exceptions.InvalidEntityFieldException;
import com.kumuluz.ee.rest.exceptions.InvalidFieldValueException;
import com.kumuluz.ee.rest.exceptions.NoSuchEntityFieldException;
import com.kumuluz.ee.rest.exceptions.QueryFormatException;
import com.kumuluz.ee.rest.interfaces.CriteriaFilter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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

    public static <T> Stream<T> getEntityStream(EntityManager em, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter,
                                                List<QueryHintPair> queryHints, String rootAlias) {
        return getEntityStream(em, entity, q, customFilter, queryHints, rootAlias, false);
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

    public static <T> Queried<T> getQueried(EntityManager em, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter,
                                            List<QueryHintPair> queryHints, String rootAlias) {
        return getQueried(em, entity, q, customFilter, queryHints, rootAlias, false);
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

    public static <T> List<T> queryEntities(EntityManager em, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter,
                                            List<QueryHintPair> queryHints, String rootAlias) {
        return queryEntities(em, entity, q, customFilter, queryHints, rootAlias, false);
    }

    public static <T> Queried<T> getQueried(EntityManager em, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter,
                                            List<QueryHintPair> queryHints, String rootAlias, boolean forceDistinct) {

        Long totalCount = null;
        if (q.getCount()) {
            totalCount = queryEntitiesCount(em, entity, q, customFilter);
        }
        Stream<T> entityStream = getEntityStream(em, entity, q, customFilter, queryHints, rootAlias, forceDistinct);

        return Queried.result(totalCount, entityStream);
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<T> getEntityStream(EntityManager em, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter,
                                                List<QueryHintPair> queryHints, String rootAlias, boolean forceDistinct) {

        Optional<TypedQuery<T>> tqOptional = buildQuery(em, entity, q, customFilter, queryHints, rootAlias, forceDistinct);
        if (tqOptional.isPresent()) {
            TypedQuery<T> tq = tqOptional.get();

            if (q.getFields().isEmpty()) {
                return tq.getResultStream();
            } else {
                return createEntitiesFromTuples((List<Tuple>) tq.getResultList(), entity, getEntityIdField(em, entity));
            }
        } else {
            return Stream.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> queryEntities(EntityManager em, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter,
                                            List<QueryHintPair> queryHints, String rootAlias, boolean forceDistinct) {

        Optional<TypedQuery<T>> tqOptional = buildQuery(em, entity, q, customFilter, queryHints, rootAlias, forceDistinct);

        if (tqOptional.isPresent()) {
            TypedQuery<T> tq = tqOptional.get();

            if (q.getFields().isEmpty()) {
                return tq.getResultList();
            } else {
                return createEntitiesFromTuples((List<Tuple>) tq.getResultList(), entity, getEntityIdField(em, entity)).collect(Collectors.toList());
            }
        } else {
            return new ArrayList<>();
        }
    }

    private static <T> Optional<TypedQuery<T>> buildQuery(EntityManager em, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter,
                                                          List<QueryHintPair> queryHints, String rootAlias, boolean forceDistinct) {
        if (em == null || entity == null)
            throw new IllegalArgumentException("The entity manager and the entity cannot be null.");

        if (q == null)
            throw new IllegalArgumentException("Query parameters can't be null. " +
                    "If you don't have any parameters either pass a empty object or " +
                    "use the queryEntities(EntityManager, Class<T>) method.");

        LOG.finest("Querying entity: '" + entity.getSimpleName() + "' with parameters: " + q);

        String idField = getEntityIdField(em, entity);

        if (q.getFields().isEmpty() || q.getFields().stream().distinct().allMatch(f -> f.equals(idField))) {

            return buildQuerySimple(em, entity, q, customFilter, queryHints, rootAlias, forceDistinct);
        } else {

            return buildQueryAdvanced(em, entity, q, customFilter, queryHints, rootAlias, forceDistinct);
        }
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

        if (q == null) {
            throw new IllegalArgumentException("Query parameters can't be null. " +
                    "If you don't have any parameters either pass a empty object or " +
                    "use the queryEntitiesCount(EntityManager, Class<T>) method.");
        }

        LOG.finest("Querying entity count: '" + entity.getSimpleName() + "' with parameters: " + q);

        Boolean requiresDistinct = false;

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> cq = cb.createQuery(Long.class);

        Root<T> r = cq.from(entity);

        Predicate wherePredicate = null;

        Map<String, From> fieldJoins = new HashMap<>();

        if (q.getFilterExpression() != null || !q.getFilters().isEmpty()) {

            CriteriaWhereQuery criteriaWhereQuery = createWhereQueryInternal(em, cb, r, q, fieldJoins);

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

        cq.select(requiresDistinct ? cb.countDistinct(r) : cb.count(r));

        return em.createQuery(cq).getSingleResult();
    }

    public static List<Order> createOrderQuery(CriteriaBuilder cb, Root<?> r, QueryParameters q) {
        return createOrderQuery(cb, r, q, null, new HashMap<>());
    }

    public static List<Order> createOrderQuery(CriteriaBuilder cb, Root<?> r, QueryParameters q, String id, Map<String, From> fieldJoins) {

        List<Order> orders = new ArrayList<>();

        q.getOrder().stream().filter(qo -> qo.getField() != null).forEach(qo -> {

            try {

                CriteriaField field = getCriteriaField(qo.getField(), r, fieldJoins);

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
            CriteriaField criteriaField = getCriteriaField(id, r, fieldJoins);
            if (null != criteriaField) {
                orders.add(cb.asc(criteriaField.getPath()));
            }
        }

        return orders;
    }

    @Deprecated
    public static Predicate createWhereQuery(CriteriaBuilder cb, Root<?> r, QueryParameters q) {
        return createWhereQuery(null, cb, r, q);
    }

    public static Predicate createWhereQuery(EntityManager em, CriteriaBuilder cb, Root<?> r, QueryParameters q) {
        return createWhereQueryInternal(em, cb, r, q, new HashMap<>()).getPredicate();
    }

    public static List<Selection<?>> createFieldsSelect(Root<?> r, QueryParameters q, String idField) {
        return createFieldsSelect(r, q, idField, new HashMap<>());
    }

    public static List<Selection<?>> createFieldsSelect(Root<?> r, QueryParameters q, String idField, Map<String, From> fieldJoins) {

        final List<Selection<?>> fields = q.getFields().stream()
                .distinct()
                .flatMap(restField ->
                        getRestFieldMappings(r, restField)
                                .map(alias -> {
                                    CriteriaField cfc;

                                    try {
                                        cfc = getCriteriaField(alias, r, fieldJoins);
                                        if (null == cfc) {
                                            return Optional.<Selection<?>>empty();
                                        }
                                    } catch (IllegalArgumentException e) {
                                        throw new NoSuchEntityFieldException(e.getMessage(), restField, r.getJavaType().getSimpleName());
                                    }

                                    return Optional.<Selection<?>>of(cfc.getPath().alias(alias));
                                })
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                ).collect(Collectors.toList());

        try {
            boolean exists = fields.stream()
                    .anyMatch(f -> f.getAlias().equals(idField));

            if (!exists) {
                fields.add(r.get(idField).alias(idField));
            }
        } catch (IllegalArgumentException e) {

            throw new NoSuchEntityFieldException(e.getMessage(), idField, r.getJavaType().getSimpleName());
        }

        return fields.stream()
                .distinct()
                .sorted((s1, s2) -> s1.getAlias().compareToIgnoreCase(s2.getAlias()))
                .collect(Collectors.toList());
    }

    // Temporary methods to not break the public API

    private static <T> Optional<TypedQuery<T>> buildQuerySimple(EntityManager em, Class<T> entity, QueryParameters q,
                                                                CriteriaFilter<T> customFilter,
                                                                List<QueryHintPair> queryHints, String rootAlias,
                                                                boolean forceDistinct) {
        return buildQuerySimple(em, entity, q, customFilter, queryHints, rootAlias, forceDistinct, false);
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<TypedQuery<T>> buildQuerySimple(EntityManager em, Class<T> entity, QueryParameters q,
                                                                CriteriaFilter<T> customFilter,
                                                                List<QueryHintPair> queryHints, String rootAlias,
                                                                boolean forceDistinct, boolean ignorePaging) {

        LOG.finest("Querying entity: '" + entity.getSimpleName() + "' with parameters: " + q + "(simple)");

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

        Map<String, From> fieldJoins = new HashMap<>();

        if (q.getFilterExpression() != null || !q.getFilters().isEmpty()) {

            CriteriaWhereQuery criteriaWhereQuery = createWhereQueryInternal(em, cb, r, q, fieldJoins);

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

            List<Order> orders = createOrderQuery(cb, r, q, getEntityIdField(em, entity), fieldJoins);

            cq.orderBy(orders);
        }

        if (q.getFields().isEmpty()) {

            cq.select((Selection) r).distinct(requiresDistinct || forceDistinct);
        } else {

            cq.multiselect(createFieldsSelect(r, q, getEntityIdField(em, entity), fieldJoins)).distinct(requiresDistinct || forceDistinct);
        }

        TypedQuery<T> tq = (TypedQuery<T>) em.createQuery(cq);

        if (!ignorePaging) {

            if (q.getLimit() != null && q.getLimit() > -1) {

                tq.setMaxResults(q.getLimit().intValue());
            }

            if (q.getOffset() != null && q.getOffset() > -1) {

                tq.setFirstResult(q.getOffset().intValue());
            }
        }

        if (queryHints != null) {
            queryHints.forEach(i -> tq.setHint(i.getKey(), i.getValue()));
        }

        return Optional.of(tq);
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<TypedQuery<T>> buildQueryAdvanced(EntityManager em, Class<T> entity, QueryParameters q,
                                                                  CriteriaFilter<T> customFilter,
                                                                  List<QueryHintPair> queryHints, String rootAlias,
                                                                  boolean forceDistinct) {

        LOG.finest("Querying entity: '" + entity.getSimpleName() + "' with parameters: " + q + "(advanced)");

        String idFieldName = getEntityIdField(em, entity);

        Field idField = null;
        try {
            idField = getFieldFromEntity(entity, idFieldName);
        } catch (NoSuchFieldException e) {
            // ignore; getEntityIdFiled will throw error if field not found
        }

        QueryParameters qq = new QueryParameters();
        qq.setLimit(q.getLimit());
        qq.setOffset(q.getOffset());
        qq.getOrder().addAll(q.getOrder());
        qq.getFields().add(idFieldName);
        qq.setFilterExpression(q.getFilterExpression());
        qq.getFilters().addAll(q.getFilters());

        Optional<TypedQuery<T>> entityListQueryOptional = buildQuerySimple(em, entity, qq, customFilter, queryHints, rootAlias, forceDistinct);

        List<T> entityList = null;
        if (entityListQueryOptional.isPresent()) {
            TypedQuery<T> entityListQuery = entityListQueryOptional.get();

            entityList = createEntitiesFromTuples((List<Tuple>) entityListQuery.getResultList(), entity, getEntityIdField(em, entity))
                    .collect(Collectors.toList());
        }

        if (entityList == null || entityList.isEmpty()) {
            return Optional.empty();
        }

        List<?> idList = createIdListFromEntities(entityList, idField);

        CriteriaFilter<T> advancedCustomFilter = (p, cb, r) ->
                customFilter != null
                        ? cb.and(customFilter.createPredicate(p, cb, r), r.get(idFieldName).in(idList))
                        : r.get(idFieldName).in(idList);

        return buildQuerySimple(em, entity, q, advancedCustomFilter, queryHints, rootAlias, forceDistinct, true);
    }

    private static CriteriaWhereQuery createWhereQueryInternal(EntityManager em, CriteriaBuilder cb, Root<?> r, QueryParameters q, Map<String, From> fieldJoins) {

        Predicate predicate = cb.conjunction();
        AtomicBoolean containsToManyAtomic = new AtomicBoolean();

        QueryFilterExpression filterExpression = q.getFilterExpression();

        for (QueryFilter queryFilter : q.getFilters()) {
            QueryFilterExpression additionalFilterExpression = new QueryFilterExpression(queryFilter);

            if (filterExpression == null) {
                filterExpression = additionalFilterExpression;
            } else {
                filterExpression = new QueryFilterExpression(FilterExpressionOperation.AND, filterExpression, additionalFilterExpression);
            }
        }

        if (filterExpression != null) {
            Predicate filterExpressionPredicate = createWhereQueryInternal(em, cb, r, containsToManyAtomic, filterExpression, fieldJoins);
            if (filterExpressionPredicate != null) {
                predicate = cb.and(predicate, filterExpressionPredicate);
            }
        }

        return new CriteriaWhereQuery(predicate, containsToManyAtomic.get());
    }

    private static Predicate createWhereQueryInternal(EntityManager em, CriteriaBuilder cb, Root<?> r, AtomicBoolean containsToManyAtomic, QueryFilterExpression filterExpression, Map<String, From> fieldJoins) {

        if (filterExpression.isLeaf()) {
            QueryFilter f = filterExpression.value();

            Predicate np = null;

            try {
                CriteriaField criteriaField = getCriteriaField(f.getField(), r, fieldJoins);

                if (null == criteriaField) {
                    return null;
                }

                if (criteriaField.containsToMany()) {
                    containsToManyAtomic.set(true);
                }

                Path entityField = criteriaField.getPath();
                entityField.alias(f.getField());

                if (entityField.getModel() == null || !(entityField.getModel() instanceof Attribute)) {
                    return null;
                }

                Attribute attribute = (Attribute) entityField.getModel();

                boolean isBasic = attribute.getPersistentAttributeType().equals(Attribute.PersistentAttributeType.BASIC);
                boolean isCollection = attribute.isCollection();

                @SuppressWarnings("unchecked")
                Path<String> stringField = (Path<String>) entityField;
                @SuppressWarnings("unchecked")
                Path<Date> dateField = (Path<Date>) entityField;
                @SuppressWarnings("unchecked")
                Path<Comparable> compField = (Path<Comparable>) entityField;

                if (isBasic) {

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
                            } else if (entityField.getJavaType().equals(UUID.class) && f.getValue() != null) {
                                String driver = (null == em ? null : (String) em.getProperties().get(PROP_PERSISTENCE_JDBC_DRIVER));
                                if (POSTGRES_SQL_DRIVER.equalsIgnoreCase(driver)) {
                                    np = cb.like(cb.lower(cb.function("text", String.class, r.get(f.getField()).as(String.class))), f.getValue());
                                } else {
                                    np = cb.like(cb.lower(r.get(f.getField()).as(String.class)), f.getValue());
                                }
                            }
                            break;
                        case NLIKE:
                            if (entityField.getJavaType().equals(String.class) && f.getValue() != null) {
                                np = cb.notLike(stringField, f.getValue());
                            } else if (entityField.getJavaType().equals(UUID.class) && f.getValue() != null) {
                                String driver = (null == em ? null : (String) em.getProperties().get(PROP_PERSISTENCE_JDBC_DRIVER));
                                if (POSTGRES_SQL_DRIVER.equalsIgnoreCase(driver)) {
                                    np = cb.notLike(cb.function("text", String.class, r.get(f.getField()).as(String.class)), f.getValue());
                                } else {
                                    np = cb.notLike(r.get(f.getField()).as(String.class), f.getValue());
                                }
                            }
                            break;
                        case NLIKEIC:
                            if (entityField.getJavaType().equals(String.class) && f.getValue() != null) {
                                np = cb.notLike(cb.lower(stringField), f.getValue().toLowerCase());
                            } else if (entityField.getJavaType().equals(UUID.class) && f.getValue() != null) {
                                String driver = (null == em ? null : (String) em.getProperties().get(PROP_PERSISTENCE_JDBC_DRIVER));
                                if (POSTGRES_SQL_DRIVER.equalsIgnoreCase(driver)) {
                                    np = cb.notLike(cb.lower(cb.function("text", String.class, r.get(f.getField()).as(String.class))), f.getValue());
                                } else {
                                    np = cb.notLike(cb.lower(r.get(f.getField()).as(String.class)), f.getValue());
                                }
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
                        case BETWEEN:
                            if (!f.getValues().isEmpty()) {
                                Object value1 = getValueForPath(entityField, f.getValues().get(0));
                                Object value2 = getValueForPath(entityField, f.getValues().get(1));

                                if (!value1.getClass().equals(value2.getClass())) {
                                    throw new QueryFormatException("Incompatible values for BETWEEN filter", f.getField(), QueryFormatError.MALFORMED);
                                }

                                np = cb.between(compField, (Comparable) value1, (Comparable) value2);
                            }
                            break;
                        case NBETWEEN:
                            if (!f.getValues().isEmpty()) {
                                Object value1 = getValueForPath(entityField, f.getValues().get(0));
                                Object value2 = getValueForPath(entityField, f.getValues().get(1));

                                if (!value1.getClass().equals(value2.getClass())) {
                                    throw new QueryFormatException("Incompatible values for BETWEEN filter", f.getField(), QueryFormatError.MALFORMED);
                                }

                                np = cb.not(
                                        cb.between(compField, (Comparable) value1, (Comparable) value2)
                                );
                            }
                    }
                } else if (isCollection) {

                    String idField;

                    switch (f.getOperation()) {
                        case ISNULL:
                            idField = getManagedTypeIdField(attribute.getDeclaringType());
                            np = cb.isNull(entityField.get(idField));
                            break;
                        case ISNOTNULL:
                            idField = getManagedTypeIdField(attribute.getDeclaringType());
                            np = cb.isNotNull(entityField.get(idField));
                            break;
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new NoSuchEntityFieldException(e.getMessage(), f.getField(), r.getJavaType().getSimpleName());
            }

            return np;
        } else if (filterExpression.isEmptyLeaf()) {
            return cb.conjunction();
        } else {
            FilterExpressionOperation operation = filterExpression.operation();

            Predicate leftPredicate = createWhereQueryInternal(em, cb, r, containsToManyAtomic, filterExpression.left(), fieldJoins);
            Predicate rightPredicate = createWhereQueryInternal(em, cb, r, containsToManyAtomic, filterExpression.right(), fieldJoins);

            if (leftPredicate == null && rightPredicate == null) {
                return cb.conjunction();
            }

            if (leftPredicate == null) {
                leftPredicate = cb.conjunction();
            }

            if (rightPredicate == null) {
                rightPredicate = cb.conjunction();
            }

            if (operation.equals(FilterExpressionOperation.AND)) {
                return cb.and(leftPredicate, rightPredicate);
            } else {
                return cb.or(leftPredicate, rightPredicate);
            }
        }
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

            List<Tuple> tuples2 = tuplesGroup.getValue();
            for (int i = 0; i < tuples2.size(); i++) {
                Tuple t = tuples2.get(i);

                List<TupleElement<?>> tes = t.getElements()
                        .stream()
                        .sorted(Comparator.comparing(TupleElement::getAlias))
                        .collect(Collectors.toList());

                for (TupleElement<?> te : tes) {
                    Object o = t.get(te);
                    if (o == null) {
                        continue;
                    }

                    try {
                        String[] fName = te.getAlias().split("\\.");

                        createEntityFromTuple(fName, entity, el, o, i);
                    } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException |
                             InvocationTargetException | InstantiationException e) {

                        throw new NoSuchEntityFieldException(e.getMessage(), te.getAlias(), entity.getSimpleName());
                    }
                }
            }

            return el;
        });
    }

    private static <T> T createEntityFromTuple(String[] fName, Class<?> entity, T el, Object o, int row)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        String fName2 = fName[0];

        Field field = getFieldFromEntity(entity, fName2);
        if (null == field) {
            return el;
        }

        if (fName.length == 1) {
            setEntityFieldValue(el, field, o);
            return el;
        }

        Class entity2 = entity, entity2Wrapper = null;
        Object el2 = null;
        Collection<?> el2Wrapper = null;

        if (isCollectionField(field)) {
            ParameterizedType listType = (ParameterizedType) field.getGenericType();
            entity2 = (Class<?>) listType.getActualTypeArguments()[0];
            entity2Wrapper = field.getType();

            el2Wrapper = (Collection<?>) initializeField(el, field, entity2Wrapper);
            el2 = !el2Wrapper.isEmpty() && el2Wrapper.size() >= row + 1
                    ? el2Wrapper.toArray()[row]
                    : entity2.getConstructor().newInstance();
        } else {
            entity2 = field.getType();

            el2 = initializeField(el, field, entity2);
        }

        Object o2 = createEntityFromTuple(Arrays.asList(fName).subList(1, fName.length).toArray(new String[]{}), entity2, el2, o, row);

        setEntityFieldValue(el, field, o2);

        return el;
    }

    private static List<?> createIdListFromTuples(List<Tuple> tuples, String idField) {
        return tuples.stream()
                .map(t -> t.get(idField))
                .collect(Collectors.toList());
    }

    private static <T> List<?> createIdListFromEntities(List<T> entityList, Field idField) {
        return entityList.stream()
                .map(entity -> {
                    try {
                        idField.setAccessible(true);
                        return idField.get(entity);
                    } catch (IllegalAccessException e) {
                        // do nothing
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private static <T> T initializeField(T entity, Field field, Class<T> entity2Class)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        field.setAccessible(true);

        if (isCollectionField(field)) {
            Object collection = field.get(entity);

            if (collection == null) {
                if (isListField(field)) {
                    field.set(entity, new ArrayList<>());
                } else if (isSetField(field)) {
                    field.set(entity, new HashSet<>());
                }
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
            Collection<?> collection = (Collection<?>) field.get(entity);
            if (collection == null) {
                if (isListField(field)) {
                    field.set(entity, new ArrayList<>());
                } else if (isSetField(field)) {
                    field.set(entity, new HashSet<>());
                }

                collection = (Collection<?>) field.get(entity);
            }

            if (value != null && !collection.contains(value)) {
                Method add = Collection.class.getDeclaredMethod("add", Object.class);
                add.invoke(collection, value);
            }
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

    @SuppressWarnings("unchecked")
    private static String getManagedTypeIdField(ManagedType managedType) {

        String idProperty = "";

        Set<SingularAttribute> singularAttributes = managedType.getSingularAttributes();

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
            if (ClassUtils.isRestIgnored(entityClass, fieldName)) {
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

            if (c.equals(Date.class)) {
                return Date.from(ZonedDateTime.parse(value).toInstant());
            }

            if (c.equals(Instant.class)) {
                return ZonedDateTime.parse(value).toInstant();
            }

            if (c.equals(LocalTime.class)) {
                return LocalTime.parse(value);
            }

            if (c.equals(OffsetTime.class)) {
                return OffsetTime.parse(value);
            }

            if (c.equals(LocalDate.class)) {
                return LocalDate.parse(value);
            }

            if (c.equals(LocalDateTime.class)) {
                return LocalDateTime.parse(value);
            }

            if (c.equals(OffsetDateTime.class)) {
                return ZonedDateTime.parse(value).toOffsetDateTime();
            }

            if (c.equals(ZonedDateTime.class)) {
                return ZonedDateTime.parse(value);
            }

            if (c.equals(Boolean.class)) {
                return Boolean.parseBoolean(value);
            }

            if (c.isEnum()) {
                return Enum.valueOf(c, value);
            }

            if (c.equals(UUID.class)) {
                return UUID.fromString(value);
            }
        } catch (IllegalArgumentException | DateTimeParseException e) {

            throw new InvalidFieldValueException(e.getMessage(), path.getAlias(), value);
        }

        return value;
    }

    private static CriteriaField getCriteriaField(String fieldName, Root<?> r, Map<String, From> fieldJoins) {

        if (fieldName == null) fieldName = "";

        String[] fields = fieldName.split("\\.");

        From from = r;
        Path path = r;
        boolean containsToMany = false;

        String fieldPath = null;
        final Set<From> currentFieldJoins = new HashSet<>();
        for (String field : fields) {

            // only first since multiple field operations are not supported
            final Optional<String> mappedFieldOptional = getRestFieldMappings(from, field).findFirst();

            // ignored
            if (!mappedFieldOptional.isPresent()) {
                // remove joins for ignored field
                r.getJoins().removeIf(currentFieldJoins::contains);
                fieldJoins.values().removeIf(currentFieldJoins::contains);

                return null;
            }

            String mappedFieldName = mappedFieldOptional.get();

            String[] mappedFields = mappedFieldName.split("\\.");
            for (String mappedField : mappedFields) {
                fieldPath = fieldPath == null ? mappedField : fieldPath + "." + mappedField;

                if (fieldJoins.containsKey(fieldPath)) {
                    from = fieldJoins.get(fieldPath);
                    path = null;

                    continue;
                }

                path = from.get(mappedField);

                if (path.getModel() == null && (path.getJavaType().equals(List.class) || path.getJavaType().equals(Set.class))) {
                    containsToMany = true;

                    from = from.join(mappedField, JoinType.LEFT);
                    fieldJoins.put(fieldPath, from);
                    currentFieldJoins.add(from);

                    path = null;
                } else {

                    switch (((Attribute) path.getModel()).getPersistentAttributeType()) {
                        case ONE_TO_MANY:
                        case MANY_TO_MANY:
                            containsToMany = true;
                        case ONE_TO_ONE:
                        case MANY_TO_ONE:
                            from = from.join(mappedField, JoinType.LEFT);
                            fieldJoins.put(fieldPath, from);
                            currentFieldJoins.add(from);

                            path = null;
                            break;
                        case EMBEDDED:
                            from = from.join(mappedField);
                            fieldJoins.put(fieldPath, from);
                            currentFieldJoins.add(from);

                            path = null;
                            break;
                    }
                }
            }
        }

        return new CriteriaField(path != null ? path : from, fieldPath, containsToMany);
    }

    private static Stream<String> getRestFieldMappings(final Path path, final String restField) {

        if (null == restField || ClassUtils.isRestIgnored(path.getJavaType(), restField)) {
            return Stream.empty();
        }

        Class<?> javaType = path.getJavaType();

        Stream<Field> declaredFields = Stream.of(javaType.getDeclaredFields());
        while (javaType.getSuperclass() != null) {
            declaredFields = Stream.concat(declaredFields, Stream.of(javaType.getSuperclass().getDeclaredFields()));
            javaType = javaType.getSuperclass();
        }

        List<String> mappingList = declaredFields
                .flatMap(entityField -> Stream.of(entityField.getAnnotationsByType(RestMapping.class))
                        .map(annotation -> {
                                    String restFieldName = annotation.value();
                                    String jpaFieldPath = annotation.toChildField().isEmpty() ? entityField.getName() : entityField.getName() + "." + annotation.toChildField();
                                    return new AbstractMap.SimpleEntry<>(restFieldName, jpaFieldPath);
                                }
                        )
                        .filter(e -> restField.equals(e.getKey()) || restField.contains(e.getKey()))
                        .map(e -> {
                            if (!restField.equals(e.getKey()) && restField.contains(e.getKey())) {
                                return restField.replace(e.getKey(), e.getValue());
                            }

                            return e.getValue();
                        })
                ).collect(Collectors.toList());

        return mappingList.isEmpty() ? Stream.of(restField) : mappingList.stream();
    }

    private static Map<Object, List<Tuple>> getTuplesGroupingById(List<Tuple> tuples, String idField) {
        Map<Object, List<Tuple>> tupleGrouping = new LinkedHashMap<>();
        if (tuples == null || tuples.isEmpty()) {
            return tupleGrouping;
        }

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

    private static boolean isListField(Field f) {
        return List.class.isAssignableFrom(f.getType());
    }

    private static boolean isSetField(Field f) {
        return Set.class.isAssignableFrom(f.getType());
    }

    private static boolean isRestIgnored(final Class entityClass, final String restField) {
        final RestIgnore restIgnore = (RestIgnore) entityClass.getAnnotation(RestIgnore.class);

        if (restIgnore != null) {
            return Stream.of(restIgnore.value()).anyMatch(restField::equalsIgnoreCase);
        }

        return false;
    }

    private static boolean isObjectField(Field f) {
        return !f.getType().isPrimitive() && !f.getType().isAssignableFrom(String.class);
    }

    private static boolean isAssignableToInstantHoldingTemporal(Class clazz) {
        return Instant.class.isAssignableFrom(clazz) ||
                LocalDate.class.isAssignableFrom(clazz) ||
                LocalDateTime.class.isAssignableFrom(clazz) ||
                OffsetDateTime.class.isAssignableFrom(clazz) ||
                ZonedDateTime.class.isAssignableFrom(clazz);
    }
}
