package com.tosin.xpay.service.impl;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tosin.xpay.constant.CardStatus;
import com.tosin.xpay.constant.CardTransactionStatus;
import com.tosin.xpay.constant.CardTransactionType;
import com.tosin.xpay.constant.UserRole;
import com.tosin.xpay.dao.AccountDAO;
import com.tosin.xpay.dao.CardDAO;
import com.tosin.xpay.dao.CardTransactionDAO;
import com.tosin.xpay.dao.CustomerDAO;
import com.tosin.xpay.dao.UserDataDAO;
import com.tosin.xpay.dto.CardAuthorizationRequest;
import com.tosin.xpay.dto.CardCaptureRequest;
import com.tosin.xpay.dto.CardIssueRequest;
import com.tosin.xpay.dto.CardPaymentRequest;
import com.tosin.xpay.dto.CardReversalRequest;
import com.tosin.xpay.dto.CardStatusRequest;
import com.tosin.xpay.dto.RequestPayload;
import com.tosin.xpay.model.Account;
import com.tosin.xpay.model.Card;
import com.tosin.xpay.model.CardTransaction;
import com.tosin.xpay.model.Customer;
import com.tosin.xpay.model.UserData;
import com.tosin.xpay.service.CardTransactionService;
import com.tosin.xpay.service.EmailNotificationService;
import com.tosin.xpay.service.UtilService;

import jakarta.transaction.Transactional;

@Service
public class CardTransactionServiceImpl implements CardTransactionService {

	@Autowired private AccountDAO accountDAO;
	@Autowired private CardDAO cardDAO;
	@Autowired private CardTransactionDAO cardTransactionDAO;
	@Autowired private CustomerDAO customerDAO;
	@Autowired private UserDataDAO userDataDAO;
	@Autowired private UtilService utilService;
	@Autowired private EmailNotificationService emailNotificationService;
	@Autowired private PasswordEncoder passwordEncoder;

	private static final int AUTHORIZATION_EXPIRY_MINUTES = 15;
	private static final BigDecimal DEFAULT_TRANSACTION_LIMIT = new BigDecimal("500000");
	private static final BigDecimal DEFAULT_DAILY_LIMIT = new BigDecimal("1000000");
	private static final BigDecimal DEFAULT_MONTHLY_LIMIT = new BigDecimal("10000000");

