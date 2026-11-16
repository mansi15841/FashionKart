package com.fashionkart.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fashionkart.entity.Product;
import com.fashionkart.entity.ProductImage;
import com.fashionkart.repository.ProductImageRepository;
import com.fashionkart.repository.ProductRepository;
import com.fashionkart.repository.ProductSizeRepository;

import jakarta.transaction.Transactional;

@Service
public class ProductService {
	
	private ProductRepository productRepository;
	private ProductImageRepository productImageRepository;
	private ProductSizeRepository productSizeRepository;
	
	public ProductService(ProductRepository productRepository, ProductImageRepository productImageRepository, ProductSizeRepository productSizeRepository) {
		// TODO Auto-generated constructor stub
		this.productRepository = productRepository;
		this.productImageRepository = productImageRepository;
		this.productSizeRepository = productSizeRepository;
	}

	public Product saveProduct(Product product) {
		// TODO Auto-generated method stub
		return productRepository.save(product);
	}
	

public void saveProductImages(MultipartFile[] productImages, String sku,
        Product product) throws IOException {

    String uploadDirectory = "src/main/resources/static/uploads/products/";

    Path uploadPath = Paths.get(uploadDirectory);
    Files.createDirectories(uploadPath);

    for (MultipartFile image : productImages) {

        if (image == null || image.isEmpty()) {
            continue;
        }

        String originalFileName = image.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            continue;
        }

        // Keep the original filename, but remove any directory path
        String imageName = Paths.get(originalFileName)
                .getFileName()
                .toString();

        Path filePath = uploadPath.resolve(imageName);

        // Avoid overwriting an existing image with the same name
        if (Files.exists(filePath)) {
            throw new IOException(
                    "An image with this filename already exists: " + imageName);
        }

        Files.copy(image.getInputStream(), filePath);

        ProductImage productImage = new ProductImage();
        productImage.setImagePath("/uploads/products/" + imageName);
        productImage.setProduct(product);

        productImageRepository.save(productImage);
    }
}


	public List<Product> getAllProducts() {
		// TODO Auto-generated method stub
		return productRepository.findAll();
	}
	
	public List<Product> searchProducts(String keyword){
		if(keyword == null || keyword.trim().isEmpty()) {
			return productRepository.findAll();
		}
		
		String search = keyword.trim().toLowerCase();
		
		return productRepository.findAll().stream().filter(
				product -> String.valueOf(product.getId()).contains(search)
				
				|| (product.getProductName() != null &&
                product.getProductName()
                        .toLowerCase()
                        .contains(search))

            || (product.getSku() != null &&
                product.getSku()
                        .toLowerCase()
                        .contains(search))

            || (product.getBrand() != null &&
                product.getBrand()
                        .toLowerCase()
                        .contains(search))

            || (product.getCategory() != null &&
                product.getCategory()
                        .toLowerCase()
                        .contains(search))

            || (product.getSubCategory() != null &&
                product.getSubCategory()
                        .toLowerCase()
                        .contains(search))

            || (product.getDescription() != null &&
                product.getDescription()
                        .toLowerCase()
                        .contains(search))

            || (product.getColor() != null &&
                product.getColor()
                        .toLowerCase()
                        .contains(search))

            || String.valueOf(product.getPrice())
                    .contains(search)

            || String.valueOf(product.getDiscount())
                    .contains(search)

            || (product.isActive()
                    ? "active".contains(search)
                    : "inactive".contains(search))
				)
				.toList();
	}
	
	@Transactional
	public Product getProductById(Long id) {
	    Product product = productRepository.findById(id).orElse(null);

	    if (product != null) {
	        product.getSizes().size();
	        product.getImages().size();

	        System.out.println("Product ID: " + product.getId());
	        System.out.println("Product Name: " + product.getProductName());
	        System.out.println("Number of images: " + product.getImages().size());

	        for (ProductImage image : product.getImages()) {
	            System.out.println("Image Path: " + image.getImagePath());
	        }
	    }

	    return product;
	}
	

	public void deleteProductImage(Long imageId) throws IOException {
	
	    ProductImage productImage = productImageRepository.findById(imageId)
	            .orElseThrow(() -> new RuntimeException("Product image not found"));
	
	    String imagePath = productImage.getImagePath();
	
	    if (imagePath != null && imagePath.startsWith("/uploads/products/")) {
	
	        String imageName = Paths.get(imagePath).getFileName().toString();
	
	        Path filePath = Paths.get(
	                "src/main/resources/static/uploads/products/",
	                imageName
	        );
	
	        Files.deleteIfExists(filePath);
	    }
	
	    productImageRepository.delete(productImage);
	}



@Transactional
public void deleteProduct(Long id) throws IOException {

    Product product = productRepository.findById(id).orElse(null);

    if (product == null) {
        return;
    }

    List<String> imagePaths = new ArrayList<>();

    if (product.getImages() != null) {
        for (ProductImage image : product.getImages()) {
            if (image.getImagePath() != null) {
                imagePaths.add(image.getImagePath());
            }
        }
    }

    productRepository.delete(product);
    productRepository.flush();

    for (String imagePath : imagePaths) {

        if (!imagePath.startsWith("/uploads/products/")) {
            continue;
        }

        String imageName = Paths.get(imagePath).getFileName().toString();

        Path filePath = Paths.get(
                "src/main/resources/static/uploads/products/",
                imageName
        );

        Files.deleteIfExists(filePath);
    }
}

	
	public List<Product> getProductsSortedBy(String sort){
		switch(sort) {
		case "price-low":
			return productRepository.findAllByOrderByPriceAsc();
			
		case "price-high":
			return productRepository.findAllByOrderByPriceDesc();
			
		case "newest":
			return productRepository.findAllByOrderByCreatedAtDesc();
			
			default:
				return productRepository.findAll();
		}
	}
	
	public List<Product> getSaleProducts() {
	    return productRepository.findByDiscountGreaterThan(50);
	}
	
	public List<Product> getSaleProductsByCategory(String category) {

	    return productRepository
	            .findByDiscountGreaterThanAndCategoryIgnoreCase(
	                    50,
	                    category
	            );
	}
	
	public List<Product> searchProductsOverall(String keyword) {

	    return productRepository.searchProducts(keyword);

	}
	
}

