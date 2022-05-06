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
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "student_alias")
    private String studentAlias;

    @Column(name = "csc_username")
    private String cscUsername;

    @Column(name = "user_id")
    private int uid;

    @Column(name = "dns_name")
    private String dnsName;

    @Column(name = "self_made_dns_name")
    private String selfMadeDnsName;

    @Column(name = "name")
    private String name;

    @Column(name = "vps_user_name")
    private String vpsUserName;

    @Column(name = "pouta_dns")
    private String poutaDns;

    @Column(name = "ip_address")
    private String ipAddress;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    public void update(String studentAlias, String cscUsername, int uid, String dnsName, String selfMadeDnsName, String name, String vpsUserName, String poutaDns, String ipAddress) {
        setStudentAlias(studentAlias);
        setCscUsername(cscUsername);
        setUid(uid);
        setDnsName(dnsName);
        setSelfMadeDnsName(selfMadeDnsName);
        setName(name);
        setVpsUserName(vpsUserName);
        setPoutaDns(poutaDns);
        setIpAddress(ipAddress);
    }

    public DataRow(String studentAlias, String cscUsername, int uid, String dnsName, String selfMadeDnsName, String name, String vpsUserName, String poutaDns, String ipAddress, Account account, Course course) {
        this.studentAlias = studentAlias;
        this.cscUsername = cscUsername;
        this.uid = uid;
        this.dnsName = dnsName;
        this.selfMadeDnsName = selfMadeDnsName;
        this.name = name;
        this.vpsUserName = vpsUserName;
        this.poutaDns = poutaDns;
        this.ipAddress = ipAddress;
        this.account = account;
        this.course = course;
    }
}
