package com.payflow.service;

import com.payflow.payload.TransactionDTO;
import com.payflow.payload.TransactionResponse;

public interface TransactionService {

    TransactionResponse transferMoney(TransactionDTO transactionDTO);
}
