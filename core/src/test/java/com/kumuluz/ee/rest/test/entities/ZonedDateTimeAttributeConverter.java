package com.kumuluz.ee.rest.test.entities;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * JPA 2.2. does not support java.time.ZonedDateTime mapping so we use a converter for now.
 */
@Converter(autoApply = true)
public class ZonedDateTimeAttributeConverter implements AttributeConverter<ZonedDateTime, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(ZonedDateTime attribute) {
        return (attribute == null ? null : Timestamp.from(attribute.toInstant()));
    }

    @Override
    public ZonedDateTime convertToEntityAttribute(Timestamp dbData) {
        return (dbData == null ? null : ZonedDateTime.ofInstant(dbData.toInstant(), ZoneId.of("UTC")));
    }
}