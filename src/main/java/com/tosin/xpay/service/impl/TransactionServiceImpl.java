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
import com.tosin.xpay.dao.CustomerDAO;
import com.tosin.xpay.dao.TransactionDAO;
import com.tosin.xpay.dto.DepositRequest;
import com.tosin.xpay.dto.RequestPayload;
import com.tosin.xpay.dto.ReversalRequest;
import com.tosin.xpay.dto.TransferRequest;
import com.tosin.xpay.dto.WithdrawalRequest;
import com.tosin.xpay.model.Account;
import com.tosin.xpay.model.Customer;
import com.tosin.xpay.model.Transaction;
import com.tosin.xpay.service.EmailNotificationService;
import com.tosin.xpay.service.TransactionService;
import com.tosin.xpay.service.UtilService;

import jakarta.transaction.Transactional;

@Service
public class TransactionServiceImpl implements TransactionService {
	
	@Autowired private TransactionDAO transactionDAO;
	@Autowired private AccountDAO accountDAO;
	@Autowired private CustomerDAO customerDAO;
	@Autowired private UtilService utilService;
	@Autowired private EmailNotificationService emailNotificationService;
	
	Map<String, Object> response = new LinkedHashMap<>();
	
	@Override
	@Transactional
	public ResponseEntity<?> transfer(TransferRequest transferRequest) {

		if (transferRequest == null) {
			
			response.put("message", "transfer request is required");
			
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}
		
		Transaction existingIdempotentTransaction = findIdempotentTransaction(transferRequest.getIdempotencyKey());

		if (existingIdempotentTransaction != null) {
			response.put("transactionDetail", existingIdempotentTransaction);
			response.put("message", "idempotent transfer response");
			return utilService.getResponse(response, HttpStatus.OK);
		}

		Account sender = accountDAO.findAccountNumber(transferRequest.getSenderAccountNumber());
		
		if(sender == null) {
			
			response.put("message", "sender account not found");
			
			return utilService.getResponse(response, HttpStatus.NOT_FOUND);
		}
		
		Account receiver = accountDAO.findAccountNumber(transferRequest.getReceiverAccountNumber());
		
		if(receiver == null) {
			
			response.put("message", "receiver account not found");
			
			return utilService.getResponse(response, HttpStatus.NOT_FOUND);
		}

		Customer senderCustomer = customerDAO.findCustomerByAccount(sender).orElse(null);
		Customer receiverCustomer = customerDAO.findCustomerByAccount(receiver).orElse(null);

		if (senderCustomer == null) {
			
			response.put("message", "sender customer profile not found");
			
			return utilService.getResponse(response, HttpStatus.NOT_FOUND);
		}

		if (receiverCustomer == null) {
			
			response.put("message", "receiver customer profile not found");
			
			return utilService.getResponse(response, HttpStatus.NOT_FOUND);
		}
		
		BigDecimal amount = transferRequest.getAmount();
		
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			
			response.put("message", "invalid amount");
			
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}
		
		if(sender.getBalance().compareTo(amount) < 0) {
			
			response.put("message", "insufficient balance");
			
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}
		
		sender.setBalance(sender.getBalance().subtract(amount));
		receiver.setBalance(receiver.getBalance().add(amount));
		
		accountDAO.save(sender);
		accountDAO.save(receiver);
		
		Transaction transaction = Transaction.builder()
				.reference(UUID.randomUUID().toString())
				.senderAccountNumber(sender.getAccountNumber())
				.receiverAccountNumber(receiver.getAccountNumber())
				.idempotencyKey(blankToNull(transferRequest.getIdempotencyKey()))
				.amount(amount)
				.customer(senderCustomer)
				.description(safeText(transferRequest.getDescription()))
				.transactionType(TransactionType.TRANSFER)
				.transactionStatus(TransactionStatus.SUCCESS)
				.createdAt(LocalDateTime.now())
				.build();
		
		Transaction savedTransaction = transactionDAO.save(transaction);

		emailNotificationService.sendTransactionNotification(senderCustomer.getUserData(), TransactionType.TRANSFER.name(), amount, sender.getAccountNumber(), transferRequest.getDescription());
		emailNotificationService.sendTransactionNotification(receiverCustomer.getUserData(), TransactionType.TRANSFER.name(), amount, receiver.getAccountNumber(), transferRequest.getDescription());
				
		response.put("transactionDetail", savedTransaction);
		response.put("message", "transfer successful");
		
