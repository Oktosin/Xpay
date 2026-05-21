package com.tosin.xpay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardReversalRequest {

	private String reference;
	private String idempotencyKey;
	private String processedBy;
	private String terminalId;
	private String channel;
	private String location;
	private String ipAddress;
	private String description;

}
