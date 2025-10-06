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

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<Account> getAccountByNumber(@PathVariable String accountNumber) {
        Account account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAccount(@RequestParam Long userId) {
        try {
            Account account = accountService.createAccount(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Account created successfully with $5000 demo money");
            response.put("account", account);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create-with-balance")
    public ResponseEntity<?> createAccountWithBalance(
            @RequestParam Long userId,
            @RequestParam Double initialBalance) {
        try {
            Account account = accountService.createAccountWithBalance(userId, initialBalance);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Account created successfully with $" + initialBalance);
            response.put("account", account);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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

    @PostMapping("/user/{userId}/add-demo-money")
    public ResponseEntity<?> addDemoMoneyToAllUserAccounts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5000.00") Double amount) {
        try {
            accountService.addDemoMoneyToAllUserAccounts(userId, amount);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully added $" + amount + " demo money to all accounts");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferMoney(
            @RequestParam Long fromAccountId,
            @RequestParam Long toAccountId,
            @RequestParam Double amount) {
        try {
            accountService.transferMoney(fromAccountId, toAccountId, amount);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully transferred $" + amount + " between accounts");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}/total-balance")
    public ResponseEntity<?> getTotalBalance(@PathVariable Long userId) {
        try {
            Double totalBalance = accountService.getTotalBalanceByUserId(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("totalBalance", totalBalance);
            response.put("userId", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long accountId) {
        try {
            accountService.createAccount(accountId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Account deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}