package com.example.bank.service;

import com.example.bank.model.Account;

import java.util.List;

public interface AccountService {
    Account createAccount(Long userId);
    Account getAccountById(Long id);
    List<Account> getAccountsByUserId(Long userId);
    void depositMoney(Long accountId, Double amount);
    void withdrawMoney(Long accountId, Double amount);
    void transferMoney(Long fromAccountId, Long toAccountId, Double amount, String description);
}