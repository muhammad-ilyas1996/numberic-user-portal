package com.numbericsuserportal.taxbandit.form1099transactions.dto;

import com.numbericsuserportal.taxbandit.formnec.dto.CreateForm1099NECRequestDTO;
import lombok.Data;

/**
 * Bundles Form1099Transactions + Form 1099-NEC Create.
 * JSON: { "transactionsRequest": { "TxnData": [...] }, "createRequest": { ... } }
 */
@Data
public class Form1099TransactionsSubmitAndCreateNecRequestDTO {

    private Form1099TransactionsRequestDTO transactionsRequest;

    private CreateForm1099NECRequestDTO createRequest;
}
