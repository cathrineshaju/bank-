package com.example.bank.controller;

import com.example.bank.model.Transaction;
import com.example.bank.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:8080")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request) {
        try {
            Transaction transaction = transactionService.transfer(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount(),
                request.getDescription()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Transfer completed successfully");
            response.put("transaction", transaction);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody DepositRequest request) {
        try {
            Transaction transaction = transactionService.deposit(
                request.getToAccountId(),
                request.getAmount(),
                request.getDescription()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Deposit completed successfully");
            response.put("transaction", transaction);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody WithdrawRequest request) {
        try {
            Transaction transaction = transactionService.withdraw(
                request.getFromAccountId(),
                request.getAmount(),
                request.getDescription()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Withdrawal completed successfully");
            response.put("transaction", transaction);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Transaction>> getAccountTransactions(@PathVariable Long accountId) {
        List<Transaction> transactions = transactionService.getTransactionsByAccountId(accountId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getUserTransactions(@PathVariable Long userId) {
        List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Transaction>> getRecentTransactions(
            @RequestParam(defaultValue = "10") int count) {
        List<Transaction> transactions = transactionService.getRecentTransactions(count);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/type/{transactionType}")
    public ResponseEntity<List<Transaction>> getTransactionsByType(
            @PathVariable String transactionType) {
        List<Transaction> transactions = transactionService.getTransactionsByType(transactionType);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Transaction>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Transaction> transactions = transactionService.getTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long transactionId) {
        Transaction transaction = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    // DTO classes for request bodies
    public static class TransferRequest {
        private Long fromAccountId;
        private Long toAccountId;
        private BigDecimal amount;
        private String description;

        // Getters and setters
        public Long getFromAccountId() { return fromAccountId; }
        public void setFromAccountId(Long fromAccountId) { this.fromAccountId = fromAccountId; }
        public Long getToAccountId() { return toAccountId; }
        public void setToAccountId(Long toAccountId) { this.toAccountId = toAccountId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class DepositRequest {
        private Long toAccountId;
        private BigDecimal amount;
        private String description;

        // Getters and setters
        public Long getToAccountId() { return toAccountId; }
        public void setToAccountId(Long toAccountId) { this.toAccountId = toAccountId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class WithdrawRequest {
        private Long fromAccountId;
        private BigDecimal amount;
        private String description;

        // Getters and setters
        public Long getFromAccountId() { return fromAccountId; }
        public void setFromAccountId(Long fromAccountId) { this.fromAccountId = fromAccountId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}