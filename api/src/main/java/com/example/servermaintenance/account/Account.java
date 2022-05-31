package com.example.servermaintenance.account;

import com.example.servermaintenance.course.domain.CourseStudent;
import com.example.servermaintenance.course.domain.Course;
//import com.example.servermaintenance.datarow.DataRow;
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
public class Account extends AbstractPersistable<Long> implements UserDetails {
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

    @Column
    private String password;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Course> courses;

    @OneToMany(mappedBy = "account")
    private List<CourseStudent> courseStudentData;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "account_roles",
            joinColumns = @JoinColumn(name = "account_id", referencedColumnName = "id", columnDefinition = "int8"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id", columnDefinition = "int8")
    )
    private Set<Role> roles = new HashSet<>();

    public Account(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return firstName + " " + lastName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getUsername() {
        return getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
