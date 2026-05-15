package com.tosin.xpay.constant;

import java.util.ArrayList;
import java.util.List;

public enum UserAuthority {
	ADMIN,
	STAFF,
	CUSTOMER;
	
	private String userAuthority;
	
	public String getUserAuthority() {
		
		return this.userAuthority;
	}
	

	public static List<UserAuthority> getAllUserAuthorityEnumByString(String userAuthority) {
	    	
	        List<UserAuthority> roles = new ArrayList<>();
	        
	        if (userAuthority != null && !userAuthority.isEmpty()) {
	           
	            String[] roleArray = userAuthority.split(","); 
	            
	        for (String s : roleArray) {
	        	
	        	try {
	        		
	        		roles.add(UserAuthority.valueOf(s.trim().toUpperCase()));
	        		
	        	} catch (IllegalArgumentException e) {
	        		
	        		System.out.println("Invalid role: " + s);
	        		
	        	}
	        	
	            }
	        }
	        
	        return roles;
	    }	
	
	}
