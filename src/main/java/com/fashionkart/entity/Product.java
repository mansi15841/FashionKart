package com.fashionkart.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String productName;
	
	@Column(nullable = false, unique = true)
	private String sku;
	
	private String brand;
	
	@Column(nullable = false)
	private String category;
	
	private String subCategory;
	
	@Column(length = 2000)
	private String description;
	
	@Column(nullable = false)
	private double price;
	
	private double discount;
	
	private String color;
	
	private boolean active = true;
	
	private LocalDate createdAt;
	
	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProductImage> images;
	
	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProductSize> sizes;
	
	@PrePersist
	public void onCreate() {
		createdAt = LocalDate.now();
				
	}
}
