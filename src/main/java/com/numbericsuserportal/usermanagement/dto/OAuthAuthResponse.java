package com.numbericsuserportal.usermanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthAuthResponse {
    private boolean success;
    private String message;
    private String token;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String provider;
    @JsonProperty("isNewUser")
    private boolean newUser;
    private String registrationStatus;
    private String accountType;
}
