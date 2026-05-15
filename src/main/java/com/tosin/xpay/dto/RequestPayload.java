package com.tosin.xpay.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class RequestPayload {
	
	private int pageNumber;
	private int pageSize;
	
	String accountNumber;


}

