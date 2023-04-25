package com.amyojiakor.TransactionMicroService.services.servicesImplementation;

import com.amyojiakor.TransactionMicroService.apiConfig.ApiConfig;
import com.amyojiakor.TransactionMicroService.models.entities.Transaction;
import com.amyojiakor.TransactionMicroService.models.enums.TransactionStatus;
import com.amyojiakor.TransactionMicroService.models.enums.TransactionType;
import com.amyojiakor.TransactionMicroService.models.payloads.AccountResponse;
import com.amyojiakor.TransactionMicroService.models.payloads.TransactionRequest;
import com.amyojiakor.TransactionMicroService.models.payloads.TransactionResponse;
import com.amyojiakor.TransactionMicroService.repositories.TransactionRepository;
import com.amyojiakor.TransactionMicroService.services.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionServiceImplementation implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final ApiConfig apiConfig;
    private final RestTemplate restTemplate;


    @Transactional
    @Override
    public TransactionResponse transact(TransactionRequest transactionRequest) throws Exception {

        AccountResponse accountResponse = getAccount(transactionRequest.accountNumber());

        Transaction transaction = new Transaction();
        if(accountResponse != null){
            transaction.setPreviousBalance(accountResponse.accountBalance());
//            BigDecimal balance = transaction.getAccountBalBeforeTransaction();
            if(transactionRequest.transactionType().equals(TransactionType.CREDIT)) {
                transaction.setNewBalance(transaction.getPreviousBalance().add(transactionRequest.amount()));
                transaction.setStatus(TransactionStatus.COMPLETED);
            }else{
                if(transactionRequest.amount().compareTo(transaction.getPreviousBalance()) > 0){
                    transaction.setStatus(TransactionStatus.FAILED);
                    throw new Exception("Insufficient Funds");
                }
                transaction.setNewBalance(transaction.getPreviousBalance().subtract(transactionRequest.amount()));
                transaction.setStatus(TransactionStatus.COMPLETED);
            }
        }
        transaction.setAmount(transactionRequest.amount());
        transaction.setDescription(transactionRequest.description());
        transaction.setTransactionType(transactionRequest.transactionType());
        transaction.setAccountNum(transactionRequest.accountNumber());
        transaction.setTransactionDateTime(LocalDateTime.now());
        transactionRepository.save(transaction);
        assert accountResponse != null;
        return setTransactionResponse(transaction, accountResponse, transactionRequest);
    }

    private AccountResponse getAccount(String accountNum){
        String url = apiConfig.getAccountServiceBaseUrl() + "get-account/"+ accountNum;
        return restTemplate.getForObject(url, AccountResponse.class,accountNum);
    }

    private TransactionResponse setTransactionResponse(Transaction transaction, AccountResponse accountResponse, TransactionRequest transactionRequest){
        return new TransactionResponse(
                transaction.getStatus(),
                accountResponse.accountNumber(),
                accountResponse.accountName(),
                accountResponse.accountType(),
                accountResponse.currencyCode(),
                transactionRequest.transactionType(),
                transactionRequest.description(),
                transaction.getNewBalance()
        );
    }


}
