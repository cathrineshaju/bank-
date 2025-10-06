package com.example.bank.service;

import com.example.bank.exception.ApiException;
import com.example.bank.model.Account;
import com.example.bank.model.Transaction;
import com.example.bank.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }

    @Override
    @Transactional
    public Transaction transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String description) {
        if (fromAccountId.equals(toAccountId)) {
            throw new ApiException("Cannot transfer to the same account");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Amount must be greater than zero");
        }

        Account fromAccount = accountService.getAccountById(fromAccountId);
        Account toAccount = accountService.getAccountById(toAccountId);

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new ApiException("Insufficient balance");
        }

        // Update balances
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        // Create transaction record
        Transaction transaction = new Transaction(fromAccount, toAccount, amount, "TRANSFER", description);
        
        return transactionRepository.save(transaction);
    }

    @Override
    public List<Transaction> getTransactionsByAccountId(Long accountId) {
        return transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId);
    }
}