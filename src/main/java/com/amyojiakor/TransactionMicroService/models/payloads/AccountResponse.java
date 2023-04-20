package com.amyojiakor.TransactionMicroService.models.payloads;



import com.amyojiakor.TransactionMicroService.models.enums.AccountType;
import com.amyojiakor.TransactionMicroService.models.enums.CurrencyCode;

import java.math.BigDecimal;

public record AccountResponse(String accountNumber, String accountName, AccountType accountType, CurrencyCode currencyCode, BigDecimal accountBalance) {


}
