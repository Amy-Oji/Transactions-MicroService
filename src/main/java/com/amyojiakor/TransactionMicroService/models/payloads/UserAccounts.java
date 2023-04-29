package com.amyojiakor.TransactionMicroService.models.payloads;


import com.amyojiakor.TransactionMicroService.models.enums.AccountType;
import com.amyojiakor.TransactionMicroService.models.enums.CurrencyCode;

import java.math.BigDecimal;


public record UserAccounts (String accountNumber, AccountType accountType, CurrencyCode currencyCode, BigDecimal accountBalance){

}
