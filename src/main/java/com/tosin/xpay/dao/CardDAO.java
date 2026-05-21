package com.tosin.xpay.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tosin.xpay.model.Account;
import com.tosin.xpay.model.Card;

@Repository
public interface CardDAO extends JpaRepository<Card, Long> {

	@Query("SELECT c FROM Card c WHERE c.cardNumberHash = :cardNumberHash")
	Optional<Card> findCardByCardNumberHash(@Param("cardNumberHash") String cardNumberHash);

	@Query("SELECT c FROM Card c WHERE c.account = :account")
	Optional<Card> findCardByAccount(@Param("account") Account account);

}
