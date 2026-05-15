package com.tosin.xpay.dto;


import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {
	
	private String username;	
	private String password;	
	private String email;	
	private String phoneNumber;
	private String firstName;	
	private String lastName;
	private BigDecimal balance;

	
}

