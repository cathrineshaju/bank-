package com.example.bank.service;

import com.example.bank.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {
    Transaction transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String description);
    List<Transaction> getTransactionsByAccountId(Long accountId);
    List<Transaction> getTransactionsByUserId(Long userId);
    Transaction deposit(Long toAccountId, BigDecimal amount, String description);
    Transaction withdraw(Long fromAccountId, BigDecimal amount, String description);
    List<Transaction> getAllTransactions();
    Transaction getTransactionById(Long transactionId);
    List<Transaction> getRecentTransactions(int count);
    List<Transaction> getTransactionsByType(String transactionType);
	List<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}