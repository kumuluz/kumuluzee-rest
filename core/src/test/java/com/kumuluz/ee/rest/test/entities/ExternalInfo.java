package com.kumuluz.ee.rest.test.entities;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * @author gpor0
 */
@MappedSuperclass
public abstract class ExternalInfo {

    @Column(name = "external_id")
    private String externalId;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
}
