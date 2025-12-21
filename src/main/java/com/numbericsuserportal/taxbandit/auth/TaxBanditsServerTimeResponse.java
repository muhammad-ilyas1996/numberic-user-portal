package com.numbericsuserportal.taxbandit.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TaxBanditsServerTimeResponse {
    @JsonProperty("StatusCode")
    private Integer statusCode;

    @JsonProperty("StatusName")
    private String statusName;

    @JsonProperty("StatusMessage")
    private String statusMessage;

    @JsonProperty("ServerTime")
    private String serverTime;

    @JsonProperty("ServerDate")
    private String serverDate;

    @JsonProperty("Timezone")
    private String timezone;

    @JsonProperty("UnixTimestamp")
    private Long unixTimestamp;

    @JsonProperty("Errors")
    private Object errors;
}

