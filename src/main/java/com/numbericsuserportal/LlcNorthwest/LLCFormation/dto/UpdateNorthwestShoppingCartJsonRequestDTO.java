package com.numbericsuserportal.LlcNorthwest.LLCFormation.dto;

import lombok.Data;

@Data
public class UpdateNorthwestShoppingCartJsonRequestDTO {
    /**
     * Raw JSON string for Corporate Tools POST /shopping-cart (body as built by the client from filing-methods/schemas).
     */
    private String northwestShoppingCartJson;
}
