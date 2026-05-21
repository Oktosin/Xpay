package com.tosin.xpay.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardAuthorizationRequest {

	private String cardNumber;
	private LocalDate expiryDate;
	private String pin;
	private String cvv;
	private BigDecimal amount;
	private String idempotencyKey;
	private String merchantId;
	private String merchantName;
	private String terminalId;
	private String channel;
	private String location;
	private String ipAddress;
	private String description;

}
