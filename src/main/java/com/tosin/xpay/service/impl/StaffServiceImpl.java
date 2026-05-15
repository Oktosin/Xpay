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
import com.tosin.xpay.dao.StaffDAO;
import com.tosin.xpay.dao.UserDataDAO;
import com.tosin.xpay.dto.RegistrationRequest;
import com.tosin.xpay.dto.RequestPayload;
import com.tosin.xpay.dto.StaffRegistrationResponse;
import com.tosin.xpay.model.Staff;
import com.tosin.xpay.model.UserData;
import com.tosin.xpay.service.StaffService;
import com.tosin.xpay.service.UtilService;
import org.springframework.data.domain.Pageable;

@Service
public class StaffServiceImpl implements StaffService {
	
	@Autowired private StaffDAO staffDAO;
	@Autowired private UserDataDAO userDataDAO;
	@Autowired private UtilService utilService;
	@Autowired private PasswordEncoder passwordEncoder;
	
	Map<String, Object> response = new LinkedHashMap<>();
	
	@Override
	public ResponseEntity<?> createStaff(RegistrationRequest registrationRequest) {
		
		UserData userData = new UserData();
		
		LocalDateTime presentDateTime = LocalDateTime.now();
		Set<UserAuthority> authorities = new HashSet<>(UserAuthority.getAllUserAuthorityEnumByString("STAFF"));
		
		userData.setUsername(registrationRequest.getUsername());
		userData.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
		userData.setEmail(registrationRequest.getEmail());
		userData.setPhoneNumber(registrationRequest.getPhoneNumber());
		userData.setUserRole(UserRole.STAFF);
		userData.setAuthorities(authorities);
		userData.setCreatedAt(presentDateTime);
		userData.setUpdatedAt(presentDateTime);
		userData = userDataDAO.saveAndFlush(userData);
		
		Staff staff = new Staff();
		
		staff.setFirstName(registrationRequest.getFirstName());
		staff.setLastName(registrationRequest.getLastName());
		staff.setStaffId(utilService.generateStaffId());
		staff.setUserData(userData);
		staff = staffDAO.save(staff);
		
		StaffRegistrationResponse staffRegistrationResponse = utilService.getBasicStaffInformation(userData, staff);
		response.put("staffInfo", staffRegistrationResponse);
		
		return utilService.getResponse(response, HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<?> getStaffByStaffId(String staffId) {
		
		Staff existingStaff = staffDAO.findStaffByStaffId(staffId);
		
		if (existingStaff != null) {
			
			response.put("staffInfo", existingStaff);
			
		} else { 
			
			response.put("errorMessage", "staff not found with ID " + staffId);
		}
		
		return utilService.getResponse(response, HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<?> getAllStaff(RequestPayload requestPayload) {
		
		int pageNumber = requestPayload.getPageNumber();
		int pageSize = utilService.pageSizeLimit(pageNumber);
		
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Page<Staff> staff = staffDAO.findAllStaff(pageable);
		
		response.put("staff", staff.getContent());
		response.put("currentPage", staff.getNumber());
		response.put("totalElement", staff.getTotalElements());
		response.put("totalPages", staff.getTotalPages());
		
		return utilService.getResponse(response, HttpStatus.OK);
	}
	
	

}
