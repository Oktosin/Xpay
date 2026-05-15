package com.tosin.xpay.service;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.tosin.xpay.dto.BasicUserInformation;
import com.tosin.xpay.dto.CustomerRegistrationResponse;
import com.tosin.xpay.dto.StaffRegistrationResponse;
import com.tosin.xpay.model.Account;
import com.tosin.xpay.model.Customer;
import com.tosin.xpay.model.Staff;
import com.tosin.xpay.model.UserData;


public interface UtilService {

	BasicUserInformation getBasicUserInformation(UserData userData, Customer customer, Staff staff);

	CustomerRegistrationResponse getBasicCustomerInformation(UserData userData, Customer customer, Account account);

	StaffRegistrationResponse getBasicStaffInformation(UserData userData, Staff staff);
	
	ResponseEntity<?> getResponse(Map<String, Object> response, HttpStatus httpStatus);

	String generateAccountNumber();

	int pageSizeLimit(int pageSize);

	String generateStaffId();

	
	

	

	
	

}
