package com.example.bank.service;

import com.example.bank.model.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    Transaction transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String description);
    List<Transaction> getTransactionsByAccountId(Long accountId);
}