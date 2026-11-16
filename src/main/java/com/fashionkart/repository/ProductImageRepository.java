package com.fashionkart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fashionkart.entity.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long>{
	
	
}
