package com.amyojiakor.TransactionMicroService.models.payloads;


import com.amyojiakor.TransactionMicroService.models.enums.AccountType;
import com.amyojiakor.TransactionMicroService.models.enums.CurrencyCode;

public record UpdateAccountRequest(String accountName, AccountType accountType, CurrencyCode currencyCode, String accountNumber) {
}
