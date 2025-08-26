package com.medicalbillinguserportal.patientregistration.payer.controller;

//import com.medicalbillinguserportal.commonpersistence.dto.LocationDto;
//import com.medicalbillinguserportal.commonpersistence.dto.LocationSearch;
//import com.medicalbillinguserportal.commonpersistence.entity.LocationEntity;
import com.medicalbillinguserportal.patientregistration.payer.dto.PayerDTO;
import com.medicalbillinguserportal.patientregistration.payer.dto.PayerDropdownDTO;
import com.medicalbillinguserportal.patientregistration.payer.entity.PayerEntity;
import com.medicalbillinguserportal.patientregistration.payer.service.PayerService;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/payer")
public class PayerController {
    @Autowired
    public PayerService payerService;

    @PostMapping("/create")
    public ResponseEntity<PayerDTO> savepayer(@RequestBody PayerDTO dto, @AuthenticationPrincipal User currentUser)
    {
        return ResponseEntity.ok(payerService.savePayer(dto,currentUser));
    }
    @PostMapping("/activate")
    public String activatePayer(@RequestBody PayerDropdownDTO payerDropdownDTO, @AuthenticationPrincipal User currentUser) {
        return payerService.activatePayer(payerDropdownDTO.getPayerId(),currentUser);
    }

    // For Revert button
    @PostMapping("/deactivate")
    public String deactivatePayer(@RequestBody PayerDropdownDTO payerDropdownDTO, @AuthenticationPrincipal User currentUser) {
        return payerService.deactivatePayer(payerDropdownDTO.getPayerId(),currentUser);
    }

    @GetMapping("/active-dropdown")
    public List<PayerDropdownDTO> getActivePayersDropdown()
    {
        return payerService.getActivePayersForDropdown();
    }
}
