package com.fashionkart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fashionkart.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
	
	List<Product> findByCategoryIgnoreCase(String category);
	
	List<Product> findByCategoryAndSubCategoryIgnoreCase(String category, String subCategory);
	
	@Query("SELECT DISTINCT p.subCategory FROM Product p " +
		       "WHERE LOWER(p.category) = LOWER(:category) " +
		       "AND p.subCategory IS NOT NULL " +
		       "AND p.subCategory <> '' " +
		       "ORDER BY p.subCategory")
		List<String> findDistinctSubCategoriesByCategory(
		        @Param("category") String category
		);
	
	List<Product> findAllByOrderByPriceAsc();
	
	List<Product> findAllByOrderByPriceDesc();
	
	List<Product> findAllByOrderByCreatedAtDesc();
	
	List<Product> findByDiscountGreaterThan(double discount);
	
	List<Product> findByDiscountGreaterThanAndCategoryIgnoreCase(
	        double discount,
	        String category
	);
	
	@Query("SELECT p FROM Product p WHERE " +
		       "LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
		       "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
		       "LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
		       "LOWER(p.subCategory) LIKE LOWER(CONCAT('%', :keyword, '%'))")
		List<Product> searchProducts(
		        @Param("keyword") String keyword
		);
}
