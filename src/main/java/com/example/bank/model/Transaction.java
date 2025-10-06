package com.example.bank.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;
    
    @ManyToOne
    @JoinColumn(name = "to_account_id")
    private Account toAccount;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(name = "transaction_type", nullable = false)
    private String transactionType;
    
    private String description;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
    
    @Column(nullable = false)
    private String status;

    // Constructors, getters, and setters...
    public Transaction() {
        this.transactionDate = LocalDateTime.now();
        this.status = "COMPLETED";
    }
    
    public Transaction(Account fromAccount, Account toAccount, BigDecimal amount, 
                      String transactionType, String description) {
        this();
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.transactionType = transactionType;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Account getFromAccount() { return fromAccount; }
    public void setFromAccount(Account fromAccount) { this.fromAccount = fromAccount; }
    
    public Account getToAccount() { return toAccount; }
    public void setToAccount(Account toAccount) { this.toAccount = toAccount; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}