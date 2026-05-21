package com.tosin.xpay.dto;

import java.time.LocalDate;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardIssueRequest {

	private String accountNumber;
	private String cardNumber;
	private String cardHolderName;
	private LocalDate expiryDate;
	private String pin;
	private String cvv;
	private BigDecimal transactionLimit;
	private BigDecimal dailyLimit;
	private BigDecimal monthlyLimit;

}
