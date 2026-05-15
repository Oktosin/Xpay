package com.tosin.xpay.model;

import java.io.Serializable;
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
@Table(name = "customer")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer implements Serializable{


	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@OneToOne
    @JoinColumn(name = "userInformation", referencedColumnName = "id")
    private UserData userData;	
	
	@OneToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;
	
	

}
