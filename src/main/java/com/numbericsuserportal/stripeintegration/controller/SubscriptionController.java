package com.numbericsuserportal.stripeintegration.controller;

import com.numbericsuserportal.stripeintegration.dto.AdminUpdateSubscriberRequest;
import com.numbericsuserportal.stripeintegration.dto.UpdateSubscriptionPlanRequest;
import com.numbericsuserportal.stripeintegration.service.SubscriptionPlanCatalogService;
import com.numbericsuserportal.stripeintegration.service.SubscriptionService;
import com.numbericsuserportal.usermanagement.domain.Role;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.domain.UserRole;
import com.numbericsuserportal.usermanagement.domain.UserRoleId;
import com.numbericsuserportal.usermanagement.repo.RoleRepository;
import com.numbericsuserportal.usermanagement.repo.UserRepository;
import com.numbericsuserportal.usermanagement.repo.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscription")
@CrossOrigin(origins = "*")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private SubscriptionPlanCatalogService catalogService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    /** Public pricing page — active plans only (amounts/trial from admin catalog). */
    @GetMapping("/plans")
    public ResponseEntity<?> getPlans() {
        return ResponseEntity.ok(catalogService.listPublicPlans());
    }

    /**
     * Start trial for existing user.
     * Body: { paymentMethodId, plan, hybridAddOn?, seats? }
     */
    @PostMapping("/add-to-user/{userId}")
    public ResponseEntity<?> addSubscriptionToUser(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> request) {
        try {
            String paymentMethodId = request.get("paymentMethodId") != null
                    ? String.valueOf(request.get("paymentMethodId")) : null;
            String planName = request.get("plan") != null ? String.valueOf(request.get("plan")) : null;
            boolean hybrid = parseBool(request.get("hybridAddOn"));
            int seats = parseInt(request.get("seats"), 1);

            if (planName == null || planName.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Plan is required. Use GET /api/subscription/plans"));
            }
            if (paymentMethodId == null || paymentMethodId.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "paymentMethodId is required"));
            }

            User user = subscriptionService.addSubscriptionToUser(
                    userId, paymentMethodId, planName, hybrid, seats);

            Map<String, Object> response = new HashMap<>(subscriptionService.toSubscriberMap(user));
            response.put("message", "Subscription added successfully. Trial period started.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<?> getSubscriptionStatus(@PathVariable Long userId) {
        try {
            User user = subscriptionService.getSubscriptionStatus(userId);
            Map<String, Object> response = subscriptionService.toSubscriberMap(user);
            if (user.getSubscriptionPlan() == null && user.getSubscriptionStatus() == null) {
                response.put("message", "No subscription found for this user");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
    }

    @PostMapping("/process-payment/{userId}")
    public ResponseEntity<?> processPaymentManually(@PathVariable Long userId) {
        try {
            subscriptionService.processTrialEndPayment(userId);
            User user = subscriptionService.getSubscriptionStatus(userId);
            return ResponseEntity.ok(Map.of(
                    "message", "Payment processed successfully",
                    "subscriber", subscriptionService.toSubscriberMap(user)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/assign-super-admin/{userId}")
    public ResponseEntity<?> assignSuperAdmin(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Role superAdminRole = roleRepository.findByCodeName("NUMBRICS_SUPER_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Super Admin role not found"));

            userRoleRepository.deleteByUserId(userId);

            UserRole userRole = new UserRole();
            UserRoleId userRoleId = new UserRoleId();
            userRoleId.setUserId(userId);
            userRoleId.setRoleId(superAdminRole.getRoleId());
            userRole.setId(userRoleId);
            userRole.setUser(user);
            userRole.setRole(superAdminRole);
            userRole.setIsActive(true);
            userRole.setCreatedAt(LocalDateTime.now());
            userRole.setAddedBy(userId);
            userRoleRepository.save(userRole);

            user.setSubscriptionPlan(null);
            user.setSubscriptionAmount(null);
            user.setHybridAddOn(false);
            user.setSubscriptionSeats(1);
            user.setSubscriptionStatus(null);
            user.setStripeCustomerId(null);
            user.setStripePaymentMethodId(null);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "Super Admin role assigned successfully",
                    "userId", userId,
                    "role", "NUMBRICS_SUPER_ADMIN"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Admin: plan catalog ─────────────────────────────────────────────────

    @GetMapping("/admin/plans")
    public ResponseEntity<?> adminListPlans(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(catalogService.listAllPlansAdmin());
    }

    @PutMapping("/admin/plans/{id}")
    public ResponseEntity<?> adminUpdatePlan(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            @RequestBody UpdateSubscriptionPlanRequest request) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(catalogService.updatePlan(id, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/admin/plans/code/{planCode}")
    public ResponseEntity<?> adminUpdatePlanByCode(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String planCode,
            @RequestBody UpdateSubscriptionPlanRequest request) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(catalogService.updatePlanByCode(planCode, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/admin/plans/{id}/activate")
    public ResponseEntity<?> adminActivatePlan(
            @AuthenticationPrincipal User currentUser, @PathVariable Long id) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(catalogService.setActive(id, true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/admin/plans/{id}/deactivate")
    public ResponseEntity<?> adminDeactivatePlan(
            @AuthenticationPrincipal User currentUser, @PathVariable Long id) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(catalogService.setActive(id, false));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Admin: subscribers ──────────────────────────────────────────────────

    @GetMapping("/admin/subscribers")
    public ResponseEntity<?> adminListSubscribers(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String plan) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        List<Map<String, Object>> rows = subscriptionService.listSubscribers(status, plan);
        return ResponseEntity.ok(Map.of("count", rows.size(), "subscribers", rows));
    }

    @GetMapping("/admin/subscribers/{userId}")
    public ResponseEntity<?> adminGetSubscriber(
            @AuthenticationPrincipal User currentUser, @PathVariable Long userId) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(subscriptionService.getSubscriberDetail(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/admin/subscribers/{userId}")
    public ResponseEntity<?> adminUpdateSubscriber(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long userId,
            @RequestBody AdminUpdateSubscriberRequest request) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(subscriptionService.adminUpdateSubscriber(userId, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/admin/subscribers/{userId}/activate")
    public ResponseEntity<?> adminActivateSubscriber(
            @AuthenticationPrincipal User currentUser, @PathVariable Long userId) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(subscriptionService.setSubscriberActive(userId, true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/admin/subscribers/{userId}/deactivate")
    public ResponseEntity<?> adminDeactivateSubscriber(
            @AuthenticationPrincipal User currentUser, @PathVariable Long userId) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(subscriptionService.setSubscriberActive(userId, false));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private static boolean parseBool(Object v) {
        if (v == null) {
            return false;
        }
        if (v instanceof Boolean b) {
            return b;
        }
        return "true".equalsIgnoreCase(String.valueOf(v)) || "1".equals(String.valueOf(v));
    }

    private static int parseInt(Object v, int def) {
        if (v == null) {
            return def;
        }
        try {
            return Math.max(1, Integer.parseInt(String.valueOf(v)));
        } catch (Exception e) {
            return def;
        }
    }
}
