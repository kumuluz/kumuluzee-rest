package si.petrol.digital.rest.test.entities;

import com.kumuluz.ee.rest.annotations.RestMapping;
import com.kumuluz.ee.rest.test.entities.User;

import javax.persistence.*;

/**
 * @author Gregor Porocnik
 */
@Entity
@Table(name = "user_careers")
public class UserCareer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @RestMapping("experience")
    private Integer years;

    @Column(name = "current_position")
    private String currentPosition;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getYears() {
        return years;
    }

    public void setYears(Integer years) {
        this.years = years;
    }

    public String getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(String currentPosition) {
        this.currentPosition = currentPosition;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
