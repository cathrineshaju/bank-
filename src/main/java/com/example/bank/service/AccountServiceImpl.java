package com.example.bank.service;

import com.example.bank.exception.ApiException;
import com.example.bank.model.Account;
import com.example.bank.model.User;
import com.example.bank.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserService userService;
    private final Random random = new Random();

    public AccountServiceImpl(AccountRepository accountRepository, UserService userService) {
        this.accountRepository = accountRepository;
        this.userService = userService;
    }

    @Override
    public Account createAccount(Long userId) {
        User user = userService.getUserById(userId);
        
        String accountNumber;
        do {
            accountNumber = generateAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));
        
        Account account = new Account(accountNumber, "SAVINGS", user);
        return accountRepository.save(account);
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
    public void updateAccountBalance(Long accountId, Double amount) {
        Account account = getAccountById(accountId);
        BigDecimal newBalance = account.getBalance().add(BigDecimal.valueOf(amount));
        account.setBalance(newBalance);
        accountRepository.save(account);
    }

    private String generateAccountNumber() {
        return String.format("ACC%010d", random.nextInt(1000000000));
    }
}