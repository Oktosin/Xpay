package com.tosin.xpay.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.tosin.xpay.dto.RegistrationRequest;
import com.tosin.xpay.dto.RequestPayload;
import com.tosin.xpay.model.Account;
import com.tosin.xpay.service.CustomerService;

@RestController
@RequestMapping(value = "customer")
public class CustomerController {
	
	@Autowired private CustomerService customerService;
	
	@RequestMapping(value = "create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> createCustomer(@RequestBody RegistrationRequest registrationRequest) {
		return customerService.createCustomer(registrationRequest);
	}
	
	@RequestMapping(value = "find-by-account-number", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> getCustomerByAccountNumber(@RequestBody Account account) {
		return customerService.getCustomerByAccountNumber(account);
	}
	
	@RequestMapping(value = "find-all", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> getAllCustomers(@RequestBody RequestPayload requestPayload) {
		return customerService.getAllCustomers(requestPayload);
	}

}
