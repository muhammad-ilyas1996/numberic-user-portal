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
        List<User> dueUsers = userRepository.findUsersWithDuePayments(now);
        System.out.println("Processing " + dueUsers.size() + " due payments...");

        for (User user : dueUsers) {
            try {
                if ("CANCELLED".equalsIgnoreCase(user.getSubscriptionStatus())
                        || "INACTIVE".equalsIgnoreCase(user.getSubscriptionStatus())) {
                    continue;
                }
                subscriptionService.processTrialEndPayment(user.getUserId());
            } catch (Exception e) {
                System.err.println("Payment processing error for user: " + user.getEmail());
            }
        }
    }
}
