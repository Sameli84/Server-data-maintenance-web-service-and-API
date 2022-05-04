package com.example.servermaintenance;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "course")
public class Course extends AbstractPersistable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "url")
    private String url;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "course")
    private List<DataRow> data;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Account account;

    public Course(String name, String url, Account account) {
        this.name = name;
        this.url = url;
        this.account = account;
    }
}
