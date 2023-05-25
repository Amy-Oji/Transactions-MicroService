package com.amyojiakor.TransactionMicroService.models.payloads;


import com.amyojiakor.TransactionMicroService.models.enums.CurrencyCode;

public record AccountDetailsApiResponse(String accountNumber, String accountName, CurrencyCode currencyCode){
}
