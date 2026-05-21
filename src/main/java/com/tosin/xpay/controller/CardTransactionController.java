package com.tosin.xpay.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.tosin.xpay.dto.CardAuthorizationRequest;
import com.tosin.xpay.dto.CardCaptureRequest;
import com.tosin.xpay.dto.CardIssueRequest;
import com.tosin.xpay.dto.CardPaymentRequest;
import com.tosin.xpay.dto.CardReversalRequest;
import com.tosin.xpay.dto.CardStatusRequest;
import com.tosin.xpay.dto.RequestPayload;
import com.tosin.xpay.service.CardTransactionService;

@RestController
@RequestMapping(value = "card")
public class CardTransactionController {

	@Autowired private CardTransactionService cardTransactionService;

	@RequestMapping(value = "issue", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> issueCard(@RequestBody CardIssueRequest cardIssueRequest) {
		return cardTransactionService.issueCard(cardIssueRequest);
	}

	@RequestMapping(value = "status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> updateCardStatus(@RequestBody CardStatusRequest cardStatusRequest) {
		return cardTransactionService.updateCardStatus(cardStatusRequest);
	}

	@RequestMapping(value = "authorize", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> authorize(@RequestBody CardAuthorizationRequest cardAuthorizationRequest) {
		return cardTransactionService.authorize(cardAuthorizationRequest);
	}

	@RequestMapping(value = "capture", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> capture(@RequestBody CardCaptureRequest cardCaptureRequest) {
		return cardTransactionService.capture(cardCaptureRequest);
	}

	@RequestMapping(value = "payment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> payment(@RequestBody CardPaymentRequest cardPaymentRequest) {
		return cardTransactionService.payment(cardPaymentRequest);
	}

	@RequestMapping(value = "reverse", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> reverse(@RequestBody CardReversalRequest cardReversalRequest) {
		return cardTransactionService.reverse(cardReversalRequest);
	}

	@RequestMapping(value = "release-expired-authorizations", produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> releaseExpiredAuthorizationHolds() {
		return cardTransactionService.releaseExpiredAuthorizationHolds();
	}

	@RequestMapping(value = "history/{accountNumber}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> getCardTransactionHistory(@PathVariable String accountNumber, @RequestBody RequestPayload requestPayload) {
		return cardTransactionService.getCardTransactionHistory(accountNumber, requestPayload);
	}

}
