package com.numbericsuserportal.stripeintegration.service;

import com.numbericsuserportal.stripeintegration.dto.AdminUpdateSubscriberRequest;
import com.numbericsuserportal.stripeintegration.entity.SubscriptionPlanCatalogEntity;
import com.numbericsuserportal.usermanagement.domain.Role;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.domain.UserRole;
import com.numbericsuserportal.usermanagement.domain.UserRoleId;
import com.numbericsuserportal.usermanagement.repo.RoleRepository;
import com.numbericsuserportal.usermanagement.repo.UserRepository;
import com.numbericsuserportal.usermanagement.repo.UserRoleRepository;
import com.stripe.model.PaymentIntent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SubscriptionService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StripePaymentService stripePaymentService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private SubscriptionPlanCatalogService catalogService;

    @Transactional
    public User addSubscriptionToUser(Long userId, String paymentMethodId, String planCode,
            boolean hybridAddOn, int seats) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SubscriptionPlanCatalogEntity catalog = catalogService.requireActiveByCode(planCode);
        User.SubscriptionPlan planEnum = catalogService.toEnum(catalog.getPlanCode());
        int seatCount = Math.max(1, seats);

        String fullName = (user.getFirstName() != null ? user.getFirstName() : "") + " "
                + (user.getLastName() != null ? user.getLastName() : "");
        String customerId = stripePaymentService.createCustomerWithPaymentMethod(
                user.getEmail(),
                fullName.trim(),
                paymentMethodId
        );

        long chargeCents = catalogService.resolveChargeCents(catalog, hybridAddOn, seatCount);

        user.setStripeCustomerId(customerId);
        user.setStripePaymentMethodId(paymentMethodId);
        user.setSubscriptionPlan(planEnum);
        user.setHybridAddOn(hybridAddOn);
        user.setSubscriptionSeats(seatCount);
        user.setSubscriptionAmount(chargeCents);
        user.setTrialStartDate(LocalDateTime.now());
        int trialDays = catalog.getTrialDays() != null ? catalog.getTrialDays() : 7;
        user.setPaymentDueDate(LocalDateTime.now().plusDays(trialDays));
        user.setPaymentCompleted(false);
        user.setSubscriptionStatus("TRIAL");
        user.setUpdatedAt(LocalDateTime.now());

        assignRole(user, catalog.getDefaultRoleCode());
        return userRepository.save(user);
    }

    /** Backward-compatible overload. */
    @Transactional
    public User addSubscriptionToUser(Long userId, String paymentMethodId,
            User.SubscriptionPlan plan) throws Exception {
        return addSubscriptionToUser(userId, paymentMethodId, plan.name(), false, 1);
    }

    @Transactional
    public void processTrialEndPayment(Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getPaymentCompleted() != null && user.getPaymentCompleted()) {
            log.info("Payment already completed for user: {}", user.getEmail());
            return;
        }
        if ("CANCELLED".equalsIgnoreCase(user.getSubscriptionStatus())
                || "INACTIVE".equalsIgnoreCase(user.getSubscriptionStatus())) {
            log.info("Skipping charge for inactive/cancelled user: {}", user.getEmail());
            return;
        }
        if (user.getStripeCustomerId() == null) {
            throw new RuntimeException("User does not have Stripe customer ID");
        }

        long amount = user.getSubscriptionAmount() != null
                ? user.getSubscriptionAmount()
                : (user.getSubscriptionPlan() != null ? user.getSubscriptionPlan().getAmount() : 0L);
        String desc = user.getSubscriptionPlan() != null
                ? user.getSubscriptionPlan().getDescription()
                : "Numbrics subscription";

        try {
            PaymentIntent paymentIntent = stripePaymentService.chargeCustomer(
                    user.getStripeCustomerId(),
                    user.getStripePaymentMethodId(),
                    amount,
                    desc
            );

            if ("succeeded".equals(paymentIntent.getStatus())) {
                user.setPaymentCompleted(true);
                user.setSubscriptionStatus("ACTIVE");
                user.setLastPaymentDate(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                log.info("Payment successful for {} - Amount cents: {}", user.getEmail(), amount);
            }
        } catch (Exception e) {
            user.setSubscriptionStatus("PAYMENT_FAILED");
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            log.error("Payment failed for {}: {}", user.getEmail(), e.getMessage());
            throw e;
        }
    }

    public User getSubscriptionStatus(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<Map<String, Object>> listSubscribers(String statusFilter, String planFilter) {
        return userRepository.findByIsDeletedFalse().stream()
                .filter(u -> u.getSubscriptionPlan() != null || u.getSubscriptionStatus() != null)
                .filter(u -> statusFilter == null || statusFilter.isBlank()
                        || statusFilter.equalsIgnoreCase(u.getSubscriptionStatus()))
                .filter(u -> planFilter == null || planFilter.isBlank()
                        || (u.getSubscriptionPlan() != null
                        && planFilter.equalsIgnoreCase(u.getSubscriptionPlan().name())))
                .sorted(Comparator.comparing(User::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toSubscriberMap)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getSubscriberDetail(Long userId) {
        User user = getSubscriptionStatus(userId);
        return toSubscriberMap(user);
    }

    @Transactional
    public Map<String, Object> adminUpdateSubscriber(Long userId, AdminUpdateSubscriberRequest req) {
        User user = getSubscriptionStatus(userId);
        if (req == null) {
            return toSubscriberMap(user);
        }

        if (req.getPlan() != null && !req.getPlan().isBlank()) {
            SubscriptionPlanCatalogEntity catalog = catalogService.requireByCode(req.getPlan());
            user.setSubscriptionPlan(catalogService.toEnum(catalog.getPlanCode()));
            boolean hybrid = req.getHybridAddOn() != null ? req.getHybridAddOn()
                    : Boolean.TRUE.equals(user.getHybridAddOn());
            int seats = req.getSeats() != null ? Math.max(1, req.getSeats())
                    : (user.getSubscriptionSeats() != null ? user.getSubscriptionSeats() : 1);
            user.setHybridAddOn(hybrid);
            user.setSubscriptionSeats(seats);
            if (req.getSubscriptionAmountCents() == null) {
                user.setSubscriptionAmount(catalogService.resolveChargeCents(catalog, hybrid, seats));
            }
            assignRole(user, catalog.getDefaultRoleCode());
        } else {
            if (req.getHybridAddOn() != null) {
                user.setHybridAddOn(req.getHybridAddOn());
            }
            if (req.getSeats() != null) {
                user.setSubscriptionSeats(Math.max(1, req.getSeats()));
            }
            if (user.getSubscriptionPlan() != null && req.getSubscriptionAmountCents() == null
                    && (req.getHybridAddOn() != null || req.getSeats() != null)) {
                try {
                    SubscriptionPlanCatalogEntity catalog =
                            catalogService.requireByCode(user.getSubscriptionPlan().name());
                    user.setSubscriptionAmount(catalogService.resolveChargeCents(
                            catalog,
                            Boolean.TRUE.equals(user.getHybridAddOn()),
                            user.getSubscriptionSeats() != null ? user.getSubscriptionSeats() : 1));
                } catch (Exception ignored) {
                    // keep existing amount if catalog missing for legacy plan
                }
            }
        }

        if (req.getSubscriptionAmountCents() != null) {
            user.setSubscriptionAmount(req.getSubscriptionAmountCents());
        }
        if (req.getSubscriptionStatus() != null && !req.getSubscriptionStatus().isBlank()) {
            user.setSubscriptionStatus(req.getSubscriptionStatus().trim().toUpperCase(Locale.ROOT));
        }
        if (req.getPaymentCompleted() != null) {
            user.setPaymentCompleted(req.getPaymentCompleted());
        }
        if (req.getExtendTrialDays() != null && req.getExtendTrialDays() > 0) {
            LocalDateTime base = user.getPaymentDueDate() != null ? user.getPaymentDueDate() : LocalDateTime.now();
            if (base.isBefore(LocalDateTime.now())) {
                base = LocalDateTime.now();
            }
            user.setPaymentDueDate(base.plusDays(req.getExtendTrialDays()));
            if (user.getSubscriptionStatus() == null
                    || "INACTIVE".equalsIgnoreCase(user.getSubscriptionStatus())
                    || "CANCELLED".equalsIgnoreCase(user.getSubscriptionStatus())
                    || "PAYMENT_FAILED".equalsIgnoreCase(user.getSubscriptionStatus())) {
                user.setSubscriptionStatus("TRIAL");
                user.setPaymentCompleted(false);
            }
        }

        user.setUpdatedAt(LocalDateTime.now());
        return toSubscriberMap(userRepository.save(user));
    }

    @Transactional
    public Map<String, Object> setSubscriberActive(Long userId, boolean active) {
        User user = getSubscriptionStatus(userId);
        if (active) {
            user.setSubscriptionStatus(Boolean.TRUE.equals(user.getPaymentCompleted()) ? "ACTIVE" : "TRIAL");
        } else {
            user.setSubscriptionStatus("INACTIVE");
        }
        user.setUpdatedAt(LocalDateTime.now());
        return toSubscriberMap(userRepository.save(user));
    }

    private void assignRole(User user, String roleCode) {
        String code = roleCode != null ? roleCode : "NUMBRICS_BUSINESS_OWNER";
        Role role = roleRepository.findByCodeName(code)
                .orElseThrow(() -> new RuntimeException("Role not found: " + code));

        userRoleRepository.deleteByUserId(user.getUserId());

        UserRole userRole = new UserRole();
        UserRoleId userRoleId = new UserRoleId();
        userRoleId.setUserId(user.getUserId());
        userRoleId.setRoleId(role.getRoleId());
        userRole.setId(userRoleId);
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setIsActive(true);
        userRole.setCreatedAt(LocalDateTime.now());
        userRole.setAddedBy(user.getUserId());
        userRoleRepository.save(userRole);
    }

    public Map<String, Object> toSubscriberMap(User user) {
        Map<String, Object> m = new HashMap<>();
        m.put("userId", user.getUserId());
        m.put("email", user.getEmail());
        m.put("name", ((user.getFirstName() != null ? user.getFirstName() : "") + " "
                + (user.getLastName() != null ? user.getLastName() : "")).trim());
        m.put("plan", user.getSubscriptionPlan() != null ? user.getSubscriptionPlan().name() : null);
        m.put("planDescription", user.getSubscriptionPlan() != null
                ? user.getSubscriptionPlan().getDescription() : null);
        m.put("subscriptionAmountCents", user.getSubscriptionAmount());
        m.put("subscriptionAmount", user.getSubscriptionAmount() != null
                ? user.getSubscriptionAmount() / 100.0 : null);
        m.put("hybridAddOn", Boolean.TRUE.equals(user.getHybridAddOn()));
        m.put("seats", user.getSubscriptionSeats() != null ? user.getSubscriptionSeats() : 1);
        m.put("status", user.getSubscriptionStatus());
        m.put("paymentCompleted", user.getPaymentCompleted());
        m.put("trialStartDate", user.getTrialStartDate());
        m.put("paymentDueDate", user.getPaymentDueDate());
        m.put("lastPaymentDate", user.getLastPaymentDate());
        m.put("stripeCustomerId", user.getStripeCustomerId());
        m.put("stripeSubscriptionId", user.getStripeSubscriptionId());
        m.put("updatedAt", user.getUpdatedAt());
        return m;
    }
}
