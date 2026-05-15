package com.tosin.xpay.security.config;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint{

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, 
						AuthenticationException authException) throws IOException, ServletException {
		
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		
		Map<String, Object> body = new LinkedHashMap<>();
		
		body.put("status", "401");
		body.put("status", "Unauthorized");
		body.put("message", "You must be logged in to access this resource");
		body.put("path", request.getServletPath());
		
		new ObjectMapper().writeValue(response.getOutputStream(), body);
		
		
	}

}
