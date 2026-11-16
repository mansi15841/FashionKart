package com.fashionkart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fashionkart.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

}