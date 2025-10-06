package com.example.bank.controller;

import com.example.bank.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:8080")
public class AdminController {

    private final AccountService accountService;

    public AdminController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/add-demo-money")
    public ResponseEntity<?> addDemoMoneyToAllAccounts() {
        try {
            // This would require additional service methods
            // For now, you can use the SQL approach
            Map<String, String> response = new HashMap<>();
            response.put("message", "Use the SQL scripts to add demo money to all accounts");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-demo-data")
    public ResponseEntity<?> resetDemoData() {
        try {
            // This would reset all data and reload demo data
            Map<String, String> response = new HashMap<>();
            response.put("message", "Demo data reset functionality would go here");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}