package com.numbericsuserportal.usermanagement.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"portalType", "password", "authorities"})
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "USERNAME", unique = true)
    @NotNull
    private String username;

    @Column(name = "email", nullable = false, length = 128)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = true)
    private Long createdBy;

    @Column(name = "updated_by", nullable = true)
    private Long updatedBy;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "reset_token", length = 100)
    private String resetToken;

    @Column(name="reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @Column(name = "is_force_change_password", nullable = false)
    private Boolean isForceChangePassword = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "portal_type_id", nullable = false)
    private PortalType portalType;

    @Enumerated(EnumType.STRING)
    @Column(name = "suspend_reason_type")
    private SuspendReasonType suspendReasonType;

    @Column(name = "suspend_reason_description")
    private String suspendReasonDescription;

    @Transient
    private Collection<? extends GrantedAuthority> authorities;

    // ============================================
    // STRIPE SUBSCRIPTION FIELDS (NEW)
    // ============================================

    @Column(name = "stripe_customer_id", length = 100)
    private String stripeCustomerId;

    @Column(name = "stripe_payment_method_id", length = 100)
    private String stripePaymentMethodId;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan")
    private SubscriptionPlan subscriptionPlan;

    @Column(name = "subscription_amount")
    private Long subscriptionAmount; // Amount in cents

    @Column(name = "trial_start_date")
    private LocalDateTime trialStartDate;

    @Column(name = "payment_due_date")
    private LocalDateTime paymentDueDate;

    @Column(name = "payment_completed")
    private Boolean paymentCompleted = false;

    @Column(name = "subscription_status", length = 50)
    private String subscriptionStatus; // TRIAL, ACTIVE, CANCELLED, PAYMENT_FAILED

    @Column(name = "last_payment_date")
    private LocalDateTime lastPaymentDate;

    @Column(name = "stripe_subscription_id", length = 100)
    private String stripeSubscriptionId;

    // ============================================
    // REGISTRATION FIELDS
    // ============================================
    
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", length = 20)
    private AccountType accountType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "registration_status", length = 30)
    private RegistrationStatus registrationStatus;
    
    @Column(name = "terms_accepted")
    private Boolean termsAccepted = false;
    
    @Column(name = "marketing_opt_in")
    private Boolean marketingOptIn = false;
    
    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "two_factor_method", length = 20)
    private TwoFactorMethod twoFactorMethod;
    
    @Column(name = "accounting_integration", length = 20)
    private String accountingIntegration;
    
    @Column(name = "consent_to_share_data")
    private Boolean consentToShareData = false;

    // ============================================
    // EXISTING ENUMS
    // ============================================

    public enum UserType {
        admin, practice, provider, staff, patient
    }

    public enum SuspendReasonType {
        suspended_by_admin, wrong_password_attempts, other
    }
    
    public enum AccountType {
        individual, business
    }
    
    public enum RegistrationStatus {
        STEP1_COMPLETED,
        STEP2_COMPLETED,
        STEP3_COMPLETED,
        STEP4_COMPLETED,
        STEP5_COMPLETED,
        STEP6_COMPLETED,
        STEP7_COMPLETED,
        COMPLETED,
        INCOMPLETE
    }
    
    public enum TwoFactorMethod {
        sms, authenticator, email
    }

    // ============================================
    // NEW SUBSCRIPTION PLAN ENUM
    // ============================================

    public enum SubscriptionPlan {
        STARTER(29900L, "Starter Plan - $299/season"),           // $299
        PROFESSIONAL(59900L, "Professional Plan - $599/season"), // $599
        ENTERPRISE(129900L, "Enterprise Plan - $1,299/season"); // $1,299

        private final Long amount; // Amount in cents
        private final String description;

        SubscriptionPlan(Long amount, String description) {
            this.amount = amount;
            this.description = description;
        }

        public Long getAmount() {
            return amount;
        }

        public String getDescription() {
            return description;
        }

        public Double getAmountInDollars() {
            return amount / 100.0;
        }
        
        // Helper method to get default role based on plan
        public String getDefaultRoleCode() {
            return switch (this) {
                case STARTER -> "NUMBRICS_BUSINESS_OWNER";
                case PROFESSIONAL -> "NUMBRICS_ACCOUNTANT_PRO";
                case ENTERPRISE -> "NUMBRICS_ACCOUNTANT_PRO";
            };
        }
    }
    public void setSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
        if (subscriptionPlan != null) {
            this.subscriptionAmount = subscriptionPlan.getAmount();
        }
    }
}