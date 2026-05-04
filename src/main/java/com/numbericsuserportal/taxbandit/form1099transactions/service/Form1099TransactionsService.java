package com.numbericsuserportal.taxbandit.form1099transactions.service;

import com.numbericsuserportal.taxbandit.form1099k.dto.CreateForm1099KResponseDTO;
import com.numbericsuserportal.taxbandit.form1099misc.dto.CreateForm1099MISCResponseDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.dto.Form1099TransactionsRequestDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.dto.Form1099TransactionsResponseDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.dto.Form1099TransactionsSubmitAndCreateMiscRequestDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.dto.Form1099TransactionsSubmitAndCreateNecRequestDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.dto.Form1099TransactionsSubmitAndCreateRequestDTO;
import com.numbericsuserportal.taxbandit.formnec.dto.CreateForm1099NECResponseDTO;

/**
 * TaxBandits Form1099Transactions + optional orchestration with Form 1099 Create (K / NEC / MISC).
 */
public interface Form1099TransactionsService {

    Form1099TransactionsResponseDTO postTransactions(Form1099TransactionsRequestDTO request);

    /**
     * Posts transactions, then Create 1099-K; sets ReturnData[i].Recipient.RecipientId (string) from
     * TxnData recipient order.
     */
    CreateForm1099KResponseDTO submitTransactionsAndCreate(Form1099TransactionsSubmitAndCreateRequestDTO bundle);

    /**
     * Same as {@link #submitTransactionsAndCreate} but Form 1099-NEC Create (RecipientId UUID).
     */
    CreateForm1099NECResponseDTO submitTransactionsAndCreateNec(Form1099TransactionsSubmitAndCreateNecRequestDTO bundle);

    /**
     * Same as {@link #submitTransactionsAndCreate} but Form 1099-MISC Create (RecipientId UUID).
     */
    CreateForm1099MISCResponseDTO submitTransactionsAndCreateMisc(Form1099TransactionsSubmitAndCreateMiscRequestDTO bundle);
}
