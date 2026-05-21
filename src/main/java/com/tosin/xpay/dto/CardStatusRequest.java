package com.tosin.xpay.dto;

import com.tosin.xpay.constant.CardStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardStatusRequest {

	private String cardNumber;
	private CardStatus cardStatus;

}
