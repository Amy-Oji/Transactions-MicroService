package com.amyojiakor.TransactionMicroService.models.payloads;

import com.amyojiakor.TransactionMicroService.models.enums.CurrencyCode;
import com.amyojiakor.TransactionMicroService.models.enums.TransactionType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class CreditAccountMessage {
    private String recipientAccountNumber;
    private TransactionType transactionType;
    private CurrencyCode currencyCode;
    private BigDecimal amount;
    private String referenceNumber;
}
