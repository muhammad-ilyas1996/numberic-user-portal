package com.numbericsuserportal.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One fee plan option for the onboarding UI dropdown.
 * Send {@link #feeScheduleId} back as {@code feeScheduleId} on onboarding submit.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NmiFeeScheduleOptionDto {

    /** Value for POST onboarding {@code feeScheduleId} (NMI costPlan). */
    private String feeScheduleId;
    /** Fee Schedule Manager id from NMI (display/reference). */
    private String scheduleId;
    private String name;
    private String currency;
    private Boolean inUse;
}
