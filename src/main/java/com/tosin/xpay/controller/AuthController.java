package com.tosin.xpay.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.tosin.xpay.dto.PasswordResetConfirmRequest;
import com.tosin.xpay.dto.PasswordResetOtpRequest;
import com.tosin.xpay.dto.UserLoginRequest;
import com.tosin.xpay.dto.UserLogoutRequest;
import com.tosin.xpay.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("auth")
public class AuthController {
	
	@Autowired private AuthService authService;
	
	
	@RequestMapping(value = "login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public Map<String, Object> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) {
		return authService.userLogin(userLoginRequest, httpServletRequest, httpServletResponse, authentication);
	}
	
	@RequestMapping(value = "logout", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public Map<String, Object> userLogout(@RequestBody UserLogoutRequest userLogoutRequest, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) {
		return authService.userLogout(userLogoutRequest, httpServletRequest, httpServletResponse, authentication);
	}

	@RequestMapping(value = "password-reset/request-otp", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public Map<String, Object> requestPasswordResetOtp(@RequestBody PasswordResetOtpRequest passwordResetOtpRequest) {
		return authService.requestPasswordResetOtp(passwordResetOtpRequest);
	}

	@RequestMapping(value = "password-reset/confirm", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
	public Map<String, Object> resetPasswordWithOtp(@RequestBody PasswordResetConfirmRequest passwordResetConfirmRequest) {
		return authService.resetPasswordWithOtp(passwordResetConfirmRequest);
	}

}