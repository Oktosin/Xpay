package com.tosin.xpay.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import com.tosin.xpay.model.UserData;


@Repository
public interface UserDataDAO extends JpaRepository<UserData, Long> {
	
	@Query("SELECT u FROM UserData u WHERE u.username = :username")
	UserData findUserDataByUsername(@Param("username") String username);

	@Query("SELECT u FROM UserData u WHERE u.username = :username AND u.email = :email AND u.phoneNumber = :phoneNumber ")														 
	Optional<UserData> findUserDataByBasicUserInformation(@Param("username")String username, @Param("email")String email, 
													  									 @Param("phoneNumber") String phoneNumber);

	@Query("SELECT u FROM UserData u WHERE u.email = :email")
	UserData findUserByEmail(@Param("email")String email);
													  								
	

}
