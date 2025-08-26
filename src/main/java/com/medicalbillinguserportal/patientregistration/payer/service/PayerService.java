package com.medicalbillinguserportal.patientregistration.payer.service;

import com.medicalbillinguserportal.patientregistration.payer.mapper.PayerConverter;
import com.medicalbillinguserportal.patientregistration.payer.dto.PayerDTO;
import com.medicalbillinguserportal.patientregistration.payer.dto.PayerDropdownDTO;
import com.medicalbillinguserportal.patientregistration.payer.entity.PayerEntity;
import com.medicalbillinguserportal.patientregistration.payer.repository.PayerRepo;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.medicalbillinguserportal.common.constant.PayerStatus;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PayerService {

    @Autowired
    private PayerRepo payerRepository;

    public PayerDTO savePayer(PayerDTO dto, User currentUser) {
        PayerEntity entity = PayerConverter.toEntity(dto,currentUser);

        // Audit
        entity.setCreatedBy(currentUser.getUsername());
        entity.setCreatedOn(new Date());

        PayerEntity saved = payerRepository.save(entity);
        return PayerConverter.toDTO(saved);
    }

    public String activatePayer(Long payerId, User currentUser) {
        PayerEntity payer = payerRepository.findById(payerId)
                .orElseThrow(() -> new RuntimeException("Payer not found"));

        payer.setStatus(PayerStatus.ACTIVE);
        payer.setModifiedBy(currentUser.getUsername());
        payer.setModifiedOn(new Date());

        payerRepository.save(payer);
        return "Payer activated successfully.";
    }

    public String deactivatePayer(Long payerId, User currentUser) {
        PayerEntity payer = payerRepository.findById(payerId)
                .orElseThrow(() -> new RuntimeException("Payer not found"));

        payer.setStatus(PayerStatus.INACTIVE);
        payer.setModifiedBy(currentUser.getUsername());
        payer.setModifiedOn(new Date());

        payerRepository.save(payer);
        return "Payer deactivated successfully.";
    }

    public List<PayerDropdownDTO> getActivePayersForDropdown() {
        return payerRepository.findByStatus(PayerStatus.ACTIVE)
                .stream()
                .map(payer -> new PayerDropdownDTO(payer.getPayerId(), payer.getPayer()))
                .collect(Collectors.toList());
    }
}
