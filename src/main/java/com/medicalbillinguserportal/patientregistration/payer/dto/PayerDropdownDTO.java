package com.medicalbillinguserportal.patientregistration.payer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayerDropdownDTO {
    private Long payerId;
    private String payer;
}
