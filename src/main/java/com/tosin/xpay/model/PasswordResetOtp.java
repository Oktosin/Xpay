package com.tosin.xpay.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "passwordResetOtp")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordResetOtp implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserData userData;

	@Column(name = "otpHash", nullable = false)
	private String otpHash;

	@Column(name = "used", nullable = false)
	private boolean used;

	@Column(name = "expiresAt", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "createdAt", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "usedAt")
	private LocalDateTime usedAt;

}
