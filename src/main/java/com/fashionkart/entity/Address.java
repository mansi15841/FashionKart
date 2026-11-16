package com.fashionkart.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String addressType;
	
	private String fullName;
	
	private String phone;
	
	private String address;
	
	private String city;
	
	private String state;
	
	private String pincode;
	
	private boolean defaultaddress;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
}
