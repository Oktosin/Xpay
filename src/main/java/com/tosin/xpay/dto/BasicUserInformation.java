package com.tosin.xpay.dto;

import java.math.BigDecimal;

import com.tosin.xpay.constant.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BasicUserInformation{

	
	private Long id;
	private String firstName;
	private String lastName;
	private String username;	
	private String email;
	private String phoneNumber;
	private UserRole userRole;

	private BigDecimal balance;
	private String staffId;
	

}
