package com.numbericsuserportal.taxbandit.form1099transactions.service.impl;

import com.numbericsuserportal.taxbandit.exception.TaxBanditsApiException;
import com.numbericsuserportal.taxbandit.form1099k.dto.CreateForm1099KRequestDTO;
import com.numbericsuserportal.taxbandit.form1099k.dto.CreateForm1099KResponseDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.dto.Form1099TransactionsRequestDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.dto.Form1099TransactionsResponseDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.dto.Form1099TransactionsSubmitAndCreateRequestDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.service.Form1099TransactionsService;
import com.numbericsuserportal.taxbandit.service.TaxBanditsApiService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class Form1099TransactionsServiceImpl implements Form1099TransactionsService {

    private final TaxBanditsApiService taxBanditsApiService;

    public Form1099TransactionsServiceImpl(TaxBanditsApiService taxBanditsApiService) {
        this.taxBanditsApiService = taxBanditsApiService;
    }

    @Override
    public Form1099TransactionsResponseDTO postTransactions(Form1099TransactionsRequestDTO request) {
        return taxBanditsApiService.postForm1099Transactions(request);
    }

    @Override
    public CreateForm1099KResponseDTO submitTransactionsAndCreate(Form1099TransactionsSubmitAndCreateRequestDTO bundle) {
        if (bundle.getTransactionsRequest() == null || bundle.getCreateRequest() == null) {
            throw new IllegalArgumentException("transactionsRequest and createRequest are required");
        }
        Form1099TransactionsResponseDTO txnResp = taxBanditsApiService.postForm1099Transactions(bundle.getTransactionsRequest());

        List<Form1099TransactionsResponseDTO.SuccessRecordDTO> successes = null;
        if (txnResp.getForm1099TransactionsRecords() != null) {
            successes = txnResp.getForm1099TransactionsRecords().getSuccessRecords();
        }
        if (successes == null || successes.isEmpty()) {
            throw new TaxBanditsApiException(
                "Form1099Transactions returned no SuccessRecords; cannot set RecipientId for Form 1099-K Create.",
                HttpStatus.BAD_REQUEST);
        }

        CreateForm1099KRequestDTO create = bundle.getCreateRequest();
        if (create.getReturnData() == null || create.getReturnData().isEmpty()) {
            throw new IllegalArgumentException("createRequest.returnData is required");
        }

        List<CreateForm1099KRequestDTO.ReturnDataDTO> rows = create.getReturnData();
        int n = Math.min(rows.size(), successes.size());
        for (int i = 0; i < n; i++) {
            UUID recipientId = successes.get(i).getRecipientId();
            if (recipientId == null) {
                continue;
            }
            CreateForm1099KRequestDTO.ReturnDataDTO row = rows.get(i);
            if (row.getRecipient() == null) {
                row.setRecipient(new CreateForm1099KRequestDTO.RecipientDTO());
            }
            row.getRecipient().setRecipientId(recipientId.toString());
        }

        return taxBanditsApiService.createForm1099K(create);
    }
}
