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

    @Column(name = "student_alias")
    private String student_alias;

    @Column(name = "csc_username")
    private String csc_username;

    @Column(name = "juuseri_id")
    private int juuseri_id;

    @Column(name = "dns_name")
    private String dns_name;

    @Column(name = "self_made_dns_name")
    private String self_made_dns_name;

    @Column(name = "name")
    private String name;

    @Column(name = "vps_user_name")
    private String vps_user_name;

    @Column(name = "pouta_dns")
    private String pouta_dns;

    @Column(name = "ip_address")
    private String ip_address;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "course")
    private Course course;

    public DataRow(String student_alias, String csc_username, int juuseri_id, String dns_name, String self_made_dns_name, String name, String vps_user_name, String pouta_dns, String ip_address, Account account, Course course) {
        this.student_alias = student_alias;
        this.csc_username = csc_username;
        this.juuseri_id = juuseri_id;
        this.dns_name = dns_name;
        this.self_made_dns_name = self_made_dns_name;
        this.name = name;
        this.vps_user_name = vps_user_name;
        this.pouta_dns = pouta_dns;
        this.ip_address = ip_address;
        this.account = account;
        this.course = course;
    }
}
