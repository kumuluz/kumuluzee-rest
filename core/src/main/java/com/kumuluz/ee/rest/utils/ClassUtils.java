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

import java.lang.reflect.Field;
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

        return restIgnore != null && Stream.of(restIgnore.value()).anyMatch(f -> restField.equalsIgnoreCase(f));
    }

}
