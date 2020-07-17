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

import javax.persistence.AttributeConverter;
import java.util.UUID;

/**
 * @author gpor0
 */
@javax.persistence.Converter
public class UUIDConverter implements AttributeConverter<UUID, Object> {

    @Override
    public Object convertToDatabaseColumn(UUID uuid) {
        return uuid;
    }

    @Override
    public UUID convertToEntityAttribute(Object dbData) {

        if (dbData == null) {
            return null;
        } else if (dbData instanceof UUID) {
            return (UUID) dbData;
        }

        return UUID.fromString(dbData.toString());
    }
}