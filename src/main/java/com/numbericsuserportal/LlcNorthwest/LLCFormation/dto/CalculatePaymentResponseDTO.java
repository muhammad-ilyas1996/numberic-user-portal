package com.numbericsuserportal.LlcNorthwest.LLCFormation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CalculatePaymentResponseDTO {
    private Integer totalCents;
    private List<LineItem> lineItems;

    @Data
    @AllArgsConstructor
    public static class LineItem {
        private String label;
        private Integer cents;
    }
}

