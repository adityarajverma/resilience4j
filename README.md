**To Run** : docker-compose up


**URL**: localhost:8080/payment/pay?amount=10

Sometime it will work(Payment successful for amount: 10.0) and sometime it will give response like 
**Payment service is down. Please try again later**

### **Advanced Resilience4j Configuration in Spring Boot** 🚀  
Resilience4j provides powerful **fault-tolerance mechanisms** like **Circuit Breaker, Retry, Rate Limiter, Bulkhead, and Time Limiter**.  
Below is an in-depth **configuration guide** to fully utilize **Resilience4j** in a Spring Boot application.  

---

## **1. Add Dependencies**  
Include the required dependencies in `pom.xml`:  
```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot2</artifactId>
    <version>1.7.1</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```
✔ **Actuator** is used for monitoring **Resilience4j metrics**.

---

## **2. Configure Resilience4j in `application.yml`**
You can configure **Circuit Breaker, Retry, Rate Limiter, Bulkhead, and Time Limiter** using `application.yml`:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      paymentService:
        failureRateThreshold: 50 # Trips circuit if failures exceed 50%
        slowCallRateThreshold: 50 # Trips circuit if slow calls exceed 50%
        slowCallDurationThreshold: 2s # Calls taking >2s are considered slow
        minimumNumberOfCalls: 5 # Minimum calls before evaluating the circuit
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 10s # Wait 10s before moving to Half-Open state
        permittedNumberOfCallsInHalfOpenState: 3 # Allow 3 test calls in Half-Open
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 10 # Evaluates last 10 calls

  retry:
    instances:
      paymentService:
        maxAttempts: 3 # Retry up to 3 times
        waitDuration: 2s # Wait 2 seconds between retries
        exponentialBackoffMultiplier: 2 # Doubles wait time after each failure
        retryExceptions:
          - org.springframework.web.client.HttpServerErrorException

  ratelimiter:
    instances:
      paymentService:
        limitForPeriod: 5 # Allow 5 calls per refresh period
        limitRefreshPeriod: 10s # Refresh the limit every 10 seconds
        timeoutDuration: 500ms # Wait 500ms before rejecting the request

  bulkhead:
    instances:
      paymentService:
        maxConcurrentCalls: 10 # Maximum 10 parallel requests
        maxWaitDuration: 2s # Wait max 2s for available slots

  timelimiter:
    instances:
      paymentService:
        timeoutDuration: 3s # Requests taking >3s will timeout
        cancelRunningFuture: true
```

---

## **3. Implementing Circuit Breaker, Retry, and Rate Limiting**
### **Controller with Resilience4j Annotations**
```java
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
```

---

## **4. Understanding Key Configurations**
| **Resilience4j Feature** | **Purpose** | **Key Configurations** |
|--------------------------|-------------|-------------------------|
| **Circuit Breaker** | Stops failing service from being called | `failureRateThreshold`, `slidingWindowSize`, `waitDurationInOpenState` |
| **Retry** | Retries failed requests | `maxAttempts`, `waitDuration`, `exponentialBackoffMultiplier` |
| **Rate Limiter** | Controls request rate | `limitForPeriod`, `limitRefreshPeriod`, `timeoutDuration` |
| **Bulkhead** | Limits concurrent requests | `maxConcurrentCalls`, `maxWaitDuration` |
| **Time Limiter** | Prevents long-running requests | `timeoutDuration`, `cancelRunningFuture` |

---

## **5. Monitoring Resilience4j with Actuator**
Enable **Resilience4j metrics** by adding the following to `application.yml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,* # Expose all endpoints
  metrics:
    tags:
      application: resilience4j
```
### **Check Circuit Breaker Status via Actuator**
```sh
GET http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state
```
✔ Provides **real-time** Circuit Breaker statistics.

---

## **6. Testing Different Failure Scenarios**
### **1️⃣ Simulate a Service Failure**
```sh
curl -X POST "http://localhost:8080/payment/process?amount=100"
```
- If the failure rate exceeds **50%**, the Circuit Breaker **opens**.
- Subsequent requests return **fallback responses**.

### **2️⃣ Test Retry Mechanism**
Modify the `PaymentService` to throw an exception randomly:
```java
if (new Random().nextBoolean()) {
    throw new RuntimeException("Payment failed!");
}
```
- Retry logic will attempt **3 retries** before triggering **fallback**.

### **3️⃣ Check Rate Limiting**
Run the following command **6 times in 10 seconds**:
```sh
curl -X POST "http://localhost:8080/payment/process?amount=100"
```
- The **6th request should be rejected** due to the rate limit.

---

## **🚀 Conclusion**
✅ **Circuit Breaker** prevents cascading failures.  
✅ **Retry** ensures transient failures don’t disrupt services.  
✅ **Rate Limiter** prevents API abuse and protects backend resources.  
✅ **Bulkhead & TimeLimiter** improve system **stability** and **response times**.  
✅ **Actuator Integration** allows **real-time monitoring**.


