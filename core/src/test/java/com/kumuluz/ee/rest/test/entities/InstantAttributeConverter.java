package com.kumuluz.ee.rest.test.entities;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * JPA 2.2. does not support java.time.Instant mapping so we use a converter for now.
 */
@Converter(autoApply = true)
public class InstantAttributeConverter implements AttributeConverter<Instant, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(Instant attribute) {
        return (attribute == null ? null : Timestamp.from(attribute));
    }

    @Override
    public Instant convertToEntityAttribute(Timestamp dbData) {
        return (dbData == null ? null : dbData.toInstant());
    }
}