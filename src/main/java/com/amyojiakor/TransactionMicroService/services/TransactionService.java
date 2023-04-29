package com.amyojiakor.TransactionMicroService.services;

import com.amyojiakor.TransactionMicroService.models.payloads.TransactionRequest;
import com.amyojiakor.TransactionMicroService.models.payloads.TransactionResponse;

public interface TransactionService {
    TransactionResponse transact(TransactionRequest transactionRequest, String token) throws Exception;
}
