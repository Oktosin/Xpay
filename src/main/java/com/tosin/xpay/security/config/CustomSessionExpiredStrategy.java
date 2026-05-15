package com.tosin.xpay.security.config;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class CustomSessionExpiredStrategy implements SessionInformationExpiredStrategy{

	@Override
	public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException, ServletException {
		
		HttpServletRequest request = event.getRequest();
		
		HttpServletResponse response = event.getResponse();
		
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		
		Map<String, Object> body = new LinkedHashMap<>();
		
		body.put("error", "Session expired. Please login again");
		
		new ObjectMapper().writeValue(response.getOutputStream(), body);



		
	}

}
