package com.numbericsuserportal.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDto {

    private boolean success;
    private String reply;
    private String error;
    private String model;
    /** Set when using Claude Managed Agents ({@code sesn_...}). */
    private String anthropicSessionId;
}
