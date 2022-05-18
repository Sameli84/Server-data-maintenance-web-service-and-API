package com.example.servermaintenance.course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseDataDTO {
    private String cscUsername;
    private String selfMadeDnsName;
    private String project;
    private String vpsUsername;
    private String poutaDns;
    private String ipAddress;
}
