package com.example.servermaintenance.course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Deprecated
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseDataDTO {
    private String cscUsername;
    private String selfMadeDns;
    private String project;
    private String vpsUsername;
    private String poutaDns;
    private String ipAddress;
}
