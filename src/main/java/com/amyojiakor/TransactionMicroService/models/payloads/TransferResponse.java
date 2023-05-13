package com.amyojiakor.TransactionMicroService.models.payloads;

import com.amyojiakor.TransactionMicroService.models.enums.TransactionStatus;
import com.amyojiakor.TransactionMicroService.models.enums.TransactionType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class TransferResponse {
    private BigDecimal amount;
    private String sourceAccountNumber;
    private String recipientAccountNumber;
    private TransactionType transactionType;
    private String description;
    private LocalDateTime TransactionDateTime;
    private TransactionStatus status;
    private BigDecimal sourceAccountNewBalance;
    private String referenceNumber;
}
