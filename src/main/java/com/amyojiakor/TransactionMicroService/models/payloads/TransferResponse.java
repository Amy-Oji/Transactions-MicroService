package com.amyojiakor.TransactionMicroService.models.payloads;

import com.amyojiakor.TransactionMicroService.models.enums.AccountType;
import com.amyojiakor.TransactionMicroService.models.enums.CurrencyCode;
import com.amyojiakor.TransactionMicroService.models.enums.TransactionStatus;
import com.amyojiakor.TransactionMicroService.models.enums.TransactionType;

import java.math.BigDecimal;

public record TransferResponse(TransactionStatus status,
                               String sourceAccountNumber,
                               String recipientAccountNumber,
                               TransactionType transactionType,
                               String description,
                               BigDecimal sourceAccountNewBalance ) {
}
