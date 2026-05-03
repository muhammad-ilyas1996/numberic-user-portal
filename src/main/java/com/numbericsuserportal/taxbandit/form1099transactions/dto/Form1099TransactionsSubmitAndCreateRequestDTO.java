package com.numbericsuserportal.taxbandit.form1099transactions.dto;

import com.numbericsuserportal.taxbandit.form1099k.dto.CreateForm1099KRequestDTO;
import lombok.Data;

/**
 * Bundles TaxBandits transaction post + Form 1099-K Create for a single backend-orchestrated call.
 * JSON: { "transactionsRequest": {...}, "createRequest": {...} }
 */
@Data
public class Form1099TransactionsSubmitAndCreateRequestDTO {

    private Form1099TransactionsRequestDTO transactionsRequest;

    private CreateForm1099KRequestDTO createRequest;
}
