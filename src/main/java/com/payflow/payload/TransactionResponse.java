package com.payflow.payload;

import com.payflow.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private Long transactionId;
    private Double amount;
    private String receiverUpi;
    private String senderUpi;
    private String Note;
    private Double balance;
}
