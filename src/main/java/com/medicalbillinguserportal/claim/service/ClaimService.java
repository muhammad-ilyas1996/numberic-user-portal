package com.medicalbillinguserportal.claim.service;

import com.medicalbillinguserportal.claim.dto.ClaimDTO;
import com.medicalbillinguserportal.claim.dto.ClaimFullResponseDto;
import com.medicalbillinguserportal.usermanagement.domain.User;

public interface ClaimService {
    ClaimDTO createClaim(ClaimDTO dto, User currentUser);
    ClaimFullResponseDto getClaimById(Long claimId);
}
