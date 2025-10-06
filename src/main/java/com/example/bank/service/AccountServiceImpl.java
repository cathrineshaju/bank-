package com.example.bank.service;

import com.example.bank.exception.ApiException;
import com.example.bank.model.Account;
import com.example.bank.model.Transaction;
import com.example.bank.model.User;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserService userService;
    private final TransactionRepository transactionRepository;
    private final Random random = new Random();

    public AccountServiceImpl(AccountRepository accountRepository, 
                            UserService userService,
                            TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.userService = userService;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Account createAccount(Long userId) {
        User user = userService.getUserById(userId);
        
        String accountNumber;
        do {
            accountNumber = generateAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));
        
        // Create account with initial demo money
        Account account = new Account(accountNumber, "SAVINGS", user);
        BigDecimal initialBalance = BigDecimal.valueOf(5000.00); // $5000 demo money
        account.setBalance(initialBalance);
        
        Account savedAccount = accountRepository.save(account);
        
        // Create a deposit transaction record
        try {
            Transaction depositTransaction = new Transaction();
            depositTransaction.setToAccount(savedAccount);
            depositTransaction.setAmount(initialBalance);
            depositTransaction.setTransactionType("DEPOSIT");
            depositTransaction.setDescription("Initial account opening bonus");
            transactionRepository.save(depositTransaction);
        } catch (Exception e) {
            // Log error but don't fail account creation
            System.out.println("Failed to create transaction record: " + e.getMessage());
        }
        
        return savedAccount;
    }

    @Override
    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ApiException("Account not found"));
    }

    @Override
    public List<Account> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void depositMoney(Long accountId, Double amount) {
        if (amount <= 0) {
            throw new ApiException("Deposit amount must be positive");
        }
        
        Account account = getAccountById(accountId);
        BigDecimal newBalance = account.getBalance().add(BigDecimal.valueOf(amount));
        account.setBalance(newBalance);
        accountRepository.save(account);
        
        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setToAccount(account);
        transaction.setAmount(BigDecimal.valueOf(amount));
        transaction.setTransactionType("DEPOSIT");
        transaction.setDescription("Cash deposit");
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void withdrawMoney(Long accountId, Double amount) {
        if (amount <= 0) {
            throw new ApiException("Withdrawal amount must be positive");
        }
        
        Account account = getAccountById(accountId);
        
        if (account.getBalance().compareTo(BigDecimal.valueOf(amount)) < 0) {
            throw new ApiException("Insufficient balance for withdrawal");
        }
        
        BigDecimal newBalance = account.getBalance().subtract(BigDecimal.valueOf(amount));
        account.setBalance(newBalance);
        accountRepository.save(account);
        
        // Create withdrawal transaction record
        Transaction transaction = new Transaction();
        transaction.setFromAccount(account);
        transaction.setAmount(BigDecimal.valueOf(amount));
        transaction.setTransactionType("WITHDRAWAL");
        transaction.setDescription("Cash withdrawal");
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void transferMoney(Long fromAccountId, Long toAccountId, Double amount, String description) {
        if (amount <= 0) {
            throw new ApiException("Transfer amount must be positive");
        }
        
        if (fromAccountId.equals(toAccountId)) {
            throw new ApiException("Cannot transfer to the same account");
        }
        
        Account fromAccount = getAccountById(fromAccountId);
        Account toAccount = getAccountById(toAccountId);
        
        if (fromAccount.getBalance().compareTo(BigDecimal.valueOf(amount)) < 0) {
            throw new ApiException("Insufficient balance for transfer");
        }
        
        // Update balances
        fromAccount.setBalance(fromAccount.getBalance().subtract(BigDecimal.valueOf(amount)));
        toAccount.setBalance(toAccount.getBalance().add(BigDecimal.valueOf(amount)));
        
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        
        // Create transfer transaction record
        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(BigDecimal.valueOf(amount));
        transaction.setTransactionType("TRANSFER");
        transaction.setDescription(description != null ? description : "Fund Transfer");
        transactionRepository.save(transaction);
    }

    private String generateAccountNumber() {
        return String.format("ACC%010d", random.nextInt(1000000000));
    }
}