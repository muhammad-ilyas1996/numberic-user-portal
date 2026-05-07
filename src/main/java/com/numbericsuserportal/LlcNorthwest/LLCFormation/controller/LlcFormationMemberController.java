package com.numbericsuserportal.LlcNorthwest.LLCFormation.controller;

import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.FormationMemberDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormation;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.service.LlcFormationMemberService;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.service.LlcFormationService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/llc-northwest/llc-formation/{formationId}/members")
@CrossOrigin(origins = "*")
public class LlcFormationMemberController {

    @Autowired
    private LlcFormationService formationService;

    @Autowired
    private LlcFormationMemberService memberService;

    @GetMapping
    public ResponseEntity<?> list(@AuthenticationPrincipal User currentUser, @PathVariable Long formationId) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        try {
            formationService.getForUserOrThrow(formationId, currentUser);
            return ResponseEntity.ok(memberService.list(formationId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> add(@AuthenticationPrincipal User currentUser,
                                 @PathVariable Long formationId,
                                 @RequestBody FormationMemberDTO dto) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        try {
            LlcFormation formation = formationService.getForUserOrThrow(formationId, currentUser);
            return ResponseEntity.ok(memberService.add(formationId, formation, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<?> remove(@AuthenticationPrincipal User currentUser,
                                    @PathVariable Long formationId,
                                    @PathVariable Long memberId) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        try {
            LlcFormation formation = formationService.getForUserOrThrow(formationId, currentUser);
            return ResponseEntity.ok(memberService.remove(formationId, formation, memberId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

