package com.tosin.xpay.service.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tosin.xpay.constant.UserAuthority;
import com.tosin.xpay.constant.UserRole;
import com.tosin.xpay.dao.AccountDAO;
import com.tosin.xpay.dao.CustomerDAO;
import com.tosin.xpay.dao.UserDataDAO;
import com.tosin.xpay.dto.CustomerRegistrationResponse;
import com.tosin.xpay.dto.RegistrationRequest;
import com.tosin.xpay.dto.RequestPayload;
import com.tosin.xpay.model.Account;
import com.tosin.xpay.model.Customer;
import com.tosin.xpay.model.UserData;
import com.tosin.xpay.service.CustomerService;
import com.tosin.xpay.service.UtilService;
import org.springframework.data.domain.Pageable;

@Service
public class CustomerServiceImpl implements CustomerService {
	
	@Autowired private CustomerDAO customerDAO;
	@Autowired private UserDataDAO userDataDAO;
	@Autowired private UtilService utilService;
	@Autowired private AccountDAO accountDAO;
	@Autowired private PasswordEncoder passwordEncoder;
	
	Map<String, Object> response = new LinkedHashMap<>();
	
	@Override
	public ResponseEntity<?> createCustomer(RegistrationRequest registrationRequest) {
		
		UserData userData = new UserData();
		
		LocalDateTime presentDateTime = LocalDateTime.now();
		Set<UserAuthority> authorities = new HashSet<>(UserAuthority.getAllUserAuthorityEnumByString("CUSTOMER"));
		
		userData.setUsername(registrationRequest.getUsername());
		userData.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
		userData.setEmail(registrationRequest.getEmail());
		userData.setPhoneNumber(registrationRequest.getPhoneNumber());
		userData.setUserRole(UserRole.CUSTOMER);
		userData.setAuthorities(authorities);
		userData.setCreatedAt(presentDateTime);
		userData.setUpdatedAt(presentDateTime);
		userData = userDataDAO.saveAndFlush(userData);
		
		
		Account account = new Account();
		
		account.setFirstName(registrationRequest.getFirstName());
		account.setLastName(registrationRequest.getLastName());;
		account.setAccountNumber(utilService.generateAccountNumber());
		account.setBalance(registrationRequest.getBalance());
		account = accountDAO.saveAndFlush(account);
		
		Customer customer = new Customer();
		
		customer.setUserData(userData);
		customer.setAccount(account);
		customer = customerDAO.save(customer);
		
		CustomerRegistrationResponse customerRegistrationResponse = utilService.getBasicCustomerInformation(userData, customer, account);
		response.put("customerInfo", customerRegistrationResponse);
		
		return utilService.getResponse(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getCustomerByAccountNumber(Account account) {
		
		Account existingAccount = accountDAO.findByAccountNumber(account.getAccountNumber());
		
		if (existingAccount != null) {
			
			response.put("customerInfo", existingAccount);
			
		} else { 
			
			response.put("errorMessage", "account not found");
		}
		
		return utilService.getResponse(response, HttpStatus.OK);

	}
	
	@Override
	public ResponseEntity<?> getAllCustomers(RequestPayload requestPayload) {
		
		int pageNumber = requestPayload.getPageNumber();
		int pageSize = utilService.pageSizeLimit(pageNumber);
		
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		
		Page<Customer> customers = customerDAO.findAllCustomers(pageable);
		
		response.put("customers", customers.getContent());
		response.put("currentPage", customers.getNumber());
		response.put("totalElements", customers.getTotalElements());
		response.put("totaalPages", customers.getTotalPages());
		
		return utilService.getResponse(response, HttpStatus.OK);
	}
	
	
}
