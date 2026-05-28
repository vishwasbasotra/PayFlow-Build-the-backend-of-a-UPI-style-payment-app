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

    /*
     * @Autowired Injection Explanation (What Spring does at startup):
     * 1. Classpath Scanning: At startup, Spring searches all classes within the classpath under the main application's package
     *    and identifies components annotated with @Service, @Repository, etc.
     * 2. IoC Container Initialization: Spring manages the lifecycle of classes and creates singleton instances (Beans) inside 
     *    the ApplicationContext IoC container. For this service, it creates a TransactionServiceImplementation bean, 
     *    as well as bean implementations of TransactionRepository and UserRepository.
     * 3. Dependency Injection (DI): Spring detects the @Autowired annotations on transactionRepository and userRepository.
     *    It does a lookup by type in the container and automatically wires/injects the repository references into this
     *    service bean, making them fully operational at runtime without manual creation or passing arguments.
     */
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
