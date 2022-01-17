package com.kumuluz.ee.rest.test.entities;

import javax.persistence.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

/**
 * @author cen1
 */
@Entity
@Table(name = "events")
public class Event {

    @Id
    private Integer id;

    @Column(name = "begins_at")
    private Instant beginsAt;

    @Column(name = "intermission_at")
    private OffsetDateTime intermissionAt;

    @Column(name = "ends_at")
    private ZonedDateTime endsAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Instant getBeginsAt() {
        return beginsAt;
    }

    public void setBeginsAt(Instant beginsAt) {
        this.beginsAt = beginsAt;
    }

    public OffsetDateTime getIntermissionAt() {
        return intermissionAt;
    }

    public void setIntermissionAt(OffsetDateTime intermissionAt) {
        this.intermissionAt = intermissionAt;
    }

    public ZonedDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(ZonedDateTime endsAt) {
        this.endsAt = endsAt;
    }
}
