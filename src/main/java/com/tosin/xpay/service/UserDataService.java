package com.tosin.xpay.service;

import org.springframework.http.ResponseEntity;

import com.tosin.xpay.dto.BasicUserInformation;



public interface UserDataService {

	ResponseEntity<?> getUserDataByBasicUserInformation(BasicUserInformation basicUserInformation);

}
