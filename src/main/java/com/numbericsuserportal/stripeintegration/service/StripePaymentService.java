package com.numbericsuserportal.stripeintegration.service;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class StripePaymentService {
    public String createCustomerWithPaymentMethod(String email, String name, String paymentMethodId)
            throws StripeException {

        // Create customer
        CustomerCreateParams customerParams = CustomerCreateParams.builder()
                .setEmail(email)
                .setName(name)
                .setPaymentMethod(paymentMethodId)
                .setInvoiceSettings(
                        CustomerCreateParams.InvoiceSettings.builder()
                                .setDefaultPaymentMethod(paymentMethodId)
                                .build()
                )
                .build();

        Customer customer = Customer.create(customerParams);

        // Attach payment method to customer
        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
        Map<String, Object> attachParams = new HashMap<>();
        attachParams.put("customer", customer.getId());
        paymentMethod.attach(attachParams);

        return customer.getId();
    }

    public PaymentIntent chargeCustomer(String customerId, String paymentMethodId,
                                        Long amount, String planName)
            throws StripeException {

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency("usd")
                .setCustomer(customerId)
                .setPaymentMethod(paymentMethodId)
                .setConfirm(true)
                .setOffSession(true)
                .setDescription("14-day trial charge - " + planName)
                .putMetadata("plan", planName)
                .build();

        return PaymentIntent.create(params);
    }
}