	@Override
	@Transactional
	public ResponseEntity<?> issueCard(CardIssueRequest cardIssueRequest) {
		Map<String, Object> response = new LinkedHashMap<>();

		if (cardIssueRequest == null || isBlank(cardIssueRequest.getAccountNumber()) || isBlank(cardIssueRequest.getCardNumber()) || isBlank(cardIssueRequest.getCardHolderName()) || cardIssueRequest.getExpiryDate() == null || isBlank(cardIssueRequest.getPin()) || isBlank(cardIssueRequest.getCvv())) {
			response.put("message", "account number, card number, card holder name, expiry date, PIN and CVV are required");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		Account account = accountDAO.findByAccountNumber(cardIssueRequest.getAccountNumber());

		if (account == null) {
			response.put("message", "account not found");
			return utilService.getResponse(response, HttpStatus.NOT_FOUND);
		}

		if (!canAccessAccount(account)) {
			response.put("message", "you are not allowed to issue a card for this account");
			return utilService.getResponse(response, HttpStatus.FORBIDDEN);
		}

		if (!isValidCardNumber(cardIssueRequest.getCardNumber()) || !isValidPin(cardIssueRequest.getPin()) || !isValidCvv(cardIssueRequest.getCvv())) {
			response.put("message", "card number, PIN or CVV format is invalid");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		if (cardIssueRequest.getExpiryDate().isBefore(LocalDate.now()) || YearMonth.from(cardIssueRequest.getExpiryDate()).equals(YearMonth.from(LocalDate.now()).minusMonths(1))) {
			response.put("message", "card expiry date is invalid");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		String cardNumberHash = hashCardNumber(cardIssueRequest.getCardNumber());

		if (cardDAO.findCardByCardNumberHash(cardNumberHash).isPresent()) {
			response.put("message", "card already exists");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		if (cardDAO.findCardByAccount(account).isPresent()) {
			response.put("message", "account already has a card");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		LocalDateTime presentDateTime = LocalDateTime.now();
		Card card = Card.builder()
				.account(account)
				.cardNumberHash(cardNumberHash)
				.pinHash(passwordEncoder.encode(cardIssueRequest.getPin()))
				.cvvHash(passwordEncoder.encode(cardIssueRequest.getCvv()))
				.maskedCardNumber(maskCardNumber(cardIssueRequest.getCardNumber()))
				.cardHolderName(cardIssueRequest.getCardHolderName())
				.expiryDate(cardIssueRequest.getExpiryDate())
				.cardStatus(CardStatus.ACTIVE)
				.holdBalance(BigDecimal.ZERO)
				.transactionLimit(defaultLimit(cardIssueRequest.getTransactionLimit(), DEFAULT_TRANSACTION_LIMIT))
				.dailyLimit(defaultLimit(cardIssueRequest.getDailyLimit(), DEFAULT_DAILY_LIMIT))
				.monthlyLimit(defaultLimit(cardIssueRequest.getMonthlyLimit(), DEFAULT_MONTHLY_LIMIT))
				.createdAt(presentDateTime)
				.updatedAt(presentDateTime)
				.build();

		Card savedCard = cardDAO.save(card);
		response.put("card", savedCard);
		response.put("message", "card issued successfully");

		return utilService.getResponse(response, HttpStatus.OK);
	}

	@Override
	@Transactional
	public ResponseEntity<?> updateCardStatus(CardStatusRequest cardStatusRequest) {
		Map<String, Object> response = new LinkedHashMap<>();

		if (cardStatusRequest == null || isBlank(cardStatusRequest.getCardNumber()) || cardStatusRequest.getCardStatus() == null) {
			response.put("message", "card number and status are required");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		Card card = findCardByNumber(cardStatusRequest.getCardNumber());

		if (card == null) {
			response.put("message", "card not found");
			return utilService.getResponse(response, HttpStatus.NOT_FOUND);
		}

		if (!canAccessAccount(card.getAccount())) {
			response.put("message", "you are not allowed to update this card");
			return utilService.getResponse(response, HttpStatus.FORBIDDEN);
		}

		card.setCardStatus(cardStatusRequest.getCardStatus());
		card.setUpdatedAt(LocalDateTime.now());

		Card savedCard = cardDAO.save(card);
		response.put("card", savedCard);
		response.put("message", "card status updated successfully");

		return utilService.getResponse(response, HttpStatus.OK);
	}

	@Override
	@Transactional
	public ResponseEntity<?> authorize(CardAuthorizationRequest cardAuthorizationRequest) {
		Map<String, Object> response = new LinkedHashMap<>();

		if (cardAuthorizationRequest == null || isBlank(cardAuthorizationRequest.getCardNumber())) {
			response.put("message", "card number is required");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		Card card = findCardByNumber(cardAuthorizationRequest.getCardNumber());
		ResponseEntity<?> invalidCardResponse = validateCard(card, response);

		if (invalidCardResponse != null) {
			return invalidCardResponse;
		}

		if (!canAccessAccount(card.getAccount())) {
			response.put("message", "you are not allowed to use this card");
			return utilService.getResponse(response, HttpStatus.FORBIDDEN);
		}

		CardTransaction existingIdempotentTransaction = findIdempotentTransaction(cardAuthorizationRequest.getIdempotencyKey());

		if (existingIdempotentTransaction != null) {
			response.put("transactionDetail", existingIdempotentTransaction);
			response.put("message", "idempotent card authorization response");
			return utilService.getResponse(response, HttpStatus.OK);
		}

		BigDecimal amount = cardAuthorizationRequest.getAmount();

		if (isInvalidAmount(amount)) {
			response.put("message", "invalid amount");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		if (!isPresentedCardCredentialValid(card, cardAuthorizationRequest.getExpiryDate(), cardAuthorizationRequest.getPin(), cardAuthorizationRequest.getCvv())) {
			CardTransaction failedTransaction = saveCardTransaction(card, CardTransactionType.AUTHORIZATION, CardTransactionStatus.FAILED, amount, cardAuthorizationRequest.getMerchantId(), cardAuthorizationRequest.getMerchantName(), cardAuthorizationRequest.getDescription(), null, cardAuthorizationRequest.getIdempotencyKey(), "INVALID_CARD_CREDENTIALS", null, cardAuthorizationRequest.getTerminalId(), cardAuthorizationRequest.getChannel(), cardAuthorizationRequest.getLocation(), cardAuthorizationRequest.getIpAddress());
			sendCardNotification(card, failedTransaction);
			response.put("transactionDetail", failedTransaction);
			response.put("message", "invalid card credentials");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		ResponseEntity<?> limitResponse = validateSpendLimits(card, amount, response);

		if (limitResponse != null) {
			CardTransaction failedTransaction = saveCardTransaction(card, CardTransactionType.AUTHORIZATION, CardTransactionStatus.FAILED, amount, cardAuthorizationRequest.getMerchantId(), cardAuthorizationRequest.getMerchantName(), cardAuthorizationRequest.getDescription(), null, cardAuthorizationRequest.getIdempotencyKey(), response.get("message").toString(), null, cardAuthorizationRequest.getTerminalId(), cardAuthorizationRequest.getChannel(), cardAuthorizationRequest.getLocation(), cardAuthorizationRequest.getIpAddress());
			sendCardNotification(card, failedTransaction);
			response.put("transactionDetail", failedTransaction);
			return limitResponse;
		}

		if (availableBalance(card).compareTo(amount) < 0) {
			CardTransaction failedTransaction = saveCardTransaction(card, CardTransactionType.AUTHORIZATION, CardTransactionStatus.FAILED, amount, cardAuthorizationRequest.getMerchantId(), cardAuthorizationRequest.getMerchantName(), cardAuthorizationRequest.getDescription(), null, cardAuthorizationRequest.getIdempotencyKey(), "INSUFFICIENT_BALANCE", null, cardAuthorizationRequest.getTerminalId(), cardAuthorizationRequest.getChannel(), cardAuthorizationRequest.getLocation(), cardAuthorizationRequest.getIpAddress());
			sendCardNotification(card, failedTransaction);
			response.put("transactionDetail", failedTransaction);
			response.put("message", "insufficient balance");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		card.setHoldBalance(card.getHoldBalance().add(amount));
		card.setUpdatedAt(LocalDateTime.now());
		cardDAO.save(card);

		CardTransaction cardTransaction = saveCardTransaction(card, CardTransactionType.AUTHORIZATION, CardTransactionStatus.AUTHORIZED, amount, cardAuthorizationRequest.getMerchantId(), cardAuthorizationRequest.getMerchantName(), cardAuthorizationRequest.getDescription(), null, cardAuthorizationRequest.getIdempotencyKey(), null, LocalDateTime.now().plusMinutes(AUTHORIZATION_EXPIRY_MINUTES), cardAuthorizationRequest.getTerminalId(), cardAuthorizationRequest.getChannel(), cardAuthorizationRequest.getLocation(), cardAuthorizationRequest.getIpAddress());
		sendCardNotification(card, cardTransaction);

		response.put("transactionDetail", cardTransaction);
		response.put("message", "card authorization successful");

		return utilService.getResponse(response, HttpStatus.OK);
	}

	@Override
	@Transactional
	public ResponseEntity<?> capture(CardCaptureRequest cardCaptureRequest) {
		Map<String, Object> response = new LinkedHashMap<>();

		if (cardCaptureRequest == null || isBlank(cardCaptureRequest.getReference())) {
			response.put("message", "authorization reference is required");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		CardTransaction existingIdempotentTransaction = findIdempotentTransaction(cardCaptureRequest.getIdempotencyKey());

		if (existingIdempotentTransaction != null) {
			response.put("transactionDetail", existingIdempotentTransaction);
			response.put("message", "idempotent card capture response");
			return utilService.getResponse(response, HttpStatus.OK);
		}

		CardTransaction authorizationTransaction = cardTransactionDAO.findCardTransactionByReference(cardCaptureRequest.getReference());

		if (authorizationTransaction == null) {
			response.put("message", "authorization transaction not found");
			return utilService.getResponse(response, HttpStatus.NOT_FOUND);
		}

		if (authorizationTransaction.getTransactionType() != CardTransactionType.AUTHORIZATION || authorizationTransaction.getTransactionStatus() != CardTransactionStatus.AUTHORIZED) {
			response.put("message", "card transaction is not capturable");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		if (cardTransactionDAO.findCardTransactionByOriginalTransactionReference(authorizationTransaction.getReference()) != null) {
			response.put("message", "authorization has already been captured or reversed");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		Card card = authorizationTransaction.getCard();
		if (!canAccessAccount(card.getAccount())) {
			response.put("message", "you are not allowed to capture this card transaction");
			return utilService.getResponse(response, HttpStatus.FORBIDDEN);
		}

		if (authorizationTransaction.getAuthorizationExpiresAt() != null && authorizationTransaction.getAuthorizationExpiresAt().isBefore(LocalDateTime.now())) {
			releaseAuthorizationHold(authorizationTransaction);
			response.put("message", "authorization has expired");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		ResponseEntity<?> invalidCardResponse = validateCard(card, response);

		if (invalidCardResponse != null) {
			return invalidCardResponse;
		}

		if (card.getHoldBalance().compareTo(authorizationTransaction.getAmount()) < 0) {
			response.put("message", "held balance is insufficient");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		Account account = card.getAccount();
		card.setHoldBalance(card.getHoldBalance().subtract(authorizationTransaction.getAmount()));
		card.setUpdatedAt(LocalDateTime.now());
		account.setBalance(account.getBalance().subtract(authorizationTransaction.getAmount()));

		accountDAO.save(account);
		cardDAO.save(card);

		authorizationTransaction.setTransactionStatus(CardTransactionStatus.SUCCESS);
		authorizationTransaction.setUpdatedAt(LocalDateTime.now());
		cardTransactionDAO.save(authorizationTransaction);

		CardTransaction captureTransaction = saveCardTransaction(card, CardTransactionType.CAPTURE, CardTransactionStatus.SUCCESS, authorizationTransaction.getAmount(), authorizationTransaction.getMerchantId(), authorizationTransaction.getMerchantName(), cardCaptureRequest.getDescription(), authorizationTransaction.getReference(), cardCaptureRequest.getIdempotencyKey(), null, null, cardCaptureRequest.getTerminalId(), cardCaptureRequest.getChannel(), cardCaptureRequest.getLocation(), cardCaptureRequest.getIpAddress());
		sendCardNotification(card, captureTransaction);

		response.put("transactionDetail", captureTransaction);
		response.put("message", "card capture successful");

		return utilService.getResponse(response, HttpStatus.OK);
	}

	@Override
	@Transactional
	public ResponseEntity<?> payment(CardPaymentRequest cardPaymentRequest) {
		Map<String, Object> response = new LinkedHashMap<>();

		if (cardPaymentRequest == null || isBlank(cardPaymentRequest.getCardNumber())) {
			response.put("message", "card number is required");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		Card card = findCardByNumber(cardPaymentRequest.getCardNumber());
		ResponseEntity<?> invalidCardResponse = validateCard(card, response);

		if (invalidCardResponse != null) {
			return invalidCardResponse;
		}

		if (!canAccessAccount(card.getAccount())) {
			response.put("message", "you are not allowed to use this card");
			return utilService.getResponse(response, HttpStatus.FORBIDDEN);
		}

		CardTransaction existingIdempotentTransaction = findIdempotentTransaction(cardPaymentRequest.getIdempotencyKey());

		if (existingIdempotentTransaction != null) {
			response.put("transactionDetail", existingIdempotentTransaction);
			response.put("message", "idempotent card payment response");
			return utilService.getResponse(response, HttpStatus.OK);
		}

		BigDecimal amount = cardPaymentRequest.getAmount();

		if (isInvalidAmount(amount)) {
			response.put("message", "invalid amount");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		if (!isPresentedCardCredentialValid(card, cardPaymentRequest.getExpiryDate(), cardPaymentRequest.getPin(), cardPaymentRequest.getCvv())) {
			CardTransaction failedTransaction = saveCardTransaction(card, CardTransactionType.PAYMENT, CardTransactionStatus.FAILED, amount, cardPaymentRequest.getMerchantId(), cardPaymentRequest.getMerchantName(), cardPaymentRequest.getDescription(), null, cardPaymentRequest.getIdempotencyKey(), "INVALID_CARD_CREDENTIALS", null, cardPaymentRequest.getTerminalId(), cardPaymentRequest.getChannel(), cardPaymentRequest.getLocation(), cardPaymentRequest.getIpAddress());
			sendCardNotification(card, failedTransaction);
			response.put("transactionDetail", failedTransaction);
			response.put("message", "invalid card credentials");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		ResponseEntity<?> limitResponse = validateSpendLimits(card, amount, response);

		if (limitResponse != null) {
			CardTransaction failedTransaction = saveCardTransaction(card, CardTransactionType.PAYMENT, CardTransactionStatus.FAILED, amount, cardPaymentRequest.getMerchantId(), cardPaymentRequest.getMerchantName(), cardPaymentRequest.getDescription(), null, cardPaymentRequest.getIdempotencyKey(), response.get("message").toString(), null, cardPaymentRequest.getTerminalId(), cardPaymentRequest.getChannel(), cardPaymentRequest.getLocation(), cardPaymentRequest.getIpAddress());
			sendCardNotification(card, failedTransaction);
			response.put("transactionDetail", failedTransaction);
			return limitResponse;
		}

		if (availableBalance(card).compareTo(amount) < 0) {
			CardTransaction failedTransaction = saveCardTransaction(card, CardTransactionType.PAYMENT, CardTransactionStatus.FAILED, amount, cardPaymentRequest.getMerchantId(), cardPaymentRequest.getMerchantName(), cardPaymentRequest.getDescription(), null, cardPaymentRequest.getIdempotencyKey(), "INSUFFICIENT_BALANCE", null, cardPaymentRequest.getTerminalId(), cardPaymentRequest.getChannel(), cardPaymentRequest.getLocation(), cardPaymentRequest.getIpAddress());
			sendCardNotification(card, failedTransaction);
			response.put("transactionDetail", failedTransaction);
			response.put("message", "insufficient balance");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		Account account = card.getAccount();
		account.setBalance(account.getBalance().subtract(amount));
		card.setUpdatedAt(LocalDateTime.now());

		accountDAO.save(account);
		cardDAO.save(card);

		CardTransaction paymentTransaction = saveCardTransaction(card, CardTransactionType.PAYMENT, CardTransactionStatus.SUCCESS, amount, cardPaymentRequest.getMerchantId(), cardPaymentRequest.getMerchantName(), cardPaymentRequest.getDescription(), null, cardPaymentRequest.getIdempotencyKey(), null, null, cardPaymentRequest.getTerminalId(), cardPaymentRequest.getChannel(), cardPaymentRequest.getLocation(), cardPaymentRequest.getIpAddress());
		sendCardNotification(card, paymentTransaction);

		response.put("transactionDetail", paymentTransaction);
		response.put("message", "card payment successful");

		return utilService.getResponse(response, HttpStatus.OK);
	}

	@Override
	@Transactional
	public ResponseEntity<?> reverse(CardReversalRequest cardReversalRequest) {
		Map<String, Object> response = new LinkedHashMap<>();

		if (cardReversalRequest == null || isBlank(cardReversalRequest.getReference())) {
			response.put("message", "card transaction reference is required");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		CardTransaction existingIdempotentTransaction = findIdempotentTransaction(cardReversalRequest.getIdempotencyKey());

		if (existingIdempotentTransaction != null) {
			response.put("transactionDetail", existingIdempotentTransaction);
			response.put("message", "idempotent card reversal response");
			return utilService.getResponse(response, HttpStatus.OK);
		}

		CardTransaction originalTransaction = cardTransactionDAO.findCardTransactionByReference(cardReversalRequest.getReference());

		if (originalTransaction == null) {
			response.put("message", "card transaction not found");
			return utilService.getResponse(response, HttpStatus.NOT_FOUND);
		}

		if (originalTransaction.getTransactionType() == CardTransactionType.REVERSAL) {
			response.put("message", "reversal transactions cannot be reversed");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		if (originalTransaction.getTransactionStatus() != CardTransactionStatus.SUCCESS && originalTransaction.getTransactionStatus() != CardTransactionStatus.AUTHORIZED) {
			response.put("message", "card transaction is not reversible");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		if (cardTransactionDAO.findCardTransactionByOriginalTransactionReference(originalTransaction.getReference()) != null) {
			response.put("message", "card transaction has already been reversed");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		Card card = originalTransaction.getCard();
		if (!canAccessAccount(card.getAccount())) {
			response.put("message", "you are not allowed to reverse this card transaction");
			return utilService.getResponse(response, HttpStatus.FORBIDDEN);
		}

		Account account = card.getAccount();

		if (originalTransaction.getTransactionStatus() == CardTransactionStatus.AUTHORIZED) {
			if (card.getHoldBalance().compareTo(originalTransaction.getAmount()) < 0) {
			CardTransaction pendingReversalTransaction = saveCardTransaction(card, CardTransactionType.REVERSAL, CardTransactionStatus.PENDING, originalTransaction.getAmount(), originalTransaction.getMerchantId(), originalTransaction.getMerchantName(), cardReversalRequest.getDescription(), originalTransaction.getReference(), cardReversalRequest.getIdempotencyKey(), "HELD_BALANCE_INSUFFICIENT", null, cardReversalRequest.getTerminalId(), cardReversalRequest.getChannel(), cardReversalRequest.getLocation(), cardReversalRequest.getIpAddress());
			response.put("transactionDetail", pendingReversalTransaction);
			response.put("message", "card reversal is pending");
			return utilService.getResponse(response, HttpStatus.ACCEPTED);
			}

			card.setHoldBalance(card.getHoldBalance().subtract(originalTransaction.getAmount()));
		} else {
			account.setBalance(account.getBalance().add(originalTransaction.getAmount()));
			accountDAO.save(account);
		}

		card.setUpdatedAt(LocalDateTime.now());
		cardDAO.save(card);

		originalTransaction.setTransactionStatus(CardTransactionStatus.REVERSED);
		originalTransaction.setUpdatedAt(LocalDateTime.now());
		cardTransactionDAO.save(originalTransaction);

		CardTransaction reversalTransaction = saveCardTransaction(card, CardTransactionType.REVERSAL, CardTransactionStatus.SUCCESS, originalTransaction.getAmount(), originalTransaction.getMerchantId(), originalTransaction.getMerchantName(), cardReversalRequest.getDescription(), originalTransaction.getReference(), cardReversalRequest.getIdempotencyKey(), null, null, cardReversalRequest.getTerminalId(), cardReversalRequest.getChannel(), cardReversalRequest.getLocation(), cardReversalRequest.getIpAddress());
		sendCardNotification(card, reversalTransaction);

		response.put("transactionDetail", reversalTransaction);
		response.put("message", "card transaction reversed successfully");

		return utilService.getResponse(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getCardTransactionHistory(String accountNumber, RequestPayload requestPayload) {
		Map<String, Object> response = new LinkedHashMap<>();

		Account account = accountDAO.findByAccountNumber(accountNumber);

		if (account == null) {
			response.put("message", "account not found");
			return utilService.getResponse(response, HttpStatus.NOT_FOUND);
		}

		int pageNumber = requestPayload.getPageNumber();
		int pageSize = utilService.pageSizeLimit(requestPayload.getPageSize());
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Page<CardTransaction> cardTransactions = cardTransactionDAO.findCardTransactionHistoryByAccountNumber(accountNumber, pageable);

		response.put("cardTransactionHistory", cardTransactions.getContent());
		response.put("currentPage", cardTransactions.getNumber());
		response.put("totalPages", cardTransactions.getTotalPages());
		response.put("totalElements", cardTransactions.getTotalElements());

		return utilService.getResponse(response, HttpStatus.OK);
	}

	private Card findCardByNumber(String cardNumber) {
		return cardDAO.findCardByCardNumberHash(hashCardNumber(cardNumber)).orElse(null);
	}

	@Scheduled(fixedRate = 60000)
	@Transactional
	public void releaseExpiredAuthorizationHoldsSchedule() {
		releaseExpiredAuthorizationHoldsInternal();
	}

	@Override
	@Transactional
	public ResponseEntity<?> releaseExpiredAuthorizationHolds() {
		Map<String, Object> response = new LinkedHashMap<>();
		int releasedCount = releaseExpiredAuthorizationHoldsInternal();

		response.put("releasedCount", releasedCount);
		response.put("message", "expired authorization holds released");

		return utilService.getResponse(response, HttpStatus.OK);
	}

	private int releaseExpiredAuthorizationHoldsInternal() {
		List<CardTransaction> expiredAuthorizations = cardTransactionDAO.findExpiredAuthorizations(CardTransactionType.AUTHORIZATION, CardTransactionStatus.AUTHORIZED, LocalDateTime.now());

		expiredAuthorizations.forEach(this::releaseAuthorizationHold);
		return expiredAuthorizations.size();
	}

	private void releaseAuthorizationHold(CardTransaction authorizationTransaction) {
		Card card = authorizationTransaction.getCard();
		BigDecimal holdBalance = card.getHoldBalance() == null ? BigDecimal.ZERO : card.getHoldBalance();

		card.setHoldBalance(holdBalance.subtract(authorizationTransaction.getAmount()).max(BigDecimal.ZERO));
		card.setUpdatedAt(LocalDateTime.now());
		cardDAO.save(card);

		authorizationTransaction.setTransactionStatus(CardTransactionStatus.REVERSED);
		authorizationTransaction.setDeclineReason("AUTHORIZATION_EXPIRED");
		authorizationTransaction.setUpdatedAt(LocalDateTime.now());
		cardTransactionDAO.save(authorizationTransaction);
	}

	private ResponseEntity<?> validateCard(Card card, Map<String, Object> response) {

		if (card == null) {
			response.put("message", "card not found");
			return utilService.getResponse(response, HttpStatus.NOT_FOUND);
		}

		if (card.getCardStatus() != CardStatus.ACTIVE) {
			response.put("message", "card is not active");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		if (card.getExpiryDate().isBefore(LocalDate.now())) {
			card.setCardStatus(CardStatus.EXPIRED);
			card.setUpdatedAt(LocalDateTime.now());
			cardDAO.save(card);

			response.put("message", "card has expired");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		return null;
	}

	private BigDecimal availableBalance(Card card) {
		BigDecimal holdBalance = card.getHoldBalance() == null ? BigDecimal.ZERO : card.getHoldBalance();
		return card.getAccount().getBalance().subtract(holdBalance);
	}

	private CardTransaction saveCardTransaction(Card card, CardTransactionType transactionType, CardTransactionStatus transactionStatus, BigDecimal amount, String merchantId, String merchantName, String description, String originalTransactionReference, String idempotencyKey, String declineReason, LocalDateTime authorizationExpiresAt, String terminalId, String channel, String location, String ipAddress) {
		LocalDateTime presentDateTime = LocalDateTime.now();

		CardTransaction cardTransaction = CardTransaction.builder()
				.card(card)
				.reference(UUID.randomUUID().toString())
				.originalTransactionReference(originalTransactionReference)
				.idempotencyKey(blankToNull(idempotencyKey))
				.accountNumber(card.getAccount().getAccountNumber())
				.amount(amount)
				.merchantId(blankToNull(merchantId))
				.merchantName(safeText(merchantName))
				.description(safeText(description))
				.declineReason(blankToNull(declineReason))
				.transactionType(transactionType)
				.transactionStatus(transactionStatus)
				.authorizationExpiresAt(authorizationExpiresAt)
				.processedBy(currentUsername())
				.terminalId(blankToNull(terminalId))
				.channel(blankToNull(channel))
				.location(blankToNull(location))
				.ipAddress(blankToNull(ipAddress))
				.createdAt(presentDateTime)
				.updatedAt(presentDateTime)
				.build();

		return cardTransactionDAO.save(cardTransaction);
	}

	private CardTransaction findIdempotentTransaction(String idempotencyKey) {
		if (isBlank(idempotencyKey)) {
			return null;
		}

		return cardTransactionDAO.findCardTransactionByIdempotencyKey(idempotencyKey);
	}

	private ResponseEntity<?> validateSpendLimits(Card card, BigDecimal amount, Map<String, Object> response) {
		if (amount.compareTo(card.getTransactionLimit()) > 0) {
			response.put("message", "transaction limit exceeded");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		List<CardTransactionType> spendTypes = Arrays.asList(CardTransactionType.PAYMENT, CardTransactionType.CAPTURE);
		LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
		LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
		BigDecimal dailySpend = cardTransactionDAO.sumSuccessfulSpendSince(card.getAccount().getAccountNumber(), CardTransactionStatus.SUCCESS, spendTypes, startOfDay);
		BigDecimal monthlySpend = cardTransactionDAO.sumSuccessfulSpendSince(card.getAccount().getAccountNumber(), CardTransactionStatus.SUCCESS, spendTypes, startOfMonth);

		if (dailySpend.add(amount).compareTo(card.getDailyLimit()) > 0) {
			response.put("message", "daily card limit exceeded");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		if (monthlySpend.add(amount).compareTo(card.getMonthlyLimit()) > 0) {
			response.put("message", "monthly card limit exceeded");
			return utilService.getResponse(response, HttpStatus.BAD_REQUEST);
		}

		return null;
	}

	private boolean canAccessAccount(Account account) {
		String username = currentUsername();

		if (isBlank(username) || account == null) {
			return false;
		}

		UserData userData = userDataDAO.findUserDataByUsername(username);

		if (userData == null) {
			return false;
		}

		if (userData.getUserRole() == UserRole.STAFF) {
			return true;
		}

		Customer customer = customerDAO.findCustomerByAccount(account).orElse(null);

		return customer != null && customer.getUserData() != null && username.equals(customer.getUserData().getUsername());
	}

	private String currentUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || isBlank(authentication.getName())) {
			return "SYSTEM";
		}

		return authentication.getName();
	}

	private void sendCardNotification(Card card, CardTransaction cardTransaction) {
		Customer customer = customerDAO.findCustomerByAccount(card.getAccount()).orElse(null);

		if (customer == null) {
			return;
		}

		emailNotificationService.sendCardTransactionNotification(
				customer.getUserData(),
				cardTransaction.getTransactionType().name(),
				cardTransaction.getTransactionStatus().name(),
				cardTransaction.getAmount(),
				card.getMaskedCardNumber(),
				cardTransaction.getMerchantName(),
				cardTransaction.getDescription()
		);
	}

	private boolean isInvalidAmount(BigDecimal amount) {
		return amount == null || amount.compareTo(BigDecimal.ZERO) <= 0;
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private String blankToNull(String value) {
		return isBlank(value) ? null : value;
	}

	private String safeText(String value) {
		return value == null || value.isBlank() ? "N/A" : value;
	}

	private String maskCardNumber(String cardNumber) {
		String digitsOnly = cardNumber.replaceAll("\\D", "");

		if (digitsOnly.length() < 4) {
			return "****";
		}

		return "**** **** **** " + digitsOnly.substring(digitsOnly.length() - 4);
	}

	private String hashCardNumber(String cardNumber) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			byte[] digest = messageDigest.digest(cardNumber.replaceAll("\\s+", "").getBytes(StandardCharsets.UTF_8));

			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available", exception);
		}
	}

	private boolean isValidCardNumber(String cardNumber) {
		String digitsOnly = cardNumber.replaceAll("\\D", "");
		return digitsOnly.length() >= 12 && digitsOnly.length() <= 19;
	}

	private boolean isValidPin(String pin) {
		return pin != null && pin.matches("\\d{4,6}");
	}

	private boolean isValidCvv(String cvv) {
		return cvv != null && cvv.matches("\\d{3,4}");
	}

	private boolean isPresentedCardCredentialValid(Card card, LocalDate expiryDate, String pin, String cvv) {
		return expiryDate != null
				&& YearMonth.from(expiryDate).equals(YearMonth.from(card.getExpiryDate()))
				&& !isBlank(pin)
				&& !isBlank(cvv)
				&& passwordEncoder.matches(pin, card.getPinHash())
				&& passwordEncoder.matches(cvv, card.getCvvHash());
	}

	private BigDecimal defaultLimit(BigDecimal requestedLimit, BigDecimal defaultLimit) {
		return requestedLimit == null || requestedLimit.compareTo(BigDecimal.ZERO) <= 0 ? defaultLimit : requestedLimit;
	}

}
