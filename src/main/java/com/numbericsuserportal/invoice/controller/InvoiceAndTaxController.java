package com.numbericsuserportal.invoice.controller;

import com.numbericsuserportal.invoice.converter.InvoiceAndTaxConverter;
import com.numbericsuserportal.invoice.dto.InvoiceAndTaxDTO;
import com.numbericsuserportal.invoice.dto.InvoiceAndTaxRequestDto;
import com.numbericsuserportal.invoice.dto.InvoiceSearch;
import com.numbericsuserportal.invoice.dto.InvoicePayByTokenDto;
import com.numbericsuserportal.invoice.dto.InvoicePayWithTokenRequestDto;
import com.numbericsuserportal.invoice.dto.InvoiceSendHistorySearch;
import com.numbericsuserportal.invoice.dto.PaymentTransactionSearch;
import com.numbericsuserportal.invoice.dto.SendInvoiceRequestDto;
import com.numbericsuserportal.invoice.dto.SendInvoiceResponseDto;
import com.numbericsuserportal.invoice.dto.UpdateInvoiceStatusRequestDto;
import com.numbericsuserportal.invoice.entity.InvoiceAndTaxEntity;
import com.numbericsuserportal.invoice.service.InvoiceAndTaxService;
import com.numbericsuserportal.invoice.service.InvoicePdfService;
import com.numbericsuserportal.invoice.service.InvoiceSendService;
import com.numbericsuserportal.invoice.service.PaymentTransactionService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/v1/invoice")
public class InvoiceAndTaxController {

    @Autowired
    private InvoiceAndTaxService invoiceAndTaxService;
    @Autowired
    private InvoiceSendService invoiceSendService;
    @Autowired
    private PaymentTransactionService paymentTransactionService;
    @Autowired
    private InvoicePdfService invoicePdfService;

    @PostMapping("/create-invoice")
    public ResponseEntity<InvoiceAndTaxDTO> saveInvoice(@RequestBody InvoiceAndTaxDTO dto, @AuthenticationPrincipal User currentUser) {
        InvoiceAndTaxDTO savedInvoice = invoiceAndTaxService.createInvoiceAndTax(dto,currentUser);
        return ResponseEntity.ok(savedInvoice);
    }


    @PostMapping("/list")
    //  @PreAuthorize("hasAuthority('VIEW_PATIENT')")
    public ResponseEntity<Page<InvoiceAndTaxDTO>> getProviderList(@RequestBody InvoiceSearch requestDTO, @AuthenticationPrincipal User currentUser) {
        Page<InvoiceAndTaxDTO> invoiceAndTaxDTOS = invoiceAndTaxService.searchInvoice(requestDTO, currentUser).map(entity -> InvoiceAndTaxConverter.toDTO(entity, currentUser));
        return ResponseEntity.ok(invoiceAndTaxDTOS);
    }

