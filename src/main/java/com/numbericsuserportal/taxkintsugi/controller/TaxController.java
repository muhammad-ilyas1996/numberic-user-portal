package com.numbericsuserportal.taxkintsugi.controller;

import com.numbericsuserportal.taxkintsugi.dto.TransactionDTO;
import com.numbericsuserportal.taxkintsugi.service.TaxService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tax")
public class TaxController {

    private final TaxService taxService;

    public TaxController(TaxService taxService) {
        this.taxService = taxService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<TransactionDTO> calculateTax(@RequestBody TransactionDTO requestDto) {
        TransactionDTO result = taxService.calculateTax(requestDto);
        return ResponseEntity.ok(result);
    }
}
