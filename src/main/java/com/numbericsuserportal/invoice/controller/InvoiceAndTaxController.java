package com.numbericsuserportal.invoice.controller;

import com.numbericsuserportal.invoice.dto.InvoiceAndTaxDTO;
import com.numbericsuserportal.invoice.dto.InvoiceAndTaxRequestDto;
import com.numbericsuserportal.invoice.dto.InvoiceSearch;
import com.numbericsuserportal.invoice.entity.InvoiceAndTaxEntity;
import com.numbericsuserportal.invoice.service.InvoiceAndTaxService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.numbericsuserportal.invoice.converter.InvoiceAndTaxConverter;
@RestController
@RequestMapping("/v1/invoice")
public class InvoiceAndTaxController {

    @Autowired
    private InvoiceAndTaxService invoiceAndTaxService;

    @PostMapping("/create-invoice")
    public ResponseEntity<InvoiceAndTaxDTO> saveInvoice(@RequestBody InvoiceAndTaxDTO dto, @AuthenticationPrincipal User currentUser) {
        InvoiceAndTaxDTO savedInvoice = invoiceAndTaxService.createInvoiceAndTax(dto,currentUser);
        return ResponseEntity.ok(savedInvoice);
    }


    @PostMapping("/list")
    //  @PreAuthorize("hasAuthority('VIEW_PATIENT')")
    public ResponseEntity<Page<InvoiceAndTaxDTO>> getProviderList(@RequestBody InvoiceSearch requestDTO, @AuthenticationPrincipal User currentUser) {
        Page<InvoiceAndTaxDTO> invoiceAndTaxDTOS = invoiceAndTaxService.searchInvoice(requestDTO).map(entity -> InvoiceAndTaxConverter.toDTO(entity, currentUser));
        return ResponseEntity.ok(invoiceAndTaxDTOS);
    }

    @PostMapping("/view-detail")
    // @PreAuthorize("hasAuthority('VIEW_PROVIDER')")
    public ResponseEntity<InvoiceAndTaxDTO> getInvoiceData(@RequestBody InvoiceAndTaxRequestDto requestDTO,@AuthenticationPrincipal User currentUser) {
        InvoiceAndTaxEntity entity = invoiceAndTaxService.getInvoiceDetail(requestDTO.getId());

        // Convert entity → DTO (safe for JSON)
        InvoiceAndTaxDTO dto = InvoiceAndTaxConverter.toDTO(entity, currentUser);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/view-detail-by-customer-name")
    // @PreAuthorize("hasAuthority('VIEW_PROVIDER')")
    public ResponseEntity<InvoiceAndTaxDTO> getInvoiceDataByCustomerName(@RequestBody InvoiceAndTaxRequestDto requestDTO,@AuthenticationPrincipal User currentUser) {
        InvoiceAndTaxEntity entity = invoiceAndTaxService.getInvoiceDetailByCustomerName(requestDTO.getCustomerName());

        // Convert entity → DTO (safe for JSON)
        InvoiceAndTaxDTO dto = InvoiceAndTaxConverter.toDTO(entity, currentUser);
        return ResponseEntity.ok(dto);
    }
    @PostMapping("/view-detail-by-invoice-num")
    public ResponseEntity<InvoiceAndTaxDTO> getInvoiceDataByInvoiceNum(@RequestBody InvoiceAndTaxRequestDto requestDTO,@AuthenticationPrincipal User currentUser) {
        InvoiceAndTaxEntity entity = invoiceAndTaxService.getInvoiceDetailByInvoiceNumber(requestDTO.getInvoiceNum());

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
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(java.util.Map.of("error", "Failed to update invoice: " + e.getMessage()));
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
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(java.util.Map.of("error", "Failed to delete invoice: " + e.getMessage()));
        }
    }

}
