package com.example.PlacementPortal.Repositories;

import com.example.PlacementPortal.Entities.Admin;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface adminRepository extends CrudRepository<Admin, Long> {
}
