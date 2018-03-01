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

import com.kumuluz.ee.rest.beans.CriteriaField;
import com.kumuluz.ee.rest.beans.CriteriaWhereQuery;
import com.kumuluz.ee.rest.beans.QueryFilter;
import com.kumuluz.ee.rest.beans.QueryParameters;
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
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Tilen Faganel
 */
public class JPAUtils {

    private static final Logger log = Logger.getLogger(JPAUtils.class.getSimpleName());

    public static <T> List<T> queryEntities(EntityManager em, Class<T> entity) {

        return queryEntities(em, entity, new QueryParameters());
    }

    public static <T> List<T> queryEntities(EntityManager em, Class<T> entity, QueryParameters q) {

        return queryEntities(em, entity, q, null);
    }

    public static <T> List<T> queryEntities(EntityManager em, Class<T> entity, CriteriaFilter<T> customFilter) {
        return queryEntities(em, entity, new QueryParameters(), customFilter);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> queryEntities(EntityManager em, Class<T> entity, QueryParameters q, CriteriaFilter<T> customFilter) {

        if (em == null || entity == null)
            throw new IllegalArgumentException("The entity manager and the entity cannot be null.");

        if (q == null)
            throw new IllegalArgumentException("Query parameters can't be null. " +
                    "If you don't have any parameters either pass a empty object or " +
                    "use the queryEntities(EntityManager, Class<T>) method.");

        log.finest("Querying entity: '" + entity.getSimpleName() + "' with parameters: " + q);

        Boolean requiresDistinct = false;

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<?> cq;

        if (q.getFields().isEmpty()) {

            cq = cb.createQuery(entity);
        } else {

            cq = cb.createTupleQuery();
        }

        Root<T> r = cq.from(entity);

        Predicate wherePredicate = null;

        if (!q.getFilters().isEmpty()) {

            CriteriaWhereQuery criteriaWhereQuery = createWhereQueryInternal(cb, r, q);

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

        if (q.getFields().isEmpty()) {

            return (List<T>) tq.getResultList();
        } else {

            return createEntityFromTuple((List<Tuple>)tq.getResultList(), entity);
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

        if (q == null)
            throw new IllegalArgumentException("Query parameters can't be null. " +
                    "If you don't have any parameters either pass a empty object or " +
                    "use the queryEntitiesCount(EntityManager, Class<T>) method.");

        log.finest("Querying entity count: '" + entity.getSimpleName() + "' with parameters: " + q);

        Boolean requiresDistinct = false;

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> cq = cb.createQuery(Long.class);

        Root<T> r = cq.from(entity);

        Predicate wherePredicate = null;

        if (!q.getFilters().isEmpty()) {

            CriteriaWhereQuery criteriaWhereQuery = createWhereQueryInternal(cb, r, q);

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
            } catch (IllegalArgumentException e) {

                throw new NoSuchEntityFieldException(e.getMessage(), qo.getField(), r.getJavaType().getSimpleName());
            }
        });

        //Add sort by id for correct pagination when field has same values
        if (id != null) {
            orders.add(cb.asc(getCriteriaField(id, r).getPath()));
        }

        return orders;
    }

    public static Predicate createWhereQuery(CriteriaBuilder cb, Root<?> r, QueryParameters q) {
        return createWhereQueryInternal(cb, r, q).getPredicate();
    }

    public static List<Selection<?>> createFieldsSelect(Root<?> r, QueryParameters q, String
            idField) {

        List<Selection<?>> fields = q.getFields().stream().map(f -> {

            try {
                return r.get(f).alias(f);
            } catch (IllegalArgumentException e) {

                throw new NoSuchEntityFieldException(e.getMessage(), f, r.getJavaType().getSimpleName());
            }
        }).collect(Collectors.toList());

        try {
            fields.add(r.get(idField).alias(idField));
        } catch (IllegalArgumentException e) {

            throw new NoSuchEntityFieldException(e.getMessage(), idField, r.getJavaType().getSimpleName());
        }

        return fields.stream().distinct().collect(Collectors.toList());
    }

    // Temporary methods to not break the public API

    private static CriteriaWhereQuery createWhereQueryInternal(CriteriaBuilder cb, Root<?> r, QueryParameters q) {

        Predicate predicate = cb.conjunction();
        Boolean containsToMany = false;

        for (QueryFilter f : q.getFilters()) {

            Predicate np = null;

            try {
                CriteriaField criteriaField = getCriteriaField(f.getField(), r);

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
                        }
                        break;
                    case LIKEIC:
                        if (entityField.getJavaType().equals(String.class) && f.getValue() != null) {
                            np = cb.like(cb.lower(stringField), f.getValue().toLowerCase());
                        }
                        break;
                    case GT:
                        if (Date.class.isAssignableFrom(entityField.getJavaType()) ||
                                Instant.class.isAssignableFrom(entityField.getJavaType()) ||
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
                                Instant.class.isAssignableFrom(entityField.getJavaType()) ||
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
                                Instant.class.isAssignableFrom(entityField.getJavaType()) ||
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
                                Instant.class.isAssignableFrom(entityField.getJavaType()) ||
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

    private static <T> List<T> createEntityFromTuple(List<Tuple> tuples, Class<T> entity) {

        List<T> entities = new ArrayList<>();

        for (Tuple t : tuples) {

            T el;

            try {
                el = entity.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException |
                    NoSuchMethodException | InvocationTargetException e) {

                throw new AssertionError();
            }

            for (TupleElement<?> te : t.getElements()) {

                Object o = t.get(te);

                try {
                    Field f = getFieldFromEntity(entity, te.getAlias());
                    f.setAccessible(true);
                    f.set(el, o);
                } catch (NoSuchFieldException | IllegalAccessException e1) {

                    throw new NoSuchEntityFieldException(e1.getMessage(), te.getAlias(), entity.getSimpleName());
                }
            }

            entities.add(el);
        }

        return entities;
    }

    @SuppressWarnings("unchecked")
    private static String getEntityIdField(EntityManager em, Class entity) {

        String idProperty = "";

        Metamodel metamodel = em.getMetamodel();
        EntityType e = metamodel.entity(entity);
        Set<SingularAttribute> singularAttributes = e.getSingularAttributes();

        for (SingularAttribute singularAttribute : singularAttributes) {

            if (singularAttribute.isId()) {

                idProperty = singularAttribute.getName();
                break;
            }
        }

        return idProperty;
    }

    private static Field getFieldFromEntity(Class entity, String fieldName) throws
            NoSuchFieldException {

        try {
            return entity.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {

            if (entity.getSuperclass() == null) {
                throw e;
            }

            return getFieldFromEntity(entity.getSuperclass(), fieldName);
        }
    }

    private static Object getValueForPath(Path path, String value) {

        if (value == null) return null;

        Class c = path.getModel().getBindableJavaType();

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

        for (String field : fields) {

            path = from.get(field);

            if (path.getModel() == null &&
                    (path.getJavaType().equals(List.class) || path.getJavaType().equals(Set.class))) {

                containsToMany = true;
                from = from.join(field);
            } else {

                switch (((Attribute) path.getModel()).getPersistentAttributeType()) {
                    case ONE_TO_MANY:
                    case MANY_TO_MANY:
                        containsToMany = true;
                    case ONE_TO_ONE:
                    case MANY_TO_ONE:
                    case EMBEDDED:
                        from = from.join(field);
                        break;
                }
            }
        }

        return new CriteriaField(path, containsToMany);
    }
}
