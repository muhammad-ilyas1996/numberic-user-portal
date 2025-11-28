package com.medicalbillinguserportal.claim.controller;

import com.medicalbillinguserportal.claim.dto.ClaimDTO;
import com.medicalbillinguserportal.claim.dto.ClaimFullResponseDto;
import com.medicalbillinguserportal.claim.dto.ClaimIdRequestDto;
import com.medicalbillinguserportal.claim.service.ClaimService;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/claim")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping("/create")
    public ResponseEntity<ClaimDTO> create(@RequestBody ClaimDTO dto,@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(claimService.createClaim(dto,currentUser));
    }

    @PostMapping("/getClaim")
    public ResponseEntity<ClaimFullResponseDto> getClaim(@RequestBody ClaimIdRequestDto dto) {
        return ResponseEntity.ok(claimService.getClaimById(dto.getClaimId()));
    }
}
