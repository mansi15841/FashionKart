package com.fashionkart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fashionkart.entity.ProductSize;

public interface ProductSizeRepository extends JpaRepository<ProductSize, Long> {

}
