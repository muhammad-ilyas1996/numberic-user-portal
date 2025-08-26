package com.medicalbillinguserportal.appointment.imp;

import com.medicalbillinguserportal.appointment.dto.AppointmentDto;
import com.medicalbillinguserportal.appointment.dto.request.PatientSearch;
import com.medicalbillinguserportal.appointment.entity.AppointmentEntity;
import com.medicalbillinguserportal.appointment.mapper.AppointmentConverter;
import com.medicalbillinguserportal.appointment.repo.AppointmentRepo;
import com.medicalbillinguserportal.appointment.service.AppointmentService;
import com.medicalbillinguserportal.common.validation.DateValidation;
import com.medicalbillinguserportal.commonpersistence.dto.SearchDate;
import com.medicalbillinguserportal.commonpersistence.utils.SpecificationUtility;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepo appointmentRepo;
    @Autowired
    private DateValidation dateValidation;

    @Override
    public AppointmentDto createAppointment(AppointmentDto dto, User currentUser)
    {
        AppointmentEntity appointmentEntity= AppointmentConverter.toEntity(dto,currentUser);
        AppointmentEntity saved = appointmentRepo.save(appointmentEntity);
        return AppointmentConverter.toDTO(saved,currentUser);
    }

    @Override
    public Page<AppointmentEntity> searchAppointment(PatientSearch request) {
        SearchDate searchDate = dateValidation.validateDates(request.getFromDate(), request.getToDate());
        Specification<AppointmentEntity> spec = SpecificationUtility.equalsValue("isActive", true);

        if (request.getFromDate() != null) {
            spec = spec.and(SpecificationUtility.greaterThanOrEqualTo("createdOn", searchDate.getFromDate()));
        }
        if (request.getToDate() != null) {
            spec = spec.and(SpecificationUtility.lessThanOrEqualTo("createdOn", searchDate.getToDate()));
        }
        return appointmentRepo.findAll(spec,
                PageRequest.of(request.getPageNumber(),
                        request.getPageSize(),
                        Sort.Direction.DESC,
                        "createdOn")
        );
    }

    @Override
    public AppointmentEntity getAppointmentDetail(Long id) {
        Optional<AppointmentEntity> appointmentEntity = appointmentRepo.findByAppointmentIdAndIsActiveTrue(id);
        if (appointmentEntity.isEmpty()) {
            return new AppointmentEntity();
        } else {
            return appointmentEntity.get();
        }
    }
}
