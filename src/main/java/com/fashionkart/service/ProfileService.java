package com.fashionkart.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fashionkart.entity.User;
import com.fashionkart.repository.UserRepository;

@Service
public class ProfileService {
	
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	
	public ProfileService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		// TODO Auto-generated constructor stub
	}
	
	 public User updateProfile(User user, String fullName, String email, String contact) {

	        // Update full name only if a new value is provided
	        if (fullName != null && !fullName.trim().isEmpty()) {
	            user.setFullName(fullName.trim());
	        }

	        // Update email only if a new value is provided
	        if (email != null && !email.trim().isEmpty()) {

	            email = email.trim();

	            // Check only if email is actually changed
	            if (!email.equalsIgnoreCase(user.getEmail())) {

	                if (userRepository.existsByEmailAndIdNot(email, user.getId())) {
	                    throw new RuntimeException("Email already registered");
	                }

	                user.setEmail(email);
	            }
	        }

	        // Update contact only if a new value is provided
	        if (contact != null && !contact.trim().isEmpty()) {

	            contact = contact.trim();

	            // Check only if contact is actually changed
	            if (!contact.equals(user.getContact())) {

	                if (userRepository.existsByContactAndIdNot(contact, user.getId())) {
	                    throw new RuntimeException("Contact already registered");
	                }

	                user.setContact(contact);
	            }
	        }

	        return userRepository.save(user);
	    }
	 
	 public User updatePassword(User user, String newpassword) {
		 user.setPassword(passwordEncoder.encode(newpassword));
		 return userRepository.save(user);
	 }
	 
	}
