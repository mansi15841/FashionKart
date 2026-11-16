package com.fashionkart.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
	
	@NotBlank(message = "FullName is required.")
	private String fullName;
	
	@NotBlank(message = "Contact is required.")
	private String contact;
	
	@NotBlank(message = "Email is required.")
	@Email(message = "Please enter a valid email.")
	private String email;
	
	@NotBlank(message = "Password is required.")
	private String password;
	
	@NotBlank(message = "Confirm Password is required.")
	private String confirmPassword;
	
	@NotBlank(message = "Gender is required.")
	private String gender;

}
