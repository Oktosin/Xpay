package com.tosin.xpay.dao;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tosin.xpay.model.PasswordResetOtp;
import com.tosin.xpay.model.UserData;

@Repository
public interface PasswordResetOtpDAO extends JpaRepository<PasswordResetOtp, Long> {

	@Query("SELECT p FROM PasswordResetOtp p WHERE p.userData = :userData AND p.used = false")
	List<PasswordResetOtp> findAllByUserDataAndUsedFalse(@Param("userData") UserData userData);

	@Query("SELECT p FROM PasswordResetOtp p WHERE p.userData = :userData AND p.used = false ORDER BY p.createdAt DESC")
	List<PasswordResetOtp> findLatestUnusedOtpByUserData(@Param("userData") UserData userData, Pageable pageable);

}
