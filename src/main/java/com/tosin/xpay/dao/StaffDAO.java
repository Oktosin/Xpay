package com.tosin.xpay.dao;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.tosin.xpay.model.Staff;
import com.tosin.xpay.model.UserData;

@Repository
public interface StaffDAO extends JpaRepository<Staff, Long>{
	
	@Query("SELECT s FROM Staff s WHERE s.userData = :userData")
	Staff findStaffByUserData(@Param("userData")UserData userData);

	@Query("SELECT s FROM Staff s WHERE s.staffId = :staffId")
	Staff findStaffByStaffId(@Param("staffId")String staffId);
	
	@Query("SELECT s FROM Staff s ORDER BY s.firstName DESC")
	Page<Staff> findAllStaff(Pageable pageable);

	

	

	


	

	

	




}
