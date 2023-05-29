package com.amyojiakor.TransactionMicroService.models.payloads;

import com.amyojiakor.TransactionMicroService.models.enums.CurrencyCode;
import com.amyojiakor.TransactionMicroService.models.enums.TransactionStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
public class CreditResponse {
    private TransactionStatus status;
    private String recipientAccountNumber;
    private CurrencyCode currencyCode;
    private BigDecimal amountSent;
    private String senderName;
    private String description;
}
