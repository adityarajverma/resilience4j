package com.example.resilience4j.controller;


import com.example.resilience4j.service.PaymentService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
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
    @CircuitBreaker(name = "paymentService", fallbackMethod = "fallback")
    public ResponseEntity<String> makePayment(@RequestParam(name = "amount") double amount) {
        return paymentService.makePayment(amount);
    }

    public ResponseEntity<String> fallback(double amount, Exception e) {
        return ResponseEntity.ok("Payment service is down. Please try again later");
    }
}
