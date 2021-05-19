package com.example.PlacementPortal.Repositories;

import com.example.PlacementPortal.Entities.Posting;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface postingRepository extends CrudRepository<Posting, Long> {
    List<Posting> findPostingsByCompanyId(Long id);
    Posting findPostingById(Long id);
    List<Posting> findPostingsByEligibleCGPAGreaterThanEqual(String cgpa);
    List<Posting> findPostingsByCompanyIdGreaterThanEqual(Long id);
}
