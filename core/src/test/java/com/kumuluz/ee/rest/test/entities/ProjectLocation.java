package com.kumuluz.ee.rest.test.entities;

import jakarta.persistence.*;

import java.io.Serializable;

/**
 * @author gpor0
 */
@Entity
@Table(name = "project_locations")
@NamedQueries({@NamedQuery(name = "ProjectLocation.getAll", query = "SELECT pl FROM ProjectLocation pl")})
public class ProjectLocation extends ExternalInfo implements Serializable {

    @Id
    private Integer id;

    @Column(name = "location_name")
    private String locationName;

    @OneToOne
    @JoinColumn(name = "project_id")
    private Project project;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public String toString() {
        return "ProjectLocation{" +
                "id=" + id +
                ", locationName='" + locationName + '\'' +
                '}';
    }
}
