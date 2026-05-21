package com.tosin.xpay.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tosin.xpay.constant.CardTransactionStatus;
import com.tosin.xpay.constant.CardTransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "cardTransaction")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardTransaction implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "card_id", nullable = false)
	@JsonIgnore
	private Card card;

	@Column(name = "reference", nullable = false, unique = true)
	private String reference;

	@Column(name = "originalTransactionReference", unique = true)
	private String originalTransactionReference;

	@Column(name = "idempotencyKey", unique = true)
	private String idempotencyKey;

	@Column(name = "accountNumber", nullable = false)
	private String accountNumber;

	@Column(name = "amount", nullable = false)
	private BigDecimal amount;

	@Column(name = "merchantName", nullable = false)
	private String merchantName;

	@Column(name = "merchantId")
	private String merchantId;

	@Column(name = "description", nullable = false)
	private String description;

	@Column(name = "declineReason")
	private String declineReason;

	@Enumerated(EnumType.STRING)
	@Column(name = "transactionType", nullable = false)
	private CardTransactionType transactionType;

	@Enumerated(EnumType.STRING)
	@Column(name = "transactionStatus", nullable = false)
	private CardTransactionStatus transactionStatus;

	@Column(name = "authorizationExpiresAt")
	private LocalDateTime authorizationExpiresAt;

	@Column(name = "processedBy")
	private String processedBy;

	@Column(name = "channel")
	private String channel;

	@Column(name = "terminalId")
	private String terminalId;

	@Column(name = "location")
	private String location;

	@Column(name = "ipAddress")
	private String ipAddress;

	@Column(name = "createdAt", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updatedAt", nullable = false)
	private LocalDateTime updatedAt;

}