		return utilService.getResponse(response, HttpStatus.OK);
		
	}
	
	@Override
	@Transactional
	public ResponseEntity<?> deposit(DepositRequest depositRequest) {

		if (depositRequest == null) {
			
			response.put("message", "deposit request is required");
			
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}
		
		Transaction existingIdempotentTransaction = findIdempotentTransaction(depositRequest.getIdempotencyKey());

		if (existingIdempotentTransaction != null) {
			response.put("transactionDetail", existingIdempotentTransaction);
			response.put("message", "idempotent deposit response");
			return utilService.getResponse(response, HttpStatus.OK);
		}

		Account account = accountDAO.findAccountNumber(depositRequest.getAccountNumber());
		
		if(account == null) {
			
			response.put("message", "account not found");
			
			return utilService.getResponse(response, HttpStatus.NOT_FOUND);
		}

		Customer customer = customerDAO.findCustomerByAccount(account).orElse(null);

		if (customer == null) {
			
			response.put("message", "customer profile not found");
			
			return utilService.getResponse(response, HttpStatus.NOT_FOUND);
		}
		
		BigDecimal amount = depositRequest.getAmount();
		
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			
			response.put("message", "invalid amount");
			
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}
		
		account.setBalance(account.getBalance().add(amount));
		
		accountDAO.save(account);
		
		Transaction transaction = Transaction.builder()
				.reference(UUID.randomUUID().toString())
				.receiverAccountNumber(depositRequest.getAccountNumber())
				.idempotencyKey(blankToNull(depositRequest.getIdempotencyKey()))
				.amount(amount)
				.customer(customer)
				.description(safeText(depositRequest.getDescription()))
				.transactionType(TransactionType.DEPOSIT)
				.transactionStatus(TransactionStatus.SUCCESS)
				.createdAt(LocalDateTime.now())
				.build();
		
		Transaction savedTransaction = transactionDAO.save(transaction);

		emailNotificationService.sendTransactionNotification(customer.getUserData(), TransactionType.DEPOSIT.name(), amount, account.getAccountNumber(), depositRequest.getDescription());
		
		response.put("transactionDetail", savedTransaction);
		response.put("message", "account credited");
		
		return utilService.getResponse(response, HttpStatus.OK);
		
	}
	
	@Override
	@Transactional
	public ResponseEntity<?> withdrawal(WithdrawalRequest withdrawalRequest) {

		if (withdrawalRequest == null) {
			
			response.put("message", "withdrawal request is required");
			
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}
		
		Transaction existingIdempotentTransaction = findIdempotentTransaction(withdrawalRequest.getIdempotencyKey());

		if (existingIdempotentTransaction != null) {
			response.put("transactionDetail", existingIdempotentTransaction);
			response.put("message", "idempotent withdrawal response");
			return utilService.getResponse(response, HttpStatus.OK);
		}

		Account account = accountDAO.findAccountNumber(withdrawalRequest.getAccountNumber());
		
		if (account == null) {
			
			response.put("message", "account not found");
			
			return utilService.getResponse(response, HttpStatus.NOT_FOUND);
		}

		Customer customer = customerDAO.findCustomerByAccount(account).orElse(null);

		if (customer == null) {
			
			response.put("message", "customer profile not found");
			
			return utilService.getResponse(response, HttpStatus.NOT_FOUND);
		}
		
		BigDecimal amount = withdrawalRequest.getAmount();
		
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			
			response.put("message", "invalid amount");
			
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}
		
		if (account.getBalance().compareTo(amount) < 0) {
			
			response.put("message", "insufficient balance");
			
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}
		
		account.setBalance(account.getBalance().subtract(amount));
		accountDAO.save(account);
		
		Transaction transaction = Transaction.builder()
				.reference(UUID.randomUUID().toString())
				.senderAccountNumber(withdrawalRequest.getAccountNumber())
				.idempotencyKey(blankToNull(withdrawalRequest.getIdempotencyKey()))
				.amount(withdrawalRequest.getAmount())
				.customer(customer)
				.description(safeText(withdrawalRequest.getDescription()))
				.transactionType(TransactionType.WITDRAWAL)
				.transactionStatus(TransactionStatus.SUCCESS)
				.createdAt(LocalDateTime.now())
				.build();
		
		Transaction savedTransaction = transactionDAO.save(transaction);

		emailNotificationService.sendTransactionNotification(customer.getUserData(), TransactionType.WITDRAWAL.name(), amount, account.getAccountNumber(), withdrawalRequest.getDescription());
		
		response.put("transactionDetail", savedTransaction);
		response.put("message", "withdrawal successful");
		
		return utilService.getResponse(response, HttpStatus.OK);
	}

	@Override
	@Transactional
	public ResponseEntity<?> reverseTransaction(ReversalRequest reversalRequest) {

		if (reversalRequest == null || isBlank(reversalRequest.getReference())) {
			
			response.put("message", "transaction reference is required");
			
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		Transaction existingIdempotentTransaction = findIdempotentTransaction(reversalRequest.getIdempotencyKey());

		if (existingIdempotentTransaction != null) {
			response.put("transactionDetail", existingIdempotentTransaction);
			response.put("message", "idempotent reversal response");
			return utilService.getResponse(response, HttpStatus.OK);
		}

		Transaction originalTransaction = transactionDAO.findTransactionByReference(reversalRequest.getReference());

		if (originalTransaction == null) {
			
			response.put("message", "transaction not found");
			
			return utilService.getResponse(response, HttpStatus.NOT_FOUND);
		}

		if (originalTransaction.getTransactionType() == TransactionType.REVERSAL) {
			
			response.put("message", "reversal transactions cannot be reversed");
			
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		if (originalTransaction.getTransactionStatus() != TransactionStatus.SUCCESS || originalTransaction.isReversed()) {
			
			response.put("message", "transaction is not reversible");
			
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		Transaction existingReversalTransaction = transactionDAO.findTransactionByOriginalTransactionReference(originalTransaction.getReference());

		if (existingReversalTransaction != null) {
			
			response.put("transactionDetail", existingReversalTransaction);

			if (existingReversalTransaction.getTransactionStatus() == TransactionStatus.PENDING) {
				
				response.put("message", "transaction already has a pending reversal");
				
				return utilService.getResponse(response, HttpStatus.ACCEPTED);
			}

			response.put("message", "transaction has already been reversed");
			
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		BigDecimal amount = originalTransaction.getAmount();

		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			
			response.put("message", "transaction amount is invalid");
			
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		Transaction reversalTransaction;

		if (originalTransaction.getTransactionType() == TransactionType.DEPOSIT) {
			reversalTransaction = reverseDeposit(originalTransaction, reversalRequest, response);
			
		} else if (originalTransaction.getTransactionType() == TransactionType.WITDRAWAL) {
			reversalTransaction = reverseWithdrawal(originalTransaction, reversalRequest, response);
			
		} else if (originalTransaction.getTransactionType() == TransactionType.TRANSFER) {
			reversalTransaction = reverseTransfer(originalTransaction, reversalRequest, response);
			
		} else {
			response.put("message", "transaction type is not reversible");
			
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		if (reversalTransaction == null) {
			
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		Transaction savedReversalTransaction = transactionDAO.save(reversalTransaction);

		if (savedReversalTransaction.getTransactionStatus() == TransactionStatus.PENDING) {
			
			response.put("transactionDetail", savedReversalTransaction);
			
			response.put("message", "transaction reversal is pending due to insufficient balance");

			return utilService.getResponse(response, HttpStatus.ACCEPTED);
		}

		LocalDateTime presentDateTime = LocalDateTime.now();
		
		originalTransaction.setReversed(true);
		originalTransaction.setReversedAt(presentDateTime);
		originalTransaction.setTransactionStatus(TransactionStatus.REVERSED);
		transactionDAO.save(originalTransaction);

		sendReversalNotification(savedReversalTransaction);
		
		response.put("transactionDetail", savedReversalTransaction);
		response.put("message", "transaction reversed successfully");

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

	private String safeText(String value) {
		return value == null || value.isBlank() ? "N/A" : value;
	}

	private Transaction reverseDeposit(Transaction originalTransaction, ReversalRequest reversalRequest, Map<String, Object> response) {

		Account account = accountDAO.findAccountNumber(originalTransaction.getReceiverAccountNumber());

		if (account == null) {
			response.put("message", "credited account not found");
			return null;
		}

		Customer customer = findCustomerByAccount(account, response);

		if (customer == null) {
			return null;
		}

		if (account.getBalance().compareTo(originalTransaction.getAmount()) < 0) {
			
			return buildPendingReversalTransaction(
					originalTransaction,
					reversalRequest,
					account.getAccountNumber(),
					null,
					customer
			);
		}

		account.setBalance(account.getBalance().subtract(originalTransaction.getAmount()));
		accountDAO.save(account);

		Transaction reversalTransaction = buildReversalTransaction(
				originalTransaction,
				reversalRequest,
				account.getAccountNumber(),
				null,
				customer
		);

		return reversalTransaction;
	}

	private Transaction reverseWithdrawal(Transaction originalTransaction, ReversalRequest reversalRequest, Map<String, Object> response) {

		Account account = accountDAO.findAccountNumber(originalTransaction.getSenderAccountNumber());

		if (account == null) {
			
			response.put("message", "debited account not found");
			
			return null;
		}

		Customer customer = findCustomerByAccount(account, response);

		if (customer == null) {
			
			return null;
		}

		account.setBalance(account.getBalance().add(originalTransaction.getAmount()));
		accountDAO.save(account);

		Transaction reversalTransaction = buildReversalTransaction(
				originalTransaction,
				reversalRequest,
				null,
				account.getAccountNumber(),
				customer
		);

		return reversalTransaction;
	}

	private Transaction reverseTransfer(Transaction originalTransaction, ReversalRequest reversalRequest, Map<String, Object> response) {

		Account originalSender = accountDAO.findAccountNumber(originalTransaction.getSenderAccountNumber());
		Account originalReceiver = accountDAO.findAccountNumber(originalTransaction.getReceiverAccountNumber());

		if (originalSender == null || originalReceiver == null) {
			
			response.put("message", "transfer account not found");
			
			return null;
		}

		Customer originalSenderCustomer = findCustomerByAccount(originalSender, response);
		Customer originalReceiverCustomer = findCustomerByAccount(originalReceiver, response);

		if (originalSenderCustomer == null || originalReceiverCustomer == null) {
			
			return null;
		}

		if (originalReceiver.getBalance().compareTo(originalTransaction.getAmount()) < 0) {
			
			return buildPendingReversalTransaction(
					originalTransaction,
					reversalRequest,
					originalReceiver.getAccountNumber(),
					originalSender.getAccountNumber(),
					originalSenderCustomer
			);
		}

		originalReceiver.setBalance(originalReceiver.getBalance().subtract(originalTransaction.getAmount()));
		originalSender.setBalance(originalSender.getBalance().add(originalTransaction.getAmount()));

		accountDAO.save(originalReceiver);
		accountDAO.save(originalSender);

		Transaction reversalTransaction = buildReversalTransaction(
				originalTransaction,
				reversalRequest,
				originalReceiver.getAccountNumber(),
				originalSender.getAccountNumber(),
				originalSenderCustomer
		);

		return reversalTransaction;
	}

	private Transaction buildReversalTransaction(Transaction originalTransaction, ReversalRequest reversalRequest, String senderAccountNumber, String receiverAccountNumber, Customer customer) {

		return Transaction.builder()
				.reference(UUID.randomUUID().toString())
				.originalTransactionReference(originalTransaction.getReference())
				.idempotencyKey(blankToNull(reversalRequest.getIdempotencyKey()))
				.senderAccountNumber(senderAccountNumber)
				.receiverAccountNumber(receiverAccountNumber)
				.amount(originalTransaction.getAmount())
				.customer(customer)
				.description(safeText(reversalRequest.getDescription()))
				.transactionType(TransactionType.REVERSAL)
				.transactionStatus(TransactionStatus.SUCCESS)
				.createdAt(LocalDateTime.now())
				.build();
	}

	private Transaction buildPendingReversalTransaction(Transaction originalTransaction, ReversalRequest reversalRequest, String senderAccountNumber, String receiverAccountNumber, Customer customer) {

		return Transaction.builder()
				.reference(UUID.randomUUID().toString())
				.originalTransactionReference(originalTransaction.getReference())
				.idempotencyKey(blankToNull(reversalRequest.getIdempotencyKey()))
				.senderAccountNumber(senderAccountNumber)
				.receiverAccountNumber(receiverAccountNumber)
				.amount(originalTransaction.getAmount())
				.customer(customer)
				.description(safeText(reversalRequest.getDescription()))
				.transactionType(TransactionType.REVERSAL)
				.transactionStatus(TransactionStatus.PENDING)
				.createdAt(LocalDateTime.now())
				.build();
	}

	private Customer findCustomerByAccount(Account account, Map<String, Object> response) {

		Customer customer = customerDAO.findCustomerByAccount(account).orElse(null);

		if (customer == null) {
			response.put("message", "customer profile not found");
		}

		return customer;
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private String blankToNull(String value) {
		return isBlank(value) ? null : value;
	}

	private Transaction findIdempotentTransaction(String idempotencyKey) {
		if (isBlank(idempotencyKey)) {
			return null;
		}

		return transactionDAO.findTransactionByIdempotencyKey(idempotencyKey);
	}

	private void sendReversalNotification(Transaction reversalTransaction) {

		notifyAccountHolder(reversalTransaction.getSenderAccountNumber(), reversalTransaction);

		if (reversalTransaction.getReceiverAccountNumber() != null && !reversalTransaction.getReceiverAccountNumber().equals(reversalTransaction.getSenderAccountNumber())) {
			
			notifyAccountHolder(reversalTransaction.getReceiverAccountNumber(), reversalTransaction);
		}
	}

	private void notifyAccountHolder(String accountNumber, Transaction reversalTransaction) {

		if (accountNumber == null || accountNumber.isBlank()) {
			return;
		}

		Account account = accountDAO.findAccountNumber(accountNumber);

		if (account == null) {
			return;
		}

		Customer customer = customerDAO.findCustomerByAccount(account).orElse(null);

		if (customer == null) {
			return;
		}

		emailNotificationService.sendTransactionNotification(customer.getUserData(), TransactionType.REVERSAL.name(), reversalTransaction.getAmount(), accountNumber, reversalTransaction.getDescription());
	}
 
}
