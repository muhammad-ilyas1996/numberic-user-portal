package com.medicalbillinguserportal.patientregistration.payer.repository;

import com.medicalbillinguserportal.patientregistration.payer.entity.PayerEntity;
//import com.medicalbillinguserportal.queryrequest.entity.QueryRequestInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.medicalbillinguserportal.common.constant.PayerStatus;
import java.util.List;

@Repository
public interface PayerRepo extends JpaRepository<PayerEntity,Long>, JpaSpecificationExecutor<PayerEntity> {

    @Query("SELECT p FROM PayerEntity p WHERE p.status = :status")
    List<PayerEntity> findByStatus(PayerStatus status);
}
