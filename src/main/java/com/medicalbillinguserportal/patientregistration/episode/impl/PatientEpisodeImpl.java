package com.medicalbillinguserportal.patientregistration.episode.impl;

import com.medicalbillinguserportal.appointment.dto.request.PatientSearch;
import com.medicalbillinguserportal.common.validation.DateValidation;
import com.medicalbillinguserportal.commonpersistence.dto.SearchDate;
import com.medicalbillinguserportal.commonpersistence.utils.SpecificationUtility;
import com.medicalbillinguserportal.patientregistration.episode.dto.PatientEpisodeDto;
import com.medicalbillinguserportal.patientregistration.episode.entity.PatientEpisodeEntity;
import com.medicalbillinguserportal.patientregistration.episode.mapper.PatientEpisodeConverter;
import com.medicalbillinguserportal.patientregistration.episode.repo.PatientEpisodeRepo;
import com.medicalbillinguserportal.patientregistration.episode.service.PatientEpisodeService;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.usermanagement.domain.User;
import com.medicalbillinguserportal.patientregistration.patientinformation.respository.PatientInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class PatientEpisodeImpl implements PatientEpisodeService {

    @Autowired
    private PatientInfoRepository patientInfoRepository;

    @Autowired
    private DateValidation dateValidation;
    @Autowired
    public PatientEpisodeRepo patientEpisodeRepo;

    @Override
    public PatientEpisodeDto savePatientEpisode(PatientEpisodeDto dto, User currentUser)
    {
        PatientInfoEntity patientInfoEntity=patientInfoRepository.findById(dto.getPatientId())
                .orElseThrow(()->new RuntimeException("Patient Id Not Found"));
        PatientEpisodeEntity patientEpisodeEntity = PatientEpisodeConverter.toEntity(dto,patientInfoEntity,currentUser);
        PatientEpisodeDto patientEpisodeDto= PatientEpisodeConverter.toDTO(patientEpisodeRepo.save(patientEpisodeEntity));
        return patientEpisodeDto;
    }
    @Override
    public Page<PatientEpisodeDto> searchPatientEpisode(PatientSearch request) {
        SearchDate searchDate = dateValidation.validateDates(request.getFromDate(), request.getToDate());
        Specification<PatientEpisodeEntity> spec = SpecificationUtility.equalsValue("isActive", true);

        if (request.getFromDate() != null) {
            spec = spec.and(SpecificationUtility.greaterThanOrEqualTo("createdOn", searchDate.getFromDate()));
        }
        if (request.getToDate() != null) {
            spec = spec.and(SpecificationUtility.lessThanOrEqualTo("createdOn", searchDate.getToDate()));
        }
        Page<PatientEpisodeEntity> entityPage = patientEpisodeRepo.findAll(
                spec,
                PageRequest.of(request.getPageNumber() - 1,
                        request.getPageSize(),
                        Sort.Direction.DESC,
                        "createdOn")
        );
        Page<PatientEpisodeDto> dtoPage = entityPage.map(PatientEpisodeConverter::toDTO);

        return dtoPage;
    }

    @Override
    public PatientEpisodeDto getPatientEpisodeDetail(Long id) {
        PatientEpisodeEntity entity = patientEpisodeRepo.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Episode not found with id: " + id));
        return PatientEpisodeConverter.toDTO(entity);
    }
}
