package com.tosin.xpay.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tosin.xpay.model.Transaction;

@Repository
public interface TransactionDAO extends JpaRepository<Transaction, Long> {

	@Query("SELECT t FROM Transaction t ORDER BY t.createdAt DESC")
	Page<Transaction> findAllTransaction(Pageable pageable);

}
