package com.fashionkart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fashionkart.entity.Address;
import com.fashionkart.entity.User;

public interface AddressRepository extends JpaRepository<Address, Long>{
	
	List<Address> findByUser(User user);
}
