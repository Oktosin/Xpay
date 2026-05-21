package com.tosin.xpay.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.tosin.xpay.dto.DepositRequest;
import com.tosin.xpay.dto.RequestPayload;
import com.tosin.xpay.dto.ReversalRequest;
import com.tosin.xpay.dto.TransferRequest;
import com.tosin.xpay.dto.WithdrawalRequest;
import com.tosin.xpay.service.TransactionService;

@RestController
@RequestMapping(value = "transaction")
public class TransactionController {
	
	@Autowired private TransactionService transactionService;
	
	@RequestMapping(value = "transfer", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> transfer(@RequestBody TransferRequest transferRequest) {
		return transactionService.transfer(transferRequest);
	}
	
	@RequestMapping(value = "deposit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> deposit(@RequestBody DepositRequest depositRequest) {
		return transactionService.deposit(depositRequest);
	}
	
	@RequestMapping(value = "withdraw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> withdrawal(@RequestBody WithdrawalRequest withdrawalRequest) {
		return transactionService.withdrawal(withdrawalRequest);
	}

	@RequestMapping(value = "reverse", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> reverseTransaction(@RequestBody ReversalRequest reversalRequest) {
		return transactionService.reverseTransaction(reversalRequest);
	}
	
	@RequestMapping(value = "history/{accountNumber}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> getTransactionHistory(@PathVariable String accountNumber, @RequestBody RequestPayload requestPayload) {
		return transactionService.getTransactionHistory(accountNumber, requestPayload);
	}


}
