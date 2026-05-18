package com.tosin.xpay.service.impl;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.tosin.xpay.dto.BasicUserInformation;
import com.tosin.xpay.dto.CustomerRegistrationResponse;
import com.tosin.xpay.dto.StaffRegistrationResponse;
import com.tosin.xpay.model.Account;
import com.tosin.xpay.model.Customer;
import com.tosin.xpay.model.Staff;
import com.tosin.xpay.model.UserData;
import com.tosin.xpay.service.UtilService;

@Service
public class UtilServiceImpl implements UtilService{
	
	@Override
	public CustomerRegistrationResponse getBasicCustomerInformation(UserData userData, Customer customer, Account account) {
		
		return CustomerRegistrationResponse.builder()
								   .id(userData.getId())
								   .firstName(account.getFirstName())
								   .lastName(account.getLastName())
								   .username(userData.getUsername())
								   .email(userData.getEmail())
								   .phoneNumber(userData.getPhoneNumber())
								   .userRole(userData.getUserRole())
								   .balance(account.getBalance())
								   .build();
}
	
	@Override
	public StaffRegistrationResponse getBasicStaffInformation(UserData userData, Staff staff) {
		
		return StaffRegistrationResponse.builder()
								   .id(userData.getId())
								   .firstName(staff.getFirstName())
								   .lastName(staff.getLastName())
								   .username(userData.getUsername())
								   .email(userData.getEmail())
								   .phoneNumber(userData.getPhoneNumber())
								   .userRole(userData.getUserRole())
								   .staffId(staff.getStaffId())
								   .build();
}
	
	@Override
	public BasicUserInformation getBasicUserInformation(UserData userData, Customer customer, Staff staff) {
		
		return BasicUserInformation.builder()
								   .id(userData.getId())
								   .firstName(staff.getFirstName())
								   .lastName(staff.getLastName())
								   .username(userData.getUsername())
								   .email(userData.getEmail())
								   .phoneNumber(userData.getPhoneNumber())
								   .userRole(userData.getUserRole())
								   .build();
}
	
	@Override
	public ResponseEntity<?> getResponse(Map<String, Object> response, HttpStatus httpStatus) {
		return ResponseEntity.status(httpStatus).body(response);
		
	}
	
	
	@Override
	public String generateAccountNumber() {
		
	    long number = ThreadLocalRandom.current()
	            .nextLong(1000000000L, 10000000000L);

	    return String.valueOf(number);
	          
	}	
	
	@Override
	public int pageSizeLimit(int pageSize) {
		
		if (pageSize > 50) {
			
			return 50;
			
		} else if (pageSize < 1) {
			
			pageSize = 1;
			
		} return pageSize;
	} 
	
	@Override
	public String generateStaffId() {
		
		String key1    = "";
		String key2    = "";
		
		String hyphenGrouping = "";	
		
		Random random = new Random((new Date()).getTime());
		int lenght = 10;

		char[] values = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
                
		for (int i = 0; i < lenght; i++) {
			int idx = random.nextInt(values.length);
			key1 += values[idx];
		}                

		for (int i = 0; i < lenght; i++) {
			int idx = random.nextInt(values.length);
			key2 += values[idx];
		}			
		
		hyphenGrouping = "staff-id-" + key1 + "-" + key2;
		
		return hyphenGrouping;
                
	}	
	
}
