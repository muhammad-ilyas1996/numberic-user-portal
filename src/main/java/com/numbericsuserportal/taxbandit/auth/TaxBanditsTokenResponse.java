package com.numbericsuserportal.taxbandit.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TaxBanditsTokenResponse {
    @JsonProperty("StatusCode")
    private Integer statusCode;

    @JsonProperty("StatusName")
    private String statusName;

    @JsonProperty("StatusMessage")
    private String statusMessage;

    @JsonProperty("AccessToken")
    private String accessToken;

    @JsonProperty("TokenType")
    private String tokenType;

    @JsonProperty("ExpiresIn")
    private Integer expiresIn;

    @JsonProperty("Errors")
    private Object errors;
}

