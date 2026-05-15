package com.tosin.xpay.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.tosin.xpay.dto.BasicUserInformation;
import com.tosin.xpay.service.UserDataService;

@RestController
@RequestMapping(value = "user-data")
public class UserDataController {
	
	@Autowired private UserDataService userDataService;
	
	@RequestMapping(value = "find-user", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> getUserDataByBasicUserInformation(@RequestBody BasicUserInformation basicUserInformation) {
		return userDataService.getUserDataByBasicUserInformation(basicUserInformation);
	}

}
