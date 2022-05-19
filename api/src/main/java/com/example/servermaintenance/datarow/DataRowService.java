package com.example.servermaintenance.datarow;

import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.course.Course;
import com.example.servermaintenance.course.CourseDataDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@AllArgsConstructor
public class DataRowService {
    private final DataRowRepository dataRowRepository;

    public List<DataRow> getDataRows() {
        return this.dataRowRepository.findAll();
    }

    public DataRow getStudentData(Course course, Account account) {
        return dataRowRepository.findDataRowByCourseAndAccount(course, account);
    }

    public List<DataRow> getCourseDataRows(Course course) {
        return dataRowRepository.findDataRowsByCourse(course);
    }

    public void removeDataRow(Course course, Account account) {
        DataRow dr = dataRowRepository.findDataRowByCourseAndAccount(course, account);
        dataRowRepository.delete(dr);
    }

    public DataRow getDataRowById(Long id) {
        return dataRowRepository.getById(id);
    }

    public List<DataRow> getDataRowsByTeacher(Account account) {
        return dataRowRepository.findAllByCourseOwner(account);
    }

    public List<DataRow> getDataRowsByCourseAndTeacher(Account account, Course course) {
        return dataRowRepository.findAllByCourseOwnerAndCourse(account, course);
    }

    public DataRow generateData(Course course, Account account) {
        String studentAlias = account.getEmail().split("@")[0];
        int uid = 1000 + dataRowRepository.countDataRowByCourse(course);
        var courseDataDTO = new CourseDataDTO();
        return dataRowRepository.save(new DataRow(studentAlias, uid, courseDataDTO, account, course));
    }

    @Transactional
    public DataRow updateDataRow(DataRow dataRow, CourseDataDTO courseDataDTO) {
        dataRow.setProject(courseDataDTO.getProject());
        dataRow.setCscUsername(courseDataDTO.getCscUsername());
        dataRow.setSelfMadeDnsName(courseDataDTO.getSelfMadeDnsName());
        dataRow.setVpsUserName(courseDataDTO.getVpsUsername());
        dataRow.setPoutaDns(courseDataDTO.getPoutaDns());
        dataRow.setIpAddress(courseDataDTO.getIpAddress());
        return dataRowRepository.save(dataRow);
    }

    public CourseDataDTO getCourseDataDTO(DataRow data) {
        return new CourseDataDTO(
                data.getCscUsername(),
                data.getSelfMadeDnsName(),
                data.getProject(),
                data.getVpsUserName(),
                data.getPoutaDns(),
                data.getIpAddress()
        );
    }
}
