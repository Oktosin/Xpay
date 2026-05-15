package com.tosin.xpay.service.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.tosin.xpay.dao.UserDataDAO;
import com.tosin.xpay.dto.BasicUserInformation;
import com.tosin.xpay.model.UserData;
import com.tosin.xpay.service.UserDataService;
import com.tosin.xpay.service.UtilService;

@Service
public class UserDataServiceImpl implements UserDataService {
	
	Map<String, Object> response = new LinkedHashMap<>();
	
	@Autowired private UserDataDAO userDataDAO;
	@Autowired private UtilService utilService;
	
	@Override
	public ResponseEntity<?> getUserDataByBasicUserInformation(BasicUserInformation basicUserInformation) {
		
		String username = basicUserInformation.getUsername();
		String email = basicUserInformation.getEmail();
		String phoneNumber = basicUserInformation.getPhoneNumber();
		
		if(username == null || email == null || phoneNumber == null) {
					
					response.put("errorMessage","user not found");
				}
		
		Optional<UserData> userData = userDataDAO.findUserDataByBasicUserInformation(username, email, phoneNumber);
		response.put("userInfo", userData);
		
		return utilService.getResponse(response, HttpStatus.OK);
	}

}
