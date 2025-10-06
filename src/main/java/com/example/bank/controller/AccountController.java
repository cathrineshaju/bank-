package com.example.bank.controller;

import com.example.bank.model.Account;
import com.example.bank.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "http://localhost:8080")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Account>> getUserAccounts(@PathVariable Long userId) {
        List<Account> accounts = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccount(@PathVariable Long accountId) {
        Account account = accountService.getAccountById(accountId);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/create")
    public ResponseEntity<Account> createAccount(@RequestParam Long userId) {
        Account account = accountService.createAccount(userId);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<?> depositMoney(
        @PathVariable Long accountId,
        @RequestParam Double amount) {
        
        try {
            accountService.depositMoney(accountId, amount);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully deposited $" + amount + " to account");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<?> withdrawMoney(
        @PathVariable Long accountId,
        @RequestParam Double amount) {
        
        try {
            accountService.withdrawMoney(accountId, amount);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully withdrew $" + amount + " from account");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferMoney(
        @RequestParam Long fromAccountId,
        @RequestParam Long toAccountId,
        @RequestParam Double amount,
        @RequestParam(required = false) String description) {
        
        try {
            String transferDescription = description != null ? description : "Fund Transfer";
            accountService.transferMoney(fromAccountId, toAccountId, amount, transferDescription);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully transferred $" + amount + " to account");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/user/{userId}/add-demo-money")
    public ResponseEntity<?> addDemoMoneyToAllUserAccounts(@PathVariable Long userId) {
        try {
            // Add money to each account
            List<Account> accounts = accountService.getAccountsByUserId(userId);
            for (Account account : accounts) {
                accountService.depositMoney(account.getId(), 5000.00);
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Added $5000 demo money to all accounts");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}