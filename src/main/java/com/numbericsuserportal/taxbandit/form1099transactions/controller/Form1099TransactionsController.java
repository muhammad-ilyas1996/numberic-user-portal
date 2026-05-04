package com.numbericsuserportal.taxbandit.form1099transactions.controller;

import com.numbericsuserportal.taxbandit.exception.TaxBanditsApiException;
import com.numbericsuserportal.taxbandit.form1099k.dto.CreateForm1099KResponseDTO;
import com.numbericsuserportal.taxbandit.form1099misc.dto.CreateForm1099MISCResponseDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.dto.Form1099TransactionsRequestDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.dto.Form1099TransactionsResponseDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.dto.Form1099TransactionsSubmitAndCreateMiscRequestDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.dto.Form1099TransactionsSubmitAndCreateNecRequestDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.dto.Form1099TransactionsSubmitAndCreateRequestDTO;
import com.numbericsuserportal.taxbandit.form1099transactions.service.Form1099TransactionsService;
import com.numbericsuserportal.taxbandit.formnec.dto.CreateForm1099NECResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * TaxBandits Form1099Transactions — post txn rows, then optional submit-and-create for 1099-K / NEC / MISC.
 */
@RestController
@RequestMapping("/api/taxbandits/form1099transactions")
@CrossOrigin(origins = "*")
public class Form1099TransactionsController {

    private final Form1099TransactionsService form1099TransactionsService;

    public Form1099TransactionsController(Form1099TransactionsService form1099TransactionsService) {
        this.form1099TransactionsService = form1099TransactionsService;
    }

    /**
     * POST /api/taxbandits/form1099transactions
     * Proxies to TaxBandits POST Form1099Transactions.
     */
    @PostMapping
    public ResponseEntity<?> postTransactions(@RequestBody Form1099TransactionsRequestDTO request) {
        try {
            Form1099TransactionsResponseDTO response = form1099TransactionsService.postTransactions(request);
            return ResponseEntity.ok(response);
        } catch (TaxBanditsApiException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/taxbandits/form1099transactions/submit-and-create
     * Posts transactions, maps SuccessRecords[].RecipientId onto createRequest.returnData[i].recipient, then Create 1099-K.
     */
    @PostMapping("/submit-and-create")
    public ResponseEntity<?> submitTransactionsAndCreate(
        @RequestBody Form1099TransactionsSubmitAndCreateRequestDTO bundle) {
        try {
            CreateForm1099KResponseDTO response = form1099TransactionsService.submitTransactionsAndCreate(bundle);
            return ResponseEntity.ok(response);
        } catch (TaxBanditsApiException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/taxbandits/form1099transactions/submit-and-create-nec
     */
    @PostMapping("/submit-and-create-nec")
    public ResponseEntity<?> submitTransactionsAndCreateNec(
        @RequestBody Form1099TransactionsSubmitAndCreateNecRequestDTO bundle) {
        try {
            CreateForm1099NECResponseDTO response = form1099TransactionsService.submitTransactionsAndCreateNec(bundle);
            return ResponseEntity.ok(response);
        } catch (TaxBanditsApiException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/taxbandits/form1099transactions/submit-and-create-misc
     */
    @PostMapping("/submit-and-create-misc")
    public ResponseEntity<?> submitTransactionsAndCreateMisc(
        @RequestBody Form1099TransactionsSubmitAndCreateMiscRequestDTO bundle) {
        try {
            CreateForm1099MISCResponseDTO response = form1099TransactionsService.submitTransactionsAndCreateMisc(bundle);
            return ResponseEntity.ok(response);
        } catch (TaxBanditsApiException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
