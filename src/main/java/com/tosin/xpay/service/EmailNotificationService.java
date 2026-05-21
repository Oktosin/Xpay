package com.tosin.xpay.service;

import java.math.BigDecimal;

import com.tosin.xpay.model.UserData;

public interface EmailNotificationService {

	void sendRegistrationNotification(UserData userData, String firstName);

	void sendTransactionNotification(UserData userData, String transactionType, BigDecimal amount, String accountNumber, String description);

	void sendLoginNotification(UserData userData, String ipAddress, String userAgent);

	void sendPasswordResetOtpNotification(UserData userData, String otp, int expiryMinutes);

	void sendPasswordResetSuccessNotification(UserData userData);

	void sendCardTransactionNotification(UserData userData, String transactionType, String transactionStatus, BigDecimal amount, String maskedCardNumber, String merchantName, String description);

}
