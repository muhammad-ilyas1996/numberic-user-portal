package com.numbericsuserportal.invoice.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NmiFeeScheduleListDto {

    private boolean nmiConfigured;
    private String message;
    private List<NmiFeeScheduleOptionDto> plans = new ArrayList<>();
}
