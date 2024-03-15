/*
 *  Copyright (c) 2014-2019 Kumuluz and/or its affiliates
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
package com.kumuluz.ee.rest.test.utils;

import org.eclipse.persistence.sessions.Session;

import java.util.UUID;

/**
 * @author gpor0
 * @author terli00
 *
 * Implemented to override default Eclipse UUID Converter (EclipseLink version 4.0.2).
 * Original converter does not have implemented null-check for objects.
 */
public class UUIDConverter extends org.eclipse.persistence.mappings.converters.UUIDConverter {
    @Override
    public Object convertObjectValueToDataValue(Object o, Session session) {
        if (o == null) {
            return null;
        }

        if (o instanceof UUID) {
            return o.toString();
        } else {
            throw new IllegalArgumentException("Source object is not an instance of java.util.UUID");
        }
    }

    @Override
    public Object convertDataValueToObjectValue(Object o, Session session) {
        if (o == null) {
            return null;
        }

        return UUID.fromString(o.toString());
    }
}