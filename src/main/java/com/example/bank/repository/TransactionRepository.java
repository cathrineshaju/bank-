package com.example.bank.repository;

import com.example.bank.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Find transactions by account (either as sender or receiver)
    List<Transaction> findByFromAccountIdOrToAccountId(Long fromAccountId, Long toAccountId);
    
    // Find transactions by multiple accounts
    List<Transaction> findByFromAccountIdInOrToAccountIdIn(List<Long> fromAccountIds, List<Long> toAccountIds);
    
    // Find transactions by type
    List<Transaction> findByTransactionTypeOrderByTransactionDateDesc(String transactionType);
    
    // Find transactions by account and type
    List<Transaction> findByFromAccountIdAndTransactionType(Long fromAccountId, String transactionType);
    List<Transaction> findByToAccountIdAndTransactionType(Long toAccountId, String transactionType);
    
    // Find transactions by date range
    List<Transaction> findByTransactionDateBetweenOrderByTransactionDateDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find transactions by account and date range
    List<Transaction> findByFromAccountIdOrToAccountIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            Long fromAccountId, Long toAccountId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find recent transactions with pagination
    Page<Transaction> findAllByOrderByTransactionDateDesc(Pageable pageable);
    
    // Custom query for transaction statistics
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.toAccount.id = :accountId AND t.transactionType = 'DEPOSIT'")
    BigDecimal sumDepositsByAccount(@Param("accountId") Long accountId);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.fromAccount.id = :accountId AND t.transactionType = 'WITHDRAWAL'")
    BigDecimal sumWithdrawalsByAccount(@Param("accountId") Long accountId);
    
    // Find transactions with account details eagerly loaded
    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.fromAccount LEFT JOIN FETCH t.toAccount WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId ORDER BY t.transactionDate DESC")
    List<Transaction> findTransactionsByAccountWithDetails(@Param("accountId") Long accountId);
}