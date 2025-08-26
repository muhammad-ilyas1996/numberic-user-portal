package com.medicalbillinguserportal.patientregistration.patientinformation.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientsSearch {
    private Long patientId;
    private Integer pageNumber;
    private Integer pageSize;
    private String fromDate;
    private String toDate;
}
