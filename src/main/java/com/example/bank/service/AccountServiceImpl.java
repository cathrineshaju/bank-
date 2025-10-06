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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@Transactional
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
        
        String accountNumber = generateUniqueAccountNumber();
        
        // Create account with $5000 demo money
        Account account = new Account(accountNumber, "SAVINGS", user);
        BigDecimal initialBalance = BigDecimal.valueOf(5000.00);
        account.setBalance(initialBalance);
        
        Account savedAccount = accountRepository.save(account);
        
        // Create deposit transaction record
        createDepositTransaction(savedAccount, initialBalance, "Account opening bonus");
        
        return savedAccount;
    }

    @Override
    public Account createAccountWithBalance(Long userId, Double initialBalance) {
        User user = userService.getUserById(userId);
        
        String accountNumber = generateUniqueAccountNumber();
        
        Account account = new Account(accountNumber, "SAVINGS", user);
        account.setBalance(BigDecimal.valueOf(initialBalance));
        
        Account savedAccount = accountRepository.save(account);
        
        // Create deposit transaction record
        createDepositTransaction(savedAccount, BigDecimal.valueOf(initialBalance), "Initial deposit");
        
        return savedAccount;
    }

    @Override
    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ApiException("Account not found with id: " + id));
    }

    @Override
    public List<Account> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    @Override
    public void updateAccountBalance(Long accountId, Double amount) {
        Account account = getAccountById(accountId);
        BigDecimal newBalance = account.getBalance().add(BigDecimal.valueOf(amount));
        account.setBalance(newBalance);
        accountRepository.save(account);
    }

    @Override
    public void depositMoney(Long accountId, Double amount) {
        if (amount <= 0) {
            throw new ApiException("Deposit amount must be positive");
        }
        
        Account account = getAccountById(accountId);
        BigDecimal newBalance = account.getBalance().add(BigDecimal.valueOf(amount));
        account.setBalance(newBalance);
        accountRepository.save(account);
        
        // Create transaction record
        createDepositTransaction(account, BigDecimal.valueOf(amount), "Cash deposit");
    }

    @Override
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
        createWithdrawalTransaction(account, BigDecimal.valueOf(amount), "Cash withdrawal");
    }

    @Override
    public void addDemoMoneyToAllUserAccounts(Long userId, Double amount) {
        if (amount <= 0) {
            throw new ApiException("Amount must be positive");
        }
        
        List<Account> userAccounts = getAccountsByUserId(userId);
        
        if (userAccounts.isEmpty()) {
            throw new ApiException("No accounts found for user");
        }
        
        for (Account account : userAccounts) {
            BigDecimal newBalance = account.getBalance().add(BigDecimal.valueOf(amount));
            account.setBalance(newBalance);
            accountRepository.save(account);
            
            // Create deposit transaction record for each account
            createDepositTransaction(account, BigDecimal.valueOf(amount), "Demo money deposit");
        }
    }

    @Override
    public void transferMoney(Long fromAccountId, Long toAccountId, Double amount) {
        if (fromAccountId.equals(toAccountId)) {
            throw new ApiException("Cannot transfer to the same account");
        }
        
        if (amount <= 0) {
            throw new ApiException("Transfer amount must be positive");
        }
        
        Account fromAccount = getAccountById(fromAccountId);
        Account toAccount = getAccountById(toAccountId);
        
        // Check sufficient balance
        if (fromAccount.getBalance().compareTo(BigDecimal.valueOf(amount)) < 0) {
            throw new ApiException("Insufficient balance for transfer");
        }
        
        // Perform transfer
        fromAccount.setBalance(fromAccount.getBalance().subtract(BigDecimal.valueOf(amount)));
        toAccount.setBalance(toAccount.getBalance().add(BigDecimal.valueOf(amount)));
        
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        
        // Create transfer transaction record
        createTransferTransaction(fromAccount, toAccount, BigDecimal.valueOf(amount), "Fund transfer");
    }

    @Override
    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ApiException("Account not found with number: " + accountNumber));
    }

    @Override
    public Double getTotalBalanceByUserId(Long userId) {
        List<Account> accounts = getAccountsByUserId(userId);
        return accounts.stream()
                .mapToDouble(account -> account.getBalance().doubleValue())
                .sum();
    }

    // Private helper methods
    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            accountNumber = String.format("ACC%010d", random.nextInt(1000000000));
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    private void createDepositTransaction(Account account, BigDecimal amount, String description) {
        try {
            Transaction transaction = new Transaction();
            transaction.setToAccount(account);
            transaction.setAmount(amount);
            transaction.setTransactionType("DEPOSIT");
            transaction.setDescription(description);
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setStatus("COMPLETED");
            transactionRepository.save(transaction);
        } catch (Exception e) {
            // Log error but don't fail the main operation
            System.err.println("Failed to create deposit transaction: " + e.getMessage());
        }
    }

    private void createWithdrawalTransaction(Account account, BigDecimal amount, String description) {
        try {
            Transaction transaction = new Transaction();
            transaction.setFromAccount(account);
            transaction.setAmount(amount);
            transaction.setTransactionType("WITHDRAWAL");
            transaction.setDescription(description);
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setStatus("COMPLETED");
            transactionRepository.save(transaction);
        } catch (Exception e) {
            // Log error but don't fail the main operation
            System.err.println("Failed to create withdrawal transaction: " + e.getMessage());
        }
    }

    private void createTransferTransaction(Account fromAccount, Account toAccount, BigDecimal amount, String description) {
        try {
            Transaction transaction = new Transaction();
            transaction.setFromAccount(fromAccount);
            transaction.setToAccount(toAccount);
            transaction.setAmount(amount);
            transaction.setTransactionType("TRANSFER");
            transaction.setDescription(description);
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setStatus("COMPLETED");
            transactionRepository.save(transaction);
        } catch (Exception e) {
            // Log error but don't fail the main operation
            System.err.println("Failed to create transfer transaction: " + e.getMessage());
        }
    }

    // Additional utility methods
    public boolean accountExists(Long accountId) {
        return accountRepository.existsById(accountId);
    }

    public boolean accountBelongsToUser(Long accountId, Long userId) {
        return accountRepository.findById(accountId)
                .map(account -> account.getUser().getId().equals(userId))
                .orElse(false);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public void deleteAccount(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new ApiException("Account not found with id: " + accountId);
        }
        accountRepository.deleteById(accountId);
    }
}