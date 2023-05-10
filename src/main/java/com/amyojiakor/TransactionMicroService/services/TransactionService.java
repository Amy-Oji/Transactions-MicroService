package com.amyojiakor.TransactionMicroService.services;

import com.amyojiakor.TransactionMicroService.models.payloads.TransactionRequest;
import com.amyojiakor.TransactionMicroService.models.payloads.TransactionResponse;
import com.amyojiakor.TransactionMicroService.models.payloads.TransferRequest;
import com.amyojiakor.TransactionMicroService.models.payloads.TransferResponse;

public interface TransactionService {
    TransactionResponse transact(TransactionRequest transactionRequest, String token) throws Exception;
    TransferResponse transfer(TransferRequest transferRequest, String token) throws Exception;
}
