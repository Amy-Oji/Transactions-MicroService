package com.amyojiakor.TransactionMicroService.models.payloads;

import com.amyojiakor.TransactionMicroService.models.enums.TransactionType;

import java.math.BigDecimal;

public record TransactionMessage (String accountNum, TransactionType transactionType, BigDecimal amount){
}
