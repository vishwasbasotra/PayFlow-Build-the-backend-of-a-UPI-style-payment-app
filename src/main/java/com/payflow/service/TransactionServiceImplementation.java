package com.payflow.service;

import com.payflow.entity.Transaction;
import com.payflow.entity.User;
import com.payflow.exceptions.APIException;
import com.payflow.exceptions.ResourceNotFoundException;
import com.payflow.payload.TransactionDTO;
import com.payflow.payload.TransactionResponse;
import com.payflow.repository.TransactionRepository;
import com.payflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImplementation implements TransactionService{

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    private TransactionDTO transactionToTransactionDTO(Transaction transaction){
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setTransactionId(transaction.getTransactionId());
        transactionDTO.setAmount(transaction.getAmount());
        transactionDTO.setSenderUpiId(transaction.getSenderUpiId());
        transactionDTO.setReceiverUpiId(transaction.getReceiverUpiId());
        transactionDTO.setNote(transaction.getNote());

        return transactionDTO;
    }

    private Transaction transactionDtoToTransaction(TransactionDTO transactionDTO){
        Transaction transaction = new Transaction();
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setSenderUpiId(transactionDTO.getSenderUpiId());
        transaction.setReceiverUpiId(transactionDTO.getReceiverUpiId());
        transaction.setNote(transactionDTO.getNote());

        return transaction;
    }

    @Override
    public TransactionResponse transferMoney(TransactionDTO transactionDTO) {
        Transaction transaction = transactionDtoToTransaction(transactionDTO);
        String senderUpi = transaction.getSenderUpiId();
        String receiverUpi = transaction.getReceiverUpiId();

        User sender = userRepository.findByUpiId(senderUpi);
        if(sender == null)  throw new ResourceNotFoundException("Sender", "upiId", senderUpi);
        User receiver = userRepository.findByUpiId(receiverUpi);
        if(receiver == null)  throw new ResourceNotFoundException("Receiver", "upiId", receiverUpi);

        if(transaction.getAmount() > sender.getBalance())    throw new APIException("Send amount less than: "+sender.getBalance());

        sender.setBalance(sender.getBalance() - transaction.getAmount());
        receiver.setBalance(receiver.getBalance() + transaction.getAmount());

        userRepository.save(sender);
        userRepository.save(receiver);

        transactionRepository.save(transaction);

        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setTransactionId(transaction.getTransactionId());
        transactionResponse.setAmount(transaction.getAmount());
        transactionResponse.setSenderUpi(senderUpi);
        transactionResponse.setReceiverUpi(receiverUpi);
        transactionResponse.setNote(transaction.getNote());
        transactionResponse.setBalance(receiver.getBalance());

        return transactionResponse;
    }
}
