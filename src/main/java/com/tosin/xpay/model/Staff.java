package com.tosin.xpay.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "staff")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Staff implements Serializable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@OneToOne
    @JoinColumn(name = "userInformation", referencedColumnName = "id")
    private UserData userData;
	
	@Column(name = "staffId", unique = true, nullable = false)
	private String staffId;
	
	@Column(name = "firstName", nullable = false)
	private String firstName;
	
	@Column(name = "lastName", nullable = false)
	private String lastName;


	private static final long serialVersionUID = 1L;

}
