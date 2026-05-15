package com.tosin.xpay.dao;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tosin.xpay.model.Account;
import com.tosin.xpay.model.Customer;
import com.tosin.xpay.model.UserData;


@Repository
public interface CustomerDAO extends JpaRepository<Customer, Long> {
	
	@Query("SELECT c FROM Customer c WHERE c.userData = :userData")
	Customer findCustomerByUserData(@Param("userData")UserData userData);

	@Query("SELECT c FROM Customer c WHERE c.userData = :userData AND c.account = :account")
	Optional<Customer> findCustomerByUserDataAndAccount(@Param("userData")UserData userData, @Param("account")Account account);
	
	@Query("SELECT c FROM Customer c ORDER BY c.id DESC")
	Page<Customer> findAllCustomers(Pageable pageable);


}
