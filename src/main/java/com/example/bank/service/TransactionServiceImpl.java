package com.example.bank.service;

import com.example.bank.exception.ApiException;
import com.example.bank.model.Account;
import com.example.bank.model.Transaction;
import com.example.bank.model.User;
import com.example.bank.repository.TransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final UserService userService;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                AccountService accountService,
                                UserService userService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.userService = userService;
    }

    @Override
    @Transactional
    public Transaction transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String description) {
        // Validate input parameters
        validateTransferParameters(fromAccountId, toAccountId, amount);
        
        // Get accounts
        Account fromAccount = accountService.getAccountById(fromAccountId);
        Account toAccount = accountService.getAccountById(toAccountId);
        
        // Check if accounts belong to the same user (optional business rule)
        if (fromAccount.getUser().getId().equals(toAccount.getUser().getId())) {
            description = (description != null ? description + " " : "") + "(Internal Transfer)";
        }
        
        // Validate sufficient balance
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new ApiException("Insufficient balance in account " + fromAccount.getAccountNumber() + 
                                 ". Available: $" + fromAccount.getBalance() + ", Required: $" + amount);
        }
        
        // Perform the transfer
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        
        // Save account balances (this should be in a transactional method in AccountService)
        // For now, we'll assume AccountService handles this
        
        // Create and save transaction record
        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(amount);
        transaction.setTransactionType("TRANSFER");
        transaction.setDescription(description != null ? description : "Fund transfer");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus("COMPLETED");
        
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction deposit(Long toAccountId, BigDecimal amount, String description) {
        // Validate input parameters
        if (toAccountId == null) {
            throw new ApiException("Target account ID cannot be null");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Deposit amount must be greater than zero");
        }
        
        // Get target account
        Account toAccount = accountService.getAccountById(toAccountId);
        
        // Update account balance
        toAccount.setBalance(toAccount.getBalance().add(amount));
        
        // Create and save transaction record
        Transaction transaction = new Transaction();
        transaction.setToAccount(toAccount);
        transaction.setAmount(amount);
        transaction.setTransactionType("DEPOSIT");
        transaction.setDescription(description != null ? description : "Cash deposit");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus("COMPLETED");
        
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction withdraw(Long fromAccountId, BigDecimal amount, String description) {
        // Validate input parameters
        if (fromAccountId == null) {
            throw new ApiException("Source account ID cannot be null");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Withdrawal amount must be greater than zero");
        }
        
        // Get source account
        Account fromAccount = accountService.getAccountById(fromAccountId);
        
        // Check sufficient balance
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new ApiException("Insufficient balance for withdrawal. Available: $" + 
                                 fromAccount.getBalance() + ", Requested: $" + amount);
        }
        
        // Update account balance
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        
        // Create and save transaction record
        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setAmount(amount);
        transaction.setTransactionType("WITHDRAWAL");
        transaction.setDescription(description != null ? description : "Cash withdrawal");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus("COMPLETED");
        
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByAccountId(Long accountId) {
        if (accountId == null) {
            throw new ApiException("Account ID cannot be null");
        }
        
        // Verify account exists
        accountService.getAccountById(accountId);
        
        return transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByUserId(Long userId) {
        if (userId == null) {
            throw new ApiException("User ID cannot be null");
        }
        
        // Verify user exists
        userService.getUserById(userId);
        
        // Get all accounts for the user
        List<Account> userAccounts = accountService.getAccountsByUserId(userId);
        List<Long> accountIds = userAccounts.stream()
                .map(Account::getId)
                .collect(Collectors.toList());
        
        if (accountIds.isEmpty()) {
            return List.of();
        }
        
        // Get transactions where user's accounts are involved as either sender or receiver
        return transactionRepository.findByFromAccountIdInOrToAccountIdIn(accountIds, accountIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll(Sort.by(Sort.Direction.DESC, "transactionDate"));
    }

    @Override
    @Transactional(readOnly = true)
    public Transaction getTransactionById(Long transactionId) {
        if (transactionId == null) {
            throw new ApiException("Transaction ID cannot be null");
        }
        
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ApiException("Transaction not found with id: " + transactionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getRecentTransactions(int count) {
        if (count <= 0) {
            throw new ApiException("Count must be greater than zero");
        }
        
        PageRequest pageRequest = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "transactionDate"));
        return transactionRepository.findAll(pageRequest).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByType(String transactionType) {
        if (transactionType == null || transactionType.trim().isEmpty()) {
            throw new ApiException("Transaction type cannot be null or empty");
        }
        
        return transactionRepository.findByTransactionTypeOrderByTransactionDateDesc(transactionType);
    }

    // Additional utility methods
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new ApiException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new ApiException("Start date cannot be after end date");
        }
        
        return transactionRepository.findByTransactionDateBetweenOrderByTransactionDateDesc(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByAccountAndDateRange(Long accountId, LocalDateTime startDate, LocalDateTime endDate) {
        if (accountId == null) {
            throw new ApiException("Account ID cannot be null");
        }
        if (startDate == null || endDate == null) {
            throw new ApiException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new ApiException("Start date cannot be after end date");
        }
        
        return transactionRepository.findByFromAccountIdOrToAccountIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                accountId, accountId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalDepositsByAccount(Long accountId) {
        List<Transaction> deposits = transactionRepository.findByToAccountIdAndTransactionType(accountId, "DEPOSIT");
        return deposits.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalWithdrawalsByAccount(Long accountId) {
        List<Transaction> withdrawals = transactionRepository.findByFromAccountIdAndTransactionType(accountId, "WITHDRAWAL");
        return withdrawals.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalTransfersByAccount(Long accountId) {
        List<Transaction> outgoingTransfers = transactionRepository.findByFromAccountIdAndTransactionType(accountId, "TRANSFER");
        BigDecimal outgoing = outgoingTransfers.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<Transaction> incomingTransfers = transactionRepository.findByToAccountIdAndTransactionType(accountId, "TRANSFER");
        BigDecimal incoming = incomingTransfers.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return incoming.subtract(outgoing); // Net transfer amount
    }

    // Private helper methods
    private void validateTransferParameters(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        if (fromAccountId == null) {
            throw new ApiException("Source account ID cannot be null");
        }
        if (toAccountId == null) {
            throw new ApiException("Target account ID cannot be null");
        }
        if (fromAccountId.equals(toAccountId)) {
            throw new ApiException("Cannot transfer to the same account");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Transfer amount must be greater than zero");
        }
    }

    // Method to get transaction summary for an account
    @Transactional(readOnly = true)
    public TransactionSummary getTransactionSummary(Long accountId) {
        BigDecimal totalDeposits = getTotalDepositsByAccount(accountId);
        BigDecimal totalWithdrawals = getTotalWithdrawalsByAccount(accountId);
        BigDecimal netTransfers = getTotalTransfersByAccount(accountId);
        
        return new TransactionSummary(totalDeposits, totalWithdrawals, netTransfers);
    }

    // Inner class for transaction summary
    public static class TransactionSummary {
        private final BigDecimal totalDeposits;
        private final BigDecimal totalWithdrawals;
        private final BigDecimal netTransfers;

        public TransactionSummary(BigDecimal totalDeposits, BigDecimal totalWithdrawals, BigDecimal netTransfers) {
            this.totalDeposits = totalDeposits;
            this.totalWithdrawals = totalWithdrawals;
            this.netTransfers = netTransfers;
        }

        // Getters
        public BigDecimal getTotalDeposits() { return totalDeposits; }
        public BigDecimal getTotalWithdrawals() { return totalWithdrawals; }
        public BigDecimal getNetTransfers() { return netTransfers; }
        public BigDecimal getNetAmount() { 
            return totalDeposits.subtract(totalWithdrawals).add(netTransfers); 
        }
    }
}