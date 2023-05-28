package com.amyojiakor.TransactionMicroService.models.entities;

import com.amyojiakor.TransactionMicroService.models.enums.CurrencyCode;
import com.amyojiakor.TransactionMicroService.models.enums.TransactionStatus;
import com.amyojiakor.TransactionMicroService.models.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private BigDecimal amount;

    private String sourceAccountNumber;

    private String recipientAccountNumber;

    private BigDecimal sourceAccountPreviousBalance;

    private BigDecimal sourceAccountNewBalance;

    private BigDecimal recipientAccountPreviousBalance;

    private BigDecimal recipientAccountNewBalance;

    @Enumerated(value = EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(value = EnumType.STRING)
    private CurrencyCode currencyCode;

    private String description;

    private LocalDateTime TransactionDateTime;

    private String referenceNumber;

    @Enumerated(value = EnumType.STRING)
    private TransactionStatus status;
}
