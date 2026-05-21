package com.tosin.xpay.service.impl;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.tosin.xpay.model.UserData;
import com.tosin.xpay.service.EmailNotificationService;



@Service
public class EmailNotificationServiceImpl implements EmailNotificationService {
	
	private static final Logger logger = LoggerFactory.getLogger(EmailNotificationServiceImpl.class);
	
	private final JavaMailSender javaMailSender;
	
	@Value("${xpay.notification.email.enabled:false}")
	private boolean emailNotificationEnabled;
	
	@Value("${xpay.notification.email.from:no-reply@xpay.local}")
	private String fromAddress;
	
	public EmailNotificationServiceImpl(JavaMailSender javaMailSender) {
		this.javaMailSender = javaMailSender;
	}
	

	@Override
	public void sendRegistrationNotification(UserData userData, String firstName) {
		
		if (userData == null) {
			
			return;
		}
		
		String name = firstName == null || firstName.isBlank() ? userData.getUsername() : firstName;
		
		String body = "Hello " + name + ",\n\n"
				+ "Your Xpay account has been created successfully.\n\n"
				+ "Username: " + userData.getUsername() + "\n"
				+ "Role: " + userData.getUserRole() + "\n\n"
				+ "Thank you for choosing Xpay.";
		
		sendEmail(userData.getEmail(), "Welcome to Xpay", body);
		
		
	}
	
	@Override
	public void sendTransactionNotification(UserData userData, String transactionType, BigDecimal amount, String accountNumber, String description) {
		
		if (userData == null) {
			
			return;
		}

		String body = "Hello " + userData.getUsername() + ",\n\n"
				+ "A transaction has been processed on your Xpay account.\n\n"
				+ "Transaction type: " + transactionType + "\n"
				+ "Account number: " + accountNumber + "\n"
				+ "Amount: " + amount + "\n"
				+ "Description: " + safeText(description) + "\n\n"
				+ "If you did not authorize this transaction, please contact support immediately.";

		sendEmail(userData.getEmail(), "Xpay Transaction Notification", body);
	}

	@Override
	public void sendLoginNotification(UserData userData, String ipAddress, String userAgent) {

		if (userData == null) {

			return;
		}

		String body = "Hello " + userData.getUsername() + ",\n\n"
				+ "A successful login was detected on your Xpay account.\n\n"
				+ "IP address: " + safeText(ipAddress) + "\n"
				+ "Device: " + safeText(userAgent) + "\n\n"
				+ "If this was not you, please contact support immediately.";

		sendEmail(userData.getEmail(), "Xpay Login Alert", body);
	}

	@Override
	public void sendPasswordResetOtpNotification(UserData userData, String otp, int expiryMinutes) {

		if (userData == null) {

			return;
		}

		String body = "Hello " + userData.getUsername() + ",\n\n"
				+ "Use this OTP to reset your Xpay password:\n\n"
				+ otp + "\n\n"
				+ "This OTP expires in " + expiryMinutes + " minutes.\n"
				+ "If you did not request a password reset, please ignore this email and contact support.";

		sendEmail(userData.getEmail(), "Xpay Password Reset OTP", body);
	}

	@Override
	public void sendPasswordResetSuccessNotification(UserData userData) {

		if (userData == null) {

			return;
		}

		String body = "Hello " + userData.getUsername() + ",\n\n"
				+ "Your Xpay password has been reset successfully.\n\n"
				+ "If you did not make this change, please contact support immediately.";

		sendEmail(userData.getEmail(), "Xpay Password Reset Successful", body);
	}

	@Override
	public void sendCardTransactionNotification(UserData userData, String transactionType, String transactionStatus, BigDecimal amount, String maskedCardNumber, String merchantName, String description) {

		if (userData == null) {

			return;
		}

		String body = "Hello " + userData.getUsername() + ",\n\n"
				+ "A card transaction has been processed on your Xpay account.\n\n"
				+ "Transaction type: " + transactionType + "\n"
				+ "Status: " + transactionStatus + "\n"
				+ "Card: " + safeText(maskedCardNumber) + "\n"
				+ "Merchant: " + safeText(merchantName) + "\n"
				+ "Amount: " + amount + "\n"
				+ "Description: " + safeText(description) + "\n\n"
				+ "If you did not authorize this transaction, please contact support immediately.";

		sendEmail(userData.getEmail(), "Xpay Card Transaction Notification", body);
	}

	private void sendEmail(String recipient, String subject, String body) {
		
		if (!emailNotificationEnabled || recipient == null || recipient.isBlank()) {
			
			return;
		}

		try {
			
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			
			mailMessage.setFrom(fromAddress);
			mailMessage.setTo(recipient);
			mailMessage.setSubject(subject);
			mailMessage.setText(body);

			javaMailSender.send(mailMessage);
			
		} catch (MailException exception) {
			
			logger.warn("Email notification could not be sent to {}", recipient, exception);
		}
	}

	private String safeText(String value) {
		
		return value == null || value.isBlank() ? "N/A" : value;
	}

	
}
