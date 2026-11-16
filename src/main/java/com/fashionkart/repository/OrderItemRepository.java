
package com.fashionkart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fashionkart.entity.OrderItem;

@Repository
public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {
}