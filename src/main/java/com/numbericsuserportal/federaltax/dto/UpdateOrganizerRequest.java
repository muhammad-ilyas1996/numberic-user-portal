package com.numbericsuserportal.federaltax.dto;

import lombok.Data;

import java.util.Map;

@Data
public class UpdateOrganizerRequest {
    private Map<String, Object> organizer;
    private Boolean markComplete;
}
