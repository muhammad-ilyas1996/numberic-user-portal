package com.numbericsuserportal.taxbandit.formnec.controller;

import com.numbericsuserportal.taxbandit.formnec.dto.CreateForm1099NECRequestDTO;
import com.numbericsuserportal.taxbandit.formnec.dto.CreateForm1099NECResponseDTO;
import com.numbericsuserportal.taxbandit.formnec.service.Form1099NECService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/taxbandits/form1099nec")
@CrossOrigin(origins = "*")
public class Form1099NECController {

    @Autowired
    private Form1099NECService form1099NECService;

    /**
     * POST /api/taxbandits/form1099nec/create
     * Create Form 1099-NEC (up to 250 forms per payer in a single request)
     */
    @PostMapping("/create")
    public ResponseEntity<?> createForm1099NEC(@RequestBody CreateForm1099NECRequestDTO request) {
        try {
            CreateForm1099NECResponseDTO response = form1099NECService.createForm1099NEC(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
}

