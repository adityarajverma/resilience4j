package com.example.resilience4j.service;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@Service
public class PaymentService {

    Random random = new Random();
    public ResponseEntity<String> makePayment(double amount) {

        if(random.nextBoolean()) {
            throw new RuntimeException("Payment failed");
        }

        return ResponseEntity.ok("Payment successful for amount: " + amount);
    }
}
