package com.amyojiakor.TransactionMicroService.controllers;

import com.amyojiakor.TransactionMicroService.models.payloads.TransactionRequest;
import com.amyojiakor.TransactionMicroService.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("transaction")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/transact")
    public ResponseEntity<?> transact(@RequestBody TransactionRequest transactionRequest, @RequestHeader("Authorization") String token) throws Exception {
        String encodedToken = Base64.getUrlEncoder().withoutPadding().encodeToString(token.getBytes());
        return ResponseEntity.ok(transactionService.transact(transactionRequest, encodedToken));
    }

}
