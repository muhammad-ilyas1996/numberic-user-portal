package com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.corporatetools;

import lombok.Data;

import java.util.List;

@Data
public class NameCheckResponseDTO {
    private Boolean success;
    private String timestamp;
    private Result result;

    @Data
    public static class Result {
        private Boolean available;
        private List<String> suggestions;
        private String message;
    }
}

