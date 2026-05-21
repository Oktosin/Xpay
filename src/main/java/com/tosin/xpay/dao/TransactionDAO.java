package com.tosin.xpay.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tosin.xpay.model.Transaction;

@Repository
public interface TransactionDAO extends JpaRepository<Transaction, Long> {


	@Query("SELECT t FROM Transaction t WHERE t.senderAccountNumber = :accountNumber OR t.receiverAccountNumber = :accountNumber ORDER BY t.createdAt DESC")
	Page<Transaction> findTransactionHistoryByAccountNumber(@Param("accountNumber")String accountNumber, Pageable pageable);

	@Query("SELECT t FROM Transaction t WHERE t.reference = :reference")
	Transaction findTransactionByReference(@Param("reference") String reference);

	@Query("SELECT t FROM Transaction t WHERE t.originalTransactionReference = :reference")
	Transaction findTransactionByOriginalTransactionReference(@Param("reference") String reference);

}
