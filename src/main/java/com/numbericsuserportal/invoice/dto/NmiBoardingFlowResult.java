package com.numbericsuserportal.invoice.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class NmiBoardingFlowResult {

    private boolean success;
    private String gatewayId;
    private String status;
    private String securityKey;
    private String transactionUrl;
    private String errorMessage;
    private String lastCompletedStep;
    private List<String> stepsCompleted = new ArrayList<>();
    private Map<String, Object> responses = new LinkedHashMap<>();

    public void addStep(String step) {
        stepsCompleted.add(step);
        lastCompletedStep = step;
    }

    public void putResponse(String key, Map<String, Object> value) {
        if (value != null) {
            responses.put(key, value);
        }
    }
}
