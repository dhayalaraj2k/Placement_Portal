package com.example.PlacementPortal.Repositories;

import com.example.PlacementPortal.Entities.Application;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface applicationRepository extends CrudRepository<Application, Long> {
    boolean existsByPostingIdAndStudentId(Long postingId, Long studentId);
    List<Application> findApplicationsByStudentId(Long studentId);
    Application findApplicationByPostingId(Long postingId);
    Application findApplicationByPostingIdAndStudentId(Long postingId, Long studentId);
    List<Application> findApplicationsByPostingId(Long postingId);
}
