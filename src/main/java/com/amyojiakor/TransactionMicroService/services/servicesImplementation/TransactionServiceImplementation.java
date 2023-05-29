package com.amyojiakor.TransactionMicroService.services.servicesImplementation;

import com.amyojiakor.TransactionMicroService.config.ApiConfig;
import com.amyojiakor.TransactionMicroService.models.entities.Transaction;
import com.amyojiakor.TransactionMicroService.models.enums.CurrencyCode;
import com.amyojiakor.TransactionMicroService.models.enums.TransactionStatus;
import com.amyojiakor.TransactionMicroService.models.enums.TransactionType;
import com.amyojiakor.TransactionMicroService.models.payloads.*;
import com.amyojiakor.TransactionMicroService.repositories.TransactionRepository;
import com.amyojiakor.TransactionMicroService.services.TransactionService;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionServiceImplementation implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final ApiConfig apiConfig;
    private final RestTemplate restTemplate;
    private final String transactionCreationTopic;
    private final KafkaTemplate<String, TransactionMessage> kafkaTemplate;
    private final KafkaTemplate<String, CreditAccountMessage> creditAccountKafkaTemplate;
    private final String creditAccountTopic;





    @Autowired
    public TransactionServiceImplementation(TransactionRepository transactionRepository,
                                            ApiConfig apiConfig, RestTemplate restTemplate,
                                            @Value("${kafka.topic.transaction.creation}") String transactionCreationTopic,
                                            KafkaTemplate<String, TransactionMessage> kafkaTemplate,
                                            KafkaTemplate<String, CreditAccountMessage> creditAccountKafkaTemplate,
                                            @Value("${kafka.topic.transaction.credit-account}") String creditAccountTopic) {
        this.transactionRepository = transactionRepository;
        this.apiConfig = apiConfig;
        this.restTemplate = restTemplate;
        this.transactionCreationTopic = transactionCreationTopic;
        this.kafkaTemplate = kafkaTemplate;
        this.creditAccountKafkaTemplate = creditAccountKafkaTemplate;
        this.creditAccountTopic = creditAccountTopic;
    }

    @Transactional
    @Override
    public TransactionResponse transact(TransactionRequest transactionRequest, String token) throws Exception {
        UserDetailsResponse user = getUser(token);
        if (user == null) {
            // Handle case where account is not found
            throw new Exception("User not found");
        }

        var account = findAccount(transactionRequest.accountNumber(), user);
        if (account == null) {
            throw new Exception("No such account");
        }

        TransactionMessage message = new TransactionMessage(account.accountNumber(), null, transactionRequest.transactionType(), transactionRequest.amount(), null);

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

    private AccountDetails findAccount(String accountNum, UserDetailsResponse user) {
        return user.userAccounts().stream()
                .filter(account -> account.accountNumber().equals(accountNum))
                .findFirst()
                .orElse(null);
    }

    private Transaction createTransaction(TransactionRequest transactionRequest, AccountDetails account) throws Exception {

        Transaction transaction = new Transaction();
        BigDecimal previousBalance = account.accountBalance();
        transaction.setSourceAccountPreviousBalance(previousBalance);
        transaction.setAmount(transactionRequest.amount());
        transaction.setDescription(transactionRequest.description());
        transaction.setTransactionType(transactionRequest.transactionType());
        transaction.setSourceAccountNumber(account.accountNumber());
        transaction.setTransactionDateTime(LocalDateTime.now());

        if (transactionRequest.transactionType() == TransactionType.CREDIT) {
            transaction.setSourceAccountNewBalance(previousBalance.add(transactionRequest.amount()));
            transaction.setStatus(TransactionStatus.COMPLETED);
        } else {
            if (transactionRequest.amount().compareTo(previousBalance) > 0) {
                transaction.setStatus(TransactionStatus.FAILED);
                throw new Exception("Insufficient Funds");
            }
            transaction.setSourceAccountNewBalance(previousBalance.subtract(transactionRequest.amount()));
            transaction.setStatus(TransactionStatus.COMPLETED);
        }
        return transaction;
    }

    private TransactionResponse setTransactionResponse(Transaction transaction, AccountDetails account, TransactionRequest transactionRequest) {
        return new TransactionResponse(
                transaction.getStatus(),
                account.accountNumber(),
                account.accountType(),
                account.currencyCode(),
                transactionRequest.transactionType(),
                transactionRequest.description(),
                transaction.getSourceAccountNewBalance()
        );
    }
    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest transferRequest, String token) throws Exception {

        TransferResponse transferResponse = new TransferResponse();
        UserDetailsResponse user = getUser(token);

        var sourceAccount = findAccount(transferRequest.sourceAccountNumber(), user);
        if (sourceAccount== null){
            throw new Exception("Invalid Source Account");
        }

        if (transferRequest.transactionType().equals(TransactionType.INTERNAL_TRANSFER)){
            AccountDetailsApiResponse recipientAccount = getAccount(transferRequest.recipientAccountNumber());
            if (recipientAccount== null){
                throw new Exception("Invalid Recipient Account");
            }
            transferResponse = processInternalTransfer(transferRequest, sourceAccount);
            TransactionMessage message = new TransactionMessage(sourceAccount.accountNumber(), recipientAccount.accountNumber(), transferRequest.transactionType(), transferRequest.amount(), transferResponse.getReferenceNumber());
            kafkaTemplate.send(transactionCreationTopic, message);
        }
        return transferResponse;
    }

    @Override
    @Transactional
    public CreditResponse creditAccount(CreditRequest request) throws Exception {

        Transaction transaction = new Transaction();
        CreditResponse creditResponse = new CreditResponse();
        CreditAccountMessage creditAccountMessage = new CreditAccountMessage();

        var account = getAccount(request.recipientAccountNumber());

        if(!request.currencyCode().equals(account.currencyCode())){
            throw new Exception("Currency Code does not match");
        }

        String ref = generateReferenceNumber();
        while(!isUnique(ref)) ref = generateReferenceNumber();

        transaction.setReferenceNumber(ref);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setTransactionDateTime(LocalDateTime.now());

        BeanUtils.copyProperties(request, transaction);

        BeanUtils.copyProperties(request, creditResponse);
        creditResponse.setAmountSent(request.amount());
        creditResponse.setStatus(TransactionStatus.PENDING);

        BeanUtils.copyProperties(transaction, creditAccountMessage);

        transactionRepository.save(transaction);

        creditAccountKafkaTemplate.send(creditAccountTopic, creditAccountMessage);

        return creditResponse;
    }

    private TransferResponse processInternalTransfer(TransferRequest transferRequest, AccountDetails sourceAccount) throws Exception {

        TransferResponse transferResponse = new TransferResponse();
        Transaction transaction = setTransaction(transferRequest, sourceAccount);

        if (transferRequest.amount().compareTo(transaction.getSourceAccountPreviousBalance()) > 0) {
            transaction.setStatus(TransactionStatus.FAILED);
            throw new Exception("Insufficient Funds");
        }
        transaction.setSourceAccountNewBalance(transaction.getSourceAccountPreviousBalance().subtract(transferRequest.amount()));
        transaction.setStatus(TransactionStatus.PENDING);

        String ref = generateReferenceNumber();
        while(!isUnique(ref)) ref = generateReferenceNumber();
        transaction.setReferenceNumber(ref);

        BeanUtils.copyProperties(transaction, transferResponse);

        transactionRepository.save(transaction);

        return transferResponse;
    }

    private Transaction setTransaction(TransferRequest transferRequest, AccountDetails sourceAccount){
        Transaction transaction = new Transaction();
        BeanUtils.copyProperties(transferRequest, transaction);
        BigDecimal previousBalance = sourceAccount.accountBalance();
        transaction.setSourceAccountPreviousBalance(previousBalance);
        transaction.setTransactionDateTime(LocalDateTime.now());
        return transaction;
    }

    private AccountDetailsApiResponse getAccount(String accountNumber){
        return restTemplate.getForObject(
                apiConfig.getAccountServiceBaseUrl()+"get-account/{accountNumber}",
                AccountDetailsApiResponse.class, accountNumber);
    }

    private String generateReferenceNumber() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    private Boolean isUnique(String refNum) {
        Optional<Transaction> transaction = transactionRepository.findByReferenceNumber(refNum);
        return transaction.isEmpty();
    }

    @Transactional
    @KafkaListener(topics = "${kafka.topic.transaction.balance-update.account-service}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void consume(TransactionMessageResponse transactionMessageResponse) throws Exception {
        System.out.println(transactionMessageResponse);
        var transaction = transactionRepository.findByReferenceNumber(transactionMessageResponse.getReferenceNumber()).orElseThrow();
        if(transaction.getSourceAccountNewBalance().equals(transactionMessageResponse.getNewAccountBalance())) {
            transaction.setStatus(TransactionStatus.COMPLETED);
        } else {
            transaction.setSourceAccountNewBalance(transaction.getSourceAccountPreviousBalance());
            transaction.setStatus(TransactionStatus.FAILED);
        }
        transactionRepository.save(transaction);

        System.out.println(LocalDateTime.now()+ "====response time======");
    }



}
