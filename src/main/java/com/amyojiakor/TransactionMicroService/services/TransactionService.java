package com.amyojiakor.TransactionMicroService.services;

import com.amyojiakor.TransactionMicroService.models.payloads.*;

public interface TransactionService {
    TransactionResponse transact(TransactionRequest transactionRequest, String token) throws Exception;
    TransferResponse transfer(TransferRequest transferRequest, String token) throws Exception;
    CreditResponse creditAccount(CreditRequest request) throws Exception;
}
