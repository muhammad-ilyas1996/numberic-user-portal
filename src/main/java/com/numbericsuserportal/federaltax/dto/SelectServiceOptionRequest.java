package com.numbericsuserportal.federaltax.dto;

import com.numbericsuserportal.federaltax.domain.TaxServiceOption;
import lombok.Data;

@Data
public class SelectServiceOptionRequest {
    private TaxServiceOption serviceOption;
}
