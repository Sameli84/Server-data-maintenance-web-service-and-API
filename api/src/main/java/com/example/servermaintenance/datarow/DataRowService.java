package com.example.servermaintenance.datarow;

import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.course.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DataRowService {

    @Autowired
    private DataRowRepository dataRowRepository;

    public List<DataRow> getDataRows() {
        return this.dataRowRepository.findAll();
    }

    public Optional<DataRow> getStudentData(Course course, Account account) {
        return dataRowRepository.findDataRowByCourseAndAccount(course, account);
    }

    public List<DataRow> getCourseData(Course course) {
        return dataRowRepository.findDataRowsByCourse(course);
    }

    public void removeDataRow(Course course, Account account) {
        if(dataRowRepository.findDataRowByCourseAndAccount(course, account).isPresent()) {
            DataRow dr = dataRowRepository.findDataRowByCourseAndAccount(course, account).get();
            dataRowRepository.delete(dr);
        }
    }

    public Optional<DataRow> getDataRowById(Long id) {
        return Optional.of(dataRowRepository.getById(id));
    }

    public List<DataRow> getDataRowsByTeacher(Account account) {
        return dataRowRepository.findAllByCourseOwner(account);
    }

    public List<DataRow> getDataRowsByCourseAndTeacher(Account account, Course course) {
        return dataRowRepository.findAllByCourseOwnerAndCourse(account, course);
    }
}
