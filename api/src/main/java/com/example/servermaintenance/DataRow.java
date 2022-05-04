package com.example.servermaintenance;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

// project	student alias	csc username	uid	dns name	self made dns name	name		vps username		 pouta dns		ip address

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "datarow")
public class DataRow implements Serializable {

    private static final long serialVersionUID = -2343243243242432341L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Account user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course")
    private Course course;

    public DataRow(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

}
