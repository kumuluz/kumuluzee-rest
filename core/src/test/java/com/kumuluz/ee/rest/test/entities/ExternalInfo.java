package com.kumuluz.ee.rest.test.entities;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

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
