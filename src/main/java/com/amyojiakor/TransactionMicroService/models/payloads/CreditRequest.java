package com.amyojiakor.TransactionMicroService.models.payloads;

import com.amyojiakor.TransactionMicroService.models.enums.CurrencyCode;
import com.amyojiakor.TransactionMicroService.models.enums.TransactionType;

import java.math.BigDecimal;

public record CreditRequest(String recipientAccountNumber,
                            TransactionType transactionType,
                            CurrencyCode currencyCode,
                            BigDecimal amount,
                            String senderName,
                            String description) {
}
