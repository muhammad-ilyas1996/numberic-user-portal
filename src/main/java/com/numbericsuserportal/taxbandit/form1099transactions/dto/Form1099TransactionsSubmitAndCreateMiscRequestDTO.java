package com.numbericsuserportal.taxbandit.form1099transactions.dto;

import com.numbericsuserportal.taxbandit.form1099misc.dto.CreateForm1099MISCRequestDTO;
import lombok.Data;

/**
 * Bundles Form1099Transactions + Form 1099-MISC Create.
 * JSON: { "transactionsRequest": { "TxnData": [...] }, "createRequest": { ... } }
 */
@Data
public class Form1099TransactionsSubmitAndCreateMiscRequestDTO {

    private Form1099TransactionsRequestDTO transactionsRequest;

    private CreateForm1099MISCRequestDTO createRequest;
}
