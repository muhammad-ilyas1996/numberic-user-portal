package com.numbericsuserportal.taxbandit.form1099transactions.service.impl;

import com.numbericsuserportal.taxbandit.exception.TaxBanditsApiException;
import com.numbericsuserportal.taxbandit.form1099k.dto.CreateForm1099KRequestDTO;
import com.numbericsuserportal.taxbandit.form1099k.dto.CreateForm1099KResponseDTO;
import com.numbericsuserportal.taxbandit.form1099misc.dto.CreateForm1099MISCRequestDTO;
import com.numbericsuserportal.taxbandit.form1099misc.dto.CreateForm1099MISCResponseDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.dto.Form1099TransactionsRequestDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.dto.Form1099TransactionsResponseDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.dto.Form1099TransactionsSubmitAndCreateMiscRequestDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.dto.Form1099TransactionsSubmitAndCreateNecRequestDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.dto.Form1099TransactionsSubmitAndCreateRequestDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.service.Form1099TransactionsService;
import com.numbericsuserportal.taxbandit.formnec.dto.CreateForm1099NECRequestDTO;
import com.numbericsuserportal.taxbandit.formnec.dto.CreateForm1099NECResponseDTO;
import com.numbericsuserportal.taxbandit.service.TaxBanditsApiService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        taxBanditsApiService.postForm1099Transactions(bundle.getTransactionsRequest());

        CreateForm1099KRequestDTO create = bundle.getCreateRequest();
        if (create.getReturnData() == null || create.getReturnData().isEmpty()) {
            throw new IllegalArgumentException("createRequest.returnData is required");
        }

        List<UUID> recipientIds = extractRecipientIdsFromTxnRequest(bundle.getTransactionsRequest());
        if (recipientIds.isEmpty()) {
            throw new TaxBanditsApiException(
                "transactionsRequest.TxnData must include Recipients with RecipientId to align Form 1099-K Create.",
                HttpStatus.BAD_REQUEST);
        }

        List<CreateForm1099KRequestDTO.ReturnDataDTO> rows = create.getReturnData();
        int n = Math.min(rows.size(), recipientIds.size());
        for (int i = 0; i < n; i++) {
            UUID recipientId = recipientIds.get(i);
            CreateForm1099KRequestDTO.ReturnDataDTO row = rows.get(i);
            if (row.getRecipient() == null) {
                row.setRecipient(new CreateForm1099KRequestDTO.RecipientDTO());
            }
            row.getRecipient().setRecipientId(recipientId.toString());
        }

        return taxBanditsApiService.createForm1099K(create);
    }

    @Override
    public CreateForm1099NECResponseDTO submitTransactionsAndCreateNec(Form1099TransactionsSubmitAndCreateNecRequestDTO bundle) {
        if (bundle.getTransactionsRequest() == null || bundle.getCreateRequest() == null) {
            throw new IllegalArgumentException("transactionsRequest and createRequest are required");
        }
        taxBanditsApiService.postForm1099Transactions(bundle.getTransactionsRequest());
        CreateForm1099NECRequestDTO create = bundle.getCreateRequest();
        if (create.getReturnData() == null || create.getReturnData().isEmpty()) {
            throw new IllegalArgumentException("createRequest.returnData is required");
        }
        List<UUID> recipientIds = extractRecipientIdsFromTxnRequest(bundle.getTransactionsRequest());
        if (recipientIds.isEmpty()) {
            throw new TaxBanditsApiException(
                "transactionsRequest.TxnData must include Recipients with RecipientId to align Form 1099-NEC Create.",
                HttpStatus.BAD_REQUEST);
        }
        applyRecipientIdsToNec(create, recipientIds);
        return taxBanditsApiService.createForm1099NEC(create);
    }

    @Override
    public CreateForm1099MISCResponseDTO submitTransactionsAndCreateMisc(Form1099TransactionsSubmitAndCreateMiscRequestDTO bundle) {
        if (bundle.getTransactionsRequest() == null || bundle.getCreateRequest() == null) {
            throw new IllegalArgumentException("transactionsRequest and createRequest are required");
        }
        taxBanditsApiService.postForm1099Transactions(bundle.getTransactionsRequest());
        CreateForm1099MISCRequestDTO create = bundle.getCreateRequest();
        if (create.getReturnData() == null || create.getReturnData().isEmpty()) {
            throw new IllegalArgumentException("createRequest.returnData is required");
        }
        List<UUID> recipientIds = extractRecipientIdsFromTxnRequest(bundle.getTransactionsRequest());
        if (recipientIds.isEmpty()) {
            throw new TaxBanditsApiException(
                "transactionsRequest.TxnData must include Recipients with RecipientId to align Form 1099-MISC Create.",
                HttpStatus.BAD_REQUEST);
        }
        applyRecipientIdsToMisc(create, recipientIds);
        return taxBanditsApiService.createForm1099MISC(create);
    }

    private static void applyRecipientIdsToNec(CreateForm1099NECRequestDTO create, List<UUID> recipientIds) {
        List<CreateForm1099NECRequestDTO.ReturnDataDTO> rows = create.getReturnData();
        int n = Math.min(rows.size(), recipientIds.size());
        for (int i = 0; i < n; i++) {
            UUID recipientId = recipientIds.get(i);
            CreateForm1099NECRequestDTO.ReturnDataDTO row = rows.get(i);
            if (row.getRecipient() == null) {
                row.setRecipient(new CreateForm1099NECRequestDTO.RecipientDTO());
            }
            row.getRecipient().setRecipientId(recipientId);
        }
    }

    private static void applyRecipientIdsToMisc(CreateForm1099MISCRequestDTO create, List<UUID> recipientIds) {
        List<CreateForm1099MISCRequestDTO.ReturnDataDTO> rows = create.getReturnData();
        int n = Math.min(rows.size(), recipientIds.size());
        for (int i = 0; i < n; i++) {
            UUID recipientId = recipientIds.get(i);
            CreateForm1099MISCRequestDTO.ReturnDataDTO row = rows.get(i);
            if (row.getRecipient() == null) {
                row.setRecipient(new CreateForm1099MISCRequestDTO.RecipientDTO());
            }
            row.getRecipient().setRecipientId(recipientId);
        }
    }

    /**
     * Collects RecipientIds in order: each TxnData block's recipients, in array order.
     */
    static List<UUID> extractRecipientIdsFromTxnRequest(Form1099TransactionsRequestDTO req) {
        List<UUID> out = new ArrayList<>();
        if (req.getTxnData() == null) {
            return out;
        }
        for (Form1099TransactionsRequestDTO.TxnDataBlockDTO block : req.getTxnData()) {
            if (block.getRecipients() == null) {
                continue;
            }
            for (Form1099TransactionsRequestDTO.RecipientTxnsDTO r : block.getRecipients()) {
                if (r.getRecipientId() != null) {
                    out.add(r.getRecipientId());
                }
            }
        }
        return out;
    }
}
