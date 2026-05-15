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
import com.tosin.xpay.service.StaffService;

@RestController
@RequestMapping(value = "staff")
public class StaffController {
	
	@Autowired private StaffService staffService;
	
	@RequestMapping(value = "create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> createCustomer(@RequestBody RegistrationRequest registrationRequest) {
		return staffService.createStaff(registrationRequest);
	}
	
	@RequestMapping(value = "find", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> getStaffByStaffId(@RequestBody String staffId) {
		return staffService.getStaffByStaffId(staffId);
	}
	
	@RequestMapping(value = "find-all", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> getAllCustomers(@RequestBody RequestPayload requestPayload) {
		return staffService.getAllStaff(requestPayload);
	}

}
