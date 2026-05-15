package com.tosin.xpay.service;

import org.springframework.http.ResponseEntity;

import com.tosin.xpay.dto.RegistrationRequest;
import com.tosin.xpay.dto.RequestPayload;
import com.tosin.xpay.model.Account;

public interface CustomerService {

	ResponseEntity<?> createCustomer(RegistrationRequest registrationRequest);
	
	ResponseEntity<?> getCustomerByAccountNumber(Account account);
	
	ResponseEntity<?> getAllCustomers(RequestPayload requestPayload);




	






	


}
