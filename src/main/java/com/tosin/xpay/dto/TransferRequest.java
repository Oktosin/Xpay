package com.tosin.xpay.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {
	
	private String senderAccountNumber;
	private String receiverAccountNumber;
	private BigDecimal amount;
	private String idempotencyKey;
	private String description;

}
