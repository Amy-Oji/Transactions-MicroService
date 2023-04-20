package com.amyojiakor.TransactionMicroService.controllers;

import com.amyojiakor.TransactionMicroService.models.payloads.TransactionRequest;
import com.amyojiakor.TransactionMicroService.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("transaction")
@RequiredArgsConstructor
public class TransactionController {
   private final TransactionService transactionService;

    @PostMapping("/transact")
    public ResponseEntity<?> transact(@RequestBody TransactionRequest transactionRequest) throws Exception {
        return ResponseEntity.ok(transactionService.transact(transactionRequest));
    }

}
