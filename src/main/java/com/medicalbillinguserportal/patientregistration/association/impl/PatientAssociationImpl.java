package com.medicalbillinguserportal.patientregistration.association.impl;

import com.medicalbillinguserportal.appointment.dto.request.PatientSearch;
import com.medicalbillinguserportal.common.validation.DateValidation;
import com.medicalbillinguserportal.commonpersistence.dto.SearchDate;
import com.medicalbillinguserportal.commonpersistence.utils.SpecificationUtility;
import com.medicalbillinguserportal.patientregistration.association.dto.PatientAssociationDto;
import com.medicalbillinguserportal.patientregistration.association.entity.PatientAssociationEntity;
import com.medicalbillinguserportal.patientregistration.association.mapper.PatientAssociationConverter;
import com.medicalbillinguserportal.patientregistration.association.repository.PatientAssociationRepo;
import com.medicalbillinguserportal.patientregistration.association.service.PatientAssociationService;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.repository.PatientInfoRepository;
import com.medicalbillinguserportal.usermanagement.domain.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class PatientAssociationImpl implements PatientAssociationService {

    @Autowired
    private DateValidation dateValidation;
    @Autowired
    public PatientAssociationRepo patientAssociationRepo;

    @Autowired
    private PatientInfoRepository patientInfoRepository;


    @Override
    @Transactional
    public PatientAssociationDto saveAssociation(PatientAssociationDto dto, User currentUser) {
            PatientInfoEntity patientInfo = patientInfoRepository.findById(dto.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            PatientAssociationEntity entity = PatientAssociationConverter.toEntity(dto, patientInfo, currentUser);
            PatientAssociationEntity saved = patientAssociationRepo.save(entity);
            return PatientAssociationConverter.toDTO(saved);
    }
    @Override
    public Page<PatientAssociationDto> searchPatientAssociation(PatientSearch request) {
        SearchDate searchDate = dateValidation.validateDates(request.getFromDate(), request.getToDate());
        Specification<PatientAssociationEntity> spec = SpecificationUtility.equalsValue("isActive", true);

        if (request.getFromDate() != null) {
            spec = spec.and(SpecificationUtility.greaterThanOrEqualTo("createdOn", searchDate.getFromDate()));
        }
        if (request.getToDate() != null) {
            spec = spec.and(SpecificationUtility.lessThanOrEqualTo("createdOn", searchDate.getToDate()));
        }
        Page<PatientAssociationEntity> entityPage = patientAssociationRepo.findAll(
                spec,
                PageRequest.of(request.getPageNumber() - 1,
                        request.getPageSize(),
                        Sort.Direction.DESC,
                        "createdOn")
        );
        Page<PatientAssociationDto> dtoPage = entityPage.map(PatientAssociationConverter::toDTO);

        return dtoPage;
    }

    @Override
    public PatientAssociationDto getPatientAssociationDetail(Long id) {
        PatientAssociationEntity entity = patientAssociationRepo.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Association not found with id: " + id));
        return PatientAssociationConverter.toDTO(entity);
    }
}
