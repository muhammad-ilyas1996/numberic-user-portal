package com.medicalbillinguserportal.appointment.dto.request;

import lombok.Data;

@Data
public class PatientSearch {
    private Integer pageNumber;
    private Integer pageSize;
    private String fromDate;
    private String toDate;
}
