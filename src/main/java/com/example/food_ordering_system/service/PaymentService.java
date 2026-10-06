package com.example.food_ordering_system.service;

import com.example.food_ordering_system.dto.OrderRequestDto;
import com.example.food_ordering_system.entity.Order;
import com.example.food_ordering_system.entity.Payment;
import com.example.food_ordering_system.entity.UserPaymentMethod;
import com.example.food_ordering_system.repository.PaymentRepository;
import com.example.food_ordering_system.repository.UserPaymentMethodRepository;
import com.stripe.Stripe;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;

@Service
public class PaymentService {

    private final OrderService orderService;
    private final PaymentRepository paymentRepository;
    private final UserPaymentMethodRepository cardRepository;

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.currency:lkr}")
    private String currency;

    public PaymentService(OrderService orderService,
                          PaymentRepository paymentRepository,
                          UserPaymentMethodRepository cardRepository) {
        this.orderService = orderService;
        this.paymentRepository = paymentRepository;
        this.cardRepository = cardRepository;
    }

    // give the stripe library our key once, when the app boots
    @PostConstruct
    void init() {
        Stripe.apiKey = secretKey;
    }

    // the whole checkout in one go: save a pending order, charge the saved card, then either
    // make the order real (stock down, cart emptied) or throw with a message the customer can read.
    // this method is deliberately NOT @Transactional, so each step commits on its own
    // and a declined card can never leave half-saved data behind
    public Map<String, Object> pay(OrderRequestDto dto, Integer paymentMethodId) {
        UserPaymentMethod card = cardRepository.findById(paymentMethodId)
                .orElseThrow(() -> new IllegalArgumentException("That card could not be found."));
        if (!card.getUser().getId().equals(dto.getUserId())) {
            throw new IllegalArgumentException("That card does not belong to you.");
        }

        // validates stock, prices everything, saves the order as pending_payment
        Order order = orderService.createPendingOrder(dto);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setMethod("card");
        payment.setStatus("pending");
        paymentRepository.save(payment);

        // stripe wants the smallest unit (cents), so Rs. 1250.50 becomes 125050
        long amount = order.getTotalAmount().movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();

        PaymentIntent intent;
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency(currency)
                    .setPaymentMethod(card.getPaymentToken())          // the pm_... id from our db
                    .setConfirm(true)                                  // charge right now
                    .setDescription("QuickBite order #" + order.getId())
                    .putMetadata("orderId", String.valueOf(order.getId()))
                    // card only, so stripe never tries to redirect the customer anywhere
                    .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                            .build())
                    .build();

            // the idempotency key means a double click can never charge twice for one order
            RequestOptions options = RequestOptions.builder().setIdempotencyKey("order-" + order.getId()).build();
            intent = PaymentIntent.create(params, options);
        } catch (CardException declined) {
            // a normal "no": declined, insufficient funds, expired, wrong cvc...
            failPayment(order, payment, declined.getMessage());
            throw new IllegalStateException(declined.getMessage());
        } catch (StripeException other) {
            failPayment(order, payment, "Payment provider error");
            throw new IllegalStateException("We couldn't reach the payment provider. Please try again.");
        }

        payment.setStripePaymentIntentId(intent.getId());

        if (!"succeeded".equals(intent.getStatus())) {
            failPayment(order, payment, "Payment status: " + intent.getStatus());
            throw new IllegalStateException("The payment didn't go through. Please try another card.");
        }

        // money is in, now make the order real
        try {
            orderService.markOrderPaid(order.getId());
        } catch (IllegalStateException soldOut) {
            // the last item went to someone else while we were charging, so give the money back
            refund(intent.getId());
            orderService.cancelPendingOrder(order.getId());
            payment.setStatus("refunded");
            payment.setFailureReason(soldOut.getMessage());
            paymentRepository.save(payment);
            throw soldOut;
        }

        payment.setStatus("completed");
        payment.setPaidAt(Instant.now());
        paymentRepository.save(payment);

        return Map.of("orderId", order.getId(), "message", "Payment successful");
    }

    // payment didn't work: drop the pending order and leave the cart exactly as it was
    private void failPayment(Order order, Payment payment, String reason) {
        payment.setStatus("failed");
        payment.setFailureReason(reason.length() > 250 ? reason.substring(0, 250) : reason);
        paymentRepository.save(payment);
        orderService.cancelPendingOrder(order.getId());
    }

    private void refund(String paymentIntentId) {
        try {
            Refund.create(RefundCreateParams.builder().setPaymentIntent(paymentIntentId).build());
        } catch (StripeException e) {
            // worth a look in the stripe dashboard if this ever happens
            System.err.println("Refund failed for " + paymentIntentId + ": " + e.getMessage());
        }
    }
}