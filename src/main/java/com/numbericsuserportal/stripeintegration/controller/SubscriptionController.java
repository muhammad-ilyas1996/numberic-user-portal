package com.numbericsuserportal.stripeintegration.controller;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.stripeintegration.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/subscription")
@CrossOrigin(origins = "*")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

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
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Plan is required (BASIC or PREMIUM)"));
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

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid plan. Use BASIC or PREMIUM"));
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

    // Manual payment trigger (for testing)
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
}
