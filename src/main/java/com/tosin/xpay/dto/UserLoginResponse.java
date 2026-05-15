package com.tosin.xpay.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginResponse {
	
	private BasicUserInformation basicUserInformation;
	private LocalDateTime lastLogin;
	private String message;
//	private String redirectUrl;

}
