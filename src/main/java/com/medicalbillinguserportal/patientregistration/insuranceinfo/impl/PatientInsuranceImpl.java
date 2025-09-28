package com.medicalbillinguserportal.patientregistration.insuranceinfo.impl;

import com.medicalbillinguserportal.appointment.dto.request.PatientSearch;
import com.medicalbillinguserportal.common.validation.DateValidation;
import com.medicalbillinguserportal.commonpersistence.dto.SearchDate;
import com.medicalbillinguserportal.commonpersistence.utils.SpecificationUtility;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.dto.PatientInsuranceDto;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.entity.PatientInsuranceEntity;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.mapper.PatientInsuranceConverter;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.service.PatientInsuranceService;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.repo.PatientInsuranceRepo;
import com.medicalbillinguserportal.patientregistration.patientinformation.repository.PatientInfoRepository;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class PatientInsuranceImpl implements PatientInsuranceService {

    @Autowired
    private DateValidation dateValidation;
    @Autowired
    public PatientInsuranceRepo patientInsuranceRepo;
    @Autowired
    public PatientInfoRepository patientInfoRepository;
    @Override
    public PatientInsuranceDto savePatientInsurance(PatientInsuranceDto dto, User currentUser)
    {
        PatientInfoEntity patientInfoEntity =patientInfoRepository.findById(dto.getPatientId())
                .orElseThrow(()->new RuntimeException("Patient Not Found"));
        PatientInsuranceEntity patientInsuranceEntity= PatientInsuranceConverter.toEntity(dto,patientInfoEntity,currentUser);
        PatientInsuranceDto patientInsuranceDto=PatientInsuranceConverter.toDto(patientInsuranceRepo.save(patientInsuranceEntity));
        return patientInsuranceDto;
    }

    @Override
    public Page<PatientInsuranceDto> searchPatientInsurance(PatientSearch request) {
        SearchDate searchDate = dateValidation.validateDates(request.getFromDate(), request.getToDate());
        Specification<PatientInsuranceEntity> spec = SpecificationUtility.equalsValue("isActive", true);
        if (request.getFromDate() != null) {
            spec = spec.and(SpecificationUtility.greaterThanOrEqualTo("createdOn", searchDate.getFromDate()));
        }
        if (request.getToDate() != null) {
            spec = spec.and(SpecificationUtility.lessThanOrEqualTo("createdOn", searchDate.getToDate()));
        }
        Page<PatientInsuranceEntity> entityPage = patientInsuranceRepo.findAll(
                spec,
                PageRequest.of(request.getPageNumber() - 1,
                        request.getPageSize(),
                        Sort.Direction.DESC,
                        "createdOn")
        );
        Page<PatientInsuranceDto> dtoPage = entityPage.map(PatientInsuranceConverter::toDto);
        return dtoPage;
    }

    @Override
    public PatientInsuranceDto getPatientInsuranceDetail(Long id) {
        PatientInsuranceEntity entity = patientInsuranceRepo.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Authorization not found with id: " + id));
        return PatientInsuranceConverter.toDto(entity);
    }
}
