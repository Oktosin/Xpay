package com.tosin.xpay.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tosin.xpay.constant.CardTransactionStatus;
import com.tosin.xpay.constant.CardTransactionType;
import com.tosin.xpay.model.CardTransaction;

@Repository
public interface CardTransactionDAO extends JpaRepository<CardTransaction, Long> {

	@Query("SELECT c FROM CardTransaction c WHERE c.reference = :reference")
	CardTransaction findCardTransactionByReference(@Param("reference") String reference);

	@Query("SELECT c FROM CardTransaction c WHERE c.originalTransactionReference = :reference")
	CardTransaction findCardTransactionByOriginalTransactionReference(@Param("reference") String reference);

	@Query("SELECT c FROM CardTransaction c WHERE c.idempotencyKey = :idempotencyKey")
	CardTransaction findCardTransactionByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

	@Query("SELECT c FROM CardTransaction c WHERE c.accountNumber = :accountNumber ORDER BY c.createdAt DESC")
	Page<CardTransaction> findCardTransactionHistoryByAccountNumber(@Param("accountNumber") String accountNumber, Pageable pageable);

	@Query("SELECT COALESCE(SUM(c.amount), 0) FROM CardTransaction c WHERE c.accountNumber = :accountNumber AND c.transactionStatus = :status AND c.transactionType IN :transactionTypes AND c.createdAt >= :startDate")
	BigDecimal sumSuccessfulSpendSince(@Param("accountNumber") String accountNumber, @Param("status") CardTransactionStatus status, @Param("transactionTypes") List<CardTransactionType> transactionTypes, @Param("startDate") LocalDateTime startDate);

	@Query("SELECT c FROM CardTransaction c WHERE c.transactionType = :transactionType AND c.transactionStatus = :transactionStatus AND c.authorizationExpiresAt <= :currentDateTime")
	List<CardTransaction> findExpiredAuthorizations(@Param("transactionType") CardTransactionType transactionType, @Param("transactionStatus") CardTransactionStatus transactionStatus, @Param("currentDateTime") LocalDateTime currentDateTime);

}
