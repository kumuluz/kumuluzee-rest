package com.kumuluz.ee.rest.test.entities;

import com.kumuluz.ee.rest.annotations.RestIgnore;
import com.kumuluz.ee.rest.annotations.RestMapping;
import com.kumuluz.ee.rest.test.utils.UUIDConverter;
import org.eclipse.persistence.annotations.Converter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Tilen Faganel
 */
@Entity
@RestIgnore("userIgnoredField")
@Table(name = "users")
@Converter(name = "uuid", converterClass = UUIDConverter.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "uuid")
    @Convert(converter = UUIDConverter.class)
    private UUID uuid;

    @RestMapping("firstnameChanged")
    @RestMapping("firstnameAndLastname")
    private String firstname;

    @RestMapping("firstnameAndLastname")
    private String lastname;

    @RestMapping("emailAndCurrentPosition")
    private String email;

    private String country;

    private String ip_address;

    private Integer role;

    private Boolean confirmed = false;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @OneToMany(mappedBy = "user")
    private List<Project> projects;

    @RestMapping(value = "career.experience", toChildField = "years")
    @RestMapping(value = "emailAndCurrentPosition", toChildField = "currentPosition")
    @OneToOne(mappedBy = "user")
    private UserCareer career;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public UserCareer getCareer() {
        return career;
    }

    public void setCareer(UserCareer career) {
        this.career = career;
    }
}
