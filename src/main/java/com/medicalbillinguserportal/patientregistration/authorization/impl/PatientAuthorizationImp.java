package com.medicalbillinguserportal.patientregistration.authorization.impl;

import com.medicalbillinguserportal.appointment.dto.request.PatientSearch;
import com.medicalbillinguserportal.appointment.entity.AppointmentEntity;
import com.medicalbillinguserportal.common.validation.DateValidation;
import com.medicalbillinguserportal.commonpersistence.dto.SearchDate;
import com.medicalbillinguserportal.commonpersistence.utils.SpecificationUtility;
import com.medicalbillinguserportal.patientregistration.authorization.dto.PatientAuthorizationDto;
import com.medicalbillinguserportal.patientregistration.authorization.entity.PatientAuthorizationEntity;
import com.medicalbillinguserportal.patientregistration.authorization.mapper.PatientAuthorizationConverter;
import com.medicalbillinguserportal.patientregistration.authorization.repo.PatientAuthorizationRepo;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.medicalbillinguserportal.patientregistration.authorization.service.PatientAuthorizationService;

import java.util.Optional;

@Service
public class PatientAuthorizationImp implements PatientAuthorizationService {

    @Autowired
    private DateValidation dateValidation;
    @Autowired
    public PatientAuthorizationRepo patientAuthorizationRepo;

    @Autowired
    public com.medicalbillinguserportal.patientregistration.patientinformation.respository.PatientInfoRepository patientInfoRepository;

    @Override
    public PatientAuthorizationDto savePatientAuthorization(PatientAuthorizationDto dto, User currentUser)
    {
        PatientInfoEntity patientInfoEntity=patientInfoRepository.findById(dto.getPatientId())
                .orElseThrow(()->new RuntimeException("Patient Id Not Found"));
        PatientAuthorizationEntity patientAuthorizationEntity = PatientAuthorizationConverter.toEntity(dto,patientInfoEntity,currentUser);
        PatientAuthorizationDto patientAuthorizationDto= PatientAuthorizationConverter.toDTO(patientAuthorizationRepo.save(patientAuthorizationEntity));
        return patientAuthorizationDto;
    }
    @Override
    public Page<PatientAuthorizationDto> searchPatientAuthorization(PatientSearch request) {
        SearchDate searchDate = dateValidation.validateDates(request.getFromDate(), request.getToDate());
        Specification<PatientAuthorizationEntity> spec = SpecificationUtility.equalsValue("isActive", true);

        if (request.getFromDate() != null) {
            spec = spec.and(SpecificationUtility.greaterThanOrEqualTo("createdOn", searchDate.getFromDate()));
        }
        if (request.getToDate() != null) {
            spec = spec.and(SpecificationUtility.lessThanOrEqualTo("createdOn", searchDate.getToDate()));
        }
        Page<PatientAuthorizationEntity> entityPage = patientAuthorizationRepo.findAll(
                spec,
                PageRequest.of(request.getPageNumber() - 1,
                        request.getPageSize(),
                        Sort.Direction.DESC,
                        "createdOn")
        );
        Page<PatientAuthorizationDto> dtoPage = entityPage.map(PatientAuthorizationConverter::toDTO);

        return dtoPage;
    }

    @Override
    public PatientAuthorizationDto getPatientAuthorizationtDetail(Long id) {
        PatientAuthorizationEntity entity = patientAuthorizationRepo.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Authorization not found with id: " + id));
        return PatientAuthorizationConverter.toDTO(entity);
    }
}
