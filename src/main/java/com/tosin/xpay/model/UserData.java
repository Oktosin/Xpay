package com.tosin.xpay.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import com.tosin.xpay.constant.UserAuthority;
import com.tosin.xpay.constant.UserRole;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "userData")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class UserData implements Serializable{


	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "username", nullable = false, unique = true)
	private String username;
	
	@Column(name = "password", nullable = false)
	private String password;
	
	@Column(name = "email", nullable = false, unique = true)
	private String email;
	
	@Column(name = "phoneNumber", nullable = false, unique = true)
	private String phoneNumber;
	
	@ElementCollection(targetClass = UserAuthority.class, fetch = FetchType.EAGER)
	@CollectionTable(name = "userAuthorities", joinColumns = @JoinColumn(name = "userId"))
	@Column(name = "authority")
	@Enumerated(EnumType.STRING)
	private Set<UserAuthority> authorities = new HashSet<>();
	
	@Enumerated(EnumType.STRING)
	@Column(name = "userRole", nullable = false)
	private UserRole userRole;
	
	@CreationTimestamp
	@Column(name = "created_at", nullable = true, updatable = false)
	private LocalDateTime createdAt;
	
	@CreationTimestamp
	@Column(name = "updated_at", nullable = true, updatable = false)
	private LocalDateTime updatedAt;
	
	
}

	


