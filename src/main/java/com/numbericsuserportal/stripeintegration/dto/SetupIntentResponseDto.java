package com.numbericsuserportal.stripeintegration.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SetupIntentResponseDto {
    private String clientSecret;
}