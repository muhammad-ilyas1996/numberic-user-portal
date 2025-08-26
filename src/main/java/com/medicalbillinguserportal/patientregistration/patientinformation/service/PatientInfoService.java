package com.medicalbillinguserportal.patientregistration.patientinformation.service;

import com.medicalbillinguserportal.common.validation.DateValidation;
import com.medicalbillinguserportal.patientregistration.patientinformation.dto.PatientInfoDto;
import com.medicalbillinguserportal.patientregistration.patientinformation.dto.PatientInfoIdDto;
import com.medicalbillinguserportal.patientregistration.patientinformation.dto.search.PatientsSearch;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.mapper.PatientInfoConverter;
import com.medicalbillinguserportal.patientregistration.patientinformation.mapper.PatientInfoIdConverter;
import com.medicalbillinguserportal.patientregistration.patientinformation.respository.PatientInfoRepository;
//import com.medicalbillinguserportal.provider.dto.request.ProviderSearch;
//import com.medicalbillinguserportal.provider.entity.ProviderEntity;
import com.medicalbillinguserportal.commonpersistence.dto.SearchDate;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class PatientInfoService {
    @Autowired
    private PatientInfoRepository patientInfoRepository;

    @Autowired
    private DateValidation dateValidation;

    public PatientInfoDto savePatientInfo(PatientInfoDto dto, User currentUser)
    {
        PatientInfoEntity patientInfoEntity= PatientInfoConverter.toEntity(dto,currentUser);
        patientInfoEntity=patientInfoRepository.save(patientInfoEntity);
        return PatientInfoConverter.toDto(patientInfoEntity,currentUser);
    }
    public PatientInfoIdDto getPatientId(Long id)
    {
        return patientInfoRepository.findById(id).map(PatientInfoIdConverter::toDto).orElse(null);
    }
    public PatientInfoDto getPatientAllDataById(Long id)
    {
        return patientInfoRepository.findById(id).map(entity->PatientInfoConverter.toDto(entity,null)).orElse(null);
    }


    public Page<PatientInfoEntity> searchPatients(PatientsSearch requestDTO) {
        SearchDate searchDate = dateValidation.validateDates(requestDTO.getFromDate(), requestDTO.getToDate());
        Specification<PatientInfoEntity> spec = com.medicalbillinguserportal.commonpersistence.utils.SpecificationUtility.equalsValue("isActive", true);
        if (requestDTO.getPatientId() != null) {
            spec = spec.and(com.medicalbillinguserportal.commonpersistence.utils.SpecificationUtility.equalsValue("id", requestDTO.getPatientId()));
        }
        if (requestDTO.getFromDate() != null) {
            spec = spec.and(com.medicalbillinguserportal.commonpersistence.utils.SpecificationUtility.greaterThanOrEqualTo("createdOn", searchDate.getFromDate()));
        }
        if (requestDTO.getToDate() != null) {
            spec = spec.and(com.medicalbillinguserportal.commonpersistence.utils.SpecificationUtility.lessThanOrEqualTo("createdOn", searchDate.getToDate()));
        }
        if (requestDTO.getToDate() != null) {
            spec = spec.and(com.medicalbillinguserportal.commonpersistence.utils.SpecificationUtility.lessThanOrEqualTo("createdOn", searchDate.getToDate()));
        }
        return patientInfoRepository.findAll(spec,
                PageRequest.of(requestDTO.getPageNumber(),
                        requestDTO.getPageSize(),
                        Sort.Direction.DESC,
                        "createdOn")
        );
    }
}
