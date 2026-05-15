package com.tosin.xpay.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tosin.xpay.model.Account;

@Repository
public interface AccountDAO extends JpaRepository<Account, Long> {

	@Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
	Account findAccountNumber(@Param("accountNumber")String senderAccountNumber);

	@Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
	Account findByAccountNumber(@Param("accountNumber")String accountNumber);

}
