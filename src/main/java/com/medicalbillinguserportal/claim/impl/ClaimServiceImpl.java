package com.medicalbillinguserportal.claim.impl;

import com.medicalbillinguserportal.claim.converter.ClaimConverter;
import com.medicalbillinguserportal.claim.dto.ClaimDTO;
import com.medicalbillinguserportal.claim.dto.ClaimFullResponseDto;
import com.medicalbillinguserportal.claim.entity.ClaimEntity;
import com.medicalbillinguserportal.claim.repo.ClaimRepository;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.repository.PatientInfoRepository;
import com.medicalbillinguserportal.claim.service.ClaimService;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.stereotype.Service;

@Service
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final PatientInfoRepository patientRepo;
    private final ClaimConverter converter;

    public ClaimServiceImpl(ClaimRepository claimRepository, PatientInfoRepository patientRepo, ClaimConverter converter) {
        this.claimRepository = claimRepository;
        this.patientRepo = patientRepo;
        this.converter = converter;
    }

    @Override
    public ClaimDTO createClaim(ClaimDTO dto, User currentUser) {

        PatientInfoEntity patient = patientRepo.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        ClaimEntity entity = converter.toEntity(dto, patient,currentUser);
        ClaimEntity saved = claimRepository.save(entity);

        dto.setClaimId(saved.getClaimId());
        return dto;
    }

    @Override
    public ClaimFullResponseDto getClaimById(Long claimId) {
        return claimRepository.findById(claimId)
                .map(entity -> converter.toFullDto(entity, null))
                .orElse(null);
    }
}
