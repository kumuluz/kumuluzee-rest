package com.kumuluz.ee.rest.test.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;

/**
 * Time-only and date-only columns mapped as {@code java.util.Date} with {@code @Temporal}, as in
 * https://github.com/kumuluz/kumuluzee-rest/issues/22.
 */
@Entity
@Table(name = "legacy_temporals")
public class LegacyTemporal {

    @Id
    private Integer id;

    @Temporal(TemporalType.TIME)
    @Column(name = "start_time")
    private Date startTime;

    @Temporal(TemporalType.DATE)
    @Column(name = "start_date")
    private Date startDate;

    public Integer getId() {
        return id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getStartDate() {
        return startDate;
    }
}
