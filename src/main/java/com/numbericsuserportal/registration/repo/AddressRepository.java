package com.numbericsuserportal.registration.repo;

import com.numbericsuserportal.registration.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
}

