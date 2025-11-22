package com.numbericsuserportal.stripeintegration.service;

import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.repo.UserRepository;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class SubscriptionService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StripePaymentService stripePaymentService;

    @Transactional
    public User addSubscriptionToUser(Long userId, String paymentMethodId,
                                      User.SubscriptionPlan plan) throws Exception {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create Stripe customer
        String fullName = user.getFirstName() + " " + user.getLastName();
        String customerId = stripePaymentService.createCustomerWithPaymentMethod(
                user.getEmail(),
                fullName,
                paymentMethodId
        );

        // Update user with subscription details
        user.setStripeCustomerId(customerId);
        user.setStripePaymentMethodId(paymentMethodId);
        user.setSubscriptionPlan(plan); // This also sets amount
        user.setTrialStartDate(LocalDateTime.now());
        user.setPaymentDueDate(LocalDateTime.now().plusDays(7));
        user.setPaymentCompleted(false);
        user.setSubscriptionStatus("TRIAL");
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Transactional
    public void processTrialEndPayment(Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getPaymentCompleted() != null && user.getPaymentCompleted()) {
            System.out.println("Payment already completed for user: " + user.getEmail());
            return;
        }

        if (user.getStripeCustomerId() == null) {
            throw new RuntimeException("User does not have Stripe customer ID");
        }

        try {
            PaymentIntent paymentIntent = stripePaymentService.chargeCustomer(
                    user.getStripeCustomerId(),
                    user.getStripePaymentMethodId(),
                    user.getSubscriptionAmount(),
                    user.getSubscriptionPlan().getDescription()
            );

            if ("succeeded".equals(paymentIntent.getStatus())) {
                user.setPaymentCompleted(true);
                user.setSubscriptionStatus("ACTIVE");
                user.setLastPaymentDate(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);

                System.out.println("Payment successful for " + user.getEmail() +
                        " - Amount: $" + user.getSubscriptionPlan().getAmountInDollars());
            }
        } catch (Exception e) {
            user.setSubscriptionStatus("PAYMENT_FAILED");
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            System.err.println("Payment failed for " + user.getEmail() + ": " + e.getMessage());
            throw e;
        }
    }

    public User getSubscriptionStatus(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
