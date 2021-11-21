package com.kumuluz.ee.rest.test.entities;

import com.kumuluz.ee.rest.annotations.RestIgnore;
import com.kumuluz.ee.rest.annotations.RestMapping;
import com.kumuluz.ee.rest.test.entities.enums.UserStatus;
import com.kumuluz.ee.rest.test.utils.UUIDConverter;
import org.eclipse.persistence.annotations.Converter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
@NamedQueries({@NamedQuery(name = "User.getAll", query = "SELECT u FROM User u")})
public class User implements Comparable<User>, Serializable {

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

    private UserStatus status;

    @RestMapping(value = "career.experience", toChildField = "years")
    @RestMapping(value = "emailAndCurrentPosition", toChildField = "currentPosition")
    @OneToOne(mappedBy = "user")
    private UserCareer career;

    @Column(name = "score", scale = 2, precision = 10)
    private BigDecimal score;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "registration_time")
    private LocalTime registrationTime;

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

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalTime getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(LocalTime registrationTime) {
        this.registrationTime = registrationTime;
    }

    @Override
    public int compareTo(User o) {
        return id.compareTo(o.getId());
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", uuid=" + uuid +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", email='" + email + '\'' +
                ", country='" + country + '\'' +
                ", ip_address='" + ip_address + '\'' +
                ", role=" + role +
                ", confirmed=" + confirmed +
                ", createdAt=" + createdAt +
                ", projects=" + projects +
                ", status=" + status +
                ", career=" + career +
                ", score=" + score +
                ", birthDate=" + birthDate +
                ", registrationDate=" + registrationDate +
                ", registrationTime=" + registrationTime +
                '}';
    }
}
