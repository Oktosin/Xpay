package com.tosin.xpay.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.tosin.xpay.constant.TransactionStatus;
import com.tosin.xpay.constant.TransactionType;
import com.tosin.xpay.dao.AccountDAO;
import com.tosin.xpay.dao.TransactionDAO;
import com.tosin.xpay.dto.DepositRequest;
import com.tosin.xpay.dto.RequestPayload;
import com.tosin.xpay.dto.TransferRequest;
import com.tosin.xpay.dto.WithdrawalRequest;
import com.tosin.xpay.model.Account;
import com.tosin.xpay.model.Transaction;
import com.tosin.xpay.service.TransactionService;
import com.tosin.xpay.service.UtilService;

import jakarta.transaction.Transactional;

@Service
public class TransactionServiceImpl implements TransactionService {
	
	@Autowired private TransactionDAO transactionDAO;
	@Autowired private AccountDAO accountDAO;
	@Autowired private UtilService utilService;
	
	
	Map<String, Object> response = new LinkedHashMap<>();
	
	@Override
	@Transactional
	public ResponseEntity<?> transfer(TransferRequest transferRequest) {
		
		Account sender = accountDAO.findAccountNumber(transferRequest.getSenderAccountNumber());
		
		if(sender == null) {
			
			response.put("message", "sender account not found");
		}
		
									
		Account receiver = accountDAO.findAccountNumber(transferRequest.getReceiverAccountNumber());
		
		if(receiver == null) {
					
					response.put("message", "receiver account not found");
		}
		
		BigDecimal amount = transferRequest.getAmount();
		
		if (amount.compareTo(BigDecimal.ZERO)<= 0) {
			response.put("message", "invalid amount");
		}
		
		if(sender.getBalance().compareTo(amount) < 0) {
			response.put("mesage", "insufficient balance");
		}
		
		//DEBIT SENDER
		sender.setBalance(sender.getBalance().subtract(amount));
		
		//CREDIT RECEIVER
		receiver.setBalance(receiver.getBalance().add(amount));
		
		accountDAO.save(sender);
		accountDAO.save(receiver);
		
		//SAVE TRANSACTION
		Transaction transaction = Transaction.builder()
				.reference(UUID.randomUUID().toString())
				.senderAccountNumber(sender.getAccountNumber())
				.receiverAccountNumber(receiver.getAccountNumber())
				.description(transferRequest.getDescription())
				.transactionType(TransactionType.TRANSFER)
				.transactionStatus(TransactionStatus.SUCCESS)
				.createdAt(LocalDateTime.now())
				.build();
		
		Transaction savedTransaction = transactionDAO.save(transaction);
				
		response.put("transactionDetail", savedTransaction);
		response.put("message", "transfer successful");
		
		return utilService.getResponse(response, HttpStatus.OK);
		
	}
	
	@Override
	@Transactional
	public ResponseEntity<?> deposit(DepositRequest depositRequest) {
		
		Account account = accountDAO.findAccountNumber(depositRequest.getAccountNumber());
		
		if(account == null) {
			
			response.put("message", "account not found");
		}
		
		BigDecimal amount = depositRequest.getAmount();
		
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			
			response.put("message", "invalid amount");
		}
		
		//CREDIT ACCOUNT
		account.setBalance(account.getBalance().add(amount));;
		
		accountDAO.save(account);
		
		//SAVE TRANSACTION
		Transaction transaction = Transaction.builder()
				.reference(UUID.randomUUID().toString())
				.receiverAccountNumber(depositRequest.getAccountNumber())
				.amount(amount)
				.description(depositRequest.getDescription())
				.transactionType(TransactionType.DEPOSIT)
				.transactionStatus(TransactionStatus.SUCCESS)
				.createdAt(LocalDateTime.now())
				.build();
		
		Transaction savedTransaction = transactionDAO.save(transaction);
		
		response.put("transactionDetail", savedTransaction);
		response.put("message", "account credited");
		
		return utilService.getResponse(response, HttpStatus.OK);
		
	}
	
	@Override
	@Transactional
	public ResponseEntity<?> withdrawal(WithdrawalRequest withdrawalRequest) {
		
		Account account = accountDAO.findAccountNumber(withdrawalRequest.getAccountNumber());
		
		if (account == null) {
			
			response.put("message", "account not found");
		}
		
		BigDecimal amount = withdrawalRequest.getAmount();
		
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			
			response.put("message", "invalid amount");
		}
		
		if (account.getBalance().compareTo(amount) < 0) {
			response.put("message", "insufficient balance");
		}
		
		//DEBIT ACCOUNT
		account.setBalance(account.getBalance().subtract(amount));
		
		//SAVE TRANSACTION
		Transaction transaction = Transaction.builder()
				.reference(UUID.randomUUID().toString())
				.senderAccountNumber(withdrawalRequest.getAccountNumber())
				.amount(withdrawalRequest.getAmount())
				.description(withdrawalRequest.getDescription())
				.transactionType(TransactionType.WITDRAW)
				.transactionStatus(TransactionStatus.SUCCESS)
				.createdAt(LocalDateTime.now())
				.build();
		
		Transaction savedTransaction = transactionDAO.save(transaction);
		
		response.put("transactionDetail", savedTransaction);
		response.put("message", "withdrawal successful");
		
		return utilService.getResponse(response, HttpStatus.OK);
	}
	
	
	@Override
	public ResponseEntity<?> getTransactionHistory(String accountNumber, RequestPayload requestPayload) {
		
		Account existingAccount = accountDAO.findByAccountNumber(accountNumber);
		
		if (existingAccount == null) {
			
			response.put("errorMessage", "account not found");
			
			return utilService.getResponse(response, HttpStatus.NOT_FOUND);
		}
		
		int pageNumber = requestPayload.getPageNumber();
		int pageSize = utilService.pageSizeLimit(requestPayload.getPageSize());
		
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Page<Transaction> transactions = transactionDAO.findTransactionHistoryByAccountNumber(accountNumber,pageable);
		
		response.put("transactionHistory", transactions.getContent());
		response.put("currentPage", transactions.getNumber());
		response.put("totalPages", transactions.getTotalPages());
		response.put("totalElements", transactions.getTotalElements());

		return utilService.getResponse(response, HttpStatus.OK);
	}
 
}
