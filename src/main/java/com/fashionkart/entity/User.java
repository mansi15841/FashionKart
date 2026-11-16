package com.fashionkart.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "users")
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String fullName;
	
	private String contact;
	
	private String email;
	
	private String password;
	
	private String gender;
	
	private String role;
	
	private LocalDate createdAt;
	
	@OneToMany(mappedBy = "user")
	private List<Address> addresses;
	
	@PrePersist
	public void onCreate() {
		createdAt = LocalDate.now();
		
		if(role == null) 
			role = "User";
	}
}
