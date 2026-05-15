package com.tosin.xpay.constant;

import java.util.ArrayList;
import java.util.List;



public enum UserRole {
	STAFF,
	CUSTOMER;
	
	private String userRole;
	
	public String getUserRole() {
		return this.userRole;
	}
	
	public static List<UserRole> getAllUserRoleEnumByString(String userRole) {
	    	
	        List<UserRole> roles = new ArrayList<>();
	        
	        if (userRole != null && !userRole.isEmpty()) {
	        	
	            // Splits "STAFF,CUSTOMER" into ["STAFF", "CUSTOMER"]
	            String[] roleArray = userRole.split(","); 
	            
	        for (String s : roleArray) {
	        	
	        	try {
	        		
	        		// Converts the string "STAFF" into UserRole.STAFF
	        		roles.add(UserRole.valueOf(s.trim().toUpperCase()));
	        		
	        	} catch (IllegalArgumentException e) {
	        		
	        		// Handle cases where the string doesn't match any enum constant
	        		System.out.println("Invalid role: " + s);
	        		
	        	}
	        	
	            }
	        }
	        
	        return roles;
	    }

}
