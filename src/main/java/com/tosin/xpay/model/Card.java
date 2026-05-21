package com.tosin.xpay.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tosin.xpay.constant.CardStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "card")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Card implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;

	@Column(name = "cardNumberHash", nullable = false, unique = true)
	@JsonIgnore
	private String cardNumberHash;

	@Column(name = "pinHash", nullable = false)
	@JsonIgnore
	private String pinHash;

	@Column(name = "cvvHash", nullable = false)
	@JsonIgnore
	private String cvvHash;

	@Column(name = "maskedCardNumber", nullable = false)
	private String maskedCardNumber;

	@Column(name = "cardHolderName", nullable = false)
	private String cardHolderName;

	@Column(name = "expiryDate", nullable = false)
	private LocalDate expiryDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "cardStatus", nullable = false)
	private CardStatus cardStatus;

	@Column(name = "holdBalance", nullable = false)
	private BigDecimal holdBalance;

	@Column(name = "transactionLimit", nullable = false)
	private BigDecimal transactionLimit;

	@Column(name = "dailyLimit", nullable = false)
	private BigDecimal dailyLimit;

	@Column(name = "monthlyLimit", nullable = false)
	private BigDecimal monthlyLimit;

	@Column(name = "createdAt", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updatedAt", nullable = false)
	private LocalDateTime updatedAt;

}
