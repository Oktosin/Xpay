package com.tosin.xpay.service;

import org.springframework.http.ResponseEntity;


import com.tosin.xpay.dto.RegistrationRequest;
import com.tosin.xpay.dto.RequestPayload;


public interface StaffService {

	ResponseEntity<?> createStaff(RegistrationRequest registrationRequest);
	
	ResponseEntity<?> getStaffByStaffId(String staffId);
	
	ResponseEntity<?> getAllStaff(RequestPayload requestPayload);

	

	


	


}
