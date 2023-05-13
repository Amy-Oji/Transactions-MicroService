package com.amyojiakor.TransactionMicroService.models.payloads;



import com.amyojiakor.TransactionMicroService.models.enums.AccountType;
import com.amyojiakor.TransactionMicroService.models.enums.CurrencyCode;
import com.amyojiakor.TransactionMicroService.models.enums.TransactionStatus;
import com.amyojiakor.TransactionMicroService.models.enums.TransactionType;

import java.math.BigDecimal;

public record TransactionResponse(TransactionStatus status,
                                  String accountNumber,
                                  AccountType accountType,
                                  CurrencyCode currencyCode,
                                  TransactionType transactionType,
                                  String description,
                                  BigDecimal accountBalance ) {

}
