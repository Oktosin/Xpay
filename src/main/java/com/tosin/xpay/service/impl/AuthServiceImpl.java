package com.tosin.xpay.service.impl;

import org.springframework.stereotype.Service;

import com.tosin.xpay.constant.UserRole;
import com.tosin.xpay.dao.CustomerDAO;
import com.tosin.xpay.dao.PasswordResetOtpDAO;
import com.tosin.xpay.dao.StaffDAO;
import com.tosin.xpay.dao.UserDataDAO;
import com.tosin.xpay.dto.BasicUserInformation;
import com.tosin.xpay.dto.PasswordResetConfirmRequest;
import com.tosin.xpay.dto.PasswordResetOtpRequest;
import com.tosin.xpay.dto.UserLoginRequest;
import com.tosin.xpay.dto.UserLoginResponse;
import com.tosin.xpay.dto.UserLogoutRequest;
import com.tosin.xpay.model.Customer;
import com.tosin.xpay.model.PasswordResetOtp;
import com.tosin.xpay.model.Staff;
import com.tosin.xpay.model.UserData;
import com.tosin.xpay.service.AuthService;
import com.tosin.xpay.service.EmailNotificationService;
import com.tosin.xpay.service.UtilService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;



@Service
@Transactional
public class AuthServiceImpl implements AuthService {
	
	@Autowired private PasswordEncoder passwordEncoder;
	
	@Autowired private CustomerDAO customerDAO;
	@Autowired private StaffDAO staffDAO;
	@Autowired private UserDataDAO userDataDAO;
	@Autowired private PasswordResetOtpDAO passwordResetOtpDAO;

	@Autowired private UtilService utilService;
	@Autowired private EmailNotificationService emailNotificationService;

	//REQUIRED SPRING SECURITY DEPENDENCY INJECTION FOR LOGIN AND LOGOUT OPERATION
	@Autowired private AuthenticationManager authenticationManager;
	@Autowired private SecurityContextRepository securityContextRepository; 
	@Autowired private SecurityContextHolderStrategy securityContextHolderStrategy;
	
	private final int MAXIMUM_INACTIVE_PERIOD_IN_MINUTES = 10;
	private final int PASSWORD_RESET_OTP_EXPIRY_MINUTES = 10;
	private final SecureRandom secureRandom = new SecureRandom();
	
	@Override
	public Map<String, Object> userLogout(UserLogoutRequest userLogoutRequest, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) {

		Map<String, Object> map = new LinkedHashMap<>();
	
		boolean isUserLoggedOut = logoutUser(httpServletRequest, httpServletResponse, authentication);
		
		if (isUserLoggedOut) {
		
			map.put("systemStatusResponse", "You have been logged out successfully");

		} else {

			map.put("systemStatusResponse", "No active session found for logout operation");

		}

		return map;
	}    
	
	@Override
	public Map<String, Object> userLogin(UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) {

		Map<String, Object> map = new LinkedHashMap<>();
		
		logoutUser(httpServletRequest, httpServletResponse, authentication);
		
		UserData userData = userDataDAO.findUserDataByUsername(userLoginRequest.getUserId());
		
		if (userData == null) {
			
			map.put("systemStatusResponse", "Invalid user");
			
		} else {
			
			boolean isValidated = passwordEncoder.matches(userLoginRequest.getPassword(), userData.getPassword());

			if (!isValidated) {

				map.put("systemStatusResponse", "Invalid user id or password");

			} else {
				
				//GET USER CREDENTIAL WRAPPED FOR AUTHENTICATION TOKEN
		        UsernamePasswordAuthenticationToken authenticationToken = UsernamePasswordAuthenticationToken.unauthenticated(userLoginRequest.getUserId(), userLoginRequest.getPassword());			        
		        	
			     Authentication newAuthentication = authenticationManager.authenticate(authenticationToken);
			       
			    if (newAuthentication.isAuthenticated()) {// IF USER IS AUTHENTICATED			        
			        
			        SecurityContext securityContext = securityContextHolderStrategy.createEmptyContext();

			        //SET SECURITY CONTEXT APPLICATION FROM AUTHENTICATION
			        securityContext.setAuthentication(newAuthentication);
			        securityContextHolderStrategy.setContext(securityContext);
			        
			        //SAVE THE AUTHENTICATION SECURITY CONTEXT
			        securityContextRepository.saveContext(securityContext, httpServletRequest, httpServletResponse);    
			        
			        //SET MAXIMUM INACTIVE INTERVAL TO INVALIDATE USER SESSION
			        //AN INTERVAL VALUE OF ZERO INDICATES THAT THE SESSION SHOULD NEVER TIMEOUT. 
			        httpServletRequest.getSession().setMaxInactiveInterval(MAXIMUM_INACTIVE_PERIOD_IN_MINUTES * 60);//CURRENTLY SET TO 10 MINUTES
			        
					LocalDateTime presentDateTime = LocalDateTime.now();
								
			        userData.setUpdatedAt(presentDateTime);
			        userDataDAO.saveAndFlush(userData);
			        
			        Staff staff = null;
			        Customer customer = null;
			        
			        UserRole userRole = userData.getUserRole();
			        
			        if (userRole == UserRole.STAFF) {
			        	
			        	staff = staffDAO.findStaffByUserData(userData);
			        	
			        } else if (userRole == UserRole.CUSTOMER) {
			        	
			        	customer = customerDAO.findCustomerByUserData(userData);
			        }
			        
			        BasicUserInformation basicUserInformation = utilService.getBasicUserInformation(userData, customer, staff);
			        
			        if (basicUserInformation == null) {
			        	
			        	map.put("errorMessage", "user not found");
			        	
			        } else {
			        	
				        //SET USER LOGIN RESPONSE
						UserLoginResponse userLoginResponse = new UserLoginResponse();
						
						userLoginResponse.setBasicUserInformation(basicUserInformation);
						userLoginResponse.setLastLogin(presentDateTime);
				        				        
				        //PUT RESPOSNE OBJECT INTO MAP
				        userLoginResponse.setMessage("Login successful");
						map.put("UserLoginResponse", userLoginResponse);

						emailNotificationService.sendLoginNotification(userData, getClientIpAddress(httpServletRequest), httpServletRequest.getHeader("User-Agent"));
			        	
			        } 
			   	        	  	
		        } else {
		        	
		        	map.put("systemStatusResponse", "Invalid login credentials");	
		        	
		        }
		       
			}
			
		}
					
		return map;
		
	}	

