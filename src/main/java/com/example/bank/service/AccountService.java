package com.example.bank.service;

import com.example.bank.model.Account;

import java.util.List;

public interface AccountService {
    Account createAccount(Long userId);
    Account getAccountById(Long id);
    List<Account> getAccountsByUserId(Long userId);
    void updateAccountBalance(Long accountId, Double amount);
}