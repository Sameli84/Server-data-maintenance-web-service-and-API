package com.example.servermaintenance.account;

import com.example.servermaintenance.course.domain.CourseStudent;
import com.example.servermaintenance.course.domain.Course;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "account")
public class Account extends AbstractPersistable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "int8")
    private Long id;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column(unique = true)
    private String email;

    @Column(name = "keycloak_uuid")
    private UUID keyCloakUuid;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Course> courses;

    @OneToMany(mappedBy = "account", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CourseStudent> courseStudentData;

    @Transient
    private Set<String> roles = new HashSet<>();

    public Account(String firstName, String lastName, String email, UUID keyCloakUuid) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.keyCloakUuid = keyCloakUuid;
    }

    public String getName() {
        return firstName + " " + lastName;
    }
}
