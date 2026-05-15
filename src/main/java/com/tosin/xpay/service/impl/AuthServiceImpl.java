package com.tosin.xpay.service.impl;

import org.springframework.stereotype.Service;

import com.tosin.xpay.constant.UserRole;
import com.tosin.xpay.dao.CustomerDAO;
import com.tosin.xpay.dao.StaffDAO;
import com.tosin.xpay.dao.UserDataDAO;
import com.tosin.xpay.dto.BasicUserInformation;
import com.tosin.xpay.dto.UserLoginRequest;
import com.tosin.xpay.dto.UserLoginResponse;
import com.tosin.xpay.dto.UserLogoutRequest;
import com.tosin.xpay.model.Customer;
import com.tosin.xpay.model.Staff;
import com.tosin.xpay.model.UserData;
import com.tosin.xpay.service.AuthService;
import com.tosin.xpay.service.UtilService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired private UtilService utilService;

	//REQUIRED SPRING SECURITY DEPENDENCY INJECTION FOR LOGIN AND LOGOUT OPERATION
	@Autowired private AuthenticationManager authenticationManager;
	@Autowired private SecurityContextRepository securityContextRepository; 
	@Autowired private SecurityContextHolderStrategy securityContextHolderStrategy;
	
	private final int MAXIMUM_INACTIVE_PERIOD_IN_MINUTES = 10;
	
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
			        	
			        } 
			   	        	  	
		        } else {
		        	
		        	map.put("systemStatusResponse", "Invalid login credentials");	
		        	
		        }
		       
			}
			
		}
					
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

}
