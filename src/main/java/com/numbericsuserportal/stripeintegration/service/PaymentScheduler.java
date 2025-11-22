package com.numbericsuserportal.stripeintegration.service;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
@Component
public class PaymentScheduler {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionService subscriptionService;

    // Run every hour
    @Scheduled(cron = "0 0 * * * *")
    public void processDuePayments() {
        LocalDateTime now = LocalDateTime.now();

        // Find users with due payments
        List<User> dueUsers = userRepository.findAll().stream()
                .filter(user -> user.getPaymentDueDate() != null)
                .filter(user -> user.getPaymentDueDate().isBefore(now))
                .filter(user -> user.getPaymentCompleted() == null || !user.getPaymentCompleted())
                .filter(user -> user.getIsDeleted() == null || !user.getIsDeleted())
                .toList();

        System.out.println("Processing " + dueUsers.size() + " due payments...");

        for (User user : dueUsers) {
            try {
                subscriptionService.processTrialEndPayment(user.getUserId());
            } catch (Exception e) {
                System.err.println("Payment processing error for user: " + user.getEmail());
            }
        }
    }
}
