package com.tosin.xpay.service;

import org.springframework.http.ResponseEntity;

import com.tosin.xpay.dto.CardAuthorizationRequest;
import com.tosin.xpay.dto.CardCaptureRequest;
import com.tosin.xpay.dto.CardIssueRequest;
import com.tosin.xpay.dto.CardPaymentRequest;
import com.tosin.xpay.dto.CardReversalRequest;
import com.tosin.xpay.dto.CardStatusRequest;
import com.tosin.xpay.dto.RequestPayload;

public interface CardTransactionService {

	ResponseEntity<?> issueCard(CardIssueRequest cardIssueRequest);

	ResponseEntity<?> updateCardStatus(CardStatusRequest cardStatusRequest);

	ResponseEntity<?> authorize(CardAuthorizationRequest cardAuthorizationRequest);

	ResponseEntity<?> capture(CardCaptureRequest cardCaptureRequest);

	ResponseEntity<?> payment(CardPaymentRequest cardPaymentRequest);

	ResponseEntity<?> reverse(CardReversalRequest cardReversalRequest);

	ResponseEntity<?> releaseExpiredAuthorizationHolds();

	ResponseEntity<?> getCardTransactionHistory(String accountNumber, RequestPayload requestPayload);

}
