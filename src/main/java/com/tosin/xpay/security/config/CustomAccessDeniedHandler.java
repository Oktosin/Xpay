package com.tosin.xpay.security.config;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler{

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
		
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		
		Map<Object, String> body = new LinkedHashMap<>();
		
		body.put("status", "403");
		body.put("error", "forbidden");
		body.put("message", "You do not have permission to access this resource");

		new ObjectMapper().writeValue(response.getOutputStream(), body);
		
		
	
		
	}
	


}
