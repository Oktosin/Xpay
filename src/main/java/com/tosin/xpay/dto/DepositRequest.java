package com.tosin.xpay.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepositRequest {
	
	private String accountNumber;
	private BigDecimal amount;
	private String description;
	

}
