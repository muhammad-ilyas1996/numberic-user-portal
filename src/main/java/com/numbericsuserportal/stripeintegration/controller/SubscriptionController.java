package com.numbericsuserportal.stripeintegration.controller;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.domain.Role;
import com.numbericsuserportal.usermanagement.domain.UserRole;
import com.numbericsuserportal.usermanagement.domain.UserRoleId;
import com.numbericsuserportal.usermanagement.repo.UserRepository;
import com.numbericsuserportal.usermanagement.repo.RoleRepository;
import com.numbericsuserportal.usermanagement.repo.UserRoleRepository;
import com.numbericsuserportal.stripeintegration.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/subscription")
@CrossOrigin(origins = "*")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;

    // Get available plans
    @GetMapping("/plans")
    public ResponseEntity<?> getPlans() {
        List<Map<String, Object>> plans = new ArrayList<>();

        for (User.SubscriptionPlan plan : User.SubscriptionPlan.values()) {
            Map<String, Object> planInfo = new HashMap<>();
            planInfo.put("id", plan.name());
            planInfo.put("name", plan.getDescription());
            planInfo.put("amount", plan.getAmountInDollars());
            planInfo.put("currency", "USD");
            planInfo.put("defaultRole", plan.getDefaultRoleCode());
            plans.add(planInfo);
        }

        return ResponseEntity.ok(plans);
    }

    // Add subscription to existing user
    @PostMapping("/add-to-user/{userId}")
    public ResponseEntity<?> addSubscriptionToUser(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        try {
            String paymentMethodId = request.get("paymentMethodId");
            String planName = request.get("plan");

            if (planName == null || planName.isEmpty()) {
                String validPlans = String.join(", ", 
                    Arrays.stream(User.SubscriptionPlan.values())
                        .map(Enum::name)
                        .toArray(String[]::new));
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Plan is required. Valid plans: " + validPlans));
            }

            User.SubscriptionPlan plan = User.SubscriptionPlan.valueOf(planName.toUpperCase());

            User user = subscriptionService.addSubscriptionToUser(userId, paymentMethodId, plan);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Subscription added successfully. Trial period started.");
            response.put("userId", user.getUserId());
            response.put("email", user.getEmail());
            response.put("plan", user.getSubscriptionPlan().name());
            response.put("planDescription", user.getSubscriptionPlan().getDescription());
            response.put("amount", user.getSubscriptionPlan().getAmountInDollars());
            response.put("trialEndsAt", user.getPaymentDueDate());
            response.put("status", user.getSubscriptionStatus());
            response.put("assignedRole", plan.getDefaultRoleCode());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            String validPlans = String.join(", ", 
                Arrays.stream(User.SubscriptionPlan.values())
                    .map(Enum::name)
                    .toArray(String[]::new));
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid plan. Valid plans: " + validPlans));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    // Get subscription status
    @GetMapping("/status/{userId}")
    public ResponseEntity<?> getSubscriptionStatus(@PathVariable Long userId) {
        try {
            User user = subscriptionService.getSubscriptionStatus(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getUserId());
            response.put("email", user.getEmail());
            response.put("name", user.getFirstName() + " " + user.getLastName());

            if (user.getSubscriptionPlan() != null) {
                response.put("plan", user.getSubscriptionPlan().name());
                response.put("planDescription", user.getSubscriptionPlan().getDescription());
                response.put("amount", user.getSubscriptionPlan().getAmountInDollars());
                response.put("status", user.getSubscriptionStatus());
                response.put("paymentCompleted", user.getPaymentCompleted());
                response.put("trialStartDate", user.getTrialStartDate());
                response.put("paymentDueDate", user.getPaymentDueDate());
                response.put("lastPaymentDate", user.getLastPaymentDate());
            } else {
                response.put("message", "No subscription found for this user");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "User not found"));
        }
    }

    // Manual payment trigger
    @PostMapping("/process-payment/{userId}")
    public ResponseEntity<?> processPaymentManually(@PathVariable Long userId) {
        try {
            User user = subscriptionService.getSubscriptionStatus(userId);
            subscriptionService.processTrialEndPayment(userId);

            return ResponseEntity.ok(Map.of(
                "message", "Payment processed successfully",
                "plan", user.getSubscriptionPlan().name(),
                "amount", user.getSubscriptionPlan().getAmountInDollars()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Assign Super Admin role (no subscription plan)
    @PostMapping("/assign-super-admin/{userId}")
    public ResponseEntity<?> assignSuperAdmin(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Role superAdminRole = roleRepository.findByCodeName("NUMBRICS_SUPER_ADMIN")
                .orElseThrow(() -> new RuntimeException("Super Admin role not found"));
            
            // Remove existing roles
            userRoleRepository.deleteByUserId(userId);
            
            // Assign Super Admin role
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
            
            // Clear subscription plan for Super Admin
            user.setSubscriptionPlan(null);
            user.setSubscriptionAmount(null);
            user.setStripeCustomerId(null);
            user.setStripePaymentMethodId(null);
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of(
                "message", "Super Admin role assigned successfully",
                "userId", userId,
                "role", "NUMBRICS_SUPER_ADMIN"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
}
