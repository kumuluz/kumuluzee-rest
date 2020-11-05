/*
 *  Copyright (c) 2014-2020 Kumuluz and/or its affiliates
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
import com.kumuluz.ee.rest.exceptions.InvalidFieldValueException;
import com.kumuluz.ee.rest.exceptions.NoGenericTypeException;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author gpor0
 */
public class ClassUtils {

    protected static Field fieldLookup(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            if (isRestIgnored(clazz, fieldName)) {
                return null;
            }

            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass == null) {
                throw e;
            }

            return fieldLookup(superclass, fieldName);
        }
    }

    protected static boolean isRestIgnored(final Class<?> entityClass, final String restField) {
        final RestIgnore restIgnore = entityClass.getAnnotation(RestIgnore.class);

        return restIgnore != null && Stream.of(restIgnore.value()).anyMatch(restField::equalsIgnoreCase);
    }

    protected static Stream<String> getRestFieldMappings(final Class<?> clazz, final String restField) {

        if (null == restField || isRestIgnored(clazz, restField)) {
            return Stream.empty();
        }

        List<String> mappingList = Stream.of(clazz.getDeclaredFields())
                .flatMap(entityField -> Stream.of(entityField.getAnnotationsByType(RestMapping.class))
                        .map(annotation -> {
                                    String restFieldName = annotation.value();
                                    String entityFieldPath = annotation.toChildField().isEmpty() ? entityField.getName() :
                                            entityField.getName() + "." + annotation.toChildField();
                                    return new AbstractMap.SimpleEntry<>(restFieldName, entityFieldPath);
                                }
                        )
                        .filter(e -> restField.equals(e.getKey())).map(AbstractMap.SimpleEntry::getValue)
                ).collect(Collectors.toList());

        return mappingList.isEmpty() ? Stream.of(restField) : mappingList.stream();
    }

    protected static Class<?> getGenericType(Field field) {

        ParameterizedType pt = (ParameterizedType) field.getGenericType();
        Type[] fieldArgTypes = pt.getActualTypeArguments();

        if (fieldArgTypes.length > 0) {
            return (Class<?>) fieldArgTypes[0];
        } else {
            throw new NoGenericTypeException("Unable to obtain GenericType from Collection field.", field.getName(),
                    field.getClass().getSimpleName());
        }
    }

    protected static Object getValueForField(Field field, String value) {

        if (value == null) return null;

        Class c = field.getType();

        try {

            if (c.equals(Date.class))
                return Date.from(ZonedDateTime.parse(value).toInstant());

            if (c.equals(Instant.class))
                return ZonedDateTime.parse(value).toInstant();

            if (c.equals(LocalDate.class)) {
                return LocalDate.parse(value);
            }

            if (c.equals(LocalDateTime.class)) {
                return LocalDateTime.parse(value);
            }

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

    protected static Object getTargetDateTypeValue(Date value, Class<?> clazzType) {

        if (value == null) return null;

        try {

            if (clazzType.equals(Date.class))
                return value;

            if (clazzType.equals(Instant.class))
                return value.toInstant();

            if (clazzType.equals(LocalDate.class)) {
                return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }

            if (clazzType.equals(LocalDateTime.class)) {
                return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
        } catch (IllegalArgumentException | DateTimeParseException e) {

            throw new InvalidFieldValueException(e.getMessage(), "", value.toString());
        }

        return value;
    }

}
