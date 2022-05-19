package com.example.servermaintenance.datarow;

import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.course.Course;
import com.example.servermaintenance.course.CourseDataDTO;
import com.opencsv.bean.CsvIgnore;
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

    @Column(name = "userid")
    private int uid;

    @Column(name = "self_made_dns")
    private String selfMadeDnsName;

    @Column(name = "project")
    private String project;

    @Column(name = "vps_username")
    private String vpsUserName;

    @Column(name = "pouta_dns")
    private String poutaDns;

    @Column(name = "ip_address")
    private String ipAddress;

    @CsvIgnore
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @CsvIgnore
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    public DataRow(String studentAlias, int uid, CourseDataDTO courseDataDTO, Account account, Course course) {
        this.studentAlias = studentAlias;
        this.uid = uid;
        this.project = courseDataDTO.getProject();
        this.cscUsername = courseDataDTO.getCscUsername();
        this.selfMadeDnsName = courseDataDTO.getSelfMadeDnsName();
        this.vpsUserName = courseDataDTO.getVpsUsername();
        this.poutaDns = courseDataDTO.getPoutaDns();
        this.ipAddress = courseDataDTO.getIpAddress();
        this.account = account;
        this.course = course;
    }
}
