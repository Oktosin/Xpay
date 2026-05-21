package com.tosin.xpay.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.tosin.xpay.constant.TransactionStatus;
import com.tosin.xpay.constant.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "transaction")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction implements Serializable{


	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "reference")
	private String reference;

	@Column(name = "originalTransactionReference")
	private String originalTransactionReference;
	
	@Column(name = "senderAccountNumber")
	private String senderAccountNumber;
	
	@Column (name = "receiverAccountNumber")
	private String receiverAccountNumber;
	
	@Column(name = "amount", nullable = false)
	private BigDecimal amount;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "TransactionType", nullable = false)
	private TransactionType transactionType;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "TransactionStatus", nullable = false)
	private TransactionStatus transactionStatus;
	
	@ManyToOne
	@JoinColumn(name = "account_id", nullable = false)
	private Customer customer;
	
	@Column(name = "description", nullable = false)
	private String description;

	@Column(name = "reversed", nullable = false, columnDefinition = "boolean default false")
	private boolean reversed;

	@Column(name = "reversed_at")
	private LocalDateTime reversedAt;
	
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	

}
