package com.amyojiakor.TransactionMicroService.models.payloads;


import com.amyojiakor.TransactionMicroService.models.enums.TransactionType;

import java.math.BigDecimal;

public record TransactionRequest(String accountNumber, TransactionType transactionType, BigDecimal amount, String description){
}
