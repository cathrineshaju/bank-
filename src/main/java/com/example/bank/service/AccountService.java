package com.example.bank.service;

import com.example.bank.model.Account;

import java.util.List;

public interface AccountService {
    Account createAccount(Long userId);
    Account createAccountWithBalance(Long userId, Double initialBalance);
    Account getAccountById(Long id);
    List<Account> getAccountsByUserId(Long userId);
    void updateAccountBalance(Long accountId, Double amount);
    void depositMoney(Long accountId, Double amount);
    void withdrawMoney(Long accountId, Double amount);
    void addDemoMoneyToAllUserAccounts(Long userId, Double amount);
    void transferMoney(Long fromAccountId, Long toAccountId, Double amount);
    Account getAccountByNumber(String accountNumber);
    Double getTotalBalanceByUserId(Long userId);
}