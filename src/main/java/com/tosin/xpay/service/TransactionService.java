package com.tosin.xpay.service;

import org.springframework.http.ResponseEntity;

import com.tosin.xpay.dto.DepositRequest;
import com.tosin.xpay.dto.RequestPayload;
import com.tosin.xpay.dto.TransferRequest;
import com.tosin.xpay.dto.WithdrawalRequest;

public interface TransactionService {

	ResponseEntity<?> transfer(TransferRequest transferRequest);

	ResponseEntity<?> deposit(DepositRequest depositRequest);

	ResponseEntity<?> withdrawal(WithdrawalRequest withdrawalRequest);

	ResponseEntity<?> getTransactionHistory(String accountNumber, RequestPayload requestPayload);



}