    /** POST with body {"id": <invoiceId>}. Returns 404 with message if invoice not found. */
    @PostMapping("/view-detail")
    // @PreAuthorize("hasAuthority('VIEW_PROVIDER')")
    public ResponseEntity<?> getInvoiceData(@RequestBody InvoiceAndTaxRequestDto requestDTO, @AuthenticationPrincipal User currentUser) {
        if (requestDTO.getId() == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Request body must include {\"id\": <invoiceId>}"));
        }
        InvoiceAndTaxEntity entity = invoiceAndTaxService.getInvoiceDetail(requestDTO.getId(), currentUser);
        if (entity.getId() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("error", "Invoice not found for id: " + requestDTO.getId()));
        }
        InvoiceAndTaxDTO dto = InvoiceAndTaxConverter.toDTO(entity, currentUser);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/view-detail-by-customer-name")
    // @PreAuthorize("hasAuthority('VIEW_PROVIDER')")
    public ResponseEntity<InvoiceAndTaxDTO> getInvoiceDataByCustomerName(@RequestBody InvoiceAndTaxRequestDto requestDTO,@AuthenticationPrincipal User currentUser) {
        InvoiceAndTaxEntity entity = invoiceAndTaxService.getInvoiceDetailByCustomerName(requestDTO.getCustomerName(), currentUser);

        // Convert entity → DTO (safe for JSON)
        InvoiceAndTaxDTO dto = InvoiceAndTaxConverter.toDTO(entity, currentUser);
        return ResponseEntity.ok(dto);
    }
    @PostMapping("/view-detail-by-invoice-num")
    public ResponseEntity<InvoiceAndTaxDTO> getInvoiceDataByInvoiceNum(@RequestBody InvoiceAndTaxRequestDto requestDTO,@AuthenticationPrincipal User currentUser) {
        InvoiceAndTaxEntity entity = invoiceAndTaxService.getInvoiceDetailByInvoiceNumber(requestDTO.getInvoiceNum(), currentUser);

        // Convert entity → DTO (safe for JSON)
        InvoiceAndTaxDTO dto = InvoiceAndTaxConverter.toDTO(entity, currentUser);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/update-invoice")
    public ResponseEntity<?> updateInvoice(@RequestBody InvoiceAndTaxDTO dto, @AuthenticationPrincipal User currentUser) {
        try {
            if (dto.getId() == null) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "Invoice ID is required for update"));
            }
            InvoiceAndTaxDTO updatedInvoice = invoiceAndTaxService.updateInvoice(dto.getId(), dto, currentUser);
            return ResponseEntity.ok(updatedInvoice);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(java.util.Map.of("error", "Failed to update invoice: " + e.getMessage()));
        }
    }

    /** Update only invoice status (listing UI). Body: {"id": 1, "invoiceStatus": "SENT"} */
    @PostMapping("/update-status")
    public ResponseEntity<?> updateInvoiceStatus(
            @RequestBody UpdateInvoiceStatusRequestDto request,
            @AuthenticationPrincipal User currentUser) {
        try {
            if (request == null || request.getId() == null) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "Invoice ID is required"));
            }
            InvoiceAndTaxDTO updated = invoiceAndTaxService.updateInvoiceStatus(
                    request.getId(), request.getInvoiceStatus(), currentUser);
            return ResponseEntity.ok(updated);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(java.util.Map.of("error", "Failed to update invoice status: " + e.getMessage()));
        }
    }

    @PostMapping("/delete-invoice")
    public ResponseEntity<?> deleteInvoice(@RequestBody InvoiceAndTaxRequestDto requestDTO, @AuthenticationPrincipal User currentUser) {
        try {
            if (requestDTO.getId() == null) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "Invoice ID is required for delete"));
            }
            invoiceAndTaxService.deleteInvoice(requestDTO.getId(), currentUser);
            return ResponseEntity.ok(java.util.Map.of("message", "Invoice deleted successfully"));
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(java.util.Map.of("error", "Failed to delete invoice: " + e.getMessage()));
        }
    }

    /** Send invoice via WhatsApp (or email when implemented). */
    @PostMapping("/send")
    public ResponseEntity<SendInvoiceResponseDto> sendInvoice(
            @RequestBody SendInvoiceRequestDto request,
            @AuthenticationPrincipal User currentUser) {
        SendInvoiceResponseDto response = invoiceSendService.sendInvoice(request, currentUser);
        return response.isSuccess()
            ? ResponseEntity.ok(response)
            : ResponseEntity.badRequest().body(response);
    }

    /** Get send history for an invoice (paginated). */
    @PostMapping("/send-history")
    public ResponseEntity<Page<com.numbericsuserportal.invoice.dto.InvoiceSendHistoryItemDto>> getSendHistory(
            @RequestBody InvoiceSendHistorySearch search,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(invoiceSendService.getSendHistory(search, currentUser));
    }

    /** Public: get invoice summary by payment link token (for pay page). No auth required. */
    @GetMapping("/pay-by-token")
    public ResponseEntity<InvoicePayByTokenDto> getInvoiceByToken(@RequestParam String token) {
        return ResponseEntity.ok(invoiceSendService.getInvoiceByToken(token));
    }

    /** Public: pay invoice using token (NMI). No auth required. */
    @PostMapping("/pay-with-token")
    public ResponseEntity<java.util.Map<String, Object>> payWithToken(
            @RequestBody InvoicePayWithTokenRequestDto request) {
        return ResponseEntity.ok(invoiceSendService.payWithToken(request));
    }

    /** Payments & transactions list (paginated). Filter by invoiceId, status, fromDate, toDate. */
    @PostMapping("/transactions/list")
    public ResponseEntity<Page<com.numbericsuserportal.invoice.dto.PaymentTransactionDto>> getTransactionsList(
            @RequestBody PaymentTransactionSearch search,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(paymentTransactionService.list(search, currentUser));
    }

    /** Download invoice as PDF. */
    @PostMapping("/download-pdf")
    public ResponseEntity<?> downloadInvoicePdf(
            @RequestBody InvoiceAndTaxRequestDto requestDTO,
            @AuthenticationPrincipal User currentUser) {
        if (requestDTO.getId() == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Invoice ID is required"));
        }
        invoiceAndTaxService.requireAccessibleInvoice(requestDTO.getId(), currentUser);
        byte[] pdf = invoicePdfService.generatePdf(requestDTO.getId());
        if (pdf == null) {
            return ResponseEntity.notFound().build();
        }
        String filename = "invoice-" + (requestDTO.getId()) + ".pdf";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(pdf.length)
                .body(new ByteArrayResource(pdf));
    }

}
