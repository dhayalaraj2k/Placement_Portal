package com.example.PlacementPortal.Repositories;

import com.example.PlacementPortal.Entities.Student;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface studentRepository extends CrudRepository<Student, Long> {
    boolean existsByUsername(String name);
    boolean existsByEmail(String email);
    boolean existsByToken(String token);
    Student findStudentByUsername(String username);
    Student findStudentByToken(String token);
    Student findStudentByEmail(String email);
    Student findStudentById(Long id);
}
