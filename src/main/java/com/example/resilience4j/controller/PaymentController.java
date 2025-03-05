package com.example.resilience4j.controller;


import com.example.resilience4j.service.PaymentService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    PaymentService paymentService;

    @GetMapping("/pay")
    @CircuitBreaker(name = "paymentService", fallbackMethod = "fallbackPayment")
    @Retry(name = "paymentService")
//    @RateLimiter(name = "paymentService")
//    @Bulkhead(name = "paymentService")
//    @TimeLimiter(name = "paymentService")
//     If you want to use above annotation than please change  to below code
//    public CompletableFuture<ResponseEntity<String>> processPayment(@RequestParam double amount) {
//        return CompletableFuture.supplyAsync(() -> paymentService.pay(amount));
//    }

//    public CompletableFuture<ResponseEntity<String>> fallbackPayment(double amount, Exception e) {
//        return CompletableFuture.supplyAsync(() -> ResponseEntity.status(503)
//                .body("Payment system is down. Please try again later."));
//    }

    public ResponseEntity<String> makePayment(@RequestParam(name = "amount") double amount) {
        return paymentService.makePayment(amount);
    }

    public ResponseEntity<String> fallbackPayment(double amount, Exception e) {
        return ResponseEntity.ok("Payment service is down. Please try again later");
    }
}
