package edu.duke.ece568.minUPS.entity;

import javax.persistence.*;

@Entity
@Table
public class Users {
    @Id
    @SequenceGenerator(
            name = "user_sequence",
            sequenceName = "user_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_sequence"
    )
    private Integer id;
    @Column(unique = true, nullable = false)
    private String upsID;
    @Column(nullable = false)
    private String password;
    @Column(unique = true, nullable = false)
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUpsID() {
        return upsID;
    }

    public void setUpsID(String upsID) {
        this.upsID = upsID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