	@Override
	public Map<String, Object> requestPasswordResetOtp(PasswordResetOtpRequest passwordResetOtpRequest) {

		Map<String, Object> map = new LinkedHashMap<>();
		
		map.put("systemStatusResponse", "If the account exists, a password reset OTP has been sent");

		if (passwordResetOtpRequest == null || isBlank(passwordResetOtpRequest.getUserId())) {

			return map;
		}

		UserData userData = findUserByUsernameOrEmail(passwordResetOtpRequest.getUserId());

		if (userData == null) {

			return map;
		}

		List<PasswordResetOtp> activeOtps = passwordResetOtpDAO.findAllByUserDataAndUsedFalse(userData);

		activeOtps.forEach((passwordResetOtp) -> {
			passwordResetOtp.setUsed(true);
			passwordResetOtp.setUsedAt(LocalDateTime.now());
		});

		passwordResetOtpDAO.saveAll(activeOtps);

		String otp = generateOtp();
		LocalDateTime presentDateTime = LocalDateTime.now();

		PasswordResetOtp passwordResetOtp = PasswordResetOtp.builder()
				.userData(userData)
				.otpHash(passwordEncoder.encode(otp))
				.used(false)
				.expiresAt(presentDateTime.plusMinutes(PASSWORD_RESET_OTP_EXPIRY_MINUTES))
				.createdAt(presentDateTime)
				.build();

		passwordResetOtpDAO.save(passwordResetOtp);
		emailNotificationService.sendPasswordResetOtpNotification(userData, otp, PASSWORD_RESET_OTP_EXPIRY_MINUTES);

		return map;
	}

	@Override
	public Map<String, Object> resetPasswordWithOtp(PasswordResetConfirmRequest passwordResetConfirmRequest) {

		Map<String, Object> map = new LinkedHashMap<>();

		if (passwordResetConfirmRequest == null || isBlank(passwordResetConfirmRequest.getUserId()) || isBlank(passwordResetConfirmRequest.getOtp()) || isBlank(passwordResetConfirmRequest.getNewPassword())) {

			map.put("systemStatusResponse", "user id, OTP and new password are required");
			return map;
		}

		UserData userData = findUserByUsernameOrEmail(passwordResetConfirmRequest.getUserId());

		if (userData == null) {

			map.put("systemStatusResponse", "Invalid password reset request");
			return map;
		}

		PasswordResetOtp passwordResetOtp = passwordResetOtpDAO.findLatestUnusedOtpByUserData(userData, PageRequest.of(0, 1)).stream().findFirst().orElse(null);

		if (passwordResetOtp == null || passwordResetOtp.getExpiresAt().isBefore(LocalDateTime.now())) {

			map.put("systemStatusResponse", "OTP is invalid or expired");
			return map;
		}

		if (!passwordEncoder.matches(passwordResetConfirmRequest.getOtp(), passwordResetOtp.getOtpHash())) {

			map.put("systemStatusResponse", "OTP is invalid or expired");
			return map;
		}

		LocalDateTime presentDateTime = LocalDateTime.now();

		userData.setPassword(passwordEncoder.encode(passwordResetConfirmRequest.getNewPassword()));
		userData.setUpdatedAt(presentDateTime);
		userDataDAO.saveAndFlush(userData);

		passwordResetOtp.setUsed(true);
		passwordResetOtp.setUsedAt(presentDateTime);
		passwordResetOtpDAO.save(passwordResetOtp);

		emailNotificationService.sendPasswordResetSuccessNotification(userData);

		map.put("systemStatusResponse", "Password reset successful");
		return map;
	}

	private boolean logoutUser(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) {
		
		authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (!(authentication instanceof AnonymousAuthenticationToken)) {

			new SecurityContextLogoutHandler().logout(httpServletRequest, httpServletResponse, authentication);			
			
			return true;

		} 	
		
		return false;
		
	}

	private String getClientIpAddress(HttpServletRequest httpServletRequest) {

		String forwardedFor = httpServletRequest.getHeader("X-Forwarded-For");

		if (forwardedFor != null && !forwardedFor.isBlank()) {

			return forwardedFor.split(",")[0].trim();
		}

		return httpServletRequest.getRemoteAddr();
	}

	private UserData findUserByUsernameOrEmail(String userId) {

		UserData userData = userDataDAO.findUserDataByUsername(userId);

		if (userData != null) {

			return userData;
		}

		return userDataDAO.findUserByEmail(userId);
	}

	private String generateOtp() {

		return String.format("%06d", secureRandom.nextInt(1000000));
	}

	private boolean isBlank(String value) {

		return value == null || value.isBlank();
	}

}
