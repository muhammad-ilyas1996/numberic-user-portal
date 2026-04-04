package com.numbericsuserportal.LlcNorthwest.companies.repo;

import com.numbericsuserportal.LlcNorthwest.companies.entity.UserFormationCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFormationCompanyRepository extends JpaRepository<UserFormationCompany, Long> {

    List<UserFormationCompany> findByUserId(Long userId);

    boolean existsByUserIdAndCompanyId(Long userId, String companyId);
}
