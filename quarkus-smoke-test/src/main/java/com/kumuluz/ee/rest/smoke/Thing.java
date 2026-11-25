package com.kumuluz.ee.rest.smoke;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "things")
public class Thing {

    @Id
    private Integer id;

    private String name;

    /** Native UUID column: exercises the LIKE-on-UUID cast path in JPAUtils. */
    private UUID ref;

    @ManyToOne
    private Thing parent;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getRef() {
        return ref;
    }

    public Thing getParent() {
        return parent;
    }
}
