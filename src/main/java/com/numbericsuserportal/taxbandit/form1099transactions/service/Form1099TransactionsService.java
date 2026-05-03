package com.numbericsuserportal.taxbandit.form1099transactions.service;

import com.numbericsuserportal.taxbandit.form1099k.dto.CreateForm1099KResponseDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.dto.Form1099TransactionsRequestDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.dto.Form1099TransactionsResponseDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.dto.Form1099TransactionsSubmitAndCreateRequestDTO;

/**
 * TaxBandits Form1099Transactions + optional orchestration with Form 1099-K Create.
 */
public interface Form1099TransactionsService {

    Form1099TransactionsResponseDTO postTransactions(Form1099TransactionsRequestDTO request);

    /**
     * Posts transactions, then Create 1099-K, setting each ReturnData.Recipient.RecipientId from the
     * matching SuccessRecords[].RecipientId (by row index) when present.
     */
    CreateForm1099KResponseDTO submitTransactionsAndCreate(Form1099TransactionsSubmitAndCreateRequestDTO bundle);
}
