package com.tosin.xpay.service;

import java.util.Map;

import org.springframework.security.core.Authentication;

import com.tosin.xpay.dto.UserLoginRequest;
import com.tosin.xpay.dto.UserLogoutRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

	Map<String, Object> userLogout(UserLogoutRequest userLogoutRequest, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication);

	Map<String, Object> userLogin(UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication);

}
