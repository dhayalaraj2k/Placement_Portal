package com.example.PlacementPortal.Repositories;

import com.example.PlacementPortal.Entities.Company;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface companyRepository extends CrudRepository<Company, Long> {
    List<Company> findCompaniesByCompanyName(String name);
    Company findCompanyByToken(String token);
    Company findCompanyByEmail(String email);
    Company findCompanyById(Long id);
    boolean existsByEmail(String email);
    boolean existsByCompanyName(String name);
    boolean existsByToken(String token);
}
