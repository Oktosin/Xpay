package com.tosin.xpay.security.config;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;



import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

	private static final Duration WINDOW = Duration.ofMinutes(1);
	private static final int DEFAULT_LIMIT = 120;
	private static final Map<String, Integer> ENDPOINT_LIMITS = Map.of(
			"/auth/login", 5,
			"/auth/password-reset/request-otp", 3,
			"/auth/password-reset/confirm", 5,
			"/card/authorize", 20,
			"/card/payment", 20,
			"/card/reverse", 10,
			"/transaction/reverse", 10
	);

	private final Map<String, Deque<Instant>> requestLog = new ConcurrentHashMap<>();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		String path = requestPath(request);
		int limit = limitForPath(path);

		if (limit < 1) {
			filterChain.doFilter(request, response);
			return;
		}

		String key = rateLimitKey(request, path);
		Instant now = Instant.now();
		Deque<Instant> timestamps = requestLog.computeIfAbsent(key, ignored -> new ArrayDeque<>());

		synchronized (timestamps) {
			while (!timestamps.isEmpty() && timestamps.peekFirst().plus(WINDOW).isBefore(now)) {
				timestamps.removeFirst();
			}

			if (timestamps.size() >= limit) {
				writeRateLimitResponse(response, limit);
				return;
			}

			timestamps.addLast(now);
		}

		filterChain.doFilter(request, response);
	}

	private int limitForPath(String path) {
		return ENDPOINT_LIMITS.entrySet()
				.stream()
				.filter((entry) -> path.startsWith(entry.getKey()))
				.map(Map.Entry::getValue)
				.findFirst()
				.orElse(DEFAULT_LIMIT);
	}

	private String rateLimitKey(HttpServletRequest request, String path) {
		return path + ":" + clientIdentifier(request);
	}

	private String clientIdentifier(HttpServletRequest request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null && !authentication.getName().isBlank() && !"anonymousUser".equals(authentication.getName())) {
			return "user:" + authentication.getName();
		}

		return "ip:" + clientIpAddress(request);
	}

	private String requestPath(HttpServletRequest request) {
		String requestUri = request.getRequestURI();
		String contextPath = request.getContextPath();

		if (contextPath != null && !contextPath.isBlank() && requestUri.startsWith(contextPath)) {
			return requestUri.substring(contextPath.length());
		}

		return requestUri;
	}

	private String clientIpAddress(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");

		if (forwardedFor != null && !forwardedFor.isBlank()) {
			return forwardedFor.split(",")[0].trim();
		}

		return request.getRemoteAddr();
	}

	private void writeRateLimitResponse(HttpServletResponse response, int limit) throws IOException {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
		body.put("error", "Too Many Requests");
		body.put("message", "Rate limit exceeded. Please try again later.");
		body.put("limit", limit);
		body.put("windowSeconds", WINDOW.toSeconds());

		response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write(objectMapper.writeValueAsString(body));
	}

}
