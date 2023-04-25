package com.amyojiakor.TransactionMicroService.models.entities;

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

    private String accountNum;

    private BigDecimal previousBalance;

    private BigDecimal newBalance;

    @Enumerated(value = EnumType.STRING)
    private TransactionType transactionType;

    private BigDecimal amount;

    private LocalDateTime TransactionDateTime;

    @Enumerated(value = EnumType.STRING)
    private TransactionStatus status;

    private String description;
}
