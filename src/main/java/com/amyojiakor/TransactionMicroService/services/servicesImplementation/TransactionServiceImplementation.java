package com.amyojiakor.TransactionMicroService.services.servicesImplementation;

import com.amyojiakor.TransactionMicroService.config.ApiConfig;
import com.amyojiakor.TransactionMicroService.models.entities.Transaction;
import com.amyojiakor.TransactionMicroService.models.enums.TransactionStatus;
import com.amyojiakor.TransactionMicroService.models.enums.TransactionType;
import com.amyojiakor.TransactionMicroService.models.payloads.*;
import com.amyojiakor.TransactionMicroService.repositories.TransactionRepository;
import com.amyojiakor.TransactionMicroService.services.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
//@RequiredArgsConstructor
public class TransactionServiceImplementation implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final ApiConfig apiConfig;
    private final RestTemplate restTemplate;
    private final String transactionCreationTopic;

    private final KafkaTemplate<String, TransactionMessage> kafkaTemplate;

    @Autowired
    public TransactionServiceImplementation(TransactionRepository transactionRepository, ApiConfig apiConfig, RestTemplate restTemplate,  @Value("${kafka.topic.transaction-creation}") String transactionCreationTopic, KafkaTemplate<String, TransactionMessage> kafkaTemplate) {
        this.transactionRepository = transactionRepository;
        this.apiConfig = apiConfig;
        this.restTemplate = restTemplate;
        this.transactionCreationTopic = transactionCreationTopic;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    @Override
    public TransactionResponse transact(TransactionRequest transactionRequest, String token) throws Exception {
        UserDetailsResponse user = getUser(token);
        if (user == null) {
            // Handle case where account is not found
            throw new Exception("User not found");
        }

        var account = findAccount(transactionRequest, user);
        if (account == null) {
            throw new Exception("No such account");
        }

        TransactionMessage message = new TransactionMessage(account.accountNumber(), transactionRequest.transactionType(), transactionRequest.amount());

        kafkaTemplate.send(transactionCreationTopic, message);

        Transaction transaction = createTransaction(transactionRequest, account);

        transactionRepository.save(transaction);

        return setTransactionResponse(transaction, account, transactionRequest);
    }

    private UserDetailsResponse getUser(String token) {
        byte[] decodedToken = Base64.getUrlDecoder().decode(token);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(new String(decodedToken));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<UserDetailsResponse> responseEntity =
                restTemplate.exchange(
                        apiConfig.getUserServiceBaseUrl() + "get-user-details",
                        HttpMethod.GET,
                        entity,
                        UserDetailsResponse.class);

        return responseEntity.getBody();
    }

    private UserAccounts findAccount(TransactionRequest request, UserDetailsResponse user) {
        return user.userAccounts().stream()
                .filter(account -> account.accountNumber().equals(request.accountNumber()))
                .findFirst()
                .orElse(null);
    }

    private Transaction createTransaction(TransactionRequest transactionRequest, UserAccounts account) throws Exception {

        Transaction transaction = new Transaction();
        BigDecimal previousBalance = account.accountBalance();
        transaction.setPreviousBalance(previousBalance);
        transaction.setAmount(transactionRequest.amount());
        transaction.setDescription(transactionRequest.description());
        transaction.setTransactionType(transactionRequest.transactionType());
        transaction.setAccountNum(account.accountNumber());
        transaction.setTransactionDateTime(LocalDateTime.now());

        if (transactionRequest.transactionType() == TransactionType.CREDIT) {
            transaction.setNewBalance(previousBalance.add(transactionRequest.amount()));
            transaction.setStatus(TransactionStatus.COMPLETED);
        } else {
            if (transactionRequest.amount().compareTo(previousBalance) > 0) {
                transaction.setStatus(TransactionStatus.FAILED);
                throw new Exception("Insufficient Funds");
            }
            transaction.setNewBalance(previousBalance.subtract(transactionRequest.amount()));
            transaction.setStatus(TransactionStatus.COMPLETED);
        }
        return transaction;
    }

    private TransactionResponse setTransactionResponse(Transaction transaction, UserAccounts account, TransactionRequest transactionRequest) {
        return new TransactionResponse(
                transaction.getStatus(),
                account.accountNumber(),
                account.accountType(),
                account.currencyCode(),
                transactionRequest.transactionType(),
                transactionRequest.description(),
                transaction.getNewBalance()
        );
    }
}
