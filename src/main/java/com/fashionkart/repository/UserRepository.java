package com.fashionkart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fashionkart.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	boolean existsByEmail(String email);
	
	boolean existsByContact(String contact);
	
	User findByEmail(String email);
	
	boolean existsByEmailAndIdNot(String email,Long id);
	
	boolean existsByContactAndIdNot(String contact, Long id);
	
}
